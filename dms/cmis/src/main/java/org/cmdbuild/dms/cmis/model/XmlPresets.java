package org.cmdbuild.dms.cmis.model;

import static javax.xml.bind.annotation.XmlAccessType.FIELD;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(FIELD)
public class XmlPresets {

	@XmlElementWrapper(name = "models")
	@XmlElement(name = "model")
	private List<XmlModel> models;

	public List<XmlModel> getModels() {
		return models;
	}

	public void setModels(final List<XmlModel> models) {
		this.models = models;
	}

}
