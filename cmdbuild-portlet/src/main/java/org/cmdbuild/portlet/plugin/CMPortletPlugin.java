package org.cmdbuild.portlet.plugin;

import java.util.List;
import org.cmdbuild.portlet.configuration.GridConfiguration;
import org.cmdbuild.portlet.layout.ComponentLayout;
import org.cmdbuild.services.soap.*;

public abstract class CMPortletPlugin {

    public List<Reference> getReferenceList(ComponentLayout layout, Query query, List<Order> order, int limit, int offset) {
        return null;
    }

    public int customGridButtonsLength() {
        return 0;
    }

    public String serializeGridButtons(String type, Card card, List<Lookup> processLookup, GridConfiguration gridConfig, String contextPath) {
        return null;
    }

    public String getCustomTreeHtml() {
        return null;
    }

    public String[] getCustomSectionHtml() {
        return null;
    }

    public String[] getCustomjs() {
        return null;
    }

    public String[] getCustomcss() {
        return null;
    }
}
