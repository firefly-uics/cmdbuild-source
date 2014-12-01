package org.cmdbuild.service.rest.model;

import static org.cmdbuild.service.rest.constants.Serialization.ATTACHMENT;
import static org.cmdbuild.service.rest.constants.Serialization.AUTHOR;
import static org.cmdbuild.service.rest.constants.Serialization.CATEGORY;
import static org.cmdbuild.service.rest.constants.Serialization.CREATED;
import static org.cmdbuild.service.rest.constants.Serialization.DESCRIPTION;
import static org.cmdbuild.service.rest.constants.Serialization.MODIFIED;
import static org.cmdbuild.service.rest.constants.Serialization.NAME;
import static org.cmdbuild.service.rest.constants.Serialization.VERSION;

import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.cmdbuild.service.rest.model.adapter.AttachmentAdapter;

@XmlRootElement(name = ATTACHMENT)
@XmlJavaTypeAdapter(AttachmentAdapter.class)
public class Attachment extends ModelWithId<String> {

	private String name;
	private String category;
	private String description;
	private String version;
	private String author;
	private Date created;
	private Date modified;

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
	public Date getCreated() {
		return created;
	}

	void setCreated(final Date created) {
		this.created = created;
	}

	@XmlAttribute(name = MODIFIED)
	public Date getModified() {
		return modified;
	}

	void setModified(final Date modified) {
		this.modified = modified;
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
				.toHashCode();
	}

}
