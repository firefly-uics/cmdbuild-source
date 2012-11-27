package org.cmdbuild.logic.data;

import static org.apache.commons.lang.StringUtils.defaultIfBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.trim;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.cmdbuild.common.Builder;
import org.cmdbuild.dao.entrytype.CMClass;

public class ClassDTO {

	public static class ClassDTOBuilder implements Builder<ClassDTO> {

		private String name;
		private String description;
		private Long parentId;
		private boolean isSuperClass;
		private boolean isProcess;
		private boolean isUserStoppable;
		private boolean isHoldingHistory = true;
		private boolean isActive = true;

		private ClassDTOBuilder() {
			// use factory method
		}

		public ClassDTOBuilder withName(final String name) {
			this.name = trim(name);
			return this;
		}

		public ClassDTOBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public ClassDTOBuilder withParent(final Long parentId) {
			this.parentId = parentId;
			return this;
		}

		public ClassDTOBuilder thatIsSuperClass(boolean isSupeClass) {
			this.isSuperClass = isSupeClass;
			return this;
		}

		public ClassDTOBuilder thatIsProcess(boolean isProcess) {
			this.isProcess = isProcess;
			return this;
		}

		public ClassDTOBuilder thatIsUserStoppable(final boolean isUserStoppable) {
			this.isUserStoppable = isUserStoppable;
			return this;
		}

		public ClassDTOBuilder thatIsHoldingHistory(final boolean isHoldingHistory) {
			this.isHoldingHistory = isHoldingHistory;
			return this;
		}

		public ClassDTOBuilder thatIsActive(final boolean isActive) {
			this.isActive = isActive;
			return this;
		}

		@Override
		public ClassDTO build() {
			Validate.isTrue(isNotBlank(name));
			description = defaultIfBlank(description, name);
			Validate.isTrue(parentId == null || parentId > 0);
			return new ClassDTO(this);
		}

	}

	public static ClassDTOBuilder newClassDTO() {
		return new ClassDTOBuilder();
	}

	private final String name;
	private final String description;
	private final Long parentId;
	private final boolean isSuperClass;
	private final boolean isHoldingHistory;
	private final boolean isProcess;
	private final boolean isUserStoppable;
	private final boolean isActive;

	private final String toString;

	private ClassDTO(final ClassDTOBuilder builder) {
		this.name = builder.name;
		this.description = builder.description;
		this.parentId = builder.parentId;
		this.isSuperClass = builder.isSuperClass;
		this.isProcess = builder.isProcess;
		this.isUserStoppable = builder.isUserStoppable;
		this.isHoldingHistory = builder.isHoldingHistory;
		this.isActive = builder.isActive;

		this.toString = ToStringBuilder.reflectionToString(this);
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	/**
	 * Gets the parent's Class id.
	 * 
	 * @return {@code null} if the id is not specified, greater than zero
	 *         otherwise.
	 */
	public Long getParentId() {
		return parentId;
	}

	public boolean isSuperClass() {
		return isSuperClass;
	}

	/**
	 * Tells if the {@link CMClass} must be a process. This is useful only if
	 * the parent's id is not specified, so the base class (for processed) can
	 * be chosed.
	 * 
	 * @return {@code true} if the {@link CMClass} must be a process,
	 *         {@code false} otherwise.
	 */
	public Boolean isProcess() {
		return isProcess;
	}

	public boolean isUserStoppable() {
		return isUserStoppable;
	}

	public boolean isHoldingHistory() {
		return isHoldingHistory;
	}

	public boolean isActive() {
		return isActive;
	}

	@Override
	public String toString() {
		return toString;
	}

}
