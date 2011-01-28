package org.cmdbuild.portlet.plugin;

import java.util.ArrayList;
import java.util.List;
import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.configuration.PortletConfiguration;

public class CMPortletPluginLibrary extends CMPortletPlugin {

    static List<CMPortletPlugin> plugins;

    static {
        plugins = new ArrayList<CMPortletPlugin>();
        for (String pluginClassName : PortletConfiguration.getInstance().getPlugins()) {
            try {
                Class<CMPortletPlugin> pluginClass = (Class<CMPortletPlugin>) Class.forName("org.cmdbuild.portlet.plugin." + pluginClassName);
                plugins.add(pluginClass.getConstructor().newInstance());
            } catch (Exception ex) {
                Log.PORTLET.error("Could not load "+pluginClassName+" plugin");
            }
        }
    }

    public static Iterable<CMPortletPlugin> getPlugins() {
        return plugins;
    }

}
