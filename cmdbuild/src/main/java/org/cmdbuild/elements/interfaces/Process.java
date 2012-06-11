package org.cmdbuild.elements.interfaces;

public interface Process extends ICard {

	public enum ProcessAttributes {
		ProcessInstanceId("ProcessCode"),
		FlowStatus("FlowStatus"),
		ActivityInstanceId("ActivityInstanceId"),
		ActivityDefinitionName("ActivityDefinitionName"),
		CurrentActivityPerformers("NextExecutor"),
		AllActivityPerformers("PrevExecutors");

		private final String columnName;

		ProcessAttributes(String columnName) {
			this.columnName = columnName;
		}

		public String toString() {
			return columnName;
		}

		public String dbColumnName() {
			return columnName;
		}
	}
}
