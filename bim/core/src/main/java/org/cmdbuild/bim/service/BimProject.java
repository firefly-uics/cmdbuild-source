package org.cmdbuild.bim.service;

public interface BimProject {
	
	String getName();

	String getIdentifier();
	
	String getLastRevisionId();

	boolean isActive();

	boolean isValid(); 
	
	String toString();

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
		
		@Override
		public String toString() {
			return "NULL_PROJECT";
		}

		@Override
		public String getName() {
			return "NULL_PROJECT";
		}

	};

}
