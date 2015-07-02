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

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.service.UserService;
import com.liferay.portal.service.UserServiceUtil;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.osgi.service.component.annotations.Reference;

/**
 * @author Carlos Sierra Andrés
 */
@Path("/rest")
public class ExampleRestResource {

	@GET
	@Produces("text/plain")
	public String exampleGet() throws PortalException {
		return "Hey... a GET " + UserServiceUtil.getUserByScreenName(20142, "joebloggs");
	}

	@POST
	@Consumes("text/plain")
	public String examplePost(String body) {
		return "wow... a POST";
	}

	@Reference
	public void setUserService(UserService userService) {
		_userService = userService;
	}

	private volatile UserService _userService;
}
