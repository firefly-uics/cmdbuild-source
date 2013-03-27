package org.cmdbuild.logic.auth;

import org.apache.commons.lang.Validate;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.common.Builder;

public class LoginDTO {

	public static class LoginDTOBuilder implements Builder<LoginDTO> {

		private String loginString;
		private String unencryptedPassword;
		private String loginGroupName;
		private UserStore userStore;

		/**
		 * 
		 * @param loginString
		 *            could be the either the username or the email
		 * @return
		 */
		public LoginDTOBuilder withLoginString(final String loginString) {
			this.loginString = loginString;
			return this;
		}

		public LoginDTOBuilder withPassword(final String unencryptedPassword) {
			this.unencryptedPassword = unencryptedPassword;
			return this;
		}

		public LoginDTOBuilder withGroupName(final String loginGroupName) {
			this.loginGroupName = loginGroupName;
			return this;
		}

		public LoginDTOBuilder withUserStore(final UserStore userStore) {
			this.userStore = userStore;
			return this;
		}

		@Override
		public LoginDTO build() {
			Validate.notNull(loginString);
			Validate.notNull(unencryptedPassword);
			Validate.notNull(userStore);
			return new LoginDTO(this);
		}

	}

	private final String loginString;
	private final String unencryptedPassword;
	private final String loginGroupName;
	private final UserStore userStore;

	private LoginDTO(final LoginDTOBuilder builder) {
		this.loginString = builder.loginString;
		this.unencryptedPassword = builder.unencryptedPassword;
		this.loginGroupName = builder.loginGroupName;
		this.userStore = builder.userStore;
	}

	public static LoginDTOBuilder newInstanceBuilder() {
		return new LoginDTOBuilder();
	}

	public String getLoginString() {
		return loginString;
	}

	public String getPassword() {
		return unencryptedPassword;
	}

	public String getLoginGroupName() {
		return loginGroupName;
	}

	public UserStore getUserStore() {
		return userStore;
	}

}
