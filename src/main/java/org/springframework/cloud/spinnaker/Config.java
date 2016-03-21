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

import java.util.List;

import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.CloudFoundryOperations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.deployer.spi.cloudfoundry.CloudFoundryAppDeployProperties;
import org.springframework.cloud.deployer.spi.cloudfoundry.CloudFoundryAppDeployer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Greg Turnquist
 */
@Configuration
@EnableConfigurationProperties({CloudFoundryAppDeployProperties.class, SpinnakerConfiguration.class})
public class Config {

	@Bean
	public CloudFoundryClient cloudFoundryClient(CloudFoundryAppDeployProperties properties) {

		return new CloudFoundryClient(
			new CloudCredentials(properties.getUsername(), properties.getPassword()),
			properties.getApiEndpoint(),
			properties.getOrganization(),
			properties.getSpace(),
			properties.isSkipSslValidation());
	}

	@Bean
	public CloudFoundryAppDeployer cloudFoundryAppDeployer(CloudFoundryAppDeployProperties properties,
														   CloudFoundryOperations client) {
		return new CloudFoundryAppDeployer(properties, client);
	}

}
