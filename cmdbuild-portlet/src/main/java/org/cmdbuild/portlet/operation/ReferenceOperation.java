package org.cmdbuild.portlet.operation;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.layout.ComponentLayout;
import org.cmdbuild.portlet.plugin.CMPortletPlugin;
import org.cmdbuild.portlet.plugin.CMPortletPluginLibrary;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.Order;
import org.cmdbuild.services.soap.Query;
import org.cmdbuild.services.soap.Reference;

public class ReferenceOperation extends WSOperation {

	public ReferenceOperation(final SOAPClient client) {
		super(client);
	}

	public List<Reference> getReferenceList(final ComponentLayout layout, final int limit, final int offset) {
		return getReferenceList(layout, null, null, limit, offset);
	}

	public List<Reference> getReferenceList(final ComponentLayout layout, final Query query, final List<Order> order,
			final int limit, final int offset) {
		for (final CMPortletPlugin plugin : CMPortletPluginLibrary.getPlugins()) {
			final List<Reference> r = plugin.getReferenceList(layout, query, order, limit, offset);
			if (r != null) {
				return r;
			}
		}
		return getReferenceListDefault(layout, query, order, limit, offset);
	}

	private List<Reference> getReferenceListDefault(final ComponentLayout layout, final Query query, List<Order> order,
			final int limit, final int offset) {
		Log.PORTLET.debug("Getting reference for class " + layout.getSchema().getReferencedClassName());
		if (order == null) {
			order = defaultOrder();
		}
		return getService().getReference(layout.getSchema().getReferencedClassName(), query, null, limit, offset, null);
	}

	private List<Order> defaultOrder() {
		final Order orderElement = new Order();
		orderElement.setColumnName("Description");
		orderElement.setType("ASC");
		final List<Order> order = new ArrayList<Order>();
		order.add(orderElement);
		return order;
	}
}
