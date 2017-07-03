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

import javax.xml.namespace.QName;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;

/**
 * @author Carlos Sierra Andrés
 */
@Component(
	immediate = true,
	property = "jaxws.handler=true",
	service = Handler.class
)
public class SACPHandler implements LogicalHandler<LogicalMessageContext> {
	@Override
	public boolean handleMessage(LogicalMessageContext context) {
		QName operationName =
			(QName)context.get(LogicalMessageContext.WSDL_OPERATION);

		System.out.println("OPERATION NAME: " + operationName);

		return true;
	}

	@Override
	public boolean handleFault(LogicalMessageContext context) {
		return false;
	}

	@Override
	public void close(MessageContext context) {

	}
}
