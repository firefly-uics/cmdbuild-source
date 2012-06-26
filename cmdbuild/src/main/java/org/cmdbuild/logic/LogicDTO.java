package org.cmdbuild.logic;

public interface LogicDTO {

	class Card {
		public final Object classNameOrId;
		public final Long cardId; // TODO It should be an Object

		public Card(final Object classNameOrId, final Number cardId) {
			this.classNameOrId = classNameOrId;
			this.cardId = cardId.longValue();
		}

		@Override
		public String toString() {
			return String.format("(%s, %s)", classNameOrId, cardId);
		}
	}

	class DomainWithSource {
		public final Object domainNameOrId;
		public final String querySource;

		public DomainWithSource(final Object domainNameOrId, final String querySource) {
			this.domainNameOrId = domainNameOrId;
			this.querySource = querySource;
		}

		@Override
		public String toString() {
			return String.format("%s.%s", domainNameOrId, querySource);
		}

		public static DomainWithSource create(final Object domainNameOrId, final String querySource) {
			final DomainWithSource dom;
			if (domainNameOrId != null && querySource != null) {
				dom = new DomainWithSource(domainNameOrId, querySource);
			} else {
				dom = null;
			}
			return dom;
		}
	}
}
