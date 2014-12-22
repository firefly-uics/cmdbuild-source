package org.cmdbuild.dao.entrytype;

import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;

import com.google.common.base.Predicate;

public class Predicates {

	private static class AttributeTypeIsInstanceOf implements Predicate<CMAttribute> {

		private final Class<? extends CMAttributeType<?>> clazz;

		private AttributeTypeIsInstanceOf(final Class<? extends CMAttributeType<?>> clazz) {
			this.clazz = clazz;
		}

		@Override
		public boolean apply(final CMAttribute input) {
			return (input == null) ? false : clazz.isInstance(input.getType());
		};

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof AttributeTypeIsInstanceOf)) {
				return false;
			}
			final AttributeTypeIsInstanceOf other = AttributeTypeIsInstanceOf.class.cast(obj);
			return (this.clazz == other.clazz);
		}

		@Override
		public int hashCode() {
			return clazz.hashCode();
		}

		@Override
		public String toString() {
			return this.clazz.getSimpleName() + "(" + clazz.getName() + ")";
		}

	}

	public static Predicate<CMAttribute> attributeTypeInstanceOf(final Class<? extends CMAttributeType<?>> clazz) {
		return new AttributeTypeIsInstanceOf(clazz);
	}

	private Predicates() {
		// prevents instantiation
	}

}
