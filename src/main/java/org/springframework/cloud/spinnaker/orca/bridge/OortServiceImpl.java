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
package org.springframework.cloud.spinnaker.orca.bridge;

import static org.springframework.cloud.spinnaker.RetrofitUtils.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import retrofit.client.Response;

import org.springframework.stereotype.Service;

import com.netflix.spinnaker.clouddriver.cf.controllers.CloudFoundryLoadBalancerController;
import com.netflix.spinnaker.clouddriver.controllers.ApplicationsController;
import com.netflix.spinnaker.clouddriver.controllers.CacheController;
import com.netflix.spinnaker.clouddriver.controllers.ClusterController;
import com.netflix.spinnaker.clouddriver.controllers.InstanceController;
import com.netflix.spinnaker.clouddriver.controllers.SearchController;
import com.netflix.spinnaker.orca.clouddriver.OortService;

/**
 * @author Greg Turnquist
 */
@Service
public class OortServiceImpl implements OortService {

	ClusterController clusterController;

	SearchController searchController;

	ApplicationsController applicationsController;

	InstanceController instanceController;

	CloudFoundryLoadBalancerController cloudFoundryLoadBalancerController;

	CacheController cacheController;

	public OortServiceImpl(ClusterController clusterController,
						   SearchController searchController,
						   ApplicationsController applicationsController,
						   InstanceController instanceController,
						   CloudFoundryLoadBalancerController cloudFoundryLoadBalancerController,
						   CacheController cacheController) {
		this.clusterController = clusterController;
		this.searchController = searchController;
		this.applicationsController = applicationsController;
		this.instanceController = instanceController;
		this.cloudFoundryLoadBalancerController = cloudFoundryLoadBalancerController;
		this.cacheController = cacheController;
	}

	@Override
	public Response getCluster(String app, String account, String cluster, String type) {
		return response(clusterController.getForAccountAndNameAndType(app, account, cluster, type));
	}

	@Override
	public Response getServerGroup(String app, String account, String cluster, String serverGroup, String region, String type) {
		return response(clusterController.getServerGroup(app, account, cluster, type, serverGroup, region, null));
	}

	@Override
	public Response getTargetServerGroup(String app, String account, String cluster, String cloudProvider, String scope, String target) {
		return response(clusterController.getTargetServerGroup(app, account, cluster, cloudProvider, scope, target, null, null));
	}

	@Override
	public Map<String, Object> getServerGroupSummary(String app, String account, String cluster, String cloudProvider,
													 String scope, String target, String summaryType, String onlyEnabled) {
		return toMap(clusterController.getServerGroupSummary(app, account, cluster, cloudProvider, scope, target, summaryType, onlyEnabled));
	}

	@Override
	public Response getSearchResults(String searchTerm, String type, String platform) {
		final SearchController.SearchQueryCommand q = new SearchController.SearchQueryCommand();
		q.setQ(searchTerm);
		q.setType(Collections.singletonList(type));
		q.setPlatform(platform);
		return response(searchController.search(q));
	}

	@Override
	public Response getApplication(String app) {
		return response(applicationsController.get(app));
	}

	@Override
	public Response getInstance(String account, String region, String id) {
		return response(instanceController.getInstance(account, region, id));
	}

	@Override
	public List<Map> getLoadBalancerDetails(String provider, String account, String region, String name) {
		return cloudFoundryLoadBalancerController.getDetailsInAccountAndRegionByName(account, region, name);
	}

	@Override
	public Response forceCacheUpdate(String cloudProvider, String type, Map<String, ? extends Object> data) {
		return response(cacheController.handleOnDemand(cloudProvider, type, data));
	}

	@Override
	public Collection<Map> pendingForceCacheUpdates(String cloudProvider, String type) {
		return cacheController.pendingOnDemands(cloudProvider, type);
	}

	@Override
	public List<Map> getByAmiId(String s, String s1, String s2, Object o) {
		throw new RuntimeException("Cloud Foundry doesn't use this method");
	}

	@Override
	public List<Map> findImage(String s, String s1, String s2, String s3) {
		throw new RuntimeException("Cloud Foundry doesn't use this method");
	}
}
