package org.cmdbuild.dao.entrytype;

import static com.google.common.base.Predicates.alwaysTrue;
import static com.google.common.collect.Iterables.contains;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;
import static org.cmdbuild.dao.constants.Cardinality.CARDINALITY_1N;
import static org.cmdbuild.dao.constants.Cardinality.CARDINALITY_N1;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
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

	private static class DomainForClass implements Predicate<CMDomain> {

		private final CMClass target;

		private DomainForClass(final CMClass target) {
			this.target = target;
		}

		@Override
		public boolean apply(final CMDomain input) {
			return input.getClass1().isAncestorOf(target) || input.getClass2().isAncestorOf(target);
		};

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof DomainForClass)) {
				return false;
			}
			final DomainForClass other = DomainForClass.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.target, other.target) //
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(target) //
					.toHashCode();
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	public static Predicate<CMDomain> domainFor(final CMClass target) {
		return new DomainForClass(target);
	}

	private static class DisabledClass implements Predicate<CMDomain> {

		private final String target;

		private DisabledClass(final CMClass target) {
			this.target = target.getName();
		}

		@Override
		public boolean apply(final CMDomain input) {
			return contains(input.getDisabled1(), target) || contains(input.getDisabled2(), target);
		};

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof DisabledClass)) {
				return false;
			}
			final DisabledClass other = DisabledClass.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.target, other.target) //
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(target) //
					.toHashCode();
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	public static Predicate<CMDomain> disabledClass(final CMClass target) {
		return new DisabledClass(target);
	}

	private static class UsableForReferences implements Predicate<CMDomain> {

		private final CMClass target;

		private UsableForReferences(final CMClass target) {
			this.target = target;
		}

		@Override
		public boolean apply(final CMDomain input) {
			final String cardinality = input.getCardinality();
			if (cardinality.equals(CARDINALITY_1N.value()) && input.getClass2().isAncestorOf(target)) {
				return true;
			} else if (cardinality.equals(CARDINALITY_N1.value()) && input.getClass1().isAncestorOf(target)) {
				return true;
			}
			return false;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof DomainForClass)) {
				return false;
			}
			final DomainForClass other = DomainForClass.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.target, other.target) //
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(target) //
					.toHashCode();
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	public static Predicate<CMDomain> usableForReferences(final CMClass target) {
		return new UsableForReferences(target);
	}

	private static class IsSystem<T extends CMEntryType> implements Predicate<T> {

		private final Class<? extends CMEntryType> type;

		private IsSystem(final Class<? extends CMEntryType> type) {
			this.type = type;
		}

		@Override
		public boolean apply(final T input) {
			return input.isSystem();
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof IsSystem)) {
				return false;
			}
			final IsSystem<?> other = IsSystem.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.type, other.type) //
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(type) //
					.toHashCode();
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	public static Predicate<CMDomain> isSystem(final Class<? extends CMEntryType> type) {
		return new IsSystem(type);
	}

	public static Predicate<CMDomain> allDomains() {
		return alwaysTrue();
	}

	private Predicates() {
		// prevents instantiation
	}

}
