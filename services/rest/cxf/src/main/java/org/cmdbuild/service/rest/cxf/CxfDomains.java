package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;

import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.service.rest.Domains;
import org.cmdbuild.service.rest.dto.DetailResponseMetadata;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.SimpleDomainDetail;
import org.cmdbuild.service.rest.serialization.ToSimpleDomainDetail;

public class CxfDomains implements Domains {

	private static final ToSimpleDomainDetail TO_SIMPLE_DOMAIN_DETAIL = ToSimpleDomainDetail.newInstance().build();

	private final DataAccessLogic userDataAccessLogic;

	public CxfDomains(final DataAccessLogic userDataAccessLogic) {
		this.userDataAccessLogic = userDataAccessLogic;
	}

	@Override
	public ListResponse<SimpleDomainDetail> readAll(final Integer limit, final Integer offset) {
		final Iterable<? extends CMDomain> domains = userDataAccessLogic.findAllDomains();
		final Iterable<SimpleDomainDetail> elements = from(domains) //
				.skip((offset == null) ? 0 : offset) //
				.limit((limit == null) ? Integer.MAX_VALUE : limit) //
				.transform(TO_SIMPLE_DOMAIN_DETAIL);
		return ListResponse.<SimpleDomainDetail> newInstance() //
				.withElements(elements) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(Long.valueOf(size(domains))) //
						.build()) //
				.build();
	}

}
