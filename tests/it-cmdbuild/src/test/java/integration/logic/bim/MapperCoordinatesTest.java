package integration.logic.bim;

import static integration.logic.data.DataDefinitionLogicTest.a;
import static integration.logic.data.DataDefinitionLogicTest.newClass;
import static org.cmdbuild.bim.utils.BimConstants.COORDINATES;
import static org.cmdbuild.bim.utils.BimConstants.GEOMETRY_ATTRIBUTE;
import static org.cmdbuild.bim.utils.BimConstants.IFC_GLOBALID;
import static org.cmdbuild.bim.utils.BimConstants.POINT_TEMPLATE;
import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.common.Constants.ID_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.services.bim.DefaultBimDataModelManager.BIM_SCHEMA;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.cmdbuild.bim.mapper.BimAttribute;
import org.cmdbuild.bim.mapper.BimEntity;
import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.utils.bim.BimIdentifier;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.postgis.Geometry;
import org.postgis.PGgeometry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import utils.DatabaseDataFixture;
import utils.IntegrationTestBase;
import utils.IntegrationTestBim;
import utils.DatabaseDataFixture.Context;
import utils.DatabaseDataFixture.Hook;

import com.google.common.collect.Lists;
import com.mchange.util.AssertException;

public class MapperCoordinatesTest extends IntegrationTestBim {

	private static final String CLASS_NAME = "Computer";
	private JdbcTemplate jdbcTemplate;
	
	@ClassRule
	public static DatabaseDataFixture databaseDataFixture = DatabaseDataFixture.newInstance() //
			.dropAfter(true) //
			.hook(new Hook() {

				@Override
				public void before(final Context context) {
					try {
						final JdbcTemplate jdbcTemplate = new JdbcTemplate(context.dataSource());
						final URL url = IntegrationTestBase.class.getClassLoader().getResource("postgis.sql");
						final String sql = FileUtils.readFileToString(new File(url.toURI()));
						jdbcTemplate.execute(sql);
					} catch (Exception e) {
						e.printStackTrace();
						throw new AssertException("should never come here");
					}
				}

				@Override
				public void after(final Context context) {
					// do nothing
				}

			}) //
			.build();

	@Before
	public void setUp() throws Exception {
		super.setUp();
		jdbcTemplate = jdbcTemplate();

		// create the class
		dataDefinitionLogic.createOrUpdate(a(newClass(CLASS_NAME)));

		// create bim-table and geometry column
		bimLogic.updateBimLayer(CLASS_NAME, "active", "true");
		bimLogic.updateBimLayer(CLASS_NAME, "export", "true");
	}

	@Test
	public void setCoordinates() throws Exception {
		// given
		final String code = "C" + RandomStringUtils.randomAlphanumeric(5);
		final String globalId = RandomStringUtils.randomAlphanumeric(22);
		List<Entity> source = Lists.newArrayList();
		Entity e = new BimEntity(CLASS_NAME);
		List<Attribute> attributeList = e.getAttributes();

		attributeList.add(new BimAttribute(CODE_ATTRIBUTE, code));
		attributeList.add(new BimAttribute(DESCRIPTION_ATTRIBUTE, "Computer 1"));
		attributeList.add(new BimAttribute(IFC_GLOBALID, globalId));
		String x = "1.2";
		String y = "3.4";
		String z = "5.6";

		String postgisFormat = String.format(POINT_TEMPLATE, x, y, z);

		attributeList.add(new BimAttribute(COORDINATES, postgisFormat));
		source.add(e);

		// when
		mapper.update(source);

		// then
		CMClass bimClass = dbDataView().findClass(BimIdentifier.newIdentifier().withName(CLASS_NAME));

		CMQueryResult queryResult = dbDataView().select(anyAttribute(bimClass)) //
				.from(bimClass) //
				.where(condition(attribute(bimClass, IFC_GLOBALID), eq(globalId))) //
				.run();
		assertTrue(queryResult != null);
		CMCard bimCard = queryResult.getOnlyRow().getCard(bimClass);
		assertThat(bimCard.get(IFC_GLOBALID).toString(), equalTo(globalId));

		String SELECT_COORDINATES_TEMPLATE = "SELECT \"%s\" FROM %s.\"%s\" WHERE \"%s\" = %s";
		final String selectCoordinatesQuery = String.format(SELECT_COORDINATES_TEMPLATE, //
				GEOMETRY_ATTRIBUTE, //
				BIM_SCHEMA, //
				CLASS_NAME, //
				ID_ATTRIBUTE, //
				bimCard.getId().toString());

		System.out.println(selectCoordinatesQuery);

		jdbcTemplate.query(selectCoordinatesQuery, new RowCallbackHandler() {
			Geometry coordinates = null;

			@Override
			public void processRow(final ResultSet rs) throws SQLException {
				final String geometryAsString = rs.getString(GEOMETRY_ATTRIBUTE);
				if (geometryAsString != null && !geometryAsString.equals("")) {
					coordinates = PGgeometry.geomFromString(geometryAsString);
					assertTrue(coordinates != null);
					System.out.println(coordinates.toString());
				}
			}
		});
	}
}
