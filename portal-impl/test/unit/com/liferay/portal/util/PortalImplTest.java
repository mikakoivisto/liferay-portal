/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
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

package com.liferay.portal.util;

import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.test.mockito.ReturnArgumentCalledAnswer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.servlet.http.HttpSession;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Miguel Pastor
 */
@PowerMockIgnore("javax.xml.datatype.*")
@PrepareForTest({HttpUtil.class, PropsValues.class})
@RunWith(PowerMockRunner.class)
public class PortalImplTest extends PowerMockito {

	@Test
	public void testIsSecureWithSecureRequest() {
		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setSecure(true);

		boolean secure = _portalImpl.isSecure(mockHttpServletRequest);

		Assert.assertTrue(secure);
	}

	@Test
	public void testIsSecureWithHttpsInitialFalse() throws Exception {
		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setSecure(true);

		HttpSession mockSession = mockHttpServletRequest.getSession();

		mockSession.setAttribute(WebKeys.HTTPS_INITIAL, Boolean.FALSE);

		setPropsValuesValue("COMPANY_SECURITY_AUTH_REQUIRES_HTTPS", true);
		setPropsValuesValue("SESSION_ENABLE_PHISHING_PROTECTION", false);

		boolean secure = _portalImpl.isSecure(mockHttpServletRequest);

		Assert.assertFalse(secure);
	}

	@Test
	public void testIsSecureWithHttpsInitialTrue() throws Exception {
		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setSecure(true);

		HttpSession mockSession = mockHttpServletRequest.getSession();

		mockSession.setAttribute(WebKeys.HTTPS_INITIAL, Boolean.TRUE);

		setPropsValuesValue("COMPANY_SECURITY_AUTH_REQUIRES_HTTPS", true);
		setPropsValuesValue("SESSION_ENABLE_PHISHING_PROTECTION", false);

		boolean secure = _portalImpl.isSecure(mockHttpServletRequest);

		Assert.assertTrue(secure);
	}


	@Test
	public void testIsSecureWithHttpsInitialFalseXForwardedHttps()
		throws Exception {

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.addHeader("X-Forwarded-Proto", "https");
		mockHttpServletRequest.setSecure(false);

		HttpSession mockSession = mockHttpServletRequest.getSession();

		mockSession.setAttribute(WebKeys.HTTPS_INITIAL, Boolean.FALSE);

		setPropsValuesValue("COMPANY_SECURITY_AUTH_REQUIRES_HTTPS", true);
		setPropsValuesValue("SESSION_ENABLE_PHISHING_PROTECTION", false);
		setPropsValuesValue("WEB_SERVER_FORWARDED_PROTO_ENABLED", true);

		boolean secure = _portalImpl.isSecure(mockHttpServletRequest);

		Assert.assertFalse(secure);
	}

	@Test
	public void testIsSecureWithHttpsInitialTrueXForwardedHttps()
		throws Exception {

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.addHeader("X-Forwarded-Proto", "https");
		mockHttpServletRequest.setSecure(false);

		HttpSession mockSession = mockHttpServletRequest.getSession();

		mockSession.setAttribute(WebKeys.HTTPS_INITIAL, Boolean.TRUE);

		setPropsValuesValue("COMPANY_SECURITY_AUTH_REQUIRES_HTTPS", true);
		setPropsValuesValue("SESSION_ENABLE_PHISHING_PROTECTION", false);
		setPropsValuesValue("WEB_SERVER_FORWARDED_PROTO_ENABLED", true);

		boolean secure = _portalImpl.isSecure(mockHttpServletRequest);

		Assert.assertTrue(secure);
	}

	@Test
	public void testUpdateRedirectRemoveLayoutURL() {
		mockStatic(HttpUtil.class);

		when(
			HttpUtil.getQueryString(Mockito.anyString())
		).thenReturn(
			StringPool.BLANK
		);

		when(
			HttpUtil.getParameter(
				Mockito.anyString(), Mockito.anyString(), Mockito.eq(false))
		).thenReturn(
			StringPool.BLANK
		);

		when(
			HttpUtil.encodeURL(Mockito.anyString())
		).thenAnswer(
			new ReturnArgumentCalledAnswer<String>(0)
		);

		when(
			HttpUtil.getPath(Mockito.anyString())
		).thenAnswer(
			new ReturnArgumentCalledAnswer<String>(0)
		);

		Assert.assertEquals(
			"/web/group",
			_portalImpl.updateRedirect(
				"/web/group/layout", "/group/layout", "/group"));

		verifyStatic();
	}

	protected void setPropsValuesValue(String fieldName, boolean value)
		throws Exception{

		Field field = field(PropsValues.class, fieldName);

		field.setAccessible(true);

		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

		field.set(PropsValues.class, value);
	}

	private PortalImpl _portalImpl = new PortalImpl();

}