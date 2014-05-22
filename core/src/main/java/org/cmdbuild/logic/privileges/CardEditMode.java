package org.cmdbuild.logic.privileges;

import static org.apache.commons.lang.StringUtils.isBlank;

import com.google.common.base.Function;

public class CardEditMode {

	private static final String CARD_EDIT_MODE_PERSISTENCE_FORMAT = "Create=%b,Modify=%b,Clone=%b,Remove=%b";
	private final boolean allowCreate;
	private final boolean allowUpdate;
	private final boolean allowClone;
	private final boolean allowRemove;

	public static final CardEditMode ALLOW_ALL = CardEditMode.newInstance().build();

	public CardEditMode(final Builder builder) {
		this.allowClone = builder.allowClone;
		this.allowCreate = builder.allowCreate;
		this.allowRemove = builder.allowRemove;
		this.allowUpdate = builder.allowUpdate;
	}

	public boolean isAllowCreate() {
		return allowCreate;
	}

	public boolean isAllowUpdate() {
		return allowUpdate;
	}

	public boolean isAllowClone() {
		return allowClone;
	}

	public boolean isAllowRemove() {
		return allowRemove;
	}

	public static Builder newInstance() {
		return new Builder();
	}

	public static class Builder implements org.cmdbuild.common.Builder<CardEditMode> {

		private boolean allowCreate = true;
		private boolean allowUpdate = true;
		private boolean allowClone = true;
		private boolean allowRemove = true;

		@Override
		public CardEditMode build() {
			return new CardEditMode(this);
		}

		public Builder isCreateAllowed(final boolean allowCreate) {
			this.allowCreate = allowCreate;
			return this;
		}

		public Builder isUpdateAllowed(final boolean allowUpdate) {
			this.allowUpdate = allowUpdate;
			return this;
		}

		public Builder isCloneAllowed(final boolean allowClone) {
			this.allowClone = allowClone;
			return this;
		}

		public Builder isDeleteAllowed(final boolean allowRemove) {
			this.allowRemove = allowRemove;
			return this;
		}
	}

	public static Function<CardEditMode, String> LOGIC_TO_PERSISTENCE = new Function<CardEditMode, String>() {
		@Override
		public String apply(final CardEditMode input) {
			String persistenceString = null;
			if (input != null) {
				persistenceString = String.format(CARD_EDIT_MODE_PERSISTENCE_FORMAT, //
						input.isAllowCreate(), //
						input.isAllowUpdate(), //
						input.isAllowClone(), //
						input.isAllowRemove());
			}
			return persistenceString;
		}
	};

	public static Function<String, CardEditMode> PERSISTENCE_TO_LOGIC = new Function<String, CardEditMode>() {
		@Override
		public CardEditMode apply(final String input) {
			CardEditMode cardEditMode = CardEditMode.ALLOW_ALL;
			if (!isBlank(input)) {
				final String[] modes = input.split(",");
				cardEditMode = CardEditMode.newInstance() //
						.isCreateAllowed(Boolean.parseBoolean(modes[0].split("=")[1])) //
						.isUpdateAllowed(Boolean.parseBoolean(modes[1].split("=")[1])) //
						.isCloneAllowed(Boolean.parseBoolean(modes[2].split("=")[1])) //
						.isDeleteAllowed(Boolean.parseBoolean(modes[3].split("=")[1])) //
						.build();
			}
			return cardEditMode;
		}
	};

}
