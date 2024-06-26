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

package org.eclipse.tractusx.sde.pcfexchange.proxy;

import java.net.URI;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.JsonNode;

@FeignClient(name = "PCFExchangeProxy", url = "placeholder")
public interface PCFExchangeProxy {

	@PutMapping
	public ResponseEntity<Object> uploadPcfSubmodel(URI url, @RequestHeader Map<String, String> requestHeader,
			@RequestParam(value = "BPN", required = true) String bpnNumber,
			@RequestParam(value = "requestId", required = false) String requestId,
			@RequestParam(value = "message", required = false) String message, @RequestBody JsonNode pcfData);

	@GetMapping
	public ResponseEntity<Object> getPcfByProduct(URI url, @RequestHeader Map<String, String> requestHeader,
			@RequestParam(value = "BPN", required = true) String bpnNumber,
			@RequestParam(value = "requestId", required = true) String requestId, @RequestParam String message);

}
