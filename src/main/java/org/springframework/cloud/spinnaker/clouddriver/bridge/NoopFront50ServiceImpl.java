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
package org.springframework.cloud.spinnaker.clouddriver.bridge;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.netflix.spinnaker.clouddriver.core.services.Front50Service;

/**
 * @author Greg Turnquist
 */
@Service
public class NoopFront50ServiceImpl implements Front50Service {

	@Override
	public List<Map> getCredentials() {
		return null;
	}

	@Override
	public List<Map> searchByName(String account, String name) {
		return null;
	}

	@Override
	public Map getApplication(String account, String name) {
		return null;
	}

	@Override
	public Map getProject(String project) {
		return null;
	}

	@Override
	public HalList searchForProjects(String query) {
		return null;
	}
}
