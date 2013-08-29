package org.cmdbuild.services.bim;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.converter.BimProjectStorableConverter;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.model.data.Attribute;
import org.cmdbuild.model.data.Attribute.AttributeBuilder;
import org.cmdbuild.model.data.Domain;
import org.cmdbuild.model.data.Domain.DomainBuilder;
import org.cmdbuild.model.data.EntryType;
import org.cmdbuild.model.data.EntryType.ClassBuilder;
import org.cmdbuild.utils.bim.BimIdentifier;

public class DefaultBimDataModelManager implements BimDataModelManager {

	private final CMDataView dataView;
	private final DataDefinitionLogic dataDefinitionLogic;

	public static final String BIM_SCHEMA = "bim";
	public static final String DEFAULT_DOMAIN_SUFFIX = "_BimProject";

	public DefaultBimDataModelManager(CMDataView dataView, DataDefinitionLogic dataDefinitionLogic) {
		this.dataView = dataView;
		this.dataDefinitionLogic = dataDefinitionLogic;
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
				.withName("GlobalId") //
				.withType(Attribute.AttributeTypeBuilder.STRING) //
				.withLength(22) //
				.thatIsUnique(true) //
				.thatIsMandatory(true) //
				.withOwnerName(className) //
				.withOwnerNamespace(BIM_SCHEMA);
		
		Attribute attributeGlobalId = attributeBuilder.build();
		dataDefinitionLogic.createOrUpdate(attributeGlobalId);

		attributeBuilder = Attribute.newAttribute() //
				.withName("Master") //
				.withType(Attribute.AttributeTypeBuilder.FOREIGNKEY) //
				.thatIsUnique(true) //
				.thatIsMandatory(true) //
				.withOwnerName(className) //
				.withOwnerNamespace(BIM_SCHEMA) //
				.withForeignKeyDestinationClassName(className);
		Attribute attributeMaster = attributeBuilder.build();
		dataDefinitionLogic.createOrUpdate(attributeMaster);
	}

}
