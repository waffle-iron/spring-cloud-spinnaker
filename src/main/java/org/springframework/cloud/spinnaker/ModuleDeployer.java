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

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.deployer.spi.app.AppDeployer;
import org.springframework.cloud.deployer.spi.core.AppDefinition;
import org.springframework.cloud.deployer.spi.core.AppDeploymentRequest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Greg Turnquist
 */
@RestController
public class ModuleDeployer {

	private final AppDeployer appDeployer;

	private final SpinnakerConfiguration spinnakerConfiguration;

	@Autowired
	public ModuleDeployer(AppDeployer appDeployer, SpinnakerConfiguration spinnakerConfiguration) {
		this.appDeployer = appDeployer;
		this.spinnakerConfiguration = spinnakerConfiguration;
	}

	@RequestMapping(method=RequestMethod.GET, value="/modules", produces = MediaTypes.HAL_JSON_VALUE)
	public ResponseEntity<?> statuses() {

		return ResponseEntity.ok(new Resources<>(
			spinnakerConfiguration.getModules().stream()
				.map(appDeployer::status)
				.map(appStatus -> new Resource<>(
					appStatus,
					linkTo(methodOn(ModuleDeployer.class).status(appStatus.getDeploymentId())).withSelfRel()))
				.collect(Collectors.toList()),
			linkTo(methodOn(ModuleDeployer.class).statuses()).withSelfRel()
		));
	}

	@RequestMapping(method=RequestMethod.GET, value="/modules/{module}", produces = MediaTypes.HAL_JSON_VALUE)
	public ResponseEntity<?> status(@PathVariable String module) {

		if (!spinnakerConfiguration.getModules().contains(module)) {
			throw new IllegalArgumentException("Module '" + module + "' is not managed by this system");
		}

		return ResponseEntity.ok(new Resource<>(
			appDeployer.status(module),
			linkTo(methodOn(ModuleDeployer.class).status(module)).withSelfRel(),
			linkTo(methodOn(ModuleDeployer.class).statuses()).withRel("all")));
	}

	@RequestMapping(method = RequestMethod.POST, value = "/modules/{module}")
	public ResponseEntity<?> upload(@PathVariable String module, @RequestPart("file") MultipartFile file) throws IOException, URISyntaxException {

		if (!file.isEmpty()) {

			appDeployer.deploy(new AppDeploymentRequest(
				new AppDefinition(module, Collections.emptyMap()),
				new InputStreamResource(file.getInputStream())));
		}

		final Link link = linkTo(methodOn(ModuleDeployer.class).status(module)).withRel("foo");

		return ResponseEntity
			.created(new URI(link.getHref()))
			.body(new Resource<>(status(module), link));
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/modules/{module}")
	public ResponseEntity<?> undeploy(@PathVariable String module) {

		if (!spinnakerConfiguration.getModules().contains(module)) {
			throw new IllegalArgumentException("Module '" + module + "' is not managed by this system");
		}

		appDeployer.undeploy(module);

		return ResponseEntity.noContent().build();
	}

}
