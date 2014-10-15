package org.cmdbuild.service.rest.model;

import static org.cmdbuild.service.rest.constants.Serialization.DESTINATION;
import static org.cmdbuild.service.rest.constants.Serialization.RELATION;
import static org.cmdbuild.service.rest.constants.Serialization.SOURCE;
import static org.cmdbuild.service.rest.constants.Serialization.VALUES;

import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.cmdbuild.service.rest.model.adapter.RelationAdapter;
import org.cmdbuild.service.rest.model.adapter.StringObjectMapAdapter;

@XmlRootElement(name = RELATION)
@XmlJavaTypeAdapter(RelationAdapter.class)
public class Relation extends ModelWithIdAndType<Long, String> {

	private Card source;
	private Card destination;
	private Map<String, Object> values;

	Relation() {
		// package visibility
	}

	@XmlAttribute(name = SOURCE)
	public Card getSource() {
		return source;
	}

	void setSource(final Card source) {
		this.source = source;
	}

	@XmlAttribute(name = DESTINATION)
	public Card getDestination() {
		return destination;
	}

	void setDestination(final Card destination) {
		this.destination = destination;
	}

	@XmlElement(name = VALUES)
	@XmlJavaTypeAdapter(StringObjectMapAdapter.class)
	public Map<String, Object> getValues() {
		return values;
	}

	void setValues(final Map<String, Object> values) {
		this.values = values;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Relation)) {
			return false;
		}

		final Relation other = Relation.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.getType(), other.getType()) //
				.append(this.getId(), other.getId()) //
				.append(this.values, other.values) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(getType()) //
				.append(getId()) //
				.append(values) //
				.toHashCode();
	}

}
