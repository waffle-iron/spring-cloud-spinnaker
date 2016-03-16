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

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.groovy.template.GroovyTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.cloud.spinnaker.clouddriver.bridge.NoopFront50ServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.netflix.spinnaker.clouddriver.WebConfig;
import com.netflix.spinnaker.clouddriver.cf.config.CloudFoundryConfig;
import com.netflix.spinnaker.clouddriver.core.CloudDriverConfig;
import com.netflix.spinnaker.clouddriver.core.services.Front50Service;
import com.netflix.spinnaker.clouddriver.deploy.config.DeployConfiguration;

/**
 * @author Greg Turnquist
 */
@Configuration
@Import({WebConfig.class, CloudDriverConfig.class, DeployConfiguration.class, CloudFoundryConfig.class})
@EnableAutoConfiguration(exclude = {BatchAutoConfiguration.class, GroovyTemplateAutoConfiguration.class,
		SecurityAutoConfiguration.class})
@EnableScheduling
public class CloudDriverConfiguration {

	@Bean
	@ConditionalOnMissingBean
	Front50Service noopFront50Service() {
		return new NoopFront50ServiceImpl();
	}

}