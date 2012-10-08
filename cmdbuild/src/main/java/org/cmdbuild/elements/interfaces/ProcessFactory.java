package org.cmdbuild.elements.interfaces;

import org.cmdbuild.exception.NotFoundException;

public interface ProcessFactory extends CardFactory {

	Process create();
	Process get(int cardId) throws NotFoundException;

	CardQuery list();
}
