package org.cmdbuild.services.bim.connector;

import static org.cmdbuild.bim.utils.BimConstants.FK_COLUMN_NAME;
import static org.cmdbuild.bim.utils.BimConstants.GEOMETRY_ATTRIBUTE;
import static org.cmdbuild.bim.utils.BimConstants.GLOBALID;
import static org.cmdbuild.bim.utils.BimConstants.HEIGHT_ATTRIBUTE;
import static org.cmdbuild.bim.utils.BimConstants.INSERT_COORDINATES_QUERY_TEMPLATE;
import static org.cmdbuild.bim.utils.BimConstants.POINT_TEMPLATE;
import static org.cmdbuild.bim.utils.BimConstants.SELECT_CENTROID_QUERY_TEMPLATE;
import static org.cmdbuild.common.Constants.BASE_CLASS_NAME;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.FunctionCall.call;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.EntryTypeAlias;
import org.cmdbuild.dao.query.clause.alias.NameAlias;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.services.bim.BimDataView;
import org.cmdbuild.utils.bim.BimIdentifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import com.google.common.collect.Maps;

public class DefaultBimDataView implements BimDataView {

	public static final String X_COORD = "x";
	public static final String Y_COORD = "y";
	public static final String Z_COORD = "z";

	public static final String ID = Constants.ID_ATTRIBUTE;
	public static final String CODE = Constants.CODE_ATTRIBUTE;
	public static final String DESCRIPTION = Constants.DESCRIPTION_ATTRIBUTE;
	private static final String ID_FROM_GUID_FUNCTION = "_cm_get_id_from_globalid";
	private static final String ALL_GLOBALID_FUNCTION = "_cm_all_globalid_map";
	private static final String FIND_BUILDING_FUNCTION = "_opm_find_the_building";
	private static final String ID_FROM_GLOBALID = "_cm_id_from_globalid";

	private final CMDataView dataView;
	private JdbcTemplate jdbcTemplate;

	public DefaultBimDataView(CMDataView dataView, JdbcTemplate jdbcTemplate) {
		this.dataView = dataView;
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public long getMatchingId(String key, String className) {
		Long matchingId = null;
		CMClass theClass = dataView.findClass(BimIdentifier.newIdentifier().withName(className));
		Alias CLASS_ALIAS = EntryTypeAlias.canonicalAlias(theClass);
		CMQueryResult result = dataView.select( //
				anyAttribute(CLASS_ALIAS)) //
				.from(theClass)
				//
				.where(condition(attribute(CLASS_ALIAS, GLOBALID), eq(key))) //
				.run();
		if (!result.isEmpty()) {
			CMCard card = result.getOnlyRow().getCard(CLASS_ALIAS);
			if (card.get(FK_COLUMN_NAME) != null) {
				IdAndDescription reference = (IdAndDescription) card.get(FK_COLUMN_NAME);
				matchingId = reference.getId();
			}
		}
		return matchingId;
	}
	
	
	@Override
	public CMQueryResult fetchCardsOfClassInContainer(String className, long containerId, String containerAttribute) {
		CMClass theClass = dataView.findClass(className);
		CMQueryResult result = dataView.select(anyAttribute(theClass)) //
				.from(theClass) //
				.where(condition(attribute(theClass, containerAttribute), eq(containerId))) //
				.run();
		return result;
	}
	
	
	// TODO WHAT THE HELL IS THIS METHOD DOING? REPLACE WITH A STORE PROCEDURE!
	@Override
	public Map<String, String> fetchBimDataOfRow(CMQueryRow row, String className, String containerId,
			String containerClassName) {
		CMClass theClass = dataView.findClass(className);
		CMCard card = row.getCard(theClass);

		final String template = "SELECT \"%s\", st_x(\"%s\") AS x, st_y(\"%s\") AS y, st_z(\"%s\") AS z\n"
				+ "FROM bim.\"%s\" \n" + "WHERE \"%s\" = %s";
		final String readFeatureQuery = String.format( //
				template, //
				GLOBALID, //
				GEOMETRY_ATTRIBUTE, //
				GEOMETRY_ATTRIBUTE, //
				GEOMETRY_ATTRIBUTE, //
				className, //
				FK_COLUMN_NAME, //
				card.getId() //
				);

		final Map<String, String> bimData = Maps.newHashMap();
		bimData.put(ID, card.getId().toString());
		bimData.put(CODE, card.getCode().toString());
		bimData.put(DESCRIPTION, card.getDescription().toString());

		jdbcTemplate.query(readFeatureQuery, new RowCallbackHandler() {
			@Override
			public void processRow(final ResultSet rs) throws SQLException {
				bimData.put(GLOBALID, rs.getString(GLOBALID));
				bimData.put(X_COORD, rs.getString(X_COORD));
				bimData.put(Y_COORD, rs.getString(Y_COORD));
				bimData.put(Z_COORD, rs.getString(Z_COORD));
			}
		});
		bimData.put(BASE_CLASS_NAME, className);
		if (bimData.get(GLOBALID) == null) {
			final String selectCentroidQuery = String.format( //
					SELECT_CENTROID_QUERY_TEMPLATE, //
					GEOMETRY_ATTRIBUTE, //
					GEOMETRY_ATTRIBUTE, //
					HEIGHT_ATTRIBUTE, //
					containerClassName, //
					FK_COLUMN_NAME, //
					containerId //
					);
			System.out.println(selectCentroidQuery);

			final String selectNextGuid = "SELECT nextval('bim.guid')";
			jdbcTemplate.query(selectNextGuid, new RowCallbackHandler() {
				@Override
				public void processRow(final ResultSet rs) throws SQLException {
					bimData.put(GLOBALID, String.valueOf(rs.getInt("nextval")));
				}
			});


			jdbcTemplate.query(selectCentroidQuery, new RowCallbackHandler() {
				@Override
				public void processRow(final ResultSet rs) throws SQLException {
					bimData.put(X_COORD, rs.getString(X_COORD));
					bimData.put(Y_COORD, rs.getString(Y_COORD));
					bimData.put(Z_COORD, rs.getString(Z_COORD));
				}
			});

			String geometryString = String.format(POINT_TEMPLATE, bimData.get(X_COORD), bimData.get(Y_COORD),
					bimData.get(Z_COORD));
			String insertGeometryQuery = String.format(INSERT_COORDINATES_QUERY_TEMPLATE, "bim", className,
					bimData.get(GLOBALID), geometryString, card.getId());
			jdbcTemplate.execute(insertGeometryQuery);
		}

		return bimData;
	}
	
	//use by the viewer
	@Override
	public Map<String, Long> fetchIdAndIdClassFromGlobalId(String globalId) {
		final CMFunction function = dataView.findFunctionByName(ID_FROM_GUID_FUNCTION);
		final NameAlias f = NameAlias.as("f");
		final CMQueryResult queryResult = dataView.select(anyAttribute(function, f)).from(call(function, globalId), f)
				.run();
		if (queryResult.isEmpty()) {
			System.out.println("No matching card found for globalid " + globalId);
		}
		CMQueryRow row = queryResult.getOnlyRow();
		final Map<String, Long> dataRow = Maps.newHashMap();
		for (final Entry<String, Object> entry : row.getValueSet(f).getValues()) {
			Object value = entry.getValue();
			if (value != null) {
				Long longValue = new Long(((Integer) value).longValue());
				dataRow.put(entry.getKey(), longValue);
			} else {
				dataRow.put(entry.getKey(), null);
			}
		}
		return dataRow;
	}
	
	// used by the viewer
	public static class BimObjectCard {
		private Long id;
		private Long classId;
		private String className;
		private String cardDescription;

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
	}
	
	
	// used by the viewer
	@Override
	public Map<String, BimObjectCard> fetchIdAndIdClassForGlobalIdMap(Map<Long, String> globalIdMap) {
		final CMFunction function = dataView.findFunctionByName(ALL_GLOBALID_FUNCTION);
		final NameAlias f = NameAlias.as("f");
		final CMQueryResult queryResult = dataView.select(anyAttribute(function, f)).from(call(function), f).run();
		final Map<String, BimObjectCard> result = Maps.newHashMap();
		if (!queryResult.isEmpty()) {
			for (final CMQueryRow row : queryResult) {
				if (row.getValueSet(f).get("globalid") != null && row.getValueSet(f).get("id") != null
						&& row.getValueSet(f).get("idclass") != null) {
					String guid = (String) row.getValueSet(f).get("globalid");
					int id = (Integer) row.getValueSet(f).get("id");
					int idclass = (Integer) row.getValueSet(f).get("idclass");

					Long longId = new Long(id);
					Long longIdClass = new Long(idclass);

					String className =  "";
					CMEntryType entryType = this.dataView.findClass(longIdClass);
					if (entryType != null) {
						className = entryType.getName();
					}
					String cardDescription = (String) row.getValueSet(f).get("card_description");
					result.put(guid, new BimObjectCard(longId, longIdClass, cardDescription, className));
				}
			}
		}
		return result;
	}

	//used by the viewer
	@Override
	public void fillGlobalidIdMap(Map<String, Long> globalid_cmdbId_map, String className) {
		for(String globalId : globalid_cmdbId_map.keySet()){
			final CMFunction function = dataView.findFunctionByName(ID_FROM_GUID_FUNCTION);
			final NameAlias f = NameAlias.as("f");
			final CMQueryResult queryResult = dataView.select(anyAttribute(function, f)).from(call(function, globalId), f)
					.run();
			if (queryResult.isEmpty()) {
				System.out.println("No matching card found for globalid " + globalId);
			}
			CMQueryRow row = queryResult.getOnlyRow();
			if(row.getValueSet(f).get("id") != null){
				Long matchingId = new Long(Integer.class.cast(row.getValueSet(f).get("id")));
				globalid_cmdbId_map.put(globalId, matchingId);
			}
		}
	}
	
	
	
	
	//FIXME custom method to remove
	//this is a temporary workaround, waiting for the navigation of the bim-tree. 
	@Override
	@Deprecated
	public long fetchBuildingIdFromCardId(Long cardId) {
		long buildingId = -1;
		try {
			final CMFunction function = dataView.findFunctionByName(FIND_BUILDING_FUNCTION);
			final NameAlias f = NameAlias.as("f");
			final CMQueryResult queryResult = dataView.select(anyAttribute(function, f)).from(call(function, cardId), f).run();
			if (!queryResult.isEmpty()) {
				CMQueryRow row = queryResult.getOnlyRow();
				if(row.getValueSet(f).get("buildingid") != null){
					buildingId = new Long((Integer) row.getValueSet(f).get("buildingid"));
				}
			}			
		} catch (Exception e) {
			// TODO: handle exception
		}
		return buildingId;
	}
}
