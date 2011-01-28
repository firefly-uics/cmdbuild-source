package org.cmdbuild.portlet.utils;

import org.cmdbuild.portlet.plugin.CMPortletPlugin;
import org.cmdbuild.portlet.plugin.CMPortletPluginLibrary;

public class JSPPageUtils {

    public String getCustomTreeNodeHtml() {
        String customTreeNode = "";
        for (CMPortletPlugin plugin : CMPortletPluginLibrary.getPlugins()) {
            String node = plugin.getCustomTreeHtml();
            if (node != null) {
                customTreeNode = node;
            }
        }
        return customTreeNode;
    }

    public String[] getCustomSectionHtml() {
        for (CMPortletPlugin plugin : CMPortletPluginLibrary.getPlugins()) {
            String[] section = plugin.getCustomSectionHtml();
            if (section != null) {
                return section;
            }
        }
        return null;
    }
}
