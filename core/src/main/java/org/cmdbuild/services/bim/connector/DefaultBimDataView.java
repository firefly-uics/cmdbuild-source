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
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.alias.NameAlias;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.services.bim.BimDataView;
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

	private final CMDataView dataView;
	private JdbcTemplate jdbcTemplate;

	public DefaultBimDataView(CMDataView dataView, JdbcTemplate jdbcTemplate) {
		this.dataView = dataView;
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public long getId(String key, String className) {
		return BimMapperRules.INSTANCE.convertKeyToId(key, className, dataView);
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
		System.out.println(readFeatureQuery);

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
			System.out.println("This card hasn't got bim-data yet");
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

			System.out.println("Generate globalid " + bimData.get(GLOBALID));

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
			System.out.println(insertGeometryQuery);
			jdbcTemplate.execute(insertGeometryQuery);
		}

		return bimData;
	}

	@Override
	public CMDataView getDataView() {
		return this.dataView;
	}

	@Override
	public Map<String, Long> fetchIdAndIdClassFromGlobalId(String globalId) {
		System.out.println("Execute function " + ID_FROM_GUID_FUNCTION + " for parameter " + globalId);
		final CMFunction function = dataView.findFunctionByName(ID_FROM_GUID_FUNCTION);
		final NameAlias f = NameAlias.as("f");
		final CMQueryResult queryResult = dataView.select(anyAttribute(function, f)).from(call(function, globalId), f)
				.run();
		if (queryResult.isEmpty()) {
			System.out.println("No matching card found for globalid " + globalId);
		}
		CMQueryRow row = queryResult.getOnlyRow();
		final Map<String, Long> dataRow = Maps.newHashMap();
		System.out.println("globalid " + globalId + " corresponds to");
		for (final Entry<String, Object> entry : row.getValueSet(f).getValues()) {
			Object value = entry.getValue();
			if(value != null){
				Long longValue = new Long(((Integer) value).longValue());
				dataRow.put(entry.getKey(), longValue);
			}else{
				dataRow.put(entry.getKey(), null);
			}
			System.out.println(entry.getKey() + " = " + dataRow.get(entry.getKey()));
		}		
		return dataRow;
	}

}
