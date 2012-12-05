package org.cmdbuild.logic.auth;

import org.cmdbuild.common.Builder;

public class UserDTO {

	public static class UserDTOBuilder implements Builder<UserDTO> {

		private Long userId;
		private String description;
		private String username;
		private String password;
		private String email;
		private Boolean isActive = false;
		private Long defaultGroupId;

		
		public UserDTOBuilder withUserId(Long userId) {
			this.userId = userId;
			return this;
		}
		
		public UserDTOBuilder withDescription(String description) {
			this.description = description;
			return this;
		}
		
		public UserDTOBuilder withUsername(String username) {
			this.username = username;
			return this;
		}
		
		public UserDTOBuilder withPassword(String password) {
			this.password = password;
			return this;
		}
		public UserDTOBuilder withEmail(String email) {
			this.email = email;
			return this;
		}
		
		public UserDTOBuilder setActive(Boolean isActive) {
			this.isActive = isActive;
			return this;
		}
		
		public UserDTOBuilder withDefaultGroupId(Long defaultGroupId) {
			this.defaultGroupId = defaultGroupId;
			return this;
		}
		
		@Override
		public UserDTO build() {
			return new UserDTO(this);
		}
	}

	private Long userId;
	private String description;
	private String username;
	private String password;
	private String email;
	private Boolean isActive;
	private Long defaultGroupId;

	private UserDTO(UserDTOBuilder builder) {
		this.userId = builder.userId;
		this.description = builder.description;
		this.username = builder.username;
		this.password = builder.password;
		this.email = builder.email;
		this.isActive = builder.isActive;
		this.defaultGroupId = builder.defaultGroupId;
	}

	public static UserDTOBuilder newInstance() {
		return new UserDTOBuilder();
	}

	public Long getUserId() {
		return userId;
	}

	public String getDescription() {
		return description;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getEmail() {
		return email;
	}

	public Boolean isActive() {
		return isActive;
	}

	public Long getDefaultGroupId() {
		return defaultGroupId;
	}
	
}
