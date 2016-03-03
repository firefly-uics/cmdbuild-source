package org.cmdbuild.logic.icon;

import java.io.IOException;
import java.util.Optional;

import javax.activation.DataHandler;

import org.cmdbuild.logic.Logic;

public interface IconsLogic extends Logic {

	Element create(Element element, DataHandler dataHandler) throws IOException;

	Iterable<Element> read();

	Optional<Element> read(Element element);

	Optional<DataHandler> download(Element element);

	void update(Element element, DataHandler dataHandler) throws IOException;

	void delete(Element element);

}
