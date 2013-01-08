package org.cmdbuild.model.data;

import static org.apache.commons.lang.StringUtils.defaultIfBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.trim;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.cmdbuild.common.Builder;
import org.cmdbuild.dao.entrytype.CMClass;

public class Class {

	public static class ClassBuilder implements Builder<Class> {

		private String name;
		private String description;
		private Long parentId;
		private boolean isSuperClass;
		private boolean isProcess;
		private boolean isUserStoppable;
		private boolean isHoldingHistory = true;
		private boolean isActive = true;

		private ClassBuilder() {
			// use factory method
		}

		public ClassBuilder withName(final String name) {
			this.name = trim(name);
			return this;
		}

		public ClassBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public ClassBuilder withParent(final Long parentId) {
			this.parentId = parentId;
			return this;
		}

		public ClassBuilder thatIsSuperClass(final boolean isSupeClass) {
			this.isSuperClass = isSupeClass;
			return this;
		}

		public ClassBuilder thatIsProcess(final boolean isProcess) {
			this.isProcess = isProcess;
			return this;
		}

		public ClassBuilder thatIsUserStoppable(final boolean isUserStoppable) {
			this.isUserStoppable = isUserStoppable;
			return this;
		}

		public ClassBuilder thatIsHoldingHistory(final boolean isHoldingHistory) {
			this.isHoldingHistory = isHoldingHistory;
			return this;
		}

		public ClassBuilder thatIsActive(final boolean isActive) {
			this.isActive = isActive;
			return this;
		}

		@Override
		public Class build() {
			Validate.isTrue(isNotBlank(name));
			description = defaultIfBlank(description, name);
			Validate.isTrue(parentId == null || parentId > 0);
			return new Class(this);
		}

	}

	public static ClassBuilder newClass() {
		return new ClassBuilder();
	}

	private final String name;
	private final String description;
	private final Long parentId;
	private final boolean isSuperClass;
	private final boolean isHoldingHistory;
	private final boolean isProcess;
	private final boolean isUserStoppable;
	private final boolean isActive;

	private Class(final ClassBuilder builder) {
		this.name = builder.name;
		this.description = builder.description;
		this.parentId = builder.parentId;
		this.isSuperClass = builder.isSuperClass;
		this.isProcess = builder.isProcess;
		this.isUserStoppable = builder.isUserStoppable;
		this.isHoldingHistory = builder.isHoldingHistory;
		this.isActive = builder.isActive;
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
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
