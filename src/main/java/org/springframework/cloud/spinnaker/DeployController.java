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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.deployer.spi.cloudfoundry.CloudFoundryAppDeployer;
import org.springframework.cloud.deployer.spi.core.AppDefinition;
import org.springframework.cloud.deployer.spi.core.AppDeploymentRequest;
import org.springframework.core.io.InputStreamResource;
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
public class DeployController {

	private static final List<String> MODULES = Collections.unmodifiableList(Arrays.asList("clouddriver", "orca"));

	private final CloudFoundryAppDeployer appDeployer;

	@Autowired
	public DeployController(CloudFoundryAppDeployer appDeployer) {
		this.appDeployer = appDeployer;
	}

	@RequestMapping(method=RequestMethod.GET, value="/modules", produces = MediaTypes.HAL_JSON_VALUE)
	public ResponseEntity<?> statuses() {

		return ResponseEntity.ok(new Resources<>(
			MODULES.stream()
				.map(appDeployer::status)
				.map(appStatus -> new Resource<>(
					appStatus,
					linkTo(methodOn(DeployController.class).status(appStatus.getDeploymentId())).withSelfRel()))
				.collect(Collectors.toList()),
			linkTo(methodOn(DeployController.class).statuses()).withSelfRel()
		));
	}

	@RequestMapping(method=RequestMethod.GET, value="/modules/{module}", produces = MediaTypes.HAL_JSON_VALUE)
	public ResponseEntity<?> status(@PathVariable String module) {

		if (!MODULES.contains(module)) {
			throw new IllegalArgumentException("Module '" + module + "' is not managed by this system");
		}

		return ResponseEntity.ok(new Resource<>(
			appDeployer.status(module),
			linkTo(methodOn(DeployController.class).status(module)).withSelfRel(),
			linkTo(methodOn(DeployController.class).statuses()).withRel("all")));
	}

	@RequestMapping(method = RequestMethod.POST, value = "/modules/{module}")
	public ResponseEntity<?> upload(@PathVariable String module, @RequestPart("file") MultipartFile file) throws IOException {

		if (!file.isEmpty()) {

			appDeployer.deploy(new AppDeploymentRequest(
				new AppDefinition(module, Collections.emptyMap()),
				new InputStreamResource(file.getInputStream())));
		}

		return ResponseEntity.created(linkTo(methodOn(DeployController.class).status(module)).toUri()).build();
	}

}
