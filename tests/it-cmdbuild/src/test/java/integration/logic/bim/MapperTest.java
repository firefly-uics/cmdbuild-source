package integration.logic.bim;

import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.common.Constants.ID_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.List;

import org.cmdbuild.bim.mapper.BimAttribute;
import org.cmdbuild.bim.mapper.BimEntity;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.services.bim.connector.Mapper;
import org.junit.Test;

import static org.junit.Assert.*;

import com.google.common.collect.Lists;

import utils.IntegrationTestBase;
import utils.IntegrationTestUtils;

public class MapperTest extends IntegrationTestBase {
	
	
	private static final String CLASS_NAME = "Edificio";

	@Test
	public void writeOneCardOnAnEmptyClass() throws Exception {
		//given
		Mapper mapper = new Mapper(dbDataView());
		List<Entity> source = Lists.newArrayList();
		List<Entity> target = Lists.newArrayList();
		Entity e = new BimEntity("Edificio");
		List<Attribute> attributeList = e.getAttributes();
		
		attributeList.add(new BimAttribute("Code","E1"));
		attributeList.add(new BimAttribute("Description","Edificio 1"));
		
		source.add(e);
		
		IntegrationTestUtils.newClass(CLASS_NAME);
		
		
		//when
		mapper.update(source, target);
		
		
		//then
		CMClass theClass = dbDataView().findClass(CLASS_NAME);
		
		CMQueryResult queryResult = dbDataView().select(anyAttribute(theClass)) //
				.from(theClass) //
				.run();
		
		assertTrue(queryResult != null);
		
	}
}
