/********************************************************************************
 * Copyright (c) 2022, 2023 T-Systems International GmbH
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.edc.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.eclipse.tractusx.sde.common.entities.UsagePolicies;
import org.eclipse.tractusx.sde.common.enums.PolicyAccessEnum;
import org.eclipse.tractusx.sde.common.enums.UsagePolicyEnum;
import org.eclipse.tractusx.sde.edc.entities.request.policies.ConstraintRequest;
import org.eclipse.tractusx.sde.edc.enums.Type;
import org.springframework.util.CollectionUtils;

import lombok.SneakyThrows;

public class UtilityFunctions {

	/*
	 * IMP: Resolve SonarQube Code smell Issue for
	 * "Add a private constructor to hide the implicit public one"
	 * 
	 */

	private UtilityFunctions() {
	}

	public static String removeLastSlashOfUrl(String url) {
		url = url.trim();
		if (url.endsWith("/")) {
			return url.substring(0, url.lastIndexOf("/"));
		} else {
			return url;
		}
	}


	public static Map<UsagePolicyEnum, UsagePolicies> getUsagePolicies(
			Map<UsagePolicyEnum, UsagePolicies> usagePolicies, List<ConstraintRequest> constraints) {
		constraints.forEach(constraint -> {
			String leftExpVal = constraint.getLeftOperand();
			String rightExpVal = constraint.getRightOperand().toString();
			Map<UsagePolicyEnum, UsagePolicies> policyResponse = identyAndGetUsagePolicy(leftExpVal, rightExpVal);
			if (policyResponse != null)
				usagePolicies.putAll(policyResponse);
		});

		addMissingPolicies(usagePolicies);
		return usagePolicies;
	}

	public static Map<UsagePolicyEnum, UsagePolicies> identyAndGetUsagePolicy(String leftExpVal, String rightExpVal) {

		Map<UsagePolicyEnum, UsagePolicies> policyResponse = null;
		switch (leftExpVal) {
		case "idsc:ROLE":
			policyResponse = Map.of(UsagePolicyEnum.ROLE,
					UsagePolicies.builder().typeOfAccess(PolicyAccessEnum.RESTRICTED).value(rightExpVal).build());
			break;
		case "idsc:PURPOSE":
			policyResponse = Map.of(UsagePolicyEnum.PURPOSE,
					UsagePolicies.builder().typeOfAccess(PolicyAccessEnum.RESTRICTED).value(rightExpVal).build());
			break;
		default:
			break;
		}
		return policyResponse;
	}

	public static void addCustomUsagePolicy(Map<String, String> extensibleProperties,
			Map<UsagePolicyEnum, UsagePolicies> usagePolicies) {
		if (!CollectionUtils.isEmpty(extensibleProperties)
				&& extensibleProperties.keySet().contains(UsagePolicyEnum.CUSTOM.name())) {
			UsagePolicies policyObj = UsagePolicies.builder().typeOfAccess(PolicyAccessEnum.RESTRICTED)
					.value(extensibleProperties.get(UsagePolicyEnum.CUSTOM.name())).build();
			usagePolicies.put(UsagePolicyEnum.CUSTOM, policyObj);
		} else {
			UsagePolicies policyObj = UsagePolicies.builder().typeOfAccess(PolicyAccessEnum.UNRESTRICTED).value("")
					.build();
			usagePolicies.put(UsagePolicyEnum.CUSTOM, policyObj);
		}
	}

	private static void addMissingPolicies(Map<UsagePolicyEnum, UsagePolicies> usagePolicies) {
		Arrays.stream(UsagePolicyEnum.values()).forEach(policy -> {
			if (!policy.equals(UsagePolicyEnum.CUSTOM)) {
				boolean found = usagePolicies.containsKey(policy);
				if (!found) {
					UsagePolicies policyObj = UsagePolicies.builder().typeOfAccess(PolicyAccessEnum.UNRESTRICTED)
							.value("").build();
					usagePolicies.put(policy, policyObj);
				}
			}
		});
	}

	public static boolean checkTypeOfConnector(String type) {
		return StringUtils.isBlank(type) || Type.PROVIDER.name().equals(type);
	}
	
	@SneakyThrows
	public static String valueReplacer(String requestTemplatePath, Map<String, String> inputData) {
		StringSubstitutor stringSubstitutor1 = new StringSubstitutor(inputData);
		return stringSubstitutor1.replace(requestTemplatePath);
	}

}
