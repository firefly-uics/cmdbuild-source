package org.cmdbuild.service.rest.cxf.service;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.service.rest.model.Session;

import com.google.common.base.Optional;

public interface OperationUserStore {

	void put(Session key, OperationUser value);

	Optional<OperationUser> get(Session key);

	void remove(Session key);

}
