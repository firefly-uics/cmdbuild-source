package org.cmdbuild.services.soap.types;

import java.util.List;

public class CardList {
	
	private int totalRows;
	private List<Card> cards;
	
	public CardList(){}

	public int getTotalRows() {
		return totalRows;
	}

	public void setTotalRows(int totalRows) {
		this.totalRows = totalRows;
	}

	public List<Card> getCards() {
		return cards;
	}

	public void setCards(List<Card> cards) {
		this.cards = cards;
	}
	
}
