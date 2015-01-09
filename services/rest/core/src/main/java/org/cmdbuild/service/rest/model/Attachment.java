package org.cmdbuild.service.rest.model;

import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.service.rest.constants.Serialization.ATTACHMENT;
import static org.cmdbuild.service.rest.constants.Serialization.AUTHOR;
import static org.cmdbuild.service.rest.constants.Serialization.CATEGORY;
import static org.cmdbuild.service.rest.constants.Serialization.CREATED;
import static org.cmdbuild.service.rest.constants.Serialization.DESCRIPTION;
import static org.cmdbuild.service.rest.constants.Serialization.METADATA;
import static org.cmdbuild.service.rest.constants.Serialization.MODIFIED;
import static org.cmdbuild.service.rest.constants.Serialization.NAME;
import static org.cmdbuild.service.rest.constants.Serialization.VERSION;

import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.cmdbuild.service.rest.model.adapter.AttachmentAdapter;

@XmlRootElement(name = ATTACHMENT)
@XmlJavaTypeAdapter(AttachmentAdapter.class)
public class Attachment extends ModelWithId<String> {

	private static final Map<String, Object> NO_METADATA = emptyMap();

	private String name;
	private String category;
	private String description;
	private String version;
	private String author;
	private String created;
	private String modified;
	private Map<String, Object> metadata;

	Attachment() {
		// package visibility
	}

	@XmlAttribute(name = NAME)
	public String getName() {
		return name;
	}

	void setName(final String name) {
		this.name = name;
	}

	@XmlAttribute(name = CATEGORY)
	public String getCategory() {
		return category;
	}

	void setCategory(final String category) {
		this.category = category;
	}

	@XmlAttribute(name = DESCRIPTION)
	public String getDescription() {
		return description;
	}

	void setDescription(final String description) {
		this.description = description;
	}

	@XmlAttribute(name = VERSION)
	public String getVersion() {
		return version;
	}

	void setVersion(final String version) {
		this.version = version;
	}

	@XmlAttribute(name = AUTHOR)
	public String getAuthor() {
		return author;
	}

	void setAuthor(final String author) {
		this.author = author;
	}

	@XmlAttribute(name = CREATED)
	public String getCreated() {
		return created;
	}

	void setCreated(final String created) {
		this.created = created;
	}

	@XmlAttribute(name = MODIFIED)
	public String getModified() {
		return modified;
	}

	void setModified(final String modified) {
		this.modified = modified;
	}

	@XmlAttribute(name = METADATA)
	public Map<String, Object> getMetadata() {
		return defaultIfNull(metadata, NO_METADATA);
	}

	void setMetadata(final Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Attachment)) {
			return false;
		}

		final Attachment other = Attachment.class.cast(obj);

		return new EqualsBuilder() //
				.append(this.getId(), other.getId()) //
				.append(this.name, other.name) //
				.append(this.category, other.category) //
				.append(this.description, other.description) //
				.append(this.version, other.version) //
				.append(this.author, other.author) //
				.append(this.created, other.created) //
				.append(this.modified, other.modified) //
				.append(this.metadata, other.metadata) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(this.getId()) //
				.append(this.name) //
				.append(this.category) //
				.append(this.description) //
				.append(this.version) //
				.append(this.author) //
				.append(this.created) //
				.append(this.modified) //
				.append(this.metadata) //
				.toHashCode();
	}

}
