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

import rx.Observable;

import com.netflix.spinnaker.orca.bakery.api.Bake;
import com.netflix.spinnaker.orca.bakery.api.BakeRequest;
import com.netflix.spinnaker.orca.bakery.api.BakeStatus;
import com.netflix.spinnaker.orca.bakery.api.BakeryService;
import com.netflix.spinnaker.orca.bakery.api.BaseImage;

/**
 * @author Greg Turnquist
 */
public class NoopBakeryServiceImpl implements BakeryService {

	@Override
	public Observable<BakeStatus> createBake(String s, BakeRequest bakeRequest, String s1) {
		return null;
	}

	@Override
	public Observable<BakeStatus> lookupStatus(String s, String s1) {
		return null;
	}

	@Override
	public Observable<Bake> lookupBake(String s, String s1) {
		return null;
	}

	@Override
	public Observable<BaseImage> getBaseImage(String s, String s1) {
		return null;
	}
}
