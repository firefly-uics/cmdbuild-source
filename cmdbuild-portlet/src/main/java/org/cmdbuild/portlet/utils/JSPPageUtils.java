package org.cmdbuild.portlet.utils;

import org.cmdbuild.portlet.plugin.CMPortletPlugin;
import org.cmdbuild.portlet.plugin.CMPortletPluginLibrary;

public class JSPPageUtils {

	public String getCustomTreeNodeHtml() {
		String customTreeNode = "";
		for (final CMPortletPlugin plugin : CMPortletPluginLibrary.getPlugins()) {
			final String node = plugin.getCustomTreeHtml();
			if (node != null) {
				customTreeNode = node;
			}
		}
		return customTreeNode;
	}

	public String[] getCustomSectionHtml() {
		for (final CMPortletPlugin plugin : CMPortletPluginLibrary.getPlugins()) {
			final String[] section = plugin.getCustomSectionHtml();
			if (section != null) {
				return section;
			}
		}
		return null;
	}
}
