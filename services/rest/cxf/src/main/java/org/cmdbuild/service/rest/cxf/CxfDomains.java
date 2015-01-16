package org.cmdbuild.service.rest.cxf;

import static com.google.common.base.Predicates.alwaysTrue;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.cmdbuild.service.rest.model.Models.newMetadata;
import static org.cmdbuild.service.rest.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.model.Models.newResponseSingle;

import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.filter.json.JsonParser;
import org.cmdbuild.logic.data.access.filter.model.Element;
import org.cmdbuild.logic.data.access.filter.model.Filter;
import org.cmdbuild.logic.data.access.filter.model.Parser;
import org.cmdbuild.service.rest.Domains;
import org.cmdbuild.service.rest.cxf.filter.DomainElementPredicate;
import org.cmdbuild.service.rest.cxf.serialization.ToFullDomainDetail;
import org.cmdbuild.service.rest.cxf.serialization.ToSimpleDomainDetail;
import org.cmdbuild.service.rest.model.DomainWithBasicDetails;
import org.cmdbuild.service.rest.model.DomainWithFullDetails;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

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
	public ResponseMultiple<DomainWithBasicDetails> readAll(final String filter, final Integer limit,
			final Integer offset) {
		final Predicate<CMDomain> predicate;
		if (isNotBlank(filter)) {
			final Parser parser = new JsonParser(filter);
			final Filter filterModel = parser.parse();
			final Optional<Element> element = filterModel.attribute();
			if (element.isPresent()) {
				predicate = new DomainElementPredicate(element.get());
			} else {
				predicate = alwaysTrue();
			}
		} else {
			predicate = alwaysTrue();
		}
		final Iterable<? extends CMDomain> domains = userDataAccessLogic.findAllDomains();
		final Iterable<DomainWithBasicDetails> elements = from(domains) //
				.filter(predicate) //
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
	public ResponseSingle<DomainWithFullDetails> read(final String domainId) {
		final CMDomain found = userDataAccessLogic.findDomain(domainId);
		if (found == null) {
			errorHandler.domainNotFound(domainId);
		}
		return newResponseSingle(DomainWithFullDetails.class) //
				.withElement(TO_FULL_DOMAIN_DETAIL.apply(found)) //
				.build();
	}
}
