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

		public DomainWithSource(final Long domainId, final String querySource) {
			this.domainId = domainId;
			this.querySource = querySource;
		}

		@Override
		public String toString() {
			return String.format("%s.%s", domainId, querySource);
		}

		public static DomainWithSource create(final Long domainId, final String querySource) {
			final DomainWithSource dom;
			if (domainId != null && querySource != null) {
				dom = new DomainWithSource(domainId, querySource);
			} else {
				dom = null;
			}
			return dom;
		}
	}
}
