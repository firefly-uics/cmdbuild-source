package org.cmdbuild.shark.toolagent;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.Filter;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.services.soap.Query;
import org.cmdbuild.shark.util.CmdbuildUtils;
import org.cmdbuild.shark.util.CmdbuildUtils.CmdbuildTableStruct;
import org.cmdbuild.workflow.type.ReferenceType;
import org.enhydra.shark.api.internal.toolagent.AppParameter;

public class SelectReferenceByReferenceToolAgent extends AbstractCmdbuildWSToolAgent {

	@Override
	protected void invokeWebService(final Private stub, final AppParameter[] params, final String toolInfoID)
			throws Exception {
		final List<Card> cards = getReferencedCards(stub, params);
		if (cards != null && cards.size() > 0) {
			System.out.println("cards selected: " + cards.size());
			final Card destCard = cards.get(0);
			final ReferenceType outputReference = CmdbuildUtils.createReferenceFromWSCard(destCard);
			setOutputValue(params, outputReference);
		} else {
			setOutputValue(params, null);
		}
	}

	private List<Card> getReferencedCards(final Private stub, final AppParameter[] params) throws Exception,
			RemoteException {
		final ReferenceType inputReference = getInputReference(params);

		final CmdbuildTableStruct origClass = CmdbuildUtils.getInstance().getStructureFromId(
				inputReference.getIdClass());
		System.out.println(String.format("Original className: %s", origClass.getName()));

		final String attributeName = getAttributeName(params);
		final String destClassName = origClass.getAttribute(attributeName).getReferenceClass();
		System.out.println(String.format("Destination className: %s", destClassName));

		final String linkedCardId = getLinkedCardId(stub, origClass.getName(), inputReference.getId(), attributeName);
		if (linkedCardId == null) {
			return null;
		}

		final List<Attribute> attributeList = new ArrayList<Attribute>();
		final Attribute id = new Attribute();
		id.setName("Id");
		attributeList.add(id);
		final Attribute idclass = new Attribute();
		idclass.setName("IdClass");
		attributeList.add(idclass);
		final Attribute description = new Attribute();
		description.setName("Description");
		attributeList.add(description);

		final Filter filter = new Filter();
		filter.setOperator("EQUALS");
		filter.setName("Id");
		filter.getValue().add(linkedCardId);

		final Query query = new Query();
		query.setFilter(filter);

		final List<Card> cards = stub.getCardList(destClassName, attributeList, query, null, null, null, null, null)
				.getCards();
		return cards;
	}

	private String getAttributeName(final AppParameter[] params) {
		final String attributeName = (String) params[2].the_value;
		System.out.println(String.format("Attribute name: %s", attributeName));
		return attributeName;
	}

	private ReferenceType getInputReference(final AppParameter[] params) {
		final ReferenceType inputReference = (ReferenceType) params[1].the_value;
		System.out.println(String.format("InputReference: %s (classid: %d, id: %d)", inputReference.getDescription(),
				inputReference.getIdClass(), inputReference.getId()));
		return inputReference;
	}

	private void setOutputValue(final AppParameter[] params, final ReferenceType outputValue) {
		params[3].the_value = outputValue;
	}

	@SuppressWarnings("serial")
	String getLinkedCardId(final Private stub, final String origClassName, final int origCardId,
			final String attributeName) throws RemoteException {

		final List<Attribute> attributeList = new ArrayList<Attribute>() {
			{
				add(new Attribute() {
					{
						setName(attributeName);
					}
				});
			}
		};

		final Query query = new Query() {
			{
				setFilter(new Filter() {
					{
						setName("Id");
						setOperator("EQUALS");
						getValue().add(String.valueOf(origCardId));
					}
				});
			}
		};

		final List<Card> cards = stub.getCardList(origClassName, attributeList, query, null, null, null, null, null)
				.getCards();

		if (cards != null && cards.size() > 0) {
			final Card origCard = cards.get(0);
			final String linkedCardId = origCard.getAttributeList().get(0).getCode();
			System.out.println(String.format("Linked card id: %s", linkedCardId));
			return linkedCardId;
		} else {
			System.out.println("Referenced card not found");
			return null;
		}
	}
}