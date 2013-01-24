package org.cmdbuild.services.store;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.common.annotations.OldDao;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.model.dashboard.DashboardDefinition;
import org.cmdbuild.model.dashboard.DashboardObjectMapper;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;
import org.codehaus.jackson.map.ObjectMapper;

public class DBDashboardStore implements DashboardStore {

	private static final String DASHBOARD_TABLE = "_Dashboards";
	private static final String DEFINITION_ATTRIBUTE = "Definition";
	private static final ITable dashboardsTable = UserOperations.from(UserContext.systemContext()).tables()
			.get(DASHBOARD_TABLE);
	private static final ObjectMapper mapper = new DashboardObjectMapper();
	private static final ErrorMessageBuilder errors = new ErrorMessageBuilder();

	@OldDao
	@Override
	public Long add(final DashboardDefinition dashboard) {
		final String serializedDefinition = serializeDashboard(dashboard);
		final ICard c = dashboardsTable.cards().create();
		c.setValue(DEFINITION_ATTRIBUTE, serializedDefinition);
		c.save();
		return Long.valueOf(c.getId());
	}

	@OldDao
	@Override
	public DashboardDefinition get(final Long dashboardId) {
		final ICard c = dashboardsTable.cards().get(dashboardId.intValue());
		return cardToDashboardDefinition(c);
	}

	@OldDao
	@Override
	public Map<Long, DashboardDefinition> list() {
		final Map<Long, DashboardDefinition> out = new HashMap<Long, DashboardDefinition>();
		for (final ICard c : dashboardsTable.cards().list()) {
			out.put(Long.valueOf(c.getId()), cardToDashboardDefinition(c));
		}
		return out;
	}

	@OldDao
	@Override
	public void modify(final Long dashboardId, final DashboardDefinition dashboard) {
		final String serializedDefinition = serializeDashboard(dashboard);
		final ICard c = dashboardsTable.cards().get(dashboardId.intValue());
		c.setValue(DEFINITION_ATTRIBUTE, serializedDefinition);
		c.save();
	}

	@OldDao
	@Override
	public void remove(final Long dashboardId) {
		dashboardsTable.cards().get(dashboardId.intValue()).delete();
	}

	private DashboardDefinition cardToDashboardDefinition(final ICard c) {
		try {
			final String serializedDefinition = c.getAttributeValue(DEFINITION_ATTRIBUTE).getString();
			return mapper.readValue(serializedDefinition, DashboardDefinition.class);
		} catch (final Exception e) {
			throw new IllegalArgumentException(errors.decodingError());
		}
	}

	private String serializeDashboard(final DashboardDefinition dashboard) throws IllegalArgumentException {
		try {
			final String serializedDefinition = mapper.writeValueAsString(dashboard);
			return serializedDefinition;
		} catch (final Exception e) {
			throw new IllegalArgumentException(errors.encodingError());
		}
	}

	/*
	 * to avoid an useless errors hierarchy define this object that build the
	 * errors messages These are used also in the tests to ensure that a right
	 * message is provided by the exception
	 */
	public static class ErrorMessageBuilder {

		public String decodingError() {
			return "There ware some problems while trying to decode the dashboard";
		}

		public String encodingError() {
			return "There ware some problems while trying to encode the dashboard";
		}
	}
}