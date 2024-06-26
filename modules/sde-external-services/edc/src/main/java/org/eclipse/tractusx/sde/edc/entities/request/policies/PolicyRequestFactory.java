/********************************************************************************
 * Copyright (c) 2022 BMW GmbH
 * Copyright (c) 2022, 2024 T-Systems International GmbH
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.edc.entities.request.policies;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class PolicyRequestFactory {

	public PolicyDefinitionRequest getPolicy(String assetId, ActionRequest action,
			Map<String, String> extensibleProperties, String type) {

		List<PermissionRequest> permissions = getPermissions(assetId, action);

		PolicyRequest policyRequest = PolicyRequest.builder()
				.permissions(permissions)
				.obligations(new ArrayList<>())
				.extensibleProperties(extensibleProperties)
				.prohibitions(new ArrayList<>()).build();
		
		//Use submodel id to generate unique policy id for asset use policy type as prefix asset/usage
		String submodelId = assetId;
		if (assetId.indexOf("urn:uuid") != -1) {
			submodelId = assetId.substring(assetId.indexOf("urn:uuid", 9));
			submodelId =submodelId.replace("urn:uuid:", "");
		}
				
		return PolicyDefinitionRequest.builder()
				.id(type +"-"+ submodelId)
				.policyRequest(policyRequest).build();
	}

	public List<PermissionRequest> getPermissions(String assetId, ActionRequest action) {

		ArrayList<PermissionRequest> permissions = new ArrayList<>();
		if (action != null) {
			PermissionRequest permissionRequest = PermissionRequest
					.builder().action(Map.of("odrl:type", "USE"))
					.target(assetId)
					.constraint(action.getAction())
					.build();
			permissions.add(permissionRequest);
		}
		return permissions;
	}
}