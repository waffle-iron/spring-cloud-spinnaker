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

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.groovy.template.GroovyTemplateAutoConfiguration;
import org.springframework.cloud.spinnaker.orca.bridge.KatoServiceImpl;
import org.springframework.cloud.spinnaker.orca.bridge.MortServiceImpl;
import org.springframework.cloud.spinnaker.orca.bridge.NoopBakeryServiceImpl;
import org.springframework.cloud.spinnaker.orca.bridge.OortServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.spinnaker.cats.cache.Cache;
import com.netflix.spinnaker.clouddriver.cf.controllers.CloudFoundryLoadBalancerController;
import com.netflix.spinnaker.clouddriver.controllers.ApplicationsController;
import com.netflix.spinnaker.clouddriver.controllers.CacheController;
import com.netflix.spinnaker.clouddriver.controllers.ClusterController;
import com.netflix.spinnaker.clouddriver.controllers.InstanceController;
import com.netflix.spinnaker.clouddriver.controllers.SearchController;
import com.netflix.spinnaker.orca.applications.pipelines.CreateApplicationStage;
import com.netflix.spinnaker.orca.applications.tasks.DeleteApplicationTask;
import com.netflix.spinnaker.orca.bakery.api.BakeryService;
import com.netflix.spinnaker.orca.clouddriver.KatoService;
import com.netflix.spinnaker.orca.clouddriver.MortService;
import com.netflix.spinnaker.orca.clouddriver.OortService;
import com.netflix.spinnaker.orca.clouddriver.pipeline.AbstractCloudProviderAwareStage;
import com.netflix.spinnaker.orca.clouddriver.tasks.AbstractCloudProviderAwareTask;
import com.netflix.spinnaker.orca.clouddriver.utils.CloudProviderAware;
import com.netflix.spinnaker.orca.config.JesqueConfiguration;
import com.netflix.spinnaker.orca.config.OrcaPersistenceConfiguration;
import com.netflix.spinnaker.orca.config.RedisConfiguration;
import com.netflix.spinnaker.orca.data.jackson.StageMixins;
import com.netflix.spinnaker.orca.echo.config.EchoConfiguration;
import com.netflix.spinnaker.orca.eureka.DiscoveryPollingConfiguration;
import com.netflix.spinnaker.orca.front50.config.Front50Configuration;
import com.netflix.spinnaker.orca.igor.config.IgorConfiguration;
import com.netflix.spinnaker.orca.kato.pipeline.EnableInstancesInDiscoveryStage;
import com.netflix.spinnaker.orca.kato.tasks.EnableInstancesInDiscoveryTask;
import com.netflix.spinnaker.orca.pipeline.model.PipelineStage;

/**
 * @author Greg Turnquist
 */
@Configuration
@EnableAutoConfiguration(exclude = {BatchAutoConfiguration.class, GroovyTemplateAutoConfiguration.class})
@EnableBatchProcessing(modular = true)
@Import({
	com.netflix.spinnaker.orca.config.OrcaConfiguration.class,
	OrcaPersistenceConfiguration.class,
	RedisConfiguration.class,
	JesqueConfiguration.class,
	EchoConfiguration.class,
	DiscoveryPollingConfiguration.class,
	Front50Configuration.class,
	IgorConfiguration.class,
})
@ComponentScan(basePackageClasses = {
	CreateApplicationStage.class, DeleteApplicationTask.class,
	AbstractCloudProviderAwareStage.class, AbstractCloudProviderAwareTask.class, CloudProviderAware.class,
	EnableInstancesInDiscoveryStage.class, EnableInstancesInDiscoveryTask.class
})
public class OrcaConfiguration {

	@Bean
	@ConditionalOnMissingBean
	InstanceInfo instanceInfo() {
		return InstanceInfo.Builder.newBuilder().setAppName("orca").setHostName("localhost").build();
	}

	@Bean
	@ConditionalOnMissingBean
	BakeryService noopBakeryService() {
		return new NoopBakeryServiceImpl();
	}

	@Bean
	@ConditionalOnMissingBean
	OortService oortService(ClusterController clusterController,
							SearchController searchController,
							ApplicationsController applicationsController,
							InstanceController instanceController,
							CloudFoundryLoadBalancerController cloudFoundryLoadBalancerController,
							CacheController cacheController) {
		return new OortServiceImpl(clusterController, searchController, applicationsController, instanceController, cloudFoundryLoadBalancerController, cacheController);
	}

	@Bean
	ClusterController clusterController() {
		return new ClusterController();
	}

	@Bean
	SearchController searchController() {
		return new SearchController();
	}

	@Bean
	ApplicationsController applicationsController() {
		return new ApplicationsController();
	}

	@Bean
	InstanceController instanceController() {
		return new InstanceController();
	}

	@Bean
	CloudFoundryLoadBalancerController cloudFoundryLoadBalancerController(Cache cacheView) {
		return new CloudFoundryLoadBalancerController(cacheView);
	}

	@Bean
	CacheController cacheController() {
		return new CacheController();
	}

	@Bean
	@ConditionalOnMissingBean
	MortService mortService() {
		return new MortServiceImpl();
	}

	@Bean
	@ConditionalOnMissingBean
	KatoService katoService() {
		return new KatoServiceImpl();
	}

	@Bean
	StockMappingJackson2HttpMessageConverter customJacksonConverter(ObjectMapper objectMapper) {
		objectMapper.addMixIn(PipelineStage.class, StageMixins.class);
		return new StockMappingJackson2HttpMessageConverter(objectMapper);
	}

	static class StockMappingJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {
		public StockMappingJackson2HttpMessageConverter(ObjectMapper objectMapper) {
			super(objectMapper);
		}
	}

}
