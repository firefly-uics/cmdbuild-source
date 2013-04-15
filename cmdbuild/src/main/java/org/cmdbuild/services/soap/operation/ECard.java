package org.cmdbuild.services.soap.operation;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.elements.interfaces.BaseSchema.Mode;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;
import org.cmdbuild.services.soap.structure.AttributeSchema;
import org.cmdbuild.services.soap.structure.ClassSchema;

/**
 * Effective SOAP Card implementation
 */
public class ECard {

	private final UserContext userCtx;

	public ECard(final UserContext userCtx) {
		this.userCtx = userCtx;
	}

	public ClassSchema getClassSchema(final String className) {
		Log.SOAP.info(format("getting schema for class '%s'", className));
		final ClassSchema classSchema = new ClassSchema();
		final ITable table = table(className);

		classSchema.setName(table.getName());
		classSchema.setDescription(table.getDescription());
		classSchema.setSuperClass(table.isSuperClass());

		final List<AttributeSchema> attributes = new ArrayList<AttributeSchema>();
		for (final IAttribute attribute : table.getAttributes().values()) {
			if (keepAttribute(attribute)) {
				final AttributeSchema attributeSchema = attributeSchema(attribute);
				attributes.add(attributeSchema);
			}
		}
		classSchema.setAttributes(attributes);

		return classSchema;
	}

	private ITable table(final String className) {
		Log.SOAP.info(format("getting table for class '%s'", className));
		return UserOperations.from(userCtx).tables().get(className);
	}

	private boolean keepAttribute(final IAttribute attribute) {
		final boolean keep;
		if (attribute.getMode().equals(Mode.RESERVED)) {
			keep = false;
		} else if (!attribute.getStatus().isActive()) {
			keep = false;
		} else {
			keep = true;
		}
		Log.SOAP.info(format("attribute '%s' kept: %b", attribute.getName(), keep));
		return keep;
	}

	private AttributeSchema attributeSchema(final IAttribute attribute) {
		Log.SOAP.info(format("serializing attribute '%s'", attribute.getName()));
		final EAdministration administration = new EAdministration(userCtx);
		final AttributeSchema attributeSchema = administration.serialize(attribute);
		return attributeSchema;
	}

}
