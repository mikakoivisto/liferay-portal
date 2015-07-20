<%--
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
--%>

<%@ include file="/init.jsp" %>

<%
String redirect = ParamUtil.getString(request, "redirect");

long sacpEntryId = ParamUtil.getLong(request, "sacpEntryId");

SACPEntry sacpEntry = null;

if (sacpEntryId > 0) {
	sacpEntry = SACPEntryServiceUtil.getSACPEntry(sacpEntryId);
}

boolean defaultSACPEntry = false;

if (sacpEntry != null) {
	defaultSACPEntry = sacpEntry.isDefaultSACPEntry();
}

int[] allowedServiceSignaturesIndexes = new int[] {0};

%>

<liferay-ui:header
	backURL="<%= redirect %>"
	title='<%= (sacpEntry != null) ? sacpEntry.getTitle(locale) : "new-service-access-control-profile" %>'
/>

<portlet:actionURL name="updateSACPEntry" var="updateSACPEntryURL">
	<portlet:param name="mvcPath" value="/edit_entry.jsp" />
</portlet:actionURL>

<aui:form action="<%= updateSACPEntryURL %>">
	<aui:input name="redirect" type="hidden" value="<%= redirect %>" />
	<aui:input name="sacpEntryId" type="hidden" value="<%= sacpEntryId %>" />

	<liferay-ui:error exception="<%= DuplicateSACPEntryNameException.class %>" message="please-enter-a-unique-service-access-control-profile-name" />
	<liferay-ui:error exception="<%= SACPEntryNameException.class %>" message="service-access-control-profile-name-is-required" />
	<liferay-ui:error exception="<%= SACPEntryTitleException.class %>" message="service-access-control-profile-title-is-required" />

	<aui:model-context bean="<%= sacpEntry %>" model="<%= SACPEntry.class %>" />

	<aui:input disabled="<%= defaultSACPEntry %>" name="name" required="<%= true %>">
		<aui:validator errorMessage="this-field-is-required-and-must-contain-only-following-characters" name="custom">
			function(val, fieldNode, ruleValue) {
				var allowedCharacters = '<%= HtmlUtil.escapeJS(SACPEntryConstants.NAME_ALLOWED_CHARACTERS) %>';

				val = val.trim();

				var regex = new RegExp('[^' + allowedCharacters + ']');

				return !regex.test(val);
			}
		</aui:validator>
	</aui:input>

	<aui:input name="title" required="<%= true %>" />

	<portlet:resourceURL var="getServicesURL">
		<portlet:param name="<%= ActionRequest.ACTION_NAME %>" value="getServices" />
		<portlet:param name="mvcPath" value="<%= StringPool.SPACE %>" />
	</portlet:resourceURL>
	<portlet:resourceURL var="getMethodsURL">
		<portlet:param name="<%= ActionRequest.ACTION_NAME %>" value="getMethods" />
		<portlet:param name="mvcPath" value="<%= StringPool.SPACE %>" />
	</portlet:resourceURL>

	<div id="<portlet:namespace />allowedServiceSignatures">

		<%
		for (int i = 0; i < allowedServiceSignaturesIndexes.length; i++) {
			int allowedServiceIndex = allowedServiceSignaturesIndexes[i];

			String packageName = ParamUtil.getString(request, "packageName" + allowedServiceIndex, "0");
			String methodName = ParamUtil.getString(request, "methodName" + allowedServiceIndex, "All methods");
		%>

			<div class="lfr-form-row">
				<div class="row-fields">
					<aui:col md="6">
						<aui:select id='<%= "serviceClass" + allowedServiceIndex %>' name="serviceClass"></aui:select>
					</aui:col>
					<aui:col md="6">
						<aui:select id='<%= "methodName" + allowedServiceIndex %>' name="methodName"></aui:select>
					</aui:col>
				</div>
			</div>

			<aui:script use="liferay-dynamic-select,io-base">
				function getJSON(URL, callback, data) {
					A.io(
						URL,
						{
							arguments: {
								callback: callback
							},
							data: data,
							on: {
								success: <portlet:namespace />processRequest
							}
						}
					);
				}

				function getMethods(callback, key) {
					var dataKey = key.split('/');

					getJSON(
						'<%= getMethodsURL %>',
						callback,
						{
							<portlet:namespace />context: dataKey[0],
							<portlet:namespace />serviceClass: dataKey[1]
						}
					);
				}

				function getServices(callback) {
					getJSON('<%= getServicesURL %>', callback);
				}

				new Liferay.DynamicSelect(
					[
						{
							select: '<portlet:namespace />serviceClass<%= allowedServiceIndex %>',
							selectData: getServices,
							selectDesc: 'serviceClass',
							selectId: 'serviceClass',
							selectSort: '<%= true %>',
							selectVal: '<%= packageName %>'
						},
						{
							select: '<portlet:namespace />methodName<%= allowedServiceIndex %>',
							selectData: getMethods,
							selectDesc: 'method',
							selectId: 'method',
							selectVal: '<%= methodName %>'
						}
					]
				);
			</aui:script>

		<%
		}
		%>

		<aui:input name="allowedServiceSignaturesIndexes" type="hidden" value="<%= StringUtil.merge(allowedServiceSignaturesIndexes) %>" />
	</div>

	<aui:script use="liferay-auto-fields,liferay-dynamic-select,io-base">
		function getJSON(URL, callback, data) {
			A.io(
				URL,
				{
					arguments: {
						callback: callback
					},
					data: data,
					on: {
						success: <portlet:namespace />processRequest
					}
				}
			);
		}

		function getMethods(callback, key) {
			var dataKey = key.split('/');

			getJSON(
				'<%= getMethodsURL %>',
				callback,
				{
					<portlet:namespace />context: dataKey[0],
					<portlet:namespace />serviceClass: dataKey[1]
				}
			);
		}

		function getServices(callback) {
			getJSON('<%= getServicesURL %>', callback);
		}

		var allowedServiceSignatures = new Liferay.AutoFields(
			{
				contentBox: '#<portlet:namespace />allowedServiceSignatures',
				fieldIndexes: '<portlet:namespace />allowedServiceSignaturesIndexes',
				namespace: '<portlet:namespace />',
				on: {
					'clone': function(event) {
						var guid = event.guid;
						var row = event.row;

						var dynamicSelects = row.one('select[data-componentType=dynamic_select]');

						if (dynamicSelects) {
							dynamicSelects.detach('change');
						}

						var dynamicSelect = new Liferay.DynamicSelect(
							[
								{
									select: '<portlet:namespace />serviceClass' + guid,
									selectData: getServices,
									selectDesc: 'serviceClass',
									selectId: 'serviceClass',
									selectSort: '<%= true %>',
									selectVal: '0'
								},
								{
									select: '<portlet:namespace />methodName' + guid,
									selectData: getMethods,
									selectDesc: 'method',
									selectId: 'method',
									selectVal: 'All methods'
								}
							]
						);

						dynamicSelect._updateSelect(1, []);
					}
				}
			}
		).render();
	</aui:script>

	<aui:script>
		function <portlet:namespace />processRequest(index, data, args) {
			if (data.response) {
				var response = JSON.parse(data.response);

				args.callback(response);
			}
		}
	</aui:script>

	<aui:button-row>
		<aui:button type="submit" value="save" />
	</aui:button-row>
</aui:form>