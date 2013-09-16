package org.cmdbuild.bim.service;

public interface BimProject {

	String getIdentifier();

	String getLastRevisionId();

	boolean isActive();

	boolean isValid();

	final BimProject NULL_PROJECT = new BimProject() {

		@Override
		public String getLastRevisionId() {
			return "-1";
		}

		@Override
		public String getIdentifier() {
			return " -1";

		}

		@Override
		public boolean isValid() {
			return false;
		}

		@Override
		public boolean isActive() {
			return false;
		}

	};
}
