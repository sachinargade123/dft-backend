/********************************************************************************
 * Copyright (c) 2024 T-Systems International GmbH
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package org.eclipse.tractusx.sde.common.mapper;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import lombok.SneakyThrows;

@Component
public class AspectResponseFactory {

	ObjectMapper mapper = new ObjectMapper();
	Gson gson = new GsonBuilder().serializeNulls().create();

	@SneakyThrows
	public JsonObject maptoReponse(Object csvObject, Object aspectObject) {
		JsonObject jobj = new JsonObject();
		jobj.add("csv", fasterJsonMapper(csvObject));
		jobj.add("json", gsonMapper(aspectObject));

		return jobj;
	}

	private JsonObject gsonMapper(Object aspectObject) {
		if (aspectObject instanceof String) {
			return gson.fromJson(aspectObject.toString(), JsonObject.class);
		}
		return gson.toJsonTree(aspectObject).getAsJsonObject();
	}

	private JsonObject fasterJsonMapper(Object csvObject) throws JsonProcessingException {
		String writeValueAsString = "";
		if (csvObject instanceof JsonObject) {
			writeValueAsString = gson.toJson(csvObject);
		} else {
			writeValueAsString = mapper.writeValueAsString(csvObject);
		}
		writeValueAsString = writeValueAsString.replace(":null", ": \"\"");
		return gson.fromJson(writeValueAsString, JsonObject.class);
	}
}
