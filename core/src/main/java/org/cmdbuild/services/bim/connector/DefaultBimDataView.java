package org.cmdbuild.services.bim.connector;

import static org.cmdbuild.bim.utils.BimConstants.GLOBALID_ATTRIBUTE;
import static org.cmdbuild.bim.utils.BimConstants.X_ATTRIBUTE_NAME;
import static org.cmdbuild.bim.utils.BimConstants.Y_ATTRIBUTE_NAME;
import static org.cmdbuild.bim.utils.BimConstants.Z_ATTRIBUTE_NAME;
import static org.cmdbuild.common.Constants.CLASS_ID_ATTRIBUTE;
import static org.cmdbuild.common.Constants.ID_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.FunctionCall.call;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.alias.NameAlias;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.services.bim.BimDataView;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DefaultBimDataView implements BimDataView {

	public static final String X_COORD = "x";
	public static final String Y_COORD = "y";
	public static final String Z_COORD = "z";

	public static final String ID = Constants.ID_ATTRIBUTE;
	public static final String CODE = Constants.CODE_ATTRIBUTE;
	public static final String DESCRIPTION = Constants.DESCRIPTION_ATTRIBUTE;
	private static final String CARDDATA_FROM_GUID_FUNCTION = "_bim_carddata_from_globalid";
	private static final String CARDDATA_FOR_EXPORT_FUNCTION = "_bim_data_for_export";
	private static final String FIND_BUILDING_FUNCTION = "_opm_find_the_building";
	private static final String GENERATE_COORDINATES_FUNCTION = "_bim_generate_coordinates";
	private static final String STORE_BIMDATA_FUNCTION = "_bim_store_data";

	private static final String CLASSNAME = "ClassName";

	private final CMDataView dataView;

	public DefaultBimDataView(CMDataView dataView, JdbcTemplate jdbcTemplate) {
		this.dataView = dataView;
	}

	public List<CMCard> getCardsWithAttributeAndValue(CMIdentifier classIdentifier, Object attributeValue,
			String attributeName) {
		CMClass theClass = dataView.findClass(classIdentifier);
		CMQueryResult result = dataView.select(anyAttribute(theClass)) //
				.from(theClass) //
				.where(condition(attribute(theClass, attributeName), eq(attributeValue))) //
				.run();
		List<CMCard> cards = Lists.newArrayList();
		for (Iterator<CMQueryRow> it = result.iterator(); it.hasNext();) {
			CMQueryRow row = it.next();
			cards.add(row.getCard(theClass));
		}
		return cards;
	}

	@Override
	public Map<String, String> fetchBimDataOfCard(CMCard card, String className, String containerId,
			String containerClassName) {

		CMFunction function = dataView.findFunctionByName(CARDDATA_FOR_EXPORT_FUNCTION);
		NameAlias f = NameAlias.as("f");
		CMQueryResult queryResult = dataView.select(anyAttribute(function, f))
				.from(call(function, card.getId(), className), f).run();

		if (queryResult.isEmpty()) {
			System.out.println("No bim data found for card " + card.getId());
		}

		CMQueryRow row = queryResult.getOnlyRow();
		String code = String.class.cast(row.getValueSet(f).get("code"));
		String description = String.class.cast(row.getValueSet(f).get("description"));
		String globalId = String.class.cast(row.getValueSet(f).get("globalid"));
		String xCoord = String.class.cast(row.getValueSet(f).get(X_ATTRIBUTE_NAME));
		String yCoord = String.class.cast(row.getValueSet(f).get(Y_ATTRIBUTE_NAME));
		String zCoord = String.class.cast(row.getValueSet(f).get(Z_ATTRIBUTE_NAME));

		double x = xCoord != null && !xCoord.isEmpty() ? Double.parseDouble(xCoord) : 0;
		double y = yCoord != null && !yCoord.isEmpty() ? Double.parseDouble(yCoord) : 0;
		double z = zCoord != null && !zCoord.isEmpty() ? Double.parseDouble(zCoord) : 0;

		if (globalId == null || globalId.isEmpty()) {
			globalId = RandomStringUtils.randomAlphanumeric(22);
		}
		if (x == 0 && y == 0 && z == 0) {
			function = dataView.findFunctionByName(GENERATE_COORDINATES_FUNCTION);
			f = NameAlias.as("f");
			queryResult = dataView.select(anyAttribute(function,f))
					.from(call(function, containerId, containerClassName), f).run();
			if (queryResult.isEmpty()) {
				System.out.println("No coordinates generated for card " + card.getId());
			}
			CMQueryRow rowCoordinates = queryResult.getOnlyRow();

			xCoord = String.class.cast(rowCoordinates.getValueSet(f).get(X_ATTRIBUTE_NAME));
			yCoord = String.class.cast(rowCoordinates.getValueSet(f).get(Y_ATTRIBUTE_NAME));
			zCoord = String.class.cast(rowCoordinates.getValueSet(f).get(Z_ATTRIBUTE_NAME));

			function = dataView.findFunctionByName(STORE_BIMDATA_FUNCTION);
			f = NameAlias.as("f");
			queryResult = dataView
					.select(anyAttribute(function, f))
					.from(call(function, card.getId(), className, globalId, xCoord, yCoord, zCoord),
							f).run();
		}

		Map<String, String> bimData = Maps.newHashMap();
		bimData.put(CODE, code);
		bimData.put(DESCRIPTION, description);
		bimData.put(GLOBALID_ATTRIBUTE, globalId);
		bimData.put(X_ATTRIBUTE_NAME, xCoord);
		bimData.put(Y_ATTRIBUTE_NAME, yCoord);
		bimData.put(Z_ATTRIBUTE_NAME, zCoord);

		return bimData;
	}

	@Override
	public BimObjectCard fetchCardDataFromGlobalId(String globalId) {
		final CMFunction function = dataView.findFunctionByName(CARDDATA_FROM_GUID_FUNCTION);
		final NameAlias f = NameAlias.as("f");
		final CMQueryResult queryResult = dataView.select(anyAttribute(function, f)).from(call(function, globalId), f)
				.run();
		if (queryResult.isEmpty()) {
			System.out.println("No matching card found for globalid " + globalId);
		}

		BimObjectCard bimCard = new BimObjectCard();
		CMQueryRow row = queryResult.getOnlyRow();
		Integer rowIdInt = (Integer) row.getValueSet(f).get(ID_ATTRIBUTE);
		Integer rowIdClassInt = (Integer) row.getValueSet(f).get(CLASS_ID_ATTRIBUTE);
		String className = (String) row.getValueSet(f).get(CLASSNAME);
		String description = String.class.cast(row.getValueSet(f).get(DESCRIPTION));
		if (rowIdInt != null && rowIdClassInt != null) {
			Long rowId = new Long(rowIdInt.longValue());
			Long rowIdClass = new Long(rowIdClassInt.longValue());
			bimCard.setGlobalId(globalId);
			bimCard.setId(rowId);
			bimCard.setClassId(rowIdClass);
			bimCard.setCardDescription(description);
			bimCard.setClassName(className);
		}
		return bimCard;
	}

	public static class BimObjectCard {
		private Long id;
		private Long classId;
		private String className;
		private String cardDescription;
		private String globalId;

		public BimObjectCard() {
		}

		public BimObjectCard( //
				final Long id, //
				final Long classId, //
				final String cardDescription, //
				final String className //
		) {
			this.id = id;
			this.classId = classId;
			this.cardDescription = cardDescription;
			this.className = className;
		}

		public boolean isValid() {
			return id != null && classId != null;
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public Long getClassId() {
			return classId;
		}

		public void setClassId(Long classId) {
			this.classId = classId;
		}

		public String getClassName() {
			return className;
		}

		public void setClassName(String className) {
			this.className = className;
		}

		public String getCardDescription() {
			return cardDescription;
		}

		public void setCardDescription(String cardDescription) {
			this.cardDescription = cardDescription;
		}

		public String getGlobalId() {
			return globalId;
		}

		public void setGlobalId(String globalId) {
			this.globalId = globalId;
		}
	}

	// FIXME custom method to remove
	// this is a temporary workaround, waiting for the navigation of the
	// bim-tree.
	@Override
	@Deprecated
	public long fetchBuildingIdFromCardId(Long cardId) {
		long buildingId = -1;
		try {
			final CMFunction function = dataView.findFunctionByName(FIND_BUILDING_FUNCTION);
			final NameAlias f = NameAlias.as("f");
			final CMQueryResult queryResult = dataView.select(anyAttribute(function, f))
					.from(call(function, cardId), f).run();
			if (!queryResult.isEmpty()) {
				CMQueryRow row = queryResult.getOnlyRow();
				if (row.getValueSet(f).get("buildingid") != null) {
					buildingId = new Long((Integer) row.getValueSet(f).get("buildingid"));
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return buildingId;
	}

}
