package org.cmdbuild.shark.toolagent;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.EMPTY;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.api.fluent.CardDescriptor;
import org.cmdbuild.workflow.type.ReferenceType;

public class ManageRelationToolAgent extends AbstractConditionalToolAgent {

	private static enum Operation {

		CREATION, DELETION, SELECTION;

		private static final String CREATE_PREFIX = "create";
		private static final String DELETE_PREFIX = "delete";
		private static final String SELECT_PREFIX = "select";

		public static Operation from(final String id) {
			final Operation operation;
			if (id.startsWith(CREATE_PREFIX)) {
				operation = CREATION;
			} else if (id.startsWith(DELETE_PREFIX)) {
				operation = DELETION;
			} else if (id.startsWith(SELECT_PREFIX)) {
				operation = SELECTION;
			} else {
				throw new IllegalArgumentException("invalid id");
			}
			return operation;
		}
	}

	private static final String SIDE_1 = "1";
	private static final String SIDE_2 = "2";

	private static final String DOMAIN_NAME = "DomainName";
	private static final String CLASS_NAME_BASE = "ClassName";
	private static final String CARD_ID_BASE = "CardId";
	private static final String OBJ_ID_BASE = "ObjId";
	private static final String OBJ_REFERENCE_BASE = "ObjReference";
	private static final String REF_BASE = "Ref";
	private static final String DONE = "Done";
	private static final String REFARRAY = "RefArray";

	private static final boolean RESULT_ALWAYS_TRUE_OR_THROWS = true;

	@Override
	protected void innerInvoke() throws Exception {
		final String domainName = getParameterValue(DOMAIN_NAME);
		final Operation operation = Operation.from(getId());
		final String outName;
		final Object outValue;
		switch (operation) {
		case CREATION: {
			final CardRef card1 = getCard1();
			final CardRef card2 = getCard2();
			getFluentApi().newRelation(domainName) //
					.withCard1(card1.className, card1.cardId) //
					.withCard2(card2.className, card2.cardId) //
					.create();
			outName = DONE;
			outValue = RESULT_ALWAYS_TRUE_OR_THROWS;
			break;
		}

		case DELETION: {
			final CardRef card1 = getCard1();
			final CardRef card2 = getCard2();
			getFluentApi().existingRelation(domainName) //
					.withCard1(card1.className, card1.cardId) //
					.withCard2(card2.className, card2.cardId) //
					.delete();
			outName = DONE;
			outValue = RESULT_ALWAYS_TRUE_OR_THROWS;
			break;
		}

		case SELECTION: {
			final CardRef card = getCard();
			final List<CardDescriptor> descriptors = getFluentApi().queryRelations(card.className, card.cardId) //
					.withDomain(domainName) //
					.fetch();
			final ReferenceType[] referenceTypes = referenceTypeFor(descriptors);
			outName = REFARRAY;
			outValue = referenceTypes;
			break;
		}

		default: {
			final String message = format("illegal operation '%s'", operation);
			throw new IllegalArgumentException(message);
		}
		}

		setParameterValue(outName, outValue);
	}

	private CardRef getCard() {
		return getCard(CLASS_NAME_BASE, CARD_ID_BASE, REF_BASE, EMPTY);
	}

	private CardRef getCard1() {
		return getCard(CLASS_NAME_BASE, OBJ_ID_BASE, OBJ_REFERENCE_BASE, SIDE_1);
	}

	private CardRef getCard2() {
		return getCard(CLASS_NAME_BASE, OBJ_ID_BASE, OBJ_REFERENCE_BASE, SIDE_2);
	}

	private CardRef getCard(final String classNameBase, final String cardIdBase, final String referenceBase,
			final String suffix) {
		final String className;
		final int cardId;
		if (hasParameter(classNameBase + suffix)) {
			className = getParameterValue(classNameBase + suffix);
			final Long objId = getParameterValue(cardIdBase + suffix);
			cardId = objId.intValue();
		} else {
			final ReferenceType objReference = getParameterValue(referenceBase + suffix);
			className = getSchemaApi().findClass(objReference.getIdClass()).getName();
			cardId = objReference.getId();
		}
		return new CardRef(className, cardId);
	}

	private ReferenceType[] referenceTypeFor(final List<CardDescriptor> descriptors) {
		final List<ReferenceType> referenceTypes = new ArrayList<ReferenceType>();
		for (final CardDescriptor descriptor : descriptors) {
			referenceTypes.add(referenceTypeFor(descriptor));
		}
		return referenceTypes.toArray(new ReferenceType[referenceTypes.size()]);
	}

	private ReferenceType referenceTypeFor(final CardDescriptor descriptor) {
		final int id = descriptor.getId();
		final int idClass = idClassFrom(descriptor);
		final String description = EMPTY;
		return new ReferenceType(id, idClass, description);
	}

	private int idClassFrom(final CardDescriptor descriptor) {
		return getSchemaApi().findClass(descriptor.getClassName()).getId();
	}

}
