package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.constants.Serialization.CARDINALITY;
import static org.cmdbuild.service.rest.constants.Serialization.CLASS_DESTINATION;
import static org.cmdbuild.service.rest.constants.Serialization.CLASS_SOURCE;
import static org.cmdbuild.service.rest.constants.Serialization.DESCRIPTION;
import static org.cmdbuild.service.rest.constants.Serialization.DESCRIPTION_DIRECT;
import static org.cmdbuild.service.rest.constants.Serialization.DESCRIPTION_INVERSE;
import static org.cmdbuild.service.rest.constants.Serialization.DESCRIPTION_MASTER_DETAIL;
import static org.cmdbuild.service.rest.constants.Serialization.FULL_DOMAIN_DETAIL;
import static org.cmdbuild.service.rest.constants.Serialization.NAME;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlRootElement(name = FULL_DOMAIN_DETAIL)
public class FullDomainDetail {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<FullDomainDetail> {

		private String name;
		private String description;
		private String classSource;
		private String classDestination;
		private String cardinality;
		private String descriptionDirect;
		private String descriptionInverse;
		private String descriptionMasterDetail;

		private Builder() {
			// use static method
		}

		@Override
		public FullDomainDetail build() {
			validate();
			return new FullDomainDetail(this);
		}

		private void validate() {
			// TODO Auto-generated method stub
		}

		public Builder withName(final String name) {
			this.name = name;
			return this;
		}

		public Builder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public Builder withClassSource(final String classSource) {
			this.classSource = classSource;
			return this;
		}

		public Builder withClassDestination(final String classDestination) {
			this.classDestination = classDestination;
			return this;
		}

		public Builder withCardinality(final String cardinality) {
			this.cardinality = cardinality;
			return this;
		}

		public Builder withDescriptionDirect(final String descriptionDirect) {
			this.descriptionDirect = descriptionDirect;
			return this;
		}

		public Builder withDescriptionInverse(final String descriptionInverse) {
			this.descriptionInverse = descriptionInverse;
			return this;
		}

		public Builder withDescriptionMasterDetail(final String descriptionMasterDetail) {
			this.descriptionMasterDetail = descriptionMasterDetail;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private String name;
	private String description;
	private String classSource;
	private String classDestination;
	private String cardinality;
	private String descriptionDirect;
	private String descriptionInverse;
	private String descriptionMasterDetail;

	FullDomainDetail() {
		// package visibility
	}

	private FullDomainDetail(final Builder builder) {
		this.name = builder.name;
		this.description = builder.description;
		this.classSource = builder.classSource;
		this.classDestination = builder.classDestination;
		this.cardinality = builder.cardinality;
		this.descriptionDirect = builder.descriptionDirect;
		this.descriptionInverse = builder.descriptionInverse;
		this.descriptionMasterDetail = builder.descriptionMasterDetail;
	}

	@XmlAttribute(name = NAME)
	public String getName() {
		return name;
	}

	void setName(final String name) {
		this.name = name;
	}

	@XmlAttribute(name = DESCRIPTION)
	public String getDescription() {
		return description;
	}

	void setDescription(final String description) {
		this.description = description;
	}

	@XmlAttribute(name = CLASS_SOURCE)
	public String getClassSource() {
		return classSource;
	}

	void setClassSource(final String classnameSource) {
		this.classSource = classnameSource;
	}

	@XmlAttribute(name = CLASS_DESTINATION)
	public String getClassDestination() {
		return classDestination;
	}

	void setClassDestination(final String classnameDestination) {
		this.classDestination = classnameDestination;
	}

	@XmlAttribute(name = CARDINALITY)
	public String getCardinality() {
		return cardinality;
	}

	void setCardinality(final String cardinality) {
		this.cardinality = cardinality;
	}

	@XmlAttribute(name = DESCRIPTION_DIRECT)
	public String getDescriptionDirect() {
		return descriptionDirect;
	}

	void setDescriptionDirect(final String descriptionDirect) {
		this.descriptionDirect = descriptionDirect;
	}

	@XmlAttribute(name = DESCRIPTION_INVERSE)
	public String getDescriptionInverse() {
		return descriptionInverse;
	}

	void setDescriptionInverse(final String descriptionInverse) {
		this.descriptionInverse = descriptionInverse;
	}

	@XmlAttribute(name = DESCRIPTION_MASTER_DETAIL)
	public String getDescriptionMasterDetail() {
		return descriptionMasterDetail;
	}

	void setDescriptionMasterDetail(final String descriptionMasterDetail) {
		this.descriptionMasterDetail = descriptionMasterDetail;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof FullDomainDetail)) {
			return false;
		}

		final FullDomainDetail other = FullDomainDetail.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.name, other.name) //
				.append(this.description, other.description) //
				.append(this.classSource, other.classSource) //
				.append(this.classDestination, other.classDestination) //
				.append(this.cardinality, other.cardinality) //
				.append(this.descriptionDirect, other.descriptionDirect) //
				.append(this.descriptionInverse, other.descriptionInverse) //
				.append(this.descriptionMasterDetail, other.descriptionMasterDetail) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(name) //
				.append(description) //
				.append(classSource) //
				.append(classDestination) //
				.append(cardinality) //
				.append(descriptionDirect) //
				.append(descriptionInverse) //
				.append(descriptionMasterDetail) //
				.toHashCode();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}

}
