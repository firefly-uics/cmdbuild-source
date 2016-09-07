package org.cmdbuild.dao.entrytype;

import static com.google.common.base.Predicates.alwaysTrue;
import static com.google.common.base.Predicates.compose;
import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.or;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;
import static org.cmdbuild.dao.constants.Cardinality.CARDINALITY_1N;
import static org.cmdbuild.dao.constants.Cardinality.CARDINALITY_N1;
import static org.cmdbuild.dao.entrytype.Functions.anchestorOf;
import static org.cmdbuild.dao.entrytype.Functions.class1;
import static org.cmdbuild.dao.entrytype.Functions.class2;
import static org.cmdbuild.dao.entrytype.Functions.disabled1;
import static org.cmdbuild.dao.entrytype.Functions.disabled2;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.function.CMFunction.CMFunctionParameter;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingObject;
import com.google.common.collect.Iterables;

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

	public static Predicate<CMDomain> domainFor(final CMClass target) {
		return or(domain(class1(), clazz(anchestorOf(target), equalTo(true))),
				domain(class2(), clazz(anchestorOf(target), equalTo(true))));
	}

	/**
	 * @deprecated Use basic predicates instead.
	 */
	@Deprecated
	public static Predicate<CMDomain> disabledClass(final CMClass target) {
		return or(domain(disabled1(), contains(target.getName())), domain(disabled2(), contains(target.getName())));
	}

	private static class Contains implements Predicate<Iterable<String>> {

		private final String value;

		public Contains(final String value) {
			this.value = value;
		}

		@Override
		public boolean apply(final Iterable<String> input) {
			return Iterables.contains(input, value);
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof Contains)) {
				return false;
			}
			final Contains other = Contains.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.value, other.value) //
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(value) //
					.toHashCode();
		}

		@Override
		public final String toString() {
			return reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	public static Predicate<Iterable<String>> contains(final String value) {
		return new Contains(value);
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
			if (!(obj instanceof UsableForReferences)) {
				return false;
			}
			final UsableForReferences other = UsableForReferences.class.cast(obj);
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

	private static abstract class AttributePredicate<T> extends ForwardingObject implements Predicate<CMAttribute> {

		/**
		 * Usable by subclasses only.
		 */
		protected AttributePredicate() {
		}

		@Override
		protected abstract Predicate<T> delegate();

		protected abstract T value(CMAttribute input);

		@Override
		public final boolean apply(final CMAttribute input) {
			return delegate().apply(value(input));
		}

	}

	private static class Name extends AttributePredicate<String> {

		private final Predicate<String> delegate;

		public Name(final Predicate<String> delegate) {
			this.delegate = delegate;
		}

		@Override
		protected Predicate<String> delegate() {
			return delegate;
		}

		@Override
		protected String value(final CMAttribute input) {
			return input.getName();
		}

	}

	public static Predicate<CMAttribute> name(final Predicate<String> delegate) {
		return new Name(delegate);
	}

	private static class ClassOrder extends AttributePredicate<Integer> {

		private final Predicate<Integer> delegate;

		public ClassOrder(final Predicate<Integer> delegate) {
			this.delegate = delegate;
		}

		@Override
		protected Predicate<Integer> delegate() {
			return delegate;
		}

		@Override
		protected Integer value(final CMAttribute input) {
			return input.getClassOrder();
		}

	}

	public static Predicate<CMAttribute> classOrder(final Predicate<Integer> delegate) {
		return new ClassOrder(delegate);
	}

	private static class Mode extends AttributePredicate<org.cmdbuild.dao.entrytype.CMAttribute.Mode> {

		private final Predicate<org.cmdbuild.dao.entrytype.CMAttribute.Mode> delegate;

		public Mode(final Predicate<org.cmdbuild.dao.entrytype.CMAttribute.Mode> delegate) {
			this.delegate = delegate;
		}

		@Override
		protected Predicate<org.cmdbuild.dao.entrytype.CMAttribute.Mode> delegate() {
			return delegate;
		}

		@Override
		protected org.cmdbuild.dao.entrytype.CMAttribute.Mode value(final CMAttribute input) {
			return input.getMode();
		}

	}

	public static Predicate<CMAttribute> mode(final Predicate<org.cmdbuild.dao.entrytype.CMAttribute.Mode> delegate) {
		return new Mode(delegate);
	}

	private static abstract class FunctionPredicate<T> extends ForwardingObject implements Predicate<CMFunction> {

		/**
		 * Usable by subclasses only.
		 */
		protected FunctionPredicate() {
		}

		@Override
		protected abstract Predicate<T> delegate();

		protected abstract T value(CMFunction input);

		@Override
		public final boolean apply(final CMFunction input) {
			return delegate().apply(value(input));
		}

	}

	private static class FunctionId extends FunctionPredicate<Long> {

		private final Predicate<Long> delegate;

		public FunctionId(final Predicate<Long> delegate) {
			this.delegate = delegate;
		}

		@Override
		protected Predicate<Long> delegate() {
			return delegate;
		}

		@Override
		protected Long value(final CMFunction input) {
			return input.getId();
		}

	}

	public static Predicate<CMFunction> functionId(final Predicate<Long> delegate) {
		return new FunctionId(delegate);
	}

	private static abstract class FunctionParameterPredicate<T> extends ForwardingObject
			implements Predicate<CMFunctionParameter> {

		/**
		 * Usable by subclasses only.
		 */
		protected FunctionParameterPredicate() {
		}

		@Override
		protected abstract Predicate<T> delegate();

		protected abstract T value(CMFunctionParameter input);

		@Override
		public final boolean apply(final CMFunctionParameter input) {
			return delegate().apply(value(input));
		}

	}

	private static class FunctionParameterName extends FunctionParameterPredicate<String> {

		private final Predicate<String> delegate;

		public FunctionParameterName(final Predicate<String> delegate) {
			this.delegate = delegate;
		}

		@Override
		protected Predicate<String> delegate() {
			return delegate;
		}

		@Override
		protected String value(final CMFunctionParameter input) {
			return input.getName();
		}

	}

	public static Predicate<CMFunctionParameter> parameterName(final Predicate<String> delegate) {
		return new FunctionParameterName(delegate);
	}

	private static abstract class EntryTypePredicate<T> extends ForwardingObject implements Predicate<CMEntryType> {

		/**
		 * Usable by subclasses only.
		 */
		protected EntryTypePredicate() {
		}

		@Override
		protected abstract Predicate<T> delegate();

		protected abstract T value(CMEntryType input);

		@Override
		public final boolean apply(final CMEntryType input) {
			return delegate().apply(value(input));
		}

	}

	private static class IsSystem_ extends EntryTypePredicate<Boolean> {

		private final Predicate<Boolean> delegate;

		public IsSystem_(final Predicate<Boolean> delegate) {
			this.delegate = delegate;
		}

		@Override
		protected Predicate<Boolean> delegate() {
			return delegate;
		}

		@Override
		protected Boolean value(final CMEntryType input) {
			return input.isSystem();
		}

	}

	public static Predicate<CMEntryType> isSystem(final Predicate<Boolean> delegate) {
		return new IsSystem_(delegate);
	}

	private static class IsBaseClass extends EntryTypePredicate<Boolean> {

		private final Predicate<Boolean> delegate;

		public IsBaseClass(final Predicate<Boolean> delegate) {
			this.delegate = delegate;
		}

		@Override
		protected Predicate<Boolean> delegate() {
			return delegate;
		}

		@Override
		protected Boolean value(final CMEntryType input) {
			return input.isBaseClass();
		}

	}

	public static Predicate<CMEntryType> isBaseClass(final Predicate<Boolean> delegate) {
		return new IsBaseClass(delegate);
	}

	private static class HasAncestor implements Predicate<CMClass> {

		private final CMClass anchestor;

		public HasAncestor(final CMClass anchestor) {
			this.anchestor = anchestor;
		}

		@Override
		public boolean apply(final CMClass input) {
			return anchestor.isAncestorOf(input);
		}

	}

	public static Predicate<CMClass> hasAnchestor(final CMClass value) {
		return new HasAncestor(value);
	}

	/**
	 * Syntactic sugar for
	 * {@link org.cmdbuild.common.utils.guava.Predicates.compose}.
	 */
	public static <F extends CMClass, T> Predicate<F> clazz(final Function<F, T> function,
			final Predicate<T> predicate) {
		return compose(predicate, function);
	}

	/**
	 * Syntactic sugar for
	 * {@link org.cmdbuild.common.utils.guava.Predicates.compose}.
	 */
	public static <F extends CMDomain, T> Predicate<F> domain(final Function<F, T> function,
			final Predicate<T> predicate) {
		return compose(predicate, function);
	}

	private Predicates() {
		// prevents instantiation
	}

}
