/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.spinnaker;

import static java.util.stream.Stream.concat;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.deployer.spi.app.AppDeployer;
import org.springframework.cloud.deployer.spi.cloudfoundry.CloudFoundryAppDeployer;
import org.springframework.cloud.deployer.spi.cloudfoundry.CloudFoundryDeployerProperties;
import org.springframework.cloud.deployer.spi.core.AppDefinition;
import org.springframework.cloud.deployer.spi.core.AppDeploymentRequest;
import org.springframework.context.ApplicationContext;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.security.util.InMemoryResource;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Greg Turnquist
 */
@RestController
public class ModuleController {

	private static final Logger log = LoggerFactory.getLogger(ModuleController.class);

	public static final String BASE_PATH = "/api";

	private final CloudFoundryAppDeployer appDeployer;

	private final CloudFoundryDeployerProperties cloudFoundryDeployerProperties;

	private final CloudFoundryOperations operations;

	private final CloudFoundryClient client;

	private final SpinnakerConfiguration spinnakerConfiguration;

	private final ApplicationContext ctx;

	public static final String DEFAULT_DOMAIN = "cfapps.io"; // PWS

	public static final String DEFAULT_PRIMARY_ACCOUNT = "prod";


	@Autowired
	public ModuleController(CloudFoundryDeployerProperties cloudFoundryDeployerProperties, CloudFoundryOperations operations,
							CloudFoundryClient client, SpinnakerConfiguration spinnakerConfiguration, ApplicationContext ctx) {

		this.appDeployer = new CloudFoundryAppDeployer(cloudFoundryDeployerProperties, operations, client);
		this.cloudFoundryDeployerProperties = cloudFoundryDeployerProperties;
		this.operations = operations;
		this.client = client;
		this.spinnakerConfiguration = spinnakerConfiguration;
		this.ctx = ctx;
	}

	@RequestMapping(method = RequestMethod.GET, value = BASE_PATH + "/modules", produces = MediaTypes.HAL_JSON_VALUE)
	public ResponseEntity<?> statuses() {

		return ResponseEntity.ok(new Resources<>(
			spinnakerConfiguration.getModules().stream()
				.map(ModuleDetails::getName)
				.map(appDeployer::status)
				.map(appStatus -> new Resource<>(
					appStatus,
					linkTo(methodOn(ModuleController.class).status(appStatus.getDeploymentId())).withSelfRel()))
				.collect(Collectors.toList()),
			linkTo(methodOn(ModuleController.class).statuses()).withSelfRel()
		));
	}

	@RequestMapping(method = RequestMethod.GET, value = BASE_PATH + "/modules/{module}", produces = MediaTypes.HAL_JSON_VALUE)
	public ResponseEntity<?> status(@PathVariable String module) {

		ModuleDetails details = getModuleDetails(module);

		return ResponseEntity.ok(new Resource<>(
			appDeployer.status(details.getName()),
			linkTo(methodOn(ModuleController.class).status(module)).withSelfRel(),
			linkTo(methodOn(ModuleController.class).statuses()).withRel("all"),
			linkTo(methodOn(ApiController.class).root()).withRel("root")
		));
	}

	@RequestMapping(method = RequestMethod.POST, value = BASE_PATH + "/modules/{module}")
	public ResponseEntity<?> deploy(@PathVariable String module, @RequestBody Map<String, String> data) throws IOException {

		ModuleDetails details = getModuleDetails(module);

		final org.springframework.core.io.Resource[] resources = ctx.getResources(
			"file:" + details.getName() + "/**/build/libs/" + details.getArtifact() + "-*.jar");

		Assert.state(resources.length == 1, "Number of resources MUST be 1");

		log.info("Need to also chew on " + data);

		final Map<String, String> properties = concat(
			spinnakerConfiguration.getProperties().entrySet().stream(),
			details.getProperties().entrySet().stream()
		).collect(Collectors.toMap(
			Map.Entry::getKey,
			e -> translateTemplatedValue(details, e),
			(a, b) -> b));

		data.entrySet().stream()
			.forEach(entry -> properties.put(entry.getKey(), entry.getValue()));

		AppDeployer appDeployer = Optional.ofNullable(details.getProperties().get("buildpack"))
				.map(buildpack -> mutateBuildpack(this.cloudFoundryDeployerProperties, buildpack))
				.map(props -> new CloudFoundryAppDeployer(props, this.operations, this.client))
				.orElse(this.appDeployer);

		final org.springframework.core.io.Resource artifactToDeploy =
				(details.getName().equals("deck")
					? pluginSettingsJs(resources[0], data)
					: resources[0]);

		log.debug("Uploading " + artifactToDeploy + "...");

		appDeployer.deploy(new AppDeploymentRequest(
			new AppDefinition(module, Collections.emptyMap()),
			artifactToDeploy,
			properties
		));

		return ResponseEntity.created(linkTo(methodOn(ModuleController.class).status(module)).toUri()).build();
	}

	private org.springframework.core.io.Resource pluginSettingsJs(org.springframework.core.io.Resource originalDeckJarFile, Map<String, String> data) {
		try {
			Manifest manifest = new Manifest();
			manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
			final ByteArrayOutputStream jarByteStream = new ByteArrayOutputStream();

			try (
				ZipFile zipFile = new ZipFile(originalDeckJarFile.getFile());
				JarOutputStream newDeckJarFile = new JarOutputStream(jarByteStream, manifest)
			) {
				zipFile.stream().forEach(entry -> {
					try {

						if (entry.getName().contains("META-INF") || entry.getName().contains("MANIFEST.MF")) {
							// Skip the manifest since it's set up above.
						} else if (entry.getName().equals("settings.js")) {
							transformSettingsJs(data, zipFile, newDeckJarFile, entry);
						} else {
							passThroughFileEntry(zipFile, newDeckJarFile, entry);
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});
			}

			if (log.isDebugEnabled()) {
				Path file = Files.createTempFile("deck-preview", ".jar");
				log.info("Dumping JAR contents to " + file);
				Files.write(file, jarByteStream.toByteArray());
				file.toFile().deleteOnExit();
			}

			return new InMemoryResource(jarByteStream.toByteArray(), "In memory JAR file for deck");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void transformSettingsJs(Map<String, String> data, ZipFile zipFile, JarOutputStream newDeckJarFile, ZipEntry entry) throws IOException {
		JarEntry newEntry = new JarEntry(entry.getName());
		newEntry.setTime(entry.getTime());
		newDeckJarFile.putNextEntry(newEntry);
		if (!entry.isDirectory()) {
			String settingsJs = StreamUtils.copyToString(zipFile.getInputStream(entry), Charset.defaultCharset());
			settingsJs = settingsJs.replace("{gate}", "https://gate." + data.getOrDefault("deck.domain", DEFAULT_DOMAIN));
			settingsJs = settingsJs.replace("{primaryAccount}", data.getOrDefault("deck.primaryAccount", DEFAULT_PRIMARY_ACCOUNT));
			final String primaryAccounts = data.getOrDefault("deck.primaryAccounts", DEFAULT_PRIMARY_ACCOUNT);
			final String[] primaryAccountsArray = primaryAccounts.split(",");
			final List<String> accounts = Arrays.stream(primaryAccountsArray)
					.map(account -> "'" + account + "'")
					.collect(Collectors.toList());
			final String formattedAccounts = StringUtils.collectionToCommaDelimitedString(accounts);
			settingsJs = settingsJs.replace("'{primaryAccounts}'", "[" + formattedAccounts + "]");
			StreamUtils.copy(settingsJs, Charset.defaultCharset(), newDeckJarFile);
		}
		newDeckJarFile.closeEntry();
	}

	private static void passThroughFileEntry(ZipFile zipFile, JarOutputStream newDeckJarFile, ZipEntry entry) throws IOException {
		JarEntry newEntry = new JarEntry(entry.getName());
		newEntry.setTime(entry.getTime());
		newDeckJarFile.putNextEntry(newEntry);
		if (!entry.isDirectory()) {
			StreamUtils.copy(zipFile.getInputStream(entry), newDeckJarFile);
		}
		newDeckJarFile.closeEntry();
	}

	private static CloudFoundryDeployerProperties cloneDeployerProperties(CloudFoundryDeployerProperties properties) {
		CloudFoundryDeployerProperties localProps = new CloudFoundryDeployerProperties();
		BeanUtils.copyProperties(properties, localProps);
		return localProps;
	}

	private static CloudFoundryDeployerProperties mutateBuildpack(CloudFoundryDeployerProperties properties, String buildpack) {
		CloudFoundryDeployerProperties clonedProps = cloneDeployerProperties(properties);
		clonedProps.setBuildpack(buildpack);
		return clonedProps;
	}

	private String translateTemplatedValue(ModuleDetails details, Map.Entry<String, String> e) {
		return concat(spinnakerConfiguration.getPatterns().entrySet().stream(), details.getPatterns().entrySet().stream())
			.reduce(e, (accumEntry, patternEntry) -> {
				String newValue = accumEntry.getValue().replace("{" + patternEntry.getKey() + "}", patternEntry.getValue());
				accumEntry.setValue(newValue);
				return accumEntry;
			})
			.getValue()
			.replace("{module}", details.getName());
	}

	private static <T> Stream<T> append(Stream<? extends T> stream, T element) {
		return concat(stream, Stream.of(element));
	}

	private static <T> Stream<T> append(Stream<? extends T> stream, T element1, T element2) {
		return append(append(stream, element1), element2);
	}

	private static <T> Stream<T> append(Stream<? extends T> stream, T element1, T element2, T element3) {
		return append(append(append(stream, element1), element2), element3);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = BASE_PATH + "/modules/{module}")
	public ResponseEntity<?> undeploy(@PathVariable String module) {

		ModuleDetails details = getModuleDetails(module);

		log.debug("Deleting " + details.getName() + " on the server...");

		appDeployer.undeploy(details.getName());

		return ResponseEntity.noContent().build();
	}

	private ModuleDetails getModuleDetails(String module) {

		return spinnakerConfiguration.getModules().stream()
			.filter(m -> m.getName().equals(module))
			.findFirst()
			.map(moduleDetails -> moduleDetails)
			.orElseThrow(() -> new IllegalArgumentException("Module '" + module + "' is not managed by this system"));
	}

}
