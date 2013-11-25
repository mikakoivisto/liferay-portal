package com.liferay.portal.kernel.repository.cmis.search;

import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.util.StringBundler;

class ContainsNodesCollapser {

	public CMISCriterion collapse(CMISCriterion node) throws SearchException {

		if (node instanceof CMISJunction) {
			CMISJunction junction = (CMISJunction) node;

			CMISJunction collapsed = _collapseChildren(junction);

			if (_areAllChildrenContainsNodes(collapsed)) {

				String connector = _connectorFor(junction);
				String content = _collapseContainsChildrenContent(
					connector, collapsed);
				return new CMISContainsExpression(content);
			}
			else {
				return collapsed;
			}
		}
		else {
			return node;
		}
	}

	private boolean _areAllChildrenContainsNodes(CMISJunction collapsed)
		throws SearchException {

		for (CMISCriterion child : collapsed.list()) {
			if (!(child instanceof CMISContainsExpression)) {
				return false;
			}
		}

		return collapsed.list().size()>0;
	}

	private CMISJunction _collapseChildren(CMISJunction junction)
		throws SearchException {

		CMISJunction collapsed = _createJunctionOfSameType(junction);

		for (CMISCriterion child : junction.list()) {
			if (child instanceof CMISJunction) {
				collapsed.add( collapse(child) );
			}
			else {
				collapsed.add( child );
			}
		}

		return collapsed;
	}

	private String _collapseContainsChildrenContent(
		String connector, CMISJunction node) throws SearchException {

		StringBundler sb = new StringBundler(1+2*node.list().size());

		sb.append("(");
		for (CMISCriterion child : node.list()) {
			CMISContainsExpression expr = (CMISContainsExpression) child;
			if (sb.length()>1) {
				sb.append(connector);
			}
			sb.append(expr.getContent());
		}
		sb.append(")");

		return sb.toString();
	}

	private String _connectorFor(CMISJunction junction) throws SearchException {

		if (junction instanceof CMISConjunction) {
			return " ";
		}
		else if (junction instanceof CMISDisjunction) {
			return " OR ";
		}
		else {
			throw new SearchException(
				"Unsupported junction type: "+junction.getClass().getName());
		}
	}

	private CMISJunction _createJunctionOfSameType(CMISJunction junction) {

		if (junction instanceof CMISConjunction) {
			return new CMISConjunction();
		}
		else if (junction instanceof CMISDisjunction) {
			return new CMISDisjunction();
		}
		else {
			throw new IllegalArgumentException(
				"Argument must be of type CMISJunction");
		}
	}

}
