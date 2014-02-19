package org.cmdbuild.bim.service;

import java.util.Date;

public interface BimRevision {

	String getIdentifier();

	String getProjectId();

	Date getDate();

	String getUser();

}
