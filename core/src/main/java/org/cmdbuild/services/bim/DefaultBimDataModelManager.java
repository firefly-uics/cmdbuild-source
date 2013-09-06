package org.cmdbuild.services.bim;

import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.common.Constants.ID_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.AnyClass.anyClass;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.Utils.as;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entry.CMRelation.CMRelationDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.QueryRelation;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.EntryTypeAlias;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.converter.BimProjectStorableConverter;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.model.data.Attribute;
import org.cmdbuild.model.data.Attribute.AttributeBuilder;
import org.cmdbuild.model.data.Domain;
import org.cmdbuild.model.data.Domain.DomainBuilder;
import org.cmdbuild.model.data.EntryType;
import org.cmdbuild.model.data.EntryType.ClassBuilder;
import org.cmdbuild.services.bim.connector.Mapper;
import org.cmdbuild.utils.bim.BimIdentifier;

import com.google.common.collect.Lists;

public class DefaultBimDataModelManager implements BimDataModelManager {

	public static final String GLOBALID = "GlobalId";
	private final CMDataView dataView;
	private final DataDefinitionLogic dataDefinitionLogic;
	private final LookupStore lookupStore;

	public static final String FK_COLUMN_NAME = "Master";
	public static final String BIM_SCHEMA = "bim";
	public static final String DEFAULT_DOMAIN_SUFFIX = BimProjectStorableConverter.TABLE_NAME;

	public DefaultBimDataModelManager(CMDataView dataView, DataDefinitionLogic dataDefinitionLogic, LookupStore lookupStore) {
		this.dataView = dataView;
		this.dataDefinitionLogic = dataDefinitionLogic;
		this.lookupStore = lookupStore;
	}

	@Override
	public void createBimTableIfNeeded(String className) {

		CMClass bimClass = dataView.findClass(BimIdentifier.newIdentifier().withName(className));
		if (bimClass == null) {
			createBimTable(className);
		}
	}

	@Override
	public void createBimDomainOnClass(String className) {
		CMClass theClass = dataView.findClass(className);
		CMClass projectClass = dataView.findClass(BimProjectStorableConverter.TABLE_NAME);
		DomainBuilder domainBuilder = Domain.newDomain() //
				.withName(className + DEFAULT_DOMAIN_SUFFIX) //
				.withIdClass1(theClass.getId()) //
				.withIdClass2(projectClass.getId()) //
				.withCardinality("N:1");

		Domain domain = domainBuilder.build();

		dataDefinitionLogic.create(domain);
	}

	@Override
	public void deleteBimDomainOnClass(String className) {
		dataDefinitionLogic.deleteDomainByName(className + DEFAULT_DOMAIN_SUFFIX);
	}

	private void createBimTable(String className) {
		ClassBuilder classBuilder = EntryType.newClass() //
				.withName(className) //
				.withNamespace(BIM_SCHEMA) //
				.thatIsSystem(true);
		dataDefinitionLogic.createOrUpdate(classBuilder.build());

		AttributeBuilder attributeBuilder = Attribute.newAttribute() //
				.withName(GLOBALID) //
				.withType(Attribute.AttributeTypeBuilder.STRING) //
				.withLength(22) //
				.thatIsUnique(true) //
				.thatIsMandatory(true) //
				.withOwnerName(className) //
				.withOwnerNamespace(BIM_SCHEMA);

		Attribute attributeGlobalId = attributeBuilder.build();
		dataDefinitionLogic.createOrUpdate(attributeGlobalId);

		attributeBuilder = Attribute.newAttribute() //
				.withName(FK_COLUMN_NAME) //
				.withType(Attribute.AttributeTypeBuilder.FOREIGNKEY) //
				.thatIsUnique(true) //
				.thatIsMandatory(true) //
				.withOwnerName(className) //
				.withOwnerNamespace(BIM_SCHEMA) //
				.withForeignKeyDestinationClassName(className);
		Attribute attributeMaster = attributeBuilder.build();
		dataDefinitionLogic.createOrUpdate(attributeMaster);
	}

	@Override
	public void bindProjectToCards(String projectCardId, String className, ArrayList<String> cardsToBind) {
		CMClass projectsClass = dataView.findClass(BimProjectStorableConverter.TABLE_NAME);
		CMClass rootClass = dataView.findClass(className);

		CMDomain domain = dataView.findDomain(className + DEFAULT_DOMAIN_SUFFIX);

		removeOldRelations(domain, projectCardId);

		for (String cardId : cardsToBind) {
			CMRelationDefinition relationDefinition = dataView.createRelationFor(domain);

			CMCard projectCard = dataView.select(attribute(projectsClass, DESCRIPTION_ATTRIBUTE)) //
					.from(projectsClass) //
					.where(condition(attribute(projectsClass, ID_ATTRIBUTE), eq(Long.parseLong(projectCardId)))) //
					.run() //
					.getOnlyRow() //
					.getCard(projectsClass);

			CMCard rootCard = dataView.select(attribute(rootClass, DESCRIPTION_ATTRIBUTE)) //
					.from(rootClass) //
					.where(condition(attribute(rootClass, ID_ATTRIBUTE), eq(Long.parseLong(cardId)))) //
					.run() //
					.getOnlyRow() //
					.getCard(rootClass);

			relationDefinition.setCard1(rootCard);
			relationDefinition.setCard2(projectCard);

			relationDefinition.save();
		}

	}

	private void removeOldRelations(CMDomain domain, String projectId) {
		ArrayList<CMRelation> oldRelations = getAllRelationsForDomain(domain, projectId);
		for (CMRelation relation : oldRelations) {
			dataView.delete(relation);
		}
	}

	private ArrayList<CMRelation> getAllRelationsForDomain(CMDomain domain, String projectId) {
		ArrayList<CMRelation> oldRelations = Lists.newArrayList();

		CMClass projectClass = domain.getClass2();
		CMClass rootClass = domain.getClass1();

		Alias DOM_ALIAS = EntryTypeAlias.canonicalAlias(domain);
		Alias DST_ALIAS = EntryTypeAlias.canonicalAlias(projectClass);
		CMQueryResult result = dataView.select( //
				anyAttribute(DOM_ALIAS), attribute(DST_ALIAS, DESCRIPTION_ATTRIBUTE)) //
				.from(rootClass) //
				.join(anyClass(), as(DST_ALIAS), over(domain, as(DOM_ALIAS))) //
				.where(condition(attribute(DST_ALIAS, ID_ATTRIBUTE), eq(Long.parseLong(projectId))))//
				.run();

		for (java.util.Iterator<CMQueryRow> it = result.iterator(); it.hasNext();) {
			CMQueryRow row = it.next();
			QueryRelation queryRelation = row.getRelation(domain);
			CMRelation relation = queryRelation.getRelation();
			oldRelations.add(relation);
		}
		return oldRelations;
	}

	@Override
	public ArrayList<String> fetchCardsBindedToProject(String projectId, String className) {
		ArrayList<CMRelation> relations = Lists.newArrayList();

		CMDomain domain = dataView.findDomain(className + DEFAULT_DOMAIN_SUFFIX);
		relations = getAllRelationsForDomain(domain, projectId);

		ArrayList<String> bindedCards = Lists.newArrayList();
		for (CMRelation relation : relations) {
			bindedCards.add(relation.getCard1Id().toString());
		}
		return bindedCards;
	}

	@Override
	public void updateCardsFromSource(List<Entity> source) {
		// String className = source.get(0).getTypeName();
		// List<Entity> target = getAllCardsOfClass(className);
		Mapper mapper = new Mapper(dataView); //
		mapper.update(source);
	}

	// private List<Entity> getAllCardsOfClass(String className) {
	//
	// List<Entity> target = Lists.newArrayList();
	// CMClass theClass = dataView.findClass(className);
	// Alias CLASS_ALIAS = EntryTypeAlias.canonicalAlias(theClass);
	// CMQueryResult result = dataView.select( //
	// anyAttribute(CLASS_ALIAS)) //
	// .from(theClass) //
	// .run();
	//
	// for (java.util.Iterator<CMQueryRow> it = result.iterator();
	// it.hasNext();) {
	// CMQueryRow row = it.next();
	// CMCard card = row.getCard(CLASS_ALIAS);
	// Entity cmEntity = new CMEntity(card);
	// target.add(cmEntity);
	// }
	// return target;
	// }

}
