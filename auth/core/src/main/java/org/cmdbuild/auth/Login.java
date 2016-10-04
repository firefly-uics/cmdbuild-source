package org.cmdbuild.auth;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Login {

	public enum LoginType {

		USERNAME, EMAIL;

		private static LoginType fromLoginString(final String loginString) {
			return (loginString.contains("@")) ? LoginType.EMAIL : LoginType.USERNAME;
		}

	}

	private final String value;
	private final LoginType type;

	/**
	 * @deprecated Use {@code login(String)} instead.
	 */
	@Deprecated
	public static Login newInstance(final String loginString) {
		return login(loginString);
	}

	public static Login login(final String loginString) {
		return login(loginString, LoginType.fromLoginString(loginString));
	}

	/**
	 * @deprecated Use {@code login(String)} instead.
	 */
	@Deprecated
	public static Login newInstance(final String loginString, final LoginType type) {
		return login(loginString, type);
	}

	/**
	 * Basically used by the tests.
	 */
	public static Login login(final String loginString, final LoginType type) {
		Validate.notNull(loginString, "Null login string");
		Validate.notNull(type, "Null type");
		return new Login(loginString, type);
	}

	private Login(final String value, final LoginType type) {
		this.value = value;
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public LoginType getType() {
		return type;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Login)) {
			return false;
		}
		final Login other = Login.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.value, other.value) //
				.append(this.type, other.type) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(value) //
				.append(type) //
				.toHashCode();
	}

	@Override
	public String toString() {
		return reflectionToString(this, SHORT_PREFIX_STYLE);
	}

}
