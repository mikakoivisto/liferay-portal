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

package com.liferay.portlet.documentlibrary.util;

import com.liferay.portal.kernel.repository.model.FileVersion;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.MimeTypesUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.File;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexander Chow
 */
public class DLAppUtil {

	public static String fixExtension(
		String fileName, String extension, String mimeTypeExtension) {

		if (Validator.isNotNull(mimeTypeExtension) &&
			!mimeTypeExtension.equals(extension) &&
			Validator.isNotNull(fileName)) {

			return StringUtil.replaceLast(
				fileName, "."+ extension, "." + mimeTypeExtension);
		}

		return fileName;
	}

	public static String getExtension(String title, String sourceFileName) {
		return getExtension(title, sourceFileName, null);
	}

	public static String getExtension(
		String title, String sourceFileName, String mimeType) {

		if (Validator.isNotNull(mimeType) &&
			_extensionsMap.containsKey(mimeType)) {

			return _extensionsMap.get(mimeType);
		}

		String extension = FileUtil.getExtension(sourceFileName);

		if (Validator.isNull(extension)) {
			extension = FileUtil.getExtension(title);
		}

		return extension;
	}

	public static String getMimeType(
		String sourceFileName, String mimeType, String title, File file) {

		String extension = getExtension(title, sourceFileName);

		String fileMimeType = MimeTypesUtil.getContentType(
			file, "A." + extension);

		if (Validator.isNotNull(fileMimeType)) {
			return fileMimeType;
		}

		return mimeType;
	}

	public static boolean isMajorVersion(
		FileVersion previousFileVersion, FileVersion currentFileVersion) {

		long currentVersion = GetterUtil.getLong(
			currentFileVersion.getVersion());
		long previousVersion = GetterUtil.getLong(
			previousFileVersion.getVersion());

		return (currentVersion - previousVersion) >= 1;
	}

	private static final Map<String, String> _extensionsMap = new HashMap<>();

	static {
		_extensionsMap.put("application/pdf", "pdf");
		_extensionsMap.put("application/x-pdf", "pdf");
		_extensionsMap.put("application/x-shockwave-flash", "swf");
		_extensionsMap.put("image/gif", "gif");
		_extensionsMap.put("image/jpg", "jpg");
		_extensionsMap.put("image/png", "png");
		_extensionsMap.put("video/quicktime", "mov");
		_extensionsMap.put("video/x-flv", "flv");
		_extensionsMap.put("video/x-ms-wmv", "wmv");
	}

}