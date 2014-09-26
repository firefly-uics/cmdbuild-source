package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;
import static org.cmdbuild.service.rest.dto.Builders.newMetadata;
import static org.cmdbuild.service.rest.dto.Builders.newResponseMultiple;
import static org.cmdbuild.service.rest.dto.Builders.newResponseSingle;

import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.service.rest.Domains;
import org.cmdbuild.service.rest.cxf.serialization.ToFullDomainDetail;
import org.cmdbuild.service.rest.cxf.serialization.ToSimpleDomainDetail;
import org.cmdbuild.service.rest.dto.DomainWithBasicDetails;
import org.cmdbuild.service.rest.dto.DomainWithFullDetails;
import org.cmdbuild.service.rest.dto.ResponseMultiple;
import org.cmdbuild.service.rest.dto.ResponseSingle;

public class CxfDomains implements Domains {

	private static final ToSimpleDomainDetail TO_SIMPLE_DOMAIN_DETAIL = ToSimpleDomainDetail.newInstance().build();
	private static final ToFullDomainDetail TO_FULL_DOMAIN_DETAIL = ToFullDomainDetail.newInstance().build();

	private final ErrorHandler errorHandler;
	private final DataAccessLogic userDataAccessLogic;

	public CxfDomains(final ErrorHandler errorHandler, final DataAccessLogic userDataAccessLogic) {
		this.errorHandler = errorHandler;
		this.userDataAccessLogic = userDataAccessLogic;
	}

	@Override
	public ResponseMultiple<DomainWithBasicDetails> readAll(final Integer limit, final Integer offset) {
		final Iterable<? extends CMDomain> domains = userDataAccessLogic.findAllDomains();
		final Iterable<DomainWithBasicDetails> elements = from(domains) //
				.skip((offset == null) ? 0 : offset) //
				.limit((limit == null) ? Integer.MAX_VALUE : limit) //
				.transform(TO_SIMPLE_DOMAIN_DETAIL);
		return newResponseMultiple(DomainWithBasicDetails.class) //
				.withElements(elements) //
				.withMetadata(newMetadata() //
						.withTotal(Long.valueOf(size(domains))) //
						.build()) //
				.build();
	}

	@Override
	public ResponseSingle<DomainWithFullDetails> read(final String name) {
		final CMDomain found = userDataAccessLogic.findDomain(name);
		if (found == null) {
			errorHandler.domainNotFound(name);
		}
		return newResponseSingle(DomainWithFullDetails.class) //
				.withElement(TO_FULL_DOMAIN_DETAIL.apply(found)) //
				.build();
	}
}
