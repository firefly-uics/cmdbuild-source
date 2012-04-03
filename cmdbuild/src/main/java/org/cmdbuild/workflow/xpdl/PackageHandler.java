package org.cmdbuild.workflow.xpdl;

import javax.activation.DataSource;

import org.cmdbuild.workflow.CMProcessClass;

/**
 * Handles all the operations on workflow definition files, including
 * template generation, download and upload from/to the workflow engine.
 */
public interface PackageHandler {

	DataSource getXpdlTemplate(CMProcessClass process) throws XPDLException;
}
