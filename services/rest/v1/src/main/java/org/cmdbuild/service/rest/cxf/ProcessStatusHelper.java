package org.cmdbuild.service.rest.cxf;

import org.cmdbuild.service.rest.model.ProcessStatus;

import com.google.common.base.Optional;

public interface ProcessStatusHelper {

	Iterable<ProcessStatus> allValues();

	Optional<ProcessStatus> defaultValue();

}
