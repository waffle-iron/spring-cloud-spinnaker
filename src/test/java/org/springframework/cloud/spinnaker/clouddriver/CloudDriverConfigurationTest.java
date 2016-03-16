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
package org.springframework.cloud.spinnaker.clouddriver;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Matchers.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.test.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.netflix.spinnaker.clouddriver.cf.config.CloudFoundryConfigurationProperties;
import com.netflix.spinnaker.clouddriver.cf.deploy.handlers.CloudFoundryDeployHandler;
import com.netflix.spinnaker.clouddriver.cf.security.CloudFoundryCredentialsInitializer;
import com.netflix.spinnaker.clouddriver.cf.utils.CloudFoundryClientFactory;
import com.netflix.spinnaker.clouddriver.helpers.OperationPoller;

/**
 * @author Greg Turnquist
 */
public class CloudDriverConfigurationTest {

	private static final Logger log = LoggerFactory.getLogger(CloudDriverConfigurationTest.class);

	private AnnotationConfigApplicationContext context;

	@Test
	public void enableCloudDriverShouldPullInCloudFoundryBeans() {
		context = new AnnotationConfigApplicationContext();
		context.register(CloudDriverApp.class);
		EnvironmentTestUtils.addEnvironment(context, "cf.enabled:true");
		context.refresh();

		assertThat(context.getBean(CloudFoundryConfigurationProperties.class), not(isNull()));
		assertThat(context.getBean(CloudFoundryCredentialsInitializer.class), not(isNull()));
		assertThat(context.getBean(CloudFoundryClientFactory.class), not(isNull()));
		assertThat(context.getBean(CloudFoundryDeployHandler.class), not(isNull()));
		assertThat(context.getBean(OperationPoller.class), not(isNull()));
	}

	@EnableCloudDriver
	static class CloudDriverApp {

	}

}
