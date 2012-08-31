package org.cmdbuild.logic;

public interface LogicDTO {

	class Card {
		public final Long classId;
		public final Long cardId;

		public Card(final long classId, final long cardId) {
			this.classId = classId;
			this.cardId = cardId;
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
