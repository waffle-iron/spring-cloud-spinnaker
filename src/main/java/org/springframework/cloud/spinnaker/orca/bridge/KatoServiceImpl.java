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

import retrofit.http.Body;
import retrofit.http.Path;
import rx.Observable;

import com.netflix.spinnaker.orca.clouddriver.KatoService;
import com.netflix.spinnaker.orca.clouddriver.model.Task;
import com.netflix.spinnaker.orca.clouddriver.model.TaskId;

/**
 * @author Greg Turnquist
 */
public class KatoServiceImpl implements KatoService {

	@Override
	public Observable<TaskId> requestOperations(@Body Collection<? extends Map<String, Map>> operations) {
		return null;
	}

	@Override
	public Observable<TaskId> requestOperations(@Path("cloudProvider") String cloudProvider, @Body Collection<? extends Map<String, Map>> operations) {
		return null;
	}

	@Override
	public Observable<List<Task>> listTasks() {
		return null;
	}

	@Override
	public Observable<Task> lookupTask(@Path("id") String id) {
		return null;
	}
}
