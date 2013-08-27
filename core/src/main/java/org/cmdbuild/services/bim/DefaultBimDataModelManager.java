package org.cmdbuild.services.bim;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.model.data.Attribute;
import org.cmdbuild.model.data.Attribute.AttributeBuilder;
import org.cmdbuild.model.data.EntryType;
import org.cmdbuild.model.data.EntryType.ClassBuilder;

public class DefaultBimDataModelManager implements BimDataModelManager {

	private final CMDataView dataView;
	private final DataDefinitionLogic dataDefinitionLogic;

	private static final String BIM_TABLE_PREFIX = "_bim_";
	private static final String BIM_SCHEMA = "bim";

	public DefaultBimDataModelManager(CMDataView dataView, DataDefinitionLogic dataDefinitionLogic) {
		this.dataView = dataView;
		this.dataDefinitionLogic = dataDefinitionLogic;
	}

	@Override
	public void createBimTable(String className, String value) {
		CMClass bimClass = dataView.findClass(BIM_TABLE_PREFIX + className);
		if (bimClass == null) {
			createBimTable(className);
		}
	}

	@Override
	public void deleteBimDomainOnClass(String oldClass) {
		// TODO Auto-generated method stub

	}

	@Override
	public void createBimDomainOnClass(String className) {
		// TODO Auto-generated method stub

	}

	private void createBimTable(String className) {
		ClassBuilder classBuilder = EntryType.newClass() //
				.withName(BIM_TABLE_PREFIX + className) //
				.withNamespace(BIM_SCHEMA) //
				.thatIsSystem(true);
		dataDefinitionLogic.createOrUpdate(classBuilder.build());

		AttributeBuilder attributeBuilder = Attribute.newAttribute() //
				.withName("GlobalId") //
				.withType(Attribute.AttributeTypeBuilder.STRING) //
				.withLength(22) //
				.thatIsUnique(true) //
				.thatIsMandatory(true) //
				.withOwnerName(BIM_TABLE_PREFIX + className) //
				.withOwnerNamespace(BIM_SCHEMA);
		dataDefinitionLogic.createOrUpdate(attributeBuilder.build());

		attributeBuilder = Attribute.newAttribute() //
				.withName("Master") //
				.withType(Attribute.AttributeTypeBuilder.FOREIGNKEY) //
				.thatIsUnique(true) //
				.thatIsMandatory(true) //
				.withOwnerName(BIM_TABLE_PREFIX + className) //
				.withOwnerNamespace(BIM_SCHEMA) //
				.withForeignKeyDestinationClassName(className);
		dataDefinitionLogic.createOrUpdate(attributeBuilder.build());
	}

}
