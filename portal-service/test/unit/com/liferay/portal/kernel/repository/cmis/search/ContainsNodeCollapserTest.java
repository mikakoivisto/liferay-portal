package com.liferay.portal.kernel.repository.cmis.search;

import com.liferay.portal.kernel.search.SearchException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ContainsNodeCollapserTest {

	@Test
	public void testSingleContainsNode() throws SearchException {
		CMISContainsExpression node = new CMISContainsExpression("value");

		CMISContainsExpression collapsed =
			(CMISContainsExpression) _collapser.collapse(node);

		assertEquals("value", collapsed.getContent());
	}

	@Test
	public void testSingleNonContainsNode() throws SearchException {
		CMISSimpleExpression node = new CMISSimpleExpression(
			"field", "value", CMISSimpleExpressionOperator.EQ);

		CMISSimpleExpression collapsed =
			(CMISSimpleExpression) _collapser.collapse(node);

		_assertSimpleExpressionEquals("field", CMISSimpleExpressionOperator
			.EQ, "value", collapsed);
	}

	@Test
	public void testTreeWithContainsNodesOnly() throws SearchException {
		CMISDisjunction or = new CMISDisjunction();
		or.add(new CMISContainsExpression("value1"));
		or.add(new CMISContainsExpression("value2"));

		CMISConjunction node = new CMISConjunction();
		node.add(new CMISContainsExpression("value0"));
		node.add(or);

		CMISContainsExpression collapsed =
			(CMISContainsExpression) _collapser.collapse(node);

		assertEquals("(value0 (value1 OR value2))", collapsed.getContent());
	}

	@Test
	public void testTreeWithMixedContent() throws SearchException {
		CMISDisjunction or = new CMISDisjunction();
		or.add(new CMISContainsExpression("value1"));
		or.add(new CMISContainsExpression("value2"));

		CMISConjunction and = new CMISConjunction();
		and.add(new CMISContainsExpression("value0"));
		and.add(or);

		CMISConjunction node = new CMISConjunction();
		node.add(new CMISSimpleExpression(
			"field","value",CMISSimpleExpressionOperator.EQ));
		node.add(and);

		CMISConjunction collapsed = (CMISConjunction) _collapser.collapse(node);

		_assertSimpleExpressionEquals(
			"field", CMISSimpleExpressionOperator.EQ, "value",
			(CMISSimpleExpression)collapsed.list().get(0));
		assertEquals("(value0 (value1 OR value2))", ((CMISContainsExpression)
			collapsed.list().get(1)).getContent());
	}

	@Test
	public void testTwoAndedContainsNodes() throws SearchException {
		CMISConjunction node = new CMISConjunction();
		node.add(new CMISContainsExpression("value1"));
		node.add(new CMISContainsExpression("value2"));

		CMISContainsExpression collapsed =
			(CMISContainsExpression) _collapser.collapse(node);

		assertEquals("(value1 value2)", collapsed.getContent());
	}

	@Test
	public void testTwoNonContainsNodes() throws SearchException {
		CMISConjunction node = new CMISConjunction();
		node.add(new CMISSimpleExpression(
			"field", "value", CMISSimpleExpressionOperator.EQ));
		node.add(new CMISSimpleExpression(
			"field", "null", CMISSimpleExpressionOperator.NE));

		CMISConjunction collapsed = (CMISConjunction) _collapser.collapse(node);

		assertEquals(2, collapsed.list().size());
		_assertSimpleExpressionEquals(
			"field", CMISSimpleExpressionOperator.EQ, "value",
			(CMISSimpleExpression)collapsed.list().get(0));
		_assertSimpleExpressionEquals("field", CMISSimpleExpressionOperator
			.NE, "null", (CMISSimpleExpression) collapsed.list().get(1));
	}

	@Test
	public void testTwoOredContainsNodes() throws SearchException {
		CMISDisjunction node = new CMISDisjunction();
		node.add(new CMISContainsExpression("value1"));
		node.add(new CMISContainsExpression("value2"));

		CMISContainsExpression collapsed =
			(CMISContainsExpression) _collapser.collapse(node);

		assertEquals("(value1 OR value2)", collapsed.getContent());
	}

	private void _assertSimpleExpressionEquals(String field,
		CMISSimpleExpressionOperator operator, String value,
		CMISSimpleExpression collapsed) {

		assertEquals(field, collapsed.getField());
		assertEquals(operator, collapsed.getOperator());
		assertEquals(value, collapsed.getValue());
	}

	private final ContainsNodesCollapser _collapser =
		new ContainsNodesCollapser();

}
