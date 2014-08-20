package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.service.rest.Domains;
import org.cmdbuild.service.rest.dto.DetailResponseMetadata;
import org.cmdbuild.service.rest.dto.DomainListResponse;
import org.cmdbuild.service.rest.dto.SimpleDomainDetail;
import org.cmdbuild.service.rest.serialization.ToSimpleDomainDetail;

public class CxfDomains extends CxfService implements Domains {

	private static final ToSimpleDomainDetail TO_SIMPLE_DOMAIN_DETAIL = ToSimpleDomainDetail.newInstance().build();

	@Context
	protected SecurityContext securityContext;

	@Context
	protected UriInfo uriInfo;

	@Override
	public DomainListResponse readAll(final Integer limit, final Integer offset) {
		final Iterable<? extends CMDomain> domains = userDataAccessLogic().findAllDomains();
		final Iterable<SimpleDomainDetail> elements = from(domains) //
				.skip((offset == null) ? 0 : offset) //
				.limit((limit == null) ? Integer.MAX_VALUE : limit) //
				.transform(TO_SIMPLE_DOMAIN_DETAIL);
		return DomainListResponse.newInstance() //
				.withElements(elements) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(size(domains)) //
						.build()) //
				.build();
	}

}
