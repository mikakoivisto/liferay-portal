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

package com.liferay.portal.jax.sacp.example;

import org.osgi.service.component.annotations.Component;

import javax.jws.WebService;

/**
 * @author Carlos Sierra Andrés
 */
@Component(
	immediate = true,
	property = "jaxws=true",
	service = SoapExampleEndpoint.class
)
@WebService
public class SoapExampleEndpoint {

	public String methodOne(String parameterOne) {
		return "I am methodOne with a parameter: " + parameterOne;
	}

	public String methodTwo(String parameterTwo) {
		return "I am methodTwo with a parameter:" + parameterTwo;
	}

}
