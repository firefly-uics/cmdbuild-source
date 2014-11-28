package org.cmdbuild.service.rest.model;

import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.service.rest.constants.Serialization.ATTACHMENT_METADATA;
import static org.cmdbuild.service.rest.constants.Serialization.EXTRA;

import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.cmdbuild.service.rest.model.adapter.AttachmentMetadataAdapter;

@XmlRootElement(name = ATTACHMENT_METADATA)
@XmlJavaTypeAdapter(AttachmentMetadataAdapter.class)
public class AttachmentMetadata extends Attachment {

	private static final Map<String, Object> NO_EXTRA = emptyMap();

	private Map<String, Object> extra;

	AttachmentMetadata() {
		// package visibility
	}

	@XmlAttribute(name = EXTRA)
	public Map<String, Object> getExtra() {
		return defaultIfNull(extra, NO_EXTRA);
	}

	void setExtra(final Map<String, Object> extra) {
		this.extra = extra;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof AttachmentMetadata)) {
			return false;
		}

		final AttachmentMetadata other = AttachmentMetadata.class.cast(obj);

		return super.doEquals(other) && new EqualsBuilder() //
				.append(this.extra, other.extra) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(super.doHashCode()) //
				.append(this.extra) //
				.toHashCode();
	}

}
