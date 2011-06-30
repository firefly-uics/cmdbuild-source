package org.cmdbuild.logic;

public interface LogicDTO {

	class Card {
		// TODO: They should be Objects for maximum compatibility
		public final Long classId;
		public final Long cardId;

		public Card(final int classId, final int cardId) {
			this.classId = Long.valueOf(classId);
			this.cardId = Long.valueOf(cardId);
		}

		@Override
		public String toString() {
			return String.format("(%s, %s)", classId, cardId);
		}
	}

	class DomainWithSource {
		public final Long domainId;
		public final String querySource;

		public DomainWithSource(final int domainId, final String querySource) {
			this.domainId = Long.valueOf(domainId);
			this.querySource = querySource;
		}

		@Override
		public String toString() {
			return String.format("%s.%s", domainId, querySource);
		}
	}
}
