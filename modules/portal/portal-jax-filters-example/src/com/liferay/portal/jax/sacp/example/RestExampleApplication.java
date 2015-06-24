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

import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.Set;

/**
 * @author Carlos Sierra Andrés
 */
@Component(
	immediate = true,
	property = "jaxrs.application=true",
	service = Application.class
)
public class RestExampleApplication extends Application {

	@Override
	public Set<Class<?>> getClasses() {
		return Collections.<Class<?>>singleton(ExampleRestResource.class);
	}

}
