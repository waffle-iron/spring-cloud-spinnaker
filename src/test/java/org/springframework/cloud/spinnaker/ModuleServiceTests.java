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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.cloudfoundry.operations.applications.ApplicationDetail;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.deployer.spi.app.AppStatus;
import org.springframework.cloud.deployer.spi.app.DeploymentState;
import org.springframework.cloud.deployer.spi.cloudfoundry.CloudFoundryAppDeployer;
import org.springframework.cloud.deployer.spi.cloudfoundry.CloudFoundryAppInstanceStatus;
import org.springframework.cloud.deployer.spi.core.AppDefinition;
import org.springframework.cloud.deployer.spi.core.AppDeploymentRequest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Greg Turnquist
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(ModuleServiceTests.TestConfig.class)
public class ModuleServiceTests {

	@Autowired
	CloudFoundryAppDeployer appDeployer;

	@Autowired
	ModuleService moduleService;

	@Rule public ExpectedException thrown = none();

	@Test
	public void shouldReturnStatusCodeForRunningModules() throws Exception {

		// given
		given(appDeployer.status("clouddriver")).willReturn(
			AppStatus
				.of("clouddriver")
				.with(
					new CloudFoundryAppInstanceStatus(
							ApplicationDetail.builder()
									.name("clouddriver")
									.id("abcdef")
									.build(),
							ApplicationDetail.InstanceDetail.builder()
									.state("RUNNING")
									.build(),
							0))
				.build());

		// when
		AppStatus status = moduleService.getStatus("clouddriver", "api", "org", "space", "user", "password", "foo");

		// then
		assertThat(status.getState(), equalTo(DeploymentState.deployed));

		then(appDeployer).should().status("clouddriver");
		verifyNoMoreInteractions(appDeployer);
	}

	@Test
	public void shouldReturnErrorForNonexistentModule() throws Exception {

		// given
		given(appDeployer.status("nothing")).willReturn(
			AppStatus
				.of("nothing")
				.build());

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(containsString("Module 'nothing' is not managed by this system"));

		// when
		moduleService.getStatus("nothing", "api", "org", "space", "user", "password", "foo");

		// then
		// JUnit exception conditions are at the top
	}

	@Test
	public void shouldHandleStandardDeployment() throws Exception {

		// given
		given(appDeployer.deploy(any())).willReturn("clouddriver");

		Resource artifactToUpload = mock(Resource.class);

		Map<String, String> data = new HashMap<>();
		data.put("foo", "bar");

		// when
		moduleService.deploy("clouddriver", data, "api", "org", "space", "user", "password", "foo");

		// then
		then(appDeployer).should().deploy(new AppDeploymentRequest(
				new AppDefinition("clouddriver", Collections.emptyMap()),
				artifactToUpload,
				any()
		));
		verifyNoMoreInteractions(appDeployer);
	}

	@Test
	public void shouldHandlePrefixOverrides() throws IOException {

		// given
		given(appDeployer.deploy(any())).willReturn("clouddriver");

		Resource artifactToUpload = mock(Resource.class);

		Map<String, String> data = new HashMap<>();
		data.put("deck.domain", "white.springapps.io");
		data.put("deck.primaryAccount", "prod");

		// when
		moduleService.deploy("clouddriver", data, "api", "org", "space", "user", "password", "foo");

		// then
		then(appDeployer).should().deploy(new AppDeploymentRequest(
				new AppDefinition("clouddriver", Collections.emptyMap()),
				artifactToUpload,
				any()
		));
		verifyNoMoreInteractions(appDeployer);
	}

	@Test
	public void shouldHandleUndeployingAnApp() {

		// when
		moduleService.undeploy("clouddriver", "api", "org", "space", "user", "password", "foo");

		// then
		then(appDeployer).should().undeploy("clouddriver");
		verifyNoMoreInteractions(appDeployer);
	}

	@Configuration
	@EnableConfigurationProperties(SpinnakerConfiguration.class)
	static class TestConfig {

		@Bean
		CloudFoundryAppDeployer appDeployer() {
			return mock(CloudFoundryAppDeployer.class);
		}

		@Bean
		CloudFoundryAppDeployerFactory cloudFoundryAppDeployerFactoryBean(CloudFoundryAppDeployer stub) {
			return new TestAppDeployerFactory(stub);
		}

		@Bean
		ModuleService moduleService(SpinnakerConfiguration spinnakerConfiguration,
									CloudFoundryAppDeployerFactory appDeployerFactoryBean,
									ApplicationContext ctx) {
			return new ModuleService(spinnakerConfiguration, appDeployerFactoryBean, ctx);
		}

	}

}
