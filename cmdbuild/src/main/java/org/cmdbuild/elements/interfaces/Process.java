package org.cmdbuild.elements.interfaces;


public interface Process extends ICard {

	public enum ProcessAttributes {
		NextExecutor("NextExecutor"),
		FlowStatus("FlowStatus");

		private final String columnName;

		ProcessAttributes(String columnName) {
			this.columnName = columnName;
		}

		public String toString() {
			return columnName;
		}
	}
}
