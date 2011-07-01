package org.cmdbuild.logic;

public interface LogicDTO {

	class Card {
		public final Object classId;
		public final Long cardId; // TODO It should be an Object

		public Card(final Object classId, final Number cardId) {
			this.classId = classId;
			this.cardId = cardId.longValue();
		}

		@Override
		public String toString() {
			return String.format("(%s, %s)", classId, cardId);
		}
	}

	class DomainWithSource {
		public final Object domainId;
		public final String querySource;

		public DomainWithSource(final Object domainId, final String querySource) {
			this.domainId = domainId;
			this.querySource = querySource;
		}

		@Override
		public String toString() {
			return String.format("%s.%s", domainId, querySource);
		}

		public static DomainWithSource create(final Object domainId, final String querySource) {
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
