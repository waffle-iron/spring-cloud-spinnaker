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

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import retrofit.client.Response;
import retrofit.mime.TypedString;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Various utilities methods to smooth integration in-memory between typically remote processes.
 *
 * @author Greg Turnquist
 */
public class RetrofitUtils {

	private static final ObjectMapper mapper = new ObjectMapper();

	/**
	 * Wrap any type of {@link Object} as a Retrofit {@link Response}.
	 *
	 * @param object
	 * @return {@link Response}
	 */
	public static Response response(Object object) {
		try {
			return new Response("", HttpStatus.OK.value(), "", Collections.emptyList(), new TypedString(mapper.writeValueAsString(object)));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Convert any {@link Object} to a {@link Map}.
	 *
	 * @param object
	 * @return a map-ified version of the object.
	 */
	public static Map<String, Object> toMap(Object object) {
		try {
			return mapper.readValue(mapper.writeValueAsString(object), Map.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
