package com.liferay.portal.kernel.repository.cmis.search;

import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.util.StringBundler;

/**
 * Helper class to post-process a CMIS query tree and collapse all CONTAINS
 * nodes into one (to comply with CMIS specification).
 *
 * WARNING: This class can only collapse subtrees composed of only AND, OR, and
 * CONTAINS nodes into just one single CONTAINS node. If you have more than one
 * subtree of collapsible CONTAINS nodes the processed query will end up having
 * more than one CONTAINS clause and will make CMIS fail.
 *
 * This is because if you consider a query like:
 *
 *  ( CONTAINS(X) OR field=='J' ) AND CONTAINS(Y)
 *
 * There's no way to collapse both CONTAINS criteria into one single CONTAINS
 * which, when combined with "field=='J'", makes up a query equivalent to the
 * one given.
 *
 * @author Ivan Zaera
 */
class ContainsNodesCollapser {

	/**
	 * Collapse all subtrees made of AND, OR, and CONTAINS nodes into single
	 * CONTAINS nodes. See the explanation of the class for more info on the
	 * algorithm.
	 * @param node the tree to post-process
	 * @return a copy of the tree with collapsed CONTAINS nodes
	 * @throws SearchException if query tree is not supported
	 */
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

	/**
	 * Tests whether all children of a node are of type CONTAINS.
	 */
	private boolean _areAllChildrenContainsNodes(CMISJunction collapsed)
		throws SearchException {

		for (CMISCriterion child : collapsed.list()) {
			if (!(child instanceof CMISContainsExpression)) {
				return false;
			}
		}

		return collapsed.list().size()>0;
	}

	/**
	 * Collapses all CONTAINS children of the given node if possible.
	 */
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

	/**
	 * Merges all contents of the children CONTAINS nodes into one single
	 * content string.
	 */
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

	/**
	 * Returns the CONTAINS logical connector for a given {@link CMISJunction}.
	 */
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

	/**
	 * Creates a junction of the same type as the given one.
	 */
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
