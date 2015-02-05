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

package com.liferay.portal.kernel.servlet;

import com.liferay.portal.kernel.util.Props;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;

import java.io.IOException;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import org.testng.Assert;

/**
 * @author Tomas Polesovsky
 */
@RunWith(PowerMockRunner.class)
public class ServletResponseUtilRangeTest extends PowerMockito {

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		setUpPropsUtil();
	}

	@Test
	public void testGetMultipleRanges() throws IOException {
		long length = 1000;
		String rangeHeader = "bytes=1-3,3-8,9-11,12-12,30-";

		setupRange(request, rangeHeader);

		List<Range> ranges = ServletResponseUtil.getRanges(
			request, response, length);

		Assert.assertEquals(ranges.size(), 5);
		assertRange(ranges.get(0), 1, 3, 3);
		assertRange(ranges.get(1), 3, 8, 6);
		assertRange(ranges.get(2), 9, 11, 3);
		assertRange(ranges.get(3), 12, 12, 1);
		assertRange(ranges.get(4), 30, 999, 970);
	}

	@Test
	public void testGetRangesPerSpec() throws IOException {

		// https://tools.ietf.org/html/rfc7233#section-2.1
		// Additional examples, assuming a representation of length 10000:

		long length = 10000;

		// The final 500 bytes (byte offsets 9500-9999, inclusive):
		// 		bytes=-500
		// Or:
		// 		bytes=9500-

		String rangeHeader = "bytes=-500";
		setupRange(request, rangeHeader);
		List<Range> ranges = ServletResponseUtil.getRanges(
			request, response, length);

		Assert.assertEquals(ranges.size(), 1);
		assertRange(ranges.get(0), 9500, 9999, 500);

		rangeHeader = "bytes=9500-";
		setupRange(request, rangeHeader);
		ranges = ServletResponseUtil.getRanges(request, response, length);
		Assert.assertEquals(ranges.size(), 1);
		assertRange(ranges.get(0), 9500, 9999, 500);

		// The first and last bytes only (bytes 0 and 9999):
		// 		bytes=0-0,-1

		rangeHeader = "bytes=0-0,-1";
		setupRange(request, rangeHeader);
		ranges = ServletResponseUtil.getRanges(request, response, length);
		Assert.assertEquals(ranges.size(), 2);
		assertRange(ranges.get(0), 0, 0, 1);
		assertRange(ranges.get(1), 9999, 9999, 1);

		// Other valid (but not canonical) specifications of the second 500
		// bytes (byte offsets 500-999, inclusive):
		// 		bytes=500-600,601-999
		// 		bytes=500-700,601-999

		rangeHeader = "bytes=500-600,601-999";
		setupRange(request, rangeHeader);
		ranges = ServletResponseUtil.getRanges(request, response, length);
		Assert.assertEquals(ranges.size(), 2);
		assertRange(ranges.get(0), 500, 600, 101);
		assertRange(ranges.get(1), 601, 999, 399);

		rangeHeader = "bytes=500-700,601-999";
		setupRange(request, rangeHeader);
		ranges = ServletResponseUtil.getRanges(request, response, length);
		Assert.assertEquals(ranges.size(), 2);
		assertRange(ranges.get(0), 500, 700, 201);
		assertRange(ranges.get(1), 601, 999, 399);
	}

	@Test
	public void testGetRangesSimple() throws IOException {
		long length = 1000;
		String rangeHeader = "bytes=0-999";

		setupRange(request, rangeHeader);

		List<Range> ranges = ServletResponseUtil.getRanges(
			request, response, length);

		Assert.assertEquals(ranges.size(), 1);
		assertRange(ranges.get(0), 0, 999, 1000);
	}

	protected void assertRange(Range range, long start, long end, long length) {
		Assert.assertEquals(range.getStart(), start);
		Assert.assertEquals(range.getEnd(), end);
		Assert.assertEquals(range.getLength(), length);
	}

	protected void setUpPropsUtil() {
		PropsUtil.setProps(props);

		when (
			props.get(PropsKeys.WEB_SERVER_SERVLET_MAX_RANGE_FIELDS)
		).thenReturn(
			"10"
		);
	}

	protected void setupRange(HttpServletRequest request, String rangeHeader) {
		when(
			request.getHeader(HttpHeaders.RANGE)
		).thenReturn(
			rangeHeader
		);
	}

	@Mock
	protected Props props;

	@Mock
	protected HttpServletRequest request;

	@Mock
	protected HttpServletResponse response;

}