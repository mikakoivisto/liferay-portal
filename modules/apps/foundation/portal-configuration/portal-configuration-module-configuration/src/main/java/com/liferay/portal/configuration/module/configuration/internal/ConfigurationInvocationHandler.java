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

package com.liferay.portal.configuration.module.configuration.internal;

import aQute.bnd.annotation.metatype.Meta;

import com.liferay.portal.kernel.settings.LocalizedValuesMap;
import com.liferay.portal.kernel.settings.TypedSettings;
import com.liferay.portal.kernel.util.ProxyUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Iván Zaera
 */
public class ConfigurationInvocationHandler<S> implements InvocationHandler {

	public ConfigurationInvocationHandler(
		Class<S> clazz, Object configurationOverrideInstance,
		TypedSettings typedSettings) {

		_clazz = clazz;
		_configurationOverrideInstance = configurationOverrideInstance;
		_typedSettings = typedSettings;
	}

	public S createProxy() {
		return (S)ProxyUtil.newProxyInstance(
			_clazz.getClassLoader(), new Class[] {_clazz}, this);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
		throws InvocationTargetException {

		if (_configurationOverrideInstance != null) {
			try {
				return _invokeConfigurationOverride(method, args);
			}
			catch (InvocationTargetException ite) {
				throw ite;
			}
			catch (Exception e) {
			}
		}

		try {
			return _invokeTypedSettings(method);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Object _getValueNoDefault(Class<?> returnType, String key)
		throws NoSuchMethodException, IllegalAccessException,
		InvocationTargetException, InstantiationException {

		if (returnType.equals(boolean.class)) {
			return _typedSettings.getValue(key, null);
		}
		else if (returnType.equals(double.class)) {
			return _typedSettings.getValue(key, null);
		}
		else if (returnType.equals(float.class)) {
			return _typedSettings.getValue(key, null);
		}
		else if (returnType.equals(int.class)) {
			return _typedSettings.getValue(key, null);
		}
		else if (returnType.equals(LocalizedValuesMap.class)) {
			return _typedSettings.getValue(key, null);
		}
		else if (returnType.equals(long.class)) {
			return _typedSettings.getValue(key, null);
		}
		else if (returnType.equals(String.class)) {
			return _typedSettings.getValue(key, null);
		}
		else if (returnType.equals(String[].class)) {
			return _typedSettings.getValues(key, null);
		}
		else if (returnType.isEnum()) {
			Method valueOfMethod = returnType.getDeclaredMethod(
				"valueOf", String.class);

			return valueOfMethod.invoke(
				returnType, _typedSettings.getValue(key, null));
		}

		Constructor<?> constructor = returnType.getConstructor(String.class);

		return constructor.newInstance(_typedSettings.getValue(key, null));
	}

	private Object _getValueWithDefault(Class<?> returnType, String key)
		throws NoSuchMethodException, IllegalAccessException,
		InvocationTargetException, InstantiationException {

		if (returnType.equals(boolean.class)) {
			return _typedSettings.getBooleanValue(key);
		}
		else if (returnType.equals(double.class)) {
			return _typedSettings.getDoubleValue(key);
		}
		else if (returnType.equals(float.class)) {
			return _typedSettings.getFloatValue(key);
		}
		else if (returnType.equals(int.class)) {
			return _typedSettings.getIntegerValue(key);
		}
		else if (returnType.equals(LocalizedValuesMap.class)) {
			return _typedSettings.getLocalizedValuesMap(key);
		}
		else if (returnType.equals(long.class)) {
			return _typedSettings.getLongValue(key);
		}
		else if (returnType.equals(String.class)) {
			return _typedSettings.getValue(key);
		}
		else if (returnType.equals(String[].class)) {
			return _typedSettings.getValues(key);
		}
		else if (returnType.isEnum()) {
			Method valueOfMethod = returnType.getDeclaredMethod(
				"valueOf", String.class);

			return valueOfMethod.invoke(
				returnType, _typedSettings.getValue(key));
		}

		Constructor<?> constructor = returnType.getConstructor(String.class);

		return constructor.newInstance(_typedSettings.getValue(key));
	}

	private Object _invokeConfigurationOverride(Method method, Object[] args)
		throws IllegalAccessException, InvocationTargetException,
			   NoSuchMethodException {

		Class<?> clazz = _configurationOverrideInstance.getClass();

		method = clazz.getMethod(method.getName(), method.getParameterTypes());

		return method.invoke(_configurationOverrideInstance, args);
	}

	private Object _invokeTypedSettings(Method method)
		throws NoSuchMethodException, IllegalAccessException,
			   InvocationTargetException, InstantiationException {

		Class<?> returnType = method.getReturnType();

		Object value = null;

		Meta.AD ad = method.getAnnotation(Meta.AD.class);

		if ((ad != null) && !_NULL_VALUE.equals(ad.id())) {
			value = _getValueNoDefault(returnType, ad.id());
		}

		if (value != null) {
			return value;
		}

		return _getValueWithDefault(returnType, method.getName());
	}

	private static final String _NULL_VALUE = "§NULL§";

	private final Class<S> _clazz;
	private final Object _configurationOverrideInstance;
	private final TypedSettings _typedSettings;

}