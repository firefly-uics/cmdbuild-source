package integration.logic.bim;

import static org.cmdbuild.bim.utils.BimConstants.IFC_GLOBALID;
import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;

import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.cmdbuild.bim.mapper.BimAttribute;
import org.cmdbuild.bim.mapper.BimEntity;
import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.dao.driver.postgres.PostgresDriver;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import utils.IntegrationTestBimBase;

import com.google.common.collect.Lists;

public class MapperContainerTest extends IntegrationTestBimBase {

	private static final String CONTAINER_CLASS = "Room";
	private JdbcTemplate jdbcTemplate;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		jdbcTemplate = ((PostgresDriver) dbDriver()).getJdbcTemplate();

		// create BIM-table and geometry column
		bimLogic.updateBimLayer(CLASS_NAME, "active", "true");
		bimLogic.updateBimLayer(CLASS_NAME, "export", "true");
	}
	
	@Ignore
	//FIXME
	@Test
	public void setContainer() throws Exception {
		// given
		List<Entity> source = Lists.newArrayList();
		Entity e = new BimEntity("Edificio");
		List<Attribute> attributeList = e.getAttributes();

		final String computerCode = "E" + RandomStringUtils.random(5);
		final String globalId = RandomStringUtils.random(22);

		attributeList.add(new BimAttribute(CODE_ATTRIBUTE, computerCode));
		attributeList
				.add(new BimAttribute(DESCRIPTION_ATTRIBUTE, "Computer 1"));
		attributeList.add(new BimAttribute(IFC_GLOBALID, globalId));

		// FIXME not managed yet!!!
		attributeList.add(new BimAttribute(CONTAINER_CLASS, globalId));
		source.add(e);

		// when
		mapper.update(source);

		// then
		System.out.println("");
	}
}
