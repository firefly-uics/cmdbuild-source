package org.cmdbuild.elements.filters;

import java.util.Iterator;
import java.util.Map;

import org.cmdbuild.dao.backend.postgresql.QueryComponents.QueryAttributeDescriptor;
import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;

public class CardFilter extends AbstractFilter {
	
	private static final long serialVersionUID = 1L;

	private Iterable<ICard> cards;
	ITable table;
	AttributeFilterType filterType;

	public CardFilter(ITable table, Iterable<ICard> cards) {
		this(table, cards, AttributeFilterType.IN);
	}

	public CardFilter(ITable table, Iterable<ICard> cards, AttributeFilterType filterType) {
		this.cards = cards;
		this.table = table;
		this.filterType = filterType;
	}

	public String toString(Map<String, QueryAttributeDescriptor> queryMapping) {
		if(queryMapping != null && queryMapping.containsKey(ICard.CardAttributes.ClassId.toString())
				&& queryMapping.containsKey(ICard.CardAttributes.Id.toString()))
			return toString(queryMapping.get(ICard.CardAttributes.ClassId.toString()).getValueName(),
					queryMapping.get(ICard.CardAttributes.Id.toString()).getValueName());
		else
			return toString("\"" + table.getDBName() + "\".\"" +  ICard.CardAttributes.ClassId + "\"",
					"\"" + table.getDBName() + "\".\"" +  ICard.CardAttributes.Id + "\"");
	}

	private String toString(String classIdFullName, String idFullName) {
		return "(" + classIdFullName + ", " + idFullName + ") "+filterType.operatorMultiple()+" (" + valuesToString() + ")";
	}

	private String valuesToString() {
		StringBuffer buffer = new StringBuffer();
        for (Iterator<ICard> i = cards.iterator(); i.hasNext(); ) {
        	ICard card = i.next();
        	buffer.append("(")
        		.append(card.getIdClass())
        		.append(",")
        		.append(card.getId())
        		.append(")");
        	if (i.hasNext())
        		buffer.append(",");
        }
		return buffer.toString();
	}
}
