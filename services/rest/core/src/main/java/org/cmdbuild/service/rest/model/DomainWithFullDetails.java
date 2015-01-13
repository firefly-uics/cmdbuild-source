package org.cmdbuild.service.rest.model;

import static org.cmdbuild.service.rest.constants.Serialization.CARDINALITY;
import static org.cmdbuild.service.rest.constants.Serialization.CLASS_DESTINATION;
import static org.cmdbuild.service.rest.constants.Serialization.CLASS_SOURCE;
import static org.cmdbuild.service.rest.constants.Serialization.DESCRIPTION_DIRECT;
import static org.cmdbuild.service.rest.constants.Serialization.DESCRIPTION_INVERSE;
import static org.cmdbuild.service.rest.constants.Serialization.DESCRIPTION_MASTER_DETAIL;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlRootElement
public class DomainWithFullDetails extends DomainWithBasicDetails {

	private Long classSource;
	private Long classDestination;
	private String cardinality;
	private String descriptionDirect;
	private String descriptionInverse;
	private String descriptionMasterDetail;

	DomainWithFullDetails() {
		// package visibility
	}

	@XmlAttribute(name = CLASS_SOURCE)
	public Long getClassSource() {
		return classSource;
	}

	void setClassSource(final Long classnameSource) {
		this.classSource = classnameSource;
	}

	@XmlAttribute(name = CLASS_DESTINATION)
	public Long getClassDestination() {
		return classDestination;
	}

	void setClassDestination(final Long classnameDestination) {
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
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof DomainWithFullDetails)) {
			return false;
		}

		final DomainWithFullDetails other = DomainWithFullDetails.class.cast(obj);
		return super.doEquals(obj) && new EqualsBuilder() //
				.append(this.classSource, other.classSource) //
				.append(this.classDestination, other.classDestination) //
				.append(this.cardinality, other.cardinality) //
				.append(this.descriptionDirect, other.descriptionDirect) //
				.append(this.descriptionInverse, other.descriptionInverse) //
				.append(this.descriptionMasterDetail, other.descriptionMasterDetail) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(super.doHashCode()) //
				.append(classSource) //
				.append(classDestination) //
				.append(cardinality) //
				.append(descriptionDirect) //
				.append(descriptionInverse) //
				.append(descriptionMasterDetail) //
				.toHashCode();
	}

}
