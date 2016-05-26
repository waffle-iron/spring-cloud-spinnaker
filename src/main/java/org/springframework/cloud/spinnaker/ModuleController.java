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

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
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

	private final ModuleService moduleService;

	@Autowired
	public ModuleController(ModuleService moduleService) {
		this.moduleService = moduleService;
	}

	@RequestMapping(method = RequestMethod.GET, value = BASE_PATH + "/modules", produces = MediaTypes.HAL_JSON_VALUE)
	public ResponseEntity<?> statuses() {

		return ResponseEntity.ok(new Resources<>(
			moduleService.getStatuses()
				.map(appStatus -> new Resource<>(
					appStatus,
					linkTo(methodOn(ModuleController.class).status(appStatus.getDeploymentId())).withSelfRel()))
				.collect(Collectors.toList()),
			linkTo(methodOn(ModuleController.class).statuses()).withSelfRel()
		));
	}

	@RequestMapping(method = RequestMethod.GET, value = BASE_PATH + "/modules/{module}", produces = MediaTypes.HAL_JSON_VALUE)
	public ResponseEntity<?> status(@PathVariable String module) {

		return ResponseEntity.ok(new Resource<>(
			moduleService.getStatus(module),
			linkTo(methodOn(ModuleController.class).status(module)).withSelfRel(),
			linkTo(methodOn(ModuleController.class).statuses()).withRel("all"),
			linkTo(methodOn(ApiController.class).root()).withRel("root")
		));
	}

	@RequestMapping(method = RequestMethod.POST, value = BASE_PATH + "/modules/{module}")
	public ResponseEntity<?> deploy(@PathVariable String module, @RequestBody Map<String, String> data) throws IOException {

		moduleService.deploy(module, data);

		return ResponseEntity.created(linkTo(methodOn(ModuleController.class).status(module)).toUri()).build();
	}

	@RequestMapping(method = RequestMethod.DELETE, value = BASE_PATH + "/modules/{module}")
	public ResponseEntity<?> undeploy(@PathVariable String module) {

		log.debug("Deleting " + module + " on the server...");

		moduleService.undeploy(module);

		return ResponseEntity.noContent().build();
	}

}
