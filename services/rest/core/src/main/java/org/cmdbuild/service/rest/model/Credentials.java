package org.cmdbuild.service.rest.model;

import static org.cmdbuild.service.rest.constants.Serialization.CREDENTIALS;
import static org.cmdbuild.service.rest.constants.Serialization.GROUP;
import static org.cmdbuild.service.rest.constants.Serialization.PASSWORD;
import static org.cmdbuild.service.rest.constants.Serialization.TOKEN;
import static org.cmdbuild.service.rest.constants.Serialization.USERNAME;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlRootElement(name = CREDENTIALS)
public class Credentials extends Model {

	private String token;
	private String username;
	private String password;
	private String group;

	Credentials() {
		// package visibility
	}

	@XmlElement(name = TOKEN)
	public String getToken() {
		return token;
	}

	void setToken(final String token) {
		this.token = token;
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

	@XmlElement(name = GROUP)
	public String getGroup() {
		return group;
	}

	void setGroup(final String group) {
		this.group = group;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Credentials)) {
			return false;
		}
		final Credentials other = Credentials.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.token, other.token) //
				.append(this.username, other.username) //
				.append(this.password, other.password) //
				.append(this.group, other.group) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(this.token) //
				.append(this.username) //
				.append(this.password) //
				.append(this.group) //
				.toHashCode();
	}

}
