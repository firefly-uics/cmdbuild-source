package org.cmdbuild.dao.entrytype;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Sets.newHashSet;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.util.Collection;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Function;

public class Functions {

	private static class CMClassAllParentsFunction implements Function<CMClass, Iterable<CMClass>> {

		@Override
		public Iterable<CMClass> apply(final CMClass input) {
			final Collection<CMClass> output = newHashSet();
			for (CMClass parent = input.getParent(); parent != null; parent = parent.getParent()) {
				output.add(parent);
			}
			return output;
		}

	}

	private static class CMEntyTypeName<T extends CMEntryType> implements Function<T, String> {

		@Override
		public String apply(final T input) {
			return input.getName();
		}

	}

	private static class CMEntyTypeNames implements Function<Iterable<? extends CMEntryType>, Iterable<String>> {

		@Override
		public Iterable<String> apply(final Iterable<? extends CMEntryType> input) {
			return from(input) //
					.transform(name());
		}

	}

	private static class CMEntyTypeAttribute implements Function<String, CMAttribute> {

		private final CMEntryType entryType;

		public CMEntyTypeAttribute(final CMEntryType entryType) {
			this.entryType = entryType;
		}

		@Override
		public CMAttribute apply(final String input) {
			return (entryType == null) ? null : entryType.getAttribute(input);
		}

	}

	private static final CMClassAllParentsFunction ALL_PARENTS = new CMClassAllParentsFunction();
	private static final CMEntyTypeNames NAMES = new CMEntyTypeNames();

	public static Function<CMClass, Iterable<CMClass>> allParents() {
		return ALL_PARENTS;
	}

	public static <T extends CMEntryType> Function<T, String> name() {
		return new CMEntyTypeName<T>();
	}

	public static Function<Iterable<? extends CMEntryType>, Iterable<String>> names() {
		return NAMES;
	}

	public static Function<String, CMAttribute> attribute(final CMEntryType entryType) {
		return new CMEntyTypeAttribute(entryType);
	}

	private static final Function<CMAttribute, String> ATTRIBUTE_NAME = new Function<CMAttribute, String>() {

		@Override
		public String apply(final CMAttribute input) {
			return input.getName();
		}

	};

	public static Function<CMAttribute, String> attributeName() {
		return ATTRIBUTE_NAME;
	}

	private static class IsAnchestorOf<T extends CMClass> implements Function<T, Boolean> {

		private final T value;

		private IsAnchestorOf(final T source) {
			this.value = source;
		}

		@Override
		public Boolean apply(final T input) {
			return input.isAncestorOf(value);
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof IsAnchestorOf)) {
				return false;
			}
			final IsAnchestorOf<T> other = IsAnchestorOf.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.value, other.value).isEquals();
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

	public static <T extends CMClass> Function<T, Boolean> anchestorOf(final T value) {
		return new IsAnchestorOf<T>(value);
	}

	private static class Class1<T extends CMDomain> implements Function<T, CMClass> {

		private Class1() {
		}

		@Override
		public CMClass apply(final T input) {
			return input.getClass1();
		}

		@Override
		public final String toString() {
			return reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	public static <T extends CMDomain> Function<T, CMClass> class1() {
		return new Class1<T>();
	}

	private static class Class2<T extends CMDomain> implements Function<T, CMClass> {

		private Class2() {
		}

		@Override
		public CMClass apply(final T input) {
			return input.getClass2();
		}

		@Override
		public final String toString() {
			return reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	public static <T extends CMDomain> Function<T, CMClass> class2() {
		return new Class2<T>();
	}

	private static class Disabled1<T extends CMDomain> implements Function<T, Iterable<String>> {

		private Disabled1() {
		}

		@Override
		public Iterable<String> apply(final T input) {
			return input.getDisabled1();
		}

		@Override
		public final String toString() {
			return reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	public static <T extends CMDomain> Function<T, Iterable<String>> disabled1() {
		return new Disabled1<T>();
	}

	private static class Disabled2<T extends CMDomain> implements Function<T, Iterable<String>> {

		private Disabled2() {
		}

		@Override
		public Iterable<String> apply(final T input) {
			return input.getDisabled2();
		}

		@Override
		public final String toString() {
			return reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	public static <T extends CMDomain> Function<T, Iterable<String>> disabled2() {
		return new Disabled2<T>();
	}

	private static class Active<T extends Deactivable> implements Function<T, Boolean> {

		@Override
		public Boolean apply(final T input) {
			return input.isActive();
		}

	}

	public static <T extends Deactivable> Function<T, Boolean> active() {
		return new Active<T>();
	}

	private Functions() {
		// prevents instantiation
	}

}
