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
package org.springframework.cloud.spinnaker.orca;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.test.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author Greg Turnquist
 */
public class OrcaConfigurationTest {

	private static final Logger log = LoggerFactory.getLogger(OrcaConfigurationTest.class);

	private AnnotationConfigApplicationContext context;

	@Test
	public void enableOrcaShouldPullInCloudFoundryBeans() {
		context = new AnnotationConfigApplicationContext();
		context.register(OrcaApp.class);
		EnvironmentTestUtils.addEnvironment(context,
				"echo.baseUrl:http://echo.example.com",
				"front50.baseUrl:http://front50.example.com",
				"oort.baseUrl:http://oort.example.com",
				"kato.baseUrl:http://kato.example.com",
				"mort.baseUrl:http://mort.example.com");
		context.refresh();
	}

	@EnableOrca
	static class OrcaApp {

	}

}
