package com.liferay.portal.kernel.repository.cmis.search;

import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;

import java.util.List;

/**
 * @author Ivan Zaera
 */
public class CMISFullTextConjunction extends CMISJunction {

    @Override
    public String toQueryFragment() {
        if (isEmpty()) {
            return StringPool.BLANK;
        }

        List<CMISCriterion> cmisCriterions = list();

        StringBundler sb = new StringBundler(cmisCriterions.size() * 2 + 1);

        if (cmisCriterions.size() > 1) {
            sb.append(StringPool.OPEN_PARENTHESIS);
        }

        for (int i = 0; i < cmisCriterions.size(); i++) {
            CMISCriterion cmisCriterion = cmisCriterions.get(i);

            if (i != 0) {
                sb.append(" ");
            }

            sb.append(cmisCriterion.toQueryFragment());
        }

        if (cmisCriterions.size() > 1) {
            sb.append(StringPool.CLOSE_PARENTHESIS);
        }

        return sb.toString();
    }

}
