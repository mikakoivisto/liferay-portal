/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.service.access.policy;

import com.liferay.portal.kernel.security.access.control.profile.ServiceAccessControlProfile;
import com.liferay.service.access.policy.model.ServiceAccessPolicy;

import java.util.List;
import java.util.Locale;

/**
 * @author Mika Koivisto
 */
public class ServiceAccessControlProfileImpl
	implements ServiceAccessControlProfile {

	public ServiceAccessControlProfileImpl(
		ServiceAccessPolicy serviceAccessPolicy) {

		_serviceAccessPolicy = serviceAccessPolicy;
	}

	@Override
	public List<String> getAllowedServiceSignaturesList() {
		return _serviceAccessPolicy.getAllowedServiceSignaturesList();
	}

	@Override
	public String getName() {
		return _serviceAccessPolicy.getName();
	}

	@Override
	public String getTitle(Locale locale) {
		return _serviceAccessPolicy.getTitle(locale);
	}

	private final ServiceAccessPolicy _serviceAccessPolicy;

}