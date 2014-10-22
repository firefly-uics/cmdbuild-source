package org.cmdbuild.service.rest.model;

import static org.cmdbuild.service.rest.constants.Serialization.PASSWORD;
import static org.cmdbuild.service.rest.constants.Serialization.ROLE;
import static org.cmdbuild.service.rest.constants.Serialization.SESSION;
import static org.cmdbuild.service.rest.constants.Serialization.USERNAME;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlRootElement(name = SESSION)
public class Session extends ModelWithId<String> {

	private String username;
	private String password;
	private String role;

	Session() {
		// package visibility
	}

	@XmlElement(name = USERNAME)
	public String getUsername() {
		return username;
	}

	void setUsername(final String username) {
		this.username = username;
	}

	@XmlElement(name = PASSWORD)
	public String getPassword() {
		return password;
	}

	void setPassword(final String password) {
		this.password = password;
	}

	@XmlElement(name = ROLE)
	public String getRole() {
		return role;
	}

	void setRole(final String role) {
		this.role = role;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Session)) {
			return false;
		}
		final Session other = Session.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.getId(), other.getId()) //
				.append(this.username, other.username) //
				.append(this.password, other.password) //
				.append(this.role, other.role) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(this.getId()) //
				.append(this.username) //
				.append(this.password) //
				.append(this.role) //
				.toHashCode();
	}

}