package org.cmdbuild.services.bim.connector;

public interface Differ {

	public void findDifferences(final DifferListener listener) throws Exception;

}
