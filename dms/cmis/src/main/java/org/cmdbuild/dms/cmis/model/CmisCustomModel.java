package org.cmdbuild.dms.cmis.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "model")
@XmlAccessorType(XmlAccessType.FIELD)
public class CmisCustomModel {

	@XmlAccessorType(XmlAccessType.FIELD)
	public static class DocumentType {

		@XmlAttribute(name = "name")
		private String name;

		@XmlElement(name = "group")
		private List<MetadataGroup> groupList;

		public String getName() {
			return name;
		}

		void setName(final String name) {
			this.name = name;
		}

		public List<MetadataGroup> getGroupList() {
			return groupList;
		}

		void setGroupList(final List<MetadataGroup> groupList) {
			this.groupList = groupList;
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	public static class MetadataGroup {

		@XmlAttribute(name = "name")
		private String name;

		@XmlAttribute(name = "secondary-type")
		private String cmisSecondaryTypeId;

		@XmlElement(name = "metadata")
		private List<Metadata> metadataList;

		public String getName() {
			return name;
		}

		void setName(final String name) {
			this.name = name;
		}

		public String getCmisSecondaryTypeId() {
			return cmisSecondaryTypeId;
		}

		void setCmisSecondaryTypeId(final String cmisSecondaryTypeId) {
			this.cmisSecondaryTypeId = cmisSecondaryTypeId;
		}

		public List<Metadata> getMetadataList() {
			return metadataList;
		}

		void setMetadataList(final List<Metadata> metadataList) {
			this.metadataList = metadataList;
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Metadata {
		@XmlAttribute(name = "name")
		private String name;

		@XmlAttribute(name = "property")
		private String cmisPropertyId;

		public String getName() {
			return name;
		}

		void setName(final String name) {
			this.name = name;
		}

		public String getCmisPropertyId() {
			return cmisPropertyId;
		}

		void setCmisPropertyId(final String cmisPropertyId) {
			this.cmisPropertyId = cmisPropertyId;
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Converter {

		@XmlAttribute(name = "type")
		private String type;

		@XmlElement(name = "property")
		private List<String> cmisPropertyId;

		public String getType() {
			return type;
		}

		void setType(final String type) {
			this.type = type;
		}

		public List<String> getCmisPropertyId() {
			return cmisPropertyId;
		}

		void setCmisPropertyId(final List<String> cmisPropertyId) {
			this.cmisPropertyId = cmisPropertyId;
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Parameter {

		@XmlAttribute(name = "name")
		private String name;

		@XmlAttribute(name = "value")
		private String value;

		public String getName() {
			return name;
		}

		void setName(final String name) {
			this.name = name;
		}

		public String getValue() {
			return value;
		}

		void setValue(final String value) {
			this.value = value;
		}
	}

	@XmlAttribute(name = "type")
	private String cmisType;

	@XmlList
	@XmlAttribute(name = "secondary-types")
	private List<String> secondaryTypeList;

	@XmlElementWrapper(name = "document-types")
	@XmlElement(name = "document-type")
	private List<DocumentType> documentTypeList;

	@XmlElementWrapper(name = "property-converters")
	@XmlElement(name = "converter")
	private List<Converter> converterList;

	@XmlElement(name = "author")
	private String author;

	@XmlElement(name = "category")
	private String category;

	@XmlElement(name = "description")
	private String description;

	@XmlElementWrapper(name = "session-parameters")
	@XmlElement(name = "parameter")
	private List<Parameter> sessionParameters;

	public String getCmisType() {
		return cmisType;
	}

	void setCmisType(final String cmisType) {
		this.cmisType = cmisType;
	}

	public List<String> getSecondaryTypeList() {
		return secondaryTypeList;
	}

	void setSecondaryTypeList(final List<String> secondaryTypeList) {
		this.secondaryTypeList = secondaryTypeList;
	}

	public List<DocumentType> getDocumentTypeList() {
		return documentTypeList;
	}

	void setDocumentTypeList(final List<DocumentType> documentTypeList) {
		this.documentTypeList = documentTypeList;
	}

	public List<Converter> getConverterList() {
		return converterList;
	}

	void setConverterList(final List<Converter> converterList) {
		this.converterList = converterList;
	}

	public String getAuthor() {
		return author;
	}

	void setAuthor(final String author) {
		this.author = author;
	}

	public String getCategory() {
		return category;
	}

	void setCategory(final String category) {
		this.category = category;
	}

	public String getDescription() {
		return description;
	}

	void setDescription(final String description) {
		this.description = description;
	}

	public List<Parameter> getSessionParameters() {
		return sessionParameters;
	}

	void setSessionParameters(final List<Parameter> sessionParameters) {
		this.sessionParameters = sessionParameters;
	}
}
