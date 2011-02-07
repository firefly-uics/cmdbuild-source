package org.cmdbuild.portlet.plugin;

import java.util.List;

import org.cmdbuild.portlet.configuration.GridConfiguration;
import org.cmdbuild.portlet.layout.ComponentLayout;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.Lookup;
import org.cmdbuild.services.soap.Order;
import org.cmdbuild.services.soap.Query;
import org.cmdbuild.services.soap.Reference;

public abstract class CMPortletPlugin {

	public List<Reference> getReferenceList(final ComponentLayout layout, final Query query, final List<Order> order,
			final int limit, final int offset) {
		return null;
	}

	public int customGridButtonsLength() {
		return 0;
	}

	public String serializeGridButtons(final String type, final Card card, final List<Lookup> processLookup,
			final GridConfiguration gridConfig, final String contextPath) {
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
