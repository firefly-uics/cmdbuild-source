package org.cmdbuild.bim.model;

public interface Attribute {

	final Attribute NULL_ATTRIBUTE = new Attribute() {

		@Override
		public String getName() {
			return null;
		}

		@Override
		public boolean isValid() {
			return false;
		}

		@Override
		public String getValue() {
			return "";
		}

	};

	String getName();

	boolean isValid();

	String getValue();

}
