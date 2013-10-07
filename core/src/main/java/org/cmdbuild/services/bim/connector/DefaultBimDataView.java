package org.cmdbuild.services.bim.connector;

import static org.cmdbuild.bim.utils.BimConstants.*;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.services.bim.BimDataView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import com.google.common.collect.Maps;

public class DefaultBimDataView implements BimDataView {

	// FIXME change to some configuration parameter
	private static final String CONTAINER_ATTRIBUTE = "IsInRoom";

	public static final String X_COORD = "x";
	public static final String Y_COORD = "y";
	public static final String Z_COORD = "z";

	public static final String ID = Constants.ID_ATTRIBUTE;
	public static final String CODE = Constants.CODE_ATTRIBUTE;
	public static final String DESCRIPTION = Constants.DESCRIPTION_ATTRIBUTE;
	
	private final CMDataView dataView;
	private JdbcTemplate jdbcTemplate;

	public DefaultBimDataView(CMDataView dataView, JdbcTemplate jdbcTemplate) {
		this.dataView = dataView;
	}

	@Override
	public CMQueryResult fetchCardsOfClassInContainer(String className, long containerId) {
		CMClass theClass = dataView.findClass(className);
		CMQueryResult result = dataView.select(anyAttribute(theClass)) //
				.from(theClass) //
				.where(condition(attribute(theClass, CONTAINER_ATTRIBUTE), eq(containerId))) //
				.run();
		return result;
	}

	@Override
	public Map<String, String> fetchBimDataOfRow(CMQueryRow row, String className) {
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
		return bimData;
	}

}
