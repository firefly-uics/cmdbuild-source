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

		@Override
		public String getTransactionId() {
			return "-1";
		}

		@Override
		public boolean hasOpenTransaction() {
			return false;
		}

		@Override
		public void setTransactionId(String string) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void resetTransaction() {
			// TODO Auto-generated method stub
			
		}

	};

	String getTransactionId();

	void setTransactionId(String string);

	boolean hasOpenTransaction();

	void resetTransaction();

}
