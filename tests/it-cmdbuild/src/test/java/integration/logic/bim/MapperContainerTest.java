package integration.logic.bim;

import static integration.logic.data.DataDefinitionLogicTest.a;
import static integration.logic.data.DataDefinitionLogicTest.newAttribute;
import static integration.logic.data.DataDefinitionLogicTest.newClass;
import static integration.logic.data.DataDefinitionLogicTest.newDomain;
import static org.cmdbuild.bim.utils.BimConstants.IFC_GLOBALID;
import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.dao.constants.Cardinality.CARDINALITY_N1;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.cmdbuild.bim.mapper.BimAttribute;
import org.cmdbuild.bim.mapper.BimEntity;
import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CardReference;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.CMQueryResult;
import org.junit.Before;
import org.junit.Test;

import utils.IntegrationTestBim;

import com.google.common.collect.Lists;

public class MapperContainerTest extends IntegrationTestBim {

	private static final String CONTAINER_CLASS = "Stanza";
	private static final String CLASS_NAME = "Computer";
	private CMClass containerClass;
	private CMClass deviceClass;

	@Before
	public void setUp() throws Exception {
		super.setUp();

		// create Computer class
		containerClass = dataDefinitionLogic.createOrUpdate(a(newClass(CONTAINER_CLASS)));
		deviceClass = dataDefinitionLogic.createOrUpdate(a(newClass(CLASS_NAME)));
		
		// create BIM-table and geometry column
		bimLogic.updateBimLayer(CONTAINER_CLASS, "active", "true");
		bimLogic.updateBimLayer(CLASS_NAME, "active", "true");
		bimLogic.updateBimLayer(CLASS_NAME, "export", "true");

		// create the domain
		final String domainName = CLASS_NAME + CONTAINER_CLASS;
		CMDomain domain = dbDataView().findDomain(domainName);
		if (domain == null) {
			domain = dataDefinitionLogic.create(a(newDomain(domainName) //
					.withIdClass1(deviceClass.getId()) //
					.withIdClass2(containerClass.getId()) //
					.withCardinality(CARDINALITY_N1.value()) //
					));
		}

		// create the reference attribute
		dataDefinitionLogic.createOrUpdate( //
				a(newAttribute(CONTAINER_CLASS) //
						.withOwnerName(CLASS_NAME) //
						.withType("REFERENCE") //
						.withDomain(domain.getIdentifier().getLocalName())));
	}

	@Test
	public void setContainer() throws Exception {
		// given
		List<Entity> source = Lists.newArrayList();
		
		Entity room = new BimEntity(CONTAINER_CLASS);
		List<Attribute> roomAttributeList = room.getAttributes();
		final String roomCode = "R" + RandomStringUtils.random(5);
		final String roomGlobalId = RandomStringUtils.random(22);
		roomAttributeList.add(new BimAttribute(CODE_ATTRIBUTE, roomCode));
		roomAttributeList.add(new BimAttribute(DESCRIPTION_ATTRIBUTE, "Room 1"));
		roomAttributeList.add(new BimAttribute(IFC_GLOBALID, roomGlobalId));
		source.add(room);
		mapper.update(source);
		source.remove(room);
		
		
		Entity computer = new BimEntity(CLASS_NAME);
		List<Attribute> computerAttributeList = computer.getAttributes();
		final String computerCode = "C" + RandomStringUtils.random(5);
		final String globalId = RandomStringUtils.random(22);
		computerAttributeList.add(new BimAttribute(CODE_ATTRIBUTE, computerCode));
		computerAttributeList.add(new BimAttribute(DESCRIPTION_ATTRIBUTE, "Computer 1"));
		computerAttributeList.add(new BimAttribute(IFC_GLOBALID, globalId));
		computerAttributeList.add(new BimAttribute(CONTAINER_CLASS, roomGlobalId));
		source.add(computer);

		// when
		mapper.update(source);

		// then
		CMClass roomClass = dbDataView().findClass(CONTAINER_CLASS);
		CMQueryResult queryResult = dbDataView().select(anyAttribute(roomClass)) //
				.from(roomClass) //
				.where(condition(attribute(roomClass,CODE),eq(roomCode))) //
				.run();
		assertTrue(queryResult != null);
		CMCard roomCard = queryResult.getOnlyRow().getCard(roomClass);
		
		CMClass computerClass = dbDataView().findClass(CLASS_NAME);
		queryResult = dbDataView().select(anyAttribute(computerClass)) //
				.from(computerClass) //
				.where(condition(attribute(computerClass,CODE),eq(computerCode))) //
				.run();
		assertTrue(queryResult != null);
		CMCard computerCard = queryResult.getOnlyRow().getCard(computerClass);
		assertThat(((CardReference)computerCard.get(CONTAINER_CLASS)).getId(), equalTo(roomCard.getId()));
	}
}
