package org.cmdbuild.services.bim.connector;

import static org.cmdbuild.bim.utils.BimConstants.GLOBALID_ATTRIBUTE;
import static org.cmdbuild.bim.utils.BimConstants.X_ATTRIBUTE;
import static org.cmdbuild.bim.utils.BimConstants.Y_ATTRIBUTE_NAME;
import static org.cmdbuild.bim.utils.BimConstants.Z_ATTRIBUTE_NAME;
import static org.cmdbuild.common.Constants.BASE_CLASS_NAME;
import static org.cmdbuild.common.Constants.CLASS_ID_ATTRIBUTE;
import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.common.Constants.ID_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.FunctionCall.call;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.cmdbuild.bim.mapper.DefaultAttribute;
import org.cmdbuild.bim.mapper.DefaultEntity;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.alias.NameAlias;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.services.bim.BimDataView;

import com.google.common.collect.Lists;

public class DefaultBimDataView implements BimDataView {

	private static final String CONTAINER_ID = "container_id";
	public static final String X_COORD = "x";
	public static final String Y_COORD = "y";
	public static final String Z_COORD = "z";

	private static final String CARDDATA_FROM_GUID_FUNCTION = "_bim_carddata_from_globalid";
	private static final String CARDDATA_FOR_EXPORT_FUNCTION = "_bim_data_for_export";
	private static final String FIND_BUILDING_FUNCTION = "_opm_find_the_building";
	private static final String GENERATE_COORDINATES_FUNCTION = "_bim_generate_coordinates";
	private static final String STORE_BIMDATA_FUNCTION = "_bim_store_data";

	private static final String CLASSNAME = "ClassName";

	private final CMDataView dataView;

	public DefaultBimDataView(CMDataView dataView) {
		this.dataView = dataView;
	}
	
	@Override
	public CMCard getCmCardFromGlobalId(String globalId, String className) {
		CMCard matchingCard = null;
		BimObjectCard bimCard = getBimDataFromGlobalid(globalId);
		if (bimCard != null) {
			bimCard.getId();
			Long masterId = bimCard.getId();
			CMClass theClass = dataView.findClass(className);
			CMQueryResult result = dataView.select( //
					anyAttribute(theClass)) //
					.from(theClass).where(condition(attribute(theClass, ID_ATTRIBUTE), eq(masterId))) //
					.run();
			if (!result.isEmpty()) {
				CMQueryRow row = result.getOnlyRow();
				matchingCard = row.getCard(theClass);
			}
		}
		return matchingCard;
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
	public Entity getCardDataForExport(CMCard card, String className, String containerId, String containerClassName) {

		Entity cardToExport = Entity.NULL_ENTITY;

		CMFunction function = dataView.findFunctionByName(CARDDATA_FOR_EXPORT_FUNCTION);
		NameAlias f = NameAlias.as("f");
		CMQueryResult queryResult = dataView.select(anyAttribute(function, f))
				.from(call(function, card.getId(), className), f).run();

		if (queryResult.isEmpty()) {
			System.out.println("No bim data found for card " + card.getId());
		}

		CMQueryRow row = queryResult.getOnlyRow();
		String code = String.class.cast(row.getValueSet(f).get(CODE_ATTRIBUTE));
		String description = String.class.cast(row.getValueSet(f).get(DESCRIPTION_ATTRIBUTE));
		String globalId = String.class.cast(row.getValueSet(f).get(GLOBALID_ATTRIBUTE));
		String xCoord = String.class.cast(row.getValueSet(f).get(X_ATTRIBUTE));
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
			queryResult = dataView.select(anyAttribute(function, f))
					.from(call(function, containerId, containerClassName), f).run();
			if (queryResult.isEmpty()) {
				System.out.println("No coordinates generated for card " + card.getId());
			}
			CMQueryRow rowCoordinates = queryResult.getOnlyRow();

			xCoord = String.class.cast(rowCoordinates.getValueSet(f).get(X_ATTRIBUTE));
			yCoord = String.class.cast(rowCoordinates.getValueSet(f).get(Y_ATTRIBUTE_NAME));
			zCoord = String.class.cast(rowCoordinates.getValueSet(f).get(Z_ATTRIBUTE_NAME));

			function = dataView.findFunctionByName(STORE_BIMDATA_FUNCTION);
			f = NameAlias.as("f");
			queryResult = dataView.select(anyAttribute(function, f))
					.from(call(function, card.getId(), className, globalId, xCoord, yCoord, zCoord), f).run();
		}

		// Map<String, String> bimData = Maps.newHashMap();
		// bimData.put(ID_ATTRIBUTE, card.getId().toString());
		// bimData.put(BASE_CLASS_NAME, className);
		// bimData.put(CODE_ATTRIBUTE, code);
		// bimData.put(DESCRIPTION_ATTRIBUTE, description);
		// bimData.put(GLOBALID_ATTRIBUTE, globalId);
		// bimData.put(X_ATTRIBUTE, xCoord);
		// bimData.put(Y_ATTRIBUTE_NAME, yCoord);
		// bimData.put(Z_ATTRIBUTE_NAME, zCoord);

		DefaultEntity cardWithBimData = new DefaultEntity(StringUtils.EMPTY, null);
		cardWithBimData.addAttribute(new DefaultAttribute(ID_ATTRIBUTE, card.getId().toString()));
		cardWithBimData.addAttribute(new DefaultAttribute(BASE_CLASS_NAME, className));
		cardWithBimData.addAttribute(new DefaultAttribute(CODE_ATTRIBUTE, code));
		cardWithBimData.addAttribute(new DefaultAttribute(DESCRIPTION_ATTRIBUTE, description));
		cardWithBimData.addAttribute(new DefaultAttribute(GLOBALID_ATTRIBUTE, globalId));
		cardWithBimData.addAttribute(new DefaultAttribute(X_ATTRIBUTE, xCoord));
		cardWithBimData.addAttribute(new DefaultAttribute(Y_ATTRIBUTE_NAME, yCoord));
		cardWithBimData.addAttribute(new DefaultAttribute(Z_ATTRIBUTE_NAME, zCoord));
		cardWithBimData.addAttribute(new DefaultAttribute(CONTAINER_ID, containerId));

		cardToExport = cardWithBimData;

		return cardToExport;
	}

	@Override
	public BimObjectCard getBimDataFromGlobalid(String globalId) {
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
		String description = String.class.cast(row.getValueSet(f).get(DESCRIPTION_ATTRIBUTE));
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

	@Override
	public Long getIdFromGlobalId(String globalId, String className) {
		Long id = null;
		BimObjectCard cardData = getBimDataFromGlobalid(globalId);
		if(cardData != null){
			id = cardData.getId();
		}
		return id;
	}

}
