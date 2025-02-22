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

package com.liferay.source.formatter;

import com.liferay.portal.kernel.io.unsync.UnsyncBufferedReader;
import com.liferay.portal.kernel.io.unsync.UnsyncStringReader;
import com.liferay.portal.kernel.util.CharPool;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.tools.ImportsFormatter;
import com.liferay.portal.tools.ToolsUtil;
import com.liferay.source.formatter.checks.FTLIfStatementCheck;
import com.liferay.source.formatter.checks.FileCheck;

import java.io.File;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Hugo Huijser
 */
public class FTLSourceProcessor extends BaseSourceProcessor {

	@Override
	protected String doFormat(
			File file, String fileName, String absolutePath, String content)
		throws Exception {

		content = StringUtil.replace(content, " >\n", ">\n");

		content = sortLiferayVariables(content);

		Matcher matcher = _singleParameterTagPattern.matcher(content);

		while (matcher.find()) {
			String match = matcher.group();

			String replacement = match;

			String group1 = matcher.group(1);
			String group2 = matcher.group(2);

			if (group2 != null) {
				replacement = StringUtil.replaceFirst(
					replacement, group1 + StringPool.SPACE, group1);
			}

			String group3 = matcher.group(3);

			if (group3.startsWith(StringPool.SPACE)) {
				replacement = StringUtil.replaceLast(
					replacement, group3, group3.substring(1));
			}

			content = StringUtil.replace(content, match, replacement);
		}

		matcher = _multiParameterTagPattern.matcher(content);

		while (matcher.find()) {
			String match = matcher.group();

			if (match.contains("><")) {
				continue;
			}

			String strippedMatch = stripQuotes(match, CharPool.QUOTE);

			if (StringUtil.count(strippedMatch, CharPool.EQUAL) <= 1) {
				continue;
			}

			String replacement = match;

			String tabs = matcher.group(1);

			int x = -1;

			while (true) {
				x = replacement.indexOf(
					StringPool.EQUAL, x + tabs.length() + 2);

				if (x == -1) {
					break;
				}

				if (ToolsUtil.isInsideQuotes(replacement, x)) {
					continue;
				}

				int y = replacement.lastIndexOf(StringPool.SPACE, x);

				if (y == -1) {
					break;
				}

				replacement =
					replacement.substring(0, y) + StringPool.NEW_LINE + tabs +
						StringPool.TAB + replacement.substring(y + 1);
			}

			if (!match.equals(replacement)) {
				replacement = StringUtil.replaceLast(
					replacement, "/>", StringPool.NEW_LINE + tabs + "/>");

				content = StringUtil.replace(content, match, replacement);
			}
		}

		content = formatStringRelationalOperations(content);

		content = formatAssignTags(content);

		ImportsFormatter importsFormatter = new FTLImportsFormatter();

		content = importsFormatter.format(content, null, null);

		content = fixEmptyLinesInMultiLineTags(content);

		content = fixEmptyLinesInNestedTags(content);

		content = fixEmptyLinesBetweenTags(content);

		content = fixMissingEmptyLinesAroundComments(content);

		content = formatFTL(fileName, content);

		return StringUtil.replace(content, "\n\n\n", "\n\n");
	}

	@Override
	protected List<String> doGetFileNames() throws Exception {
		String[] excludes = new String[] {
			"**/journal/dependencies/template.ftl",
			"**/service/builder/dependencies/props.ftl"
		};

		return getFileNames(excludes, getIncludes());
	}

	@Override
	protected String[] doGetIncludes() {
		return _INCLUDES;
	}

	protected String fixMissingEmptyLinesAroundComments(String content) {
		Matcher matcher = _missingEmptyLineAfterCommentPattern.matcher(content);

		if (matcher.find()) {
			return StringUtil.replaceFirst(
				content, "\n", "\n\n", matcher.start());
		}

		matcher = _missingEmptyLineBeforeCommentPattern.matcher(content);

		if (matcher.find()) {
			return StringUtil.replaceFirst(
				content, "\n", "\n\n", matcher.start());
		}

		return content;
	}

	protected String formatAssignTags(String content) {
		Matcher matcher = _incorrectAssignTagPattern.matcher(content);

		content = matcher.replaceAll("$1 />\n");

		matcher = _assignTagsBlockPattern.matcher(content);

		while (matcher.find()) {
			String match = matcher.group();

			String tabs = matcher.group(2);

			String replacement = StringUtil.removeSubstrings(
				match, "<#assign ", "<#assign\n", " />", "\n/>", "\t/>");

			replacement = StringUtil.removeChar(replacement, CharPool.TAB);

			String[] lines = StringUtil.splitLines(replacement);

			StringBundler sb = new StringBundler((3 * lines.length) + 5);

			sb.append(tabs);
			sb.append("<#assign");

			for (String line : lines) {
				sb.append("\n\t");
				sb.append(tabs);
				sb.append(line);
			}

			sb.append(StringPool.NEW_LINE);
			sb.append(tabs);
			sb.append("/>\n\n");

			content = StringUtil.replace(content, match, sb.toString());
		}

		return content;
	}

	protected String formatFTL(String fileName, String content)
		throws Exception {

		StringBundler sb = new StringBundler();

		try (UnsyncBufferedReader unsyncBufferedReader =
				new UnsyncBufferedReader(new UnsyncStringReader(content))) {

			String line = null;

			while ((line = unsyncBufferedReader.readLine()) != null) {
				line = trimLine(line, false);

				String trimmedLine = StringUtil.trimLeading(line);

				if (trimmedLine.startsWith("<#assign ")) {
					line = formatWhitespace(line, trimmedLine, true);

					line = formatIncorrectSyntax(line, "=[", "= [", false);
					line = formatIncorrectSyntax(line, "+[", "+ [", false);
				}

				sb.append(line);
				sb.append("\n");
			}
		}

		String newContent = sb.toString();

		if (newContent.endsWith("\n")) {
			newContent = newContent.substring(0, newContent.length() - 1);
		}

		return newContent;
	}

	protected String formatStringRelationalOperations(String content) {
		Matcher matcher = _stringRelationalOperationPattern.matcher(content);

		if (!matcher.find()) {
			return content;
		}

		String match = matcher.group();

		String firstChar = matcher.group(1);
		String lastChar = matcher.group(5);

		if (!firstChar.equals(StringPool.OPEN_PARENTHESIS) ||
			!lastChar.equals(StringPool.CLOSE_PARENTHESIS)) {

			match = content.substring(matcher.end(1), matcher.start(5));
		}

		String operator = matcher.group(3);
		String quotedString = matcher.group(4);
		String variableName = matcher.group(2);

		String replacement = null;

		if (Validator.isNull(quotedString)) {
			if (operator.equals("==")) {
				replacement = "validator.isNull(" + variableName + ")";
			}
			else {
				replacement = "validator.isNotNull(" + variableName + ")";
			}
		}
		else {
			StringBundler sb = new StringBundler();

			if (operator.equals("!=")) {
				sb.append(StringPool.EXCLAMATION);
			}

			sb.append("stringUtil.equals(");
			sb.append(variableName);
			sb.append(", \"");
			sb.append(quotedString);
			sb.append("\")");

			replacement = sb.toString();
		}

		return StringUtil.replaceFirst(
			content, match, replacement, matcher.start());
	}

	@Override
	protected List<FileCheck> getFileChecks() {
		return Arrays.asList(new FileCheck[] {new FTLIfStatementCheck()});
	}

	protected String sortLiferayVariables(String content) {
		Matcher matcher = _liferayVariablesPattern.matcher(content);

		while (matcher.find()) {
			String match = matcher.group();

			Matcher matcher2 = _liferayVariablePattern.matcher(match);

			String previousVariable = null;

			while (matcher2.find()) {
				String variable = matcher2.group();

				if (Validator.isNotNull(previousVariable) &&
					(previousVariable.compareTo(variable) > 0)) {

					String replacement = StringUtil.replaceFirst(
						match, previousVariable, variable);
					replacement = StringUtil.replaceLast(
						replacement, variable, previousVariable);

					return StringUtil.replace(content, match, replacement);
				}

				previousVariable = variable;
			}
		}

		return content;
	}

	private static final String[] _INCLUDES = new String[] {"**/*.ftl"};

	private final Pattern _assignTagsBlockPattern = Pattern.compile(
		"((\t*)<#assign[^<#/>]*=[^<#/>]*/>(\n|$)+){2,}", Pattern.MULTILINE);
	private final Pattern _incorrectAssignTagPattern = Pattern.compile(
		"(<#assign .*=.*[^/])>(\n|$)");
	private final Pattern _liferayVariablePattern = Pattern.compile(
		"^\t*<#assign liferay_.*>\n", Pattern.MULTILINE);
	private final Pattern _liferayVariablesPattern = Pattern.compile(
		"(^\t*<#assign liferay_.*>\n)+", Pattern.MULTILINE);
	private final Pattern _missingEmptyLineAfterCommentPattern =
		Pattern.compile("-->\n[^\n]");
	private final Pattern _missingEmptyLineBeforeCommentPattern =
		Pattern.compile("[^\n]\n\t*<#--");
	private final Pattern _multiParameterTagPattern = Pattern.compile(
		"\n(\t*)<@.+=.+=.+/>");
	private final Pattern _singleParameterTagPattern = Pattern.compile(
		"(<@[\\w\\.]+ \\w+)( )?=([^=]+?)/>");
	private final Pattern _stringRelationalOperationPattern = Pattern.compile(
		"(\\W)([\\w.]+) ([!=]=) \"(\\w*)\"(.)");

}