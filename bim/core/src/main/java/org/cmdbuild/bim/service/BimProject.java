package org.cmdbuild.bim.service;

import org.joda.time.DateTime;

import static org.cmdbuild.bim.utils.BimConstants.*;

public interface BimProject {
	
	String getName();

	String getIdentifier();
	
	@Deprecated
	String getLastRevisionId();

	boolean isActive();

	boolean isValid(); 
	
	String toString();

	DateTime getLastCheckin();

	void setLastCheckin(DateTime lastCheckin);

	final BimProject NULL_PROJECT = new BimProject() {
		
		@Deprecated
		@Override
		public String getLastRevisionId() {
			return INVALID_ID;
		}

		@Override
		public String getIdentifier() {
			return INVALID_ID;

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

		@Override
		public DateTime getLastCheckin() {
			return null;
		}

		@Override
		public void setLastCheckin(DateTime lastCheckin) {
			throw new UnsupportedOperationException();
		}

	};





}
