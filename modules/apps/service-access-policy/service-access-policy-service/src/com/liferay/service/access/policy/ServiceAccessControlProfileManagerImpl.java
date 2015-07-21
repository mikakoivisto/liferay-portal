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

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.access.control.profile.ServiceAccessControlProfile;
import com.liferay.portal.kernel.security.access.control.profile.ServiceAccessControlProfileManager;
import com.liferay.portal.kernel.settings.CompanyServiceSettingsLocator;
import com.liferay.portal.kernel.settings.SettingsException;
import com.liferay.portal.kernel.settings.SettingsFactory;
import com.liferay.service.access.policy.configuration.ServiceAccessPolicyConfiguration;
import com.liferay.service.access.policy.constants.ServiceAccessPolicyConstants;
import com.liferay.service.access.policy.model.ServiceAccessPolicy;
import com.liferay.service.access.policy.service.ServiceAccessPolicyService;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Mika Koivisto
 */
@Component(immediate = true, service = ServiceAccessControlProfileManager.class)
public class ServiceAccessControlProfileManagerImpl
	implements ServiceAccessControlProfileManager {

	@Override
	public String getDefaultApplicationServiceAccessControlProfileName(
		long companyId) {

		ServiceAccessPolicyConfiguration serviceAccessPolicyConfiguration =
			getServiceAccessPolicyConfiguration(companyId);

		if (serviceAccessPolicyConfiguration != null) {
			return serviceAccessPolicyConfiguration.
				defaultApplicationServiceAccessPolicyName();
		}

		return null;
	}

	@Override
	public String getDefaultUserServiceAccessControlProfileName(
		long companyId) {

		ServiceAccessPolicyConfiguration serviceAccessPolicyConfiguration =
			getServiceAccessPolicyConfiguration(companyId);

		if (serviceAccessPolicyConfiguration != null) {
			return serviceAccessPolicyConfiguration.
				defaultUserServiceAccessPolicyName();
		}

		return null;
	}

	@Override
	public ServiceAccessControlProfile getServiceAccessControlProfile(
		long companyId, String name) {

		try {
			return toServiceAccessControlProfile(
				_serviceAccessPolicyService.getServiceAccessPolicy(
					companyId, name));
		}
		catch (PortalException e) {
			return null;
		}
	}

	@Override
	public List<ServiceAccessControlProfile> getServiceAccessControlProfiles(
		long companyId, int start, int end) {

		return toServiceAccessControlProfiles(
			_serviceAccessPolicyService.getCompanyServiceAccessPolicies(
				companyId, start, end));
	}

	@Override
	public int getServiceAccessControlProfilesCount(long companyId) {
		return _serviceAccessPolicyService.getCompanyServiceAccessPoliciesCount(
			companyId);
	}

	protected ServiceAccessPolicyConfiguration
		getServiceAccessPolicyConfiguration(long companyId) {

		try {
			return _settingsFactory.getSettings(
				ServiceAccessPolicyConfiguration.class,
				new CompanyServiceSettingsLocator(
					companyId, ServiceAccessPolicyConstants.SERVICE_NAME));
		}
		catch (SettingsException se) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to get Service Access Policy configuration", se);
			}

			return null;
		}
	}

	@Reference(unbind = "-")
	protected void setServiceAccessPolicyService(
		ServiceAccessPolicyService serviceAccessPolicyService) {

		_serviceAccessPolicyService = serviceAccessPolicyService;
	}

	@Reference
	protected void setSettingsFactory(SettingsFactory settingsFactory) {
		_settingsFactory = settingsFactory;
	}

	protected ServiceAccessControlProfile toServiceAccessControlProfile(
		ServiceAccessPolicy serviceAccessPolicy) {

		if (serviceAccessPolicy != null) {
			return new ServiceAccessControlProfileImpl(serviceAccessPolicy);
		}

		return null;
	}

	protected List<ServiceAccessControlProfile> toServiceAccessControlProfiles(
		List<ServiceAccessPolicy> serviceAccessPolicies) {

		if (serviceAccessPolicies == null) {
			return null;
		}

		List<ServiceAccessControlProfile> serviceAccessControlProfiles =
			new ArrayList<>(serviceAccessPolicies.size());

		for (ServiceAccessPolicy serviceAccessPolicy : serviceAccessPolicies) {
			serviceAccessControlProfiles.add(
				toServiceAccessControlProfile(serviceAccessPolicy));
		}

		return serviceAccessControlProfiles;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ServiceAccessControlProfileManagerImpl.class);

	private ServiceAccessPolicyService _serviceAccessPolicyService;
	private volatile SettingsFactory _settingsFactory;

}