/********************************************************************************
 * Copyright (c) 2022,2024 T-Systems International GmbH
 * Copyright (c) 2022,2024 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.sde.portal.api;

import java.net.URI;
import java.util.Map;

import org.eclipse.tractusx.sde.portal.model.LegalEntityData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(value = "IPartnerPoolExternalServiceApi", url = "placeholder")
public interface IPartnerPoolExternalServiceApi {
    
	@GetMapping(path = "/legal-entities")
    LegalEntityData fetchLegalEntityData(URI url, @RequestParam String bpnLs, @RequestParam String legalName, @RequestParam Integer page, @RequestParam Integer size,@RequestHeader Map<String, String> header);

}