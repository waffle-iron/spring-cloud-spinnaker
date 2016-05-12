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

import static java.nio.file.Files.getLastModifiedTime;
import static java.util.stream.Stream.concat;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Enumeration;
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
import org.springframework.core.io.FileSystemResource;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.FileSystemUtils;
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

//		properties.put(CloudFoundryAppDeployer.SERVICES_PROPERTY_KEY, collectionToCommaDelimitedString(details.getServices()));

		data.entrySet().stream()
			.forEach(entry -> properties.put(entry.getKey(), entry.getValue()));

		AppDeployer appDeployer = Optional.ofNullable(details.getProperties().get("buildpack"))
				.map(buildpack -> mutateBuildpack(this.cloudFoundryDeployerProperties, buildpack))
				.map(props -> new CloudFoundryAppDeployer(props, this.operations, this.client))
				.orElse(this.appDeployer);

		org.springframework.core.io.Resource artifactToDeploy = Optional.of(details.getName().equals("deck"))
				.map(deckQ -> pluginSettingsJs(resources[0], data))
				.orElse(resources[0]);

		log.debug("Uploading " + artifactToDeploy.getURL() + "...");

		appDeployer.deploy(new AppDeploymentRequest(
			new AppDefinition(module, Collections.emptyMap()),
			artifactToDeploy,
			properties
		));

		artifactToDeploy.getFile().delete();

		return ResponseEntity.created(linkTo(methodOn(ModuleController.class).status(module)).toUri()).build();
	}

	private org.springframework.core.io.Resource pluginSettingsJs(org.springframework.core.io.Resource originalDeckJarFile, Map<String, String> data) {
		try {
			// TODO: Unpack original deck JAR file

			Path path = Files.createTempDirectory("deck");

			try (ZipFile zipFile = new ZipFile(originalDeckJarFile.getFile())) {
				for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements();) {
					ZipEntry entry = e.nextElement();
					final Path entryPath = path.resolve(entry.getName());
					if (entry.isDirectory()) {
						Files.createDirectory(entryPath);
					} else {
						Files.copy(zipFile.getInputStream(entry), entryPath);
					}
				}
			}

			// TODO: Plugin in custom settings.js
			final URI uri = ctx.getResource("file:settings.js").getFile().toURI();
			String settingsJs = new String(Files.readAllBytes(Paths.get(uri)));
			settingsJs = settingsJs.replace("{gate}", "https://gate." + data.getOrDefault("domain", "cfapps.io"));
			Files.write(path.resolve("settings.js"), settingsJs.getBytes());

			// TODO: Repack deck JAR
			Manifest manifest = new Manifest();
			manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
			Path newPath = Files.createTempFile("deck-customized-", ".jar");
			newPath.toFile().deleteOnExit(); // Register extra hook to delete on JVM exit, in case deployment fails
			try (JarOutputStream newDeckJarFile = new JarOutputStream(new FileOutputStream(newPath.toFile()), manifest)) {
				add(path, path, newDeckJarFile);
			}

			// TODO: Delete unpacked JAR file
			FileSystemUtils.deleteRecursively(path.toFile());

			// TODO: Hand back Resource link
			return new FileSystemResource(newPath.toFile());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void add(Path basePath, Path source, JarOutputStream newDeckJarFile) {
		try {
			final String name = basePath.relativize(source).toString();

			if (name.startsWith("META-INF")) {
				return;
			}

			if (Files.isDirectory(source)) {
				// Don't create an entry for the basePath
				if (!source.equals(basePath)) {
					JarEntry entry = new JarEntry(name + "/");
					entry.setTime(getLastModifiedTime(source).toMillis());
					newDeckJarFile.putNextEntry(entry);
					newDeckJarFile.closeEntry();
				}
				Files.list(source).forEach(nestedPath -> add(basePath, nestedPath, newDeckJarFile));
				return;
			} else {
				JarEntry entry = new JarEntry(name);
				entry.setTime(getLastModifiedTime(source).toMillis());
				newDeckJarFile.putNextEntry(entry);
				Files.copy(source, newDeckJarFile);
				newDeckJarFile.closeEntry();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
