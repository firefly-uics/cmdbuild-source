package org.cmdbuild.elements.interfaces;

import org.cmdbuild.exception.NotFoundException;

public interface CardFactory {

	ICard create();
	ICard get(int cardId) throws NotFoundException;

	CardQuery list();
}
