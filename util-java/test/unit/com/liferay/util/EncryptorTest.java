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

package com.liferay.util;

import com.liferay.portal.kernel.util.Props;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;

import java.security.Key;
import java.security.KeyFactory;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Mika Koivisto
 */
@RunWith(PowerMockRunner.class)
public class EncryptorTest extends PowerMockito {

	@Before
	public void setUp() {
		Props props = mock(Props.class);

		PropsUtil propsUtil = new PropsUtil();

		propsUtil.setProps(props);

		when(
			props.get(Mockito.eq(PropsKeys.COMPANY_ENCRYPTION_ALGORITHM))
		).thenReturn(
			"DSA"
		);

		when(
			props.get(Mockito.eq(PropsKeys.COMPANY_ENCRYPTION_KEY_SIZE))
		).thenReturn(
			"56"
		);
	}

	@Test
	public void testStringToKey() throws Exception {
		for (Object obj : java.security.Security.getAlgorithms("Cipher")) {
			  System.out.println(obj);
			}
		Key key = Encryptor.generateKey();

		KeyFactory keyFactory = KeyFactory.getInstance(Encryptor.KEY_ALGORITHM);
		SecretKeySpec secretKeySpec = keyFactory.getKeySpec(key, SecretKeySpec.class);

		byte[] encodedKey = secretKeySpec.getEncoded();

		SecretKey s = new SecretKeySpec(encodedKey, Encryptor.KEY_ALGORITHM);

		String enc = Encryptor.encrypt(key, "Hello world");
		Assert.assertEquals("Hello World", Encryptor.decrypt(s, enc));
	}
}
