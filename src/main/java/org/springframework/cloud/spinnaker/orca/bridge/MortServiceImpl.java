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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.Path;
import retrofit.http.Query;

import com.netflix.spinnaker.orca.clouddriver.MortService;

/**
 * @author Greg Turnquist
 */
public class MortServiceImpl implements MortService {

	@Override
	public SecurityGroup getSecurityGroup(@Path("account") String account, @Path("type") String type, @Path("securityGroupName") String securityGroupName, @Path("region") String region) {
		return null;
	}

	@Override
	public SecurityGroup getSecurityGroup(@Path("account") String account, @Path("type") String type, @Path("securityGroupName") String securityGroupName, @Path("region") String region, @Query("vpcId") String vpcId) {
		return null;
	}

	@Override
	public Collection<VPC> getVPCs() {
		return null;
	}

	@Override
	public List<SearchResult> getSearchResults(@Query("q") String searchTerm, @Query("type") String type) {
		return null;
	}

	@Override
	public Response forceCacheUpdate(@Path("cloudProvider") String cloudProvider, @Path("type") String type, @Body Map<String, ? extends Object> data) {
		return null;
	}
}
