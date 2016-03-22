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

import java.util.Collections;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.deployer.spi.app.AppDeployer;
import org.springframework.cloud.deployer.spi.core.AppDefinition;
import org.springframework.cloud.deployer.spi.core.AppDeploymentRequest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
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

	private final AppDeployer appDeployer;

	private final SpinnakerConfiguration spinnakerConfiguration;

	@Autowired
	public ModuleController(AppDeployer appDeployer, SpinnakerConfiguration spinnakerConfiguration) {
		this.appDeployer = appDeployer;
		this.spinnakerConfiguration = spinnakerConfiguration;
	}

	@RequestMapping(method = RequestMethod.GET, value = BASE_PATH + "/modules", produces = MediaTypes.HAL_JSON_VALUE)
	public ResponseEntity<?> statuses() {

		return ResponseEntity.ok(new Resources<>(
			spinnakerConfiguration.getModules().stream()
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

		if (!spinnakerConfiguration.getModules().contains(module)) {
			throw new IllegalArgumentException("Module '" + module + "' is not managed by this system");
		}

		return ResponseEntity.ok(new Resource<>(
			appDeployer.status(module),
			linkTo(methodOn(ModuleController.class).status(module)).withSelfRel(),
			linkTo(methodOn(ModuleController.class).statuses()).withRel("all"),
			linkTo(methodOn(ApiController.class).root()).withRel("root")
		));
	}

	@RequestMapping(method = RequestMethod.POST, value = BASE_PATH + "/modules/{module}")
	public ResponseEntity<?> deploy(@PathVariable String module) {

		if (!spinnakerConfiguration.getModules().contains(module)) {
			throw new IllegalArgumentException("Module '" + module + "' is not managed by this system");
		}

		appDeployer.deploy(new AppDeploymentRequest(
			new AppDefinition(module, Collections.emptyMap()),
			new FileSystemResource("/tmp/" + module + ".jar")
		));

		return ResponseEntity.created(linkTo(methodOn(ModuleController.class).status(module)).toUri()).build();
	}

	@RequestMapping(method = RequestMethod.DELETE, value = BASE_PATH + "/modules/{module}")
	public ResponseEntity<?> undeploy(@PathVariable String module) {

		if (!spinnakerConfiguration.getModules().contains(module)) {
			throw new IllegalArgumentException("Module '" + module + "' is not managed by this system");
		}

		appDeployer.undeploy(module);

		return ResponseEntity.noContent().build();
	}

}
