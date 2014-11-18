package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static org.cmdbuild.service.rest.model.Models.newAttribute;
import static org.cmdbuild.service.rest.model.Models.newMetadata;
import static org.cmdbuild.service.rest.model.Models.newResponseMultiple;

import java.util.Map;

import org.cmdbuild.dms.DocumentTypeDefinition;
import org.cmdbuild.dms.MetadataDefinition;
import org.cmdbuild.dms.MetadataGroupDefinition;
import org.cmdbuild.logic.dms.DmsLogic;
import org.cmdbuild.service.rest.AttachmentsConfiguration;
import org.cmdbuild.service.rest.cxf.serialization.ToAttachmentCategory;
import org.cmdbuild.service.rest.model.AttachmentCategory;
import org.cmdbuild.service.rest.model.Attribute;
import org.cmdbuild.service.rest.model.AttributeType;
import org.cmdbuild.service.rest.model.Models.AttributeBuilder;
import org.cmdbuild.service.rest.model.ResponseMultiple;

public class CxfAttachmentsConfiguration implements AttachmentsConfiguration {

	private static final ToAttachmentCategory TO_ATTACHMENT_CATEGORY = new ToAttachmentCategory();

	private final DmsLogic dmsLogic;

	public CxfAttachmentsConfiguration(final DmsLogic dmsLogic) {
		this.dmsLogic = dmsLogic;
	}

	@Override
	public ResponseMultiple<AttachmentCategory> readCategories() {
		final Iterable<DocumentTypeDefinition> elements = dmsLogic.getConfiguredCategoryDefinitions();
		return newResponseMultiple(AttachmentCategory.class) //
				.withElements(from(elements) //
						.transform(TO_ATTACHMENT_CATEGORY)) //
				.withMetadata(newMetadata() //
						.withTotal(Long.valueOf(size(elements))).build()) //
				.build();
	}

	@Override
	public ResponseMultiple<Attribute> readCategoryAttributes(final String categoryId) {
		final Map<String, Attribute> elements = newLinkedHashMap();
		final DocumentTypeDefinition definition = dmsLogic.getCategoryDefinition(categoryId);
		for (final MetadataGroupDefinition groupDefinition : definition.getMetadataGroupDefinitions()) {
			for (final MetadataDefinition metadataDefinition : groupDefinition.getMetadataDefinitions()) {
				final AttributeBuilder attribute = newAttribute() //
						.withGroup(groupDefinition.getName()) //
						.withId(metadataDefinition.getName()) //
						.withDescription(metadataDefinition.getDescription()) //
						.thatIsMandatory(metadataDefinition.isMandatory()) //
						.thatIsActive(true) //
						/*
						 * custom attributes are never first
						 */
						.withIndex(Long.valueOf(elements.size() + 1));
				switch (metadataDefinition.getType()) {
				case TEXT:
					attribute.withType(AttributeType.TEXT.asString());
					break;
				case INTEGER:
					attribute.withType(AttributeType.INTEGER.asString());
					break;
				case FLOAT:
					attribute.withType(AttributeType.DOUBLE.asString());
					break;
				case DATE:
					attribute.withType(AttributeType.DATE.asString());
					break;
				case DATETIME:
					attribute.withType(AttributeType.DATE_TIME.asString());
					break;
				case BOOLEAN:
					attribute.withType(AttributeType.BOOLEAN.asString());
					break;
				case LIST:
					attribute.withType(AttributeType.LIST.asString()) //
							.withValues(metadataDefinition.getListValues());
					break;
				default:
					attribute.withType(AttributeType.TEXT.asString());
					break;
				}
				elements.put(metadataDefinition.getName(), attribute.build());
			}
		}
		/*
		 * added at last because it should override any custom attribute with
		 * the same (reserved) name
		 */
		elements.put("Description", newAttribute() //
				.withId("Description") //
				.withDescription("Description") //
				.withType(AttributeType.TEXT.asString()) //
				/*
				 * always first
				 */
				.withIndex(Long.valueOf(0L)) //
				.build());
		return newResponseMultiple(Attribute.class) //
				.withElements(elements.values()) //
				.withMetadata(newMetadata() //
						.withTotal(Long.valueOf(elements.size())) //
						.build()) //
				.build();
	}
}
