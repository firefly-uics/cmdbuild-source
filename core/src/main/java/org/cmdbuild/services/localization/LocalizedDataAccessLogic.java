package org.cmdbuild.services.localization;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.cmdbuild.logic.commands.GetCardHistory.GetCardHistoryResponse;
import org.cmdbuild.logic.commands.GetRelationHistory.GetRelationHistoryResponse;
import org.cmdbuild.logic.commands.GetRelationList.DomainWithSource;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.access.CMCardWithPosition;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.FetchCardListResponse;
import org.cmdbuild.logic.data.access.ForwardingDataAccessLogic;
import org.cmdbuild.logic.data.access.RelationDTO;
import org.cmdbuild.logic.translation.TranslationFacade;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.servlets.json.management.dataimport.csv.CSVData;
import org.json.JSONException;

import com.google.common.base.Function;
import com.google.common.base.Optional;

public class LocalizedDataAccessLogic extends ForwardingDataAccessLogic {

	private final DataAccessLogic delegate;
	private final Function<CMAttribute, CMAttribute> TO_LOCALIZED_ATTRIBUTE;

	public LocalizedDataAccessLogic(final DataAccessLogic delegate, final TranslationFacade facade) {
		this.delegate = delegate;
		this.TO_LOCALIZED_ATTRIBUTE = new Function<CMAttribute, CMAttribute>() {

			@Override
			public CMAttribute apply(final CMAttribute input) {
				return (input == null) ? null : new LocalizedAttribute(input, facade);
			}

		};
	}

	@Override
	protected DataAccessLogic delegate() {
		return delegate;
	}

	@Override
	public Map<Object, List<RelationInfo>> relationsBySource(final String sourceTypeName, final DomainWithSource dom) {
		return delegate().relationsBySource(sourceTypeName, dom);
	}

	@Override
	public GetRelationListResponse getRelationList(final Card srcCard, final DomainWithSource dom,
			final QueryOptions options) {
		return delegate().getRelationList(srcCard, dom, options);
	}

	@Override
	public GetRelationListResponse getRelationList(final Card srcCard, final DomainWithSource dom) {
		return delegate().getRelationList(srcCard, dom);
	}

	@Override
	public GetRelationListResponse getRelationListEmptyForWrongId(final Card srcCard, final DomainWithSource dom) {
		return delegate().getRelationListEmptyForWrongId(srcCard, dom);
	}

	@Override
	public GetRelationListResponse getRelationList(final CMDomain domain, final QueryOptions queryOptions) {
		return delegate().getRelationList(domain, queryOptions);
	}

	@Override
	public Optional<RelationInfo> getRelation(final CMDomain domain, final Long id) {
		return delegate().getRelation(domain, id);
	}

	@Override
	public GetRelationHistoryResponse getRelationHistory(final Card srcCard) {
		return delegate().getRelationHistory(srcCard);
	}

	@Override
	public GetRelationHistoryResponse getRelationHistory(final Card srcCard, final CMDomain domain) {
		return delegate().getRelationHistory(srcCard, domain);
	}

	@Override
	public CMRelation getRelation(final Long srcCardId, final Long dstCardId, final CMDomain domain,
			final CMClass sourceClass, final CMClass destinationClass) {
		return delegate().getRelation(srcCardId, dstCardId, domain, sourceClass, destinationClass);
	}

	@Override
	public GetCardHistoryResponse getCardHistory(final Card srcCard) {
		return delegate().getCardHistory(srcCard);
	}

	@Override
	public CMClass findClass(final Long classId) {
		return delegate().findClass(classId);
	}

	@Override
	public CMClass findClass(final String className) {
		return delegate().findClass(className);
	}

	@Override
	public CMDomain findDomain(final Long domainId) {
		return delegate().findDomain(domainId);
	}

	@Override
	public CMDomain findDomain(final String domainName) {
		return delegate().findDomain(domainName);
	}

	@Override
	public boolean hasClass(final Long classId) {
		return delegate().hasClass(classId);
	}

	@Override
	public Iterable<? extends CMClass> findActiveClasses() {
		return delegate().findActiveClasses();
	}

	@Override
	public Iterable<? extends CMDomain> findAllDomains() {
		return delegate().findAllDomains();
	}

	@Override
	public Iterable<? extends CMDomain> findActiveDomains() {
		return delegate().findActiveDomains();
	}

	@Override
	public Iterable<? extends CMDomain> findReferenceableDomains(final String className) {
		return delegate().findReferenceableDomains(className);
	}

	@Override
	public Iterable<? extends CMClass> findAllClasses() {
		return delegate().findAllClasses();
	}

	@Override
	public Iterable<? extends CMClass> findClasses(final boolean activeOnly) {
		return delegate().findClasses(activeOnly);
	}

	@Override
	public PagedElements<CMAttribute> getAttributes(final String className, final boolean onlyActive,
			final AttributesQuery attributesQuery) {
		final Iterable<CMAttribute> localizedAttributes = proxy(super.getAttributes(className, onlyActive,
				attributesQuery));
		return new PagedElements<CMAttribute>(localizedAttributes, size(localizedAttributes));
	}

	@Override
	public PagedElements<CMAttribute> getDomainAttributes(final String className, final boolean onlyActive,
			final AttributesQuery attributesQuery) {
		return delegate().getDomainAttributes(className, onlyActive, attributesQuery);
	}

	@Override
	public Card fetchCard(final String className, final Long cardId) {
		return delegate().fetchCard(className, cardId);
	}

	@Override
	public CMCard fetchCMCard(final String className, final Long cardId) {
		return delegate().fetchCMCard(className, cardId);
	}

	@Override
	public Card fetchCardShort(final String className, final Long cardId, final QueryOptions queryOptions) {
		return delegate().fetchCardShort(className, cardId, queryOptions);
	}

	@Override
	public Card fetchCard(final Long classId, final Long cardId) {
		return delegate().fetchCard(classId, cardId);
	}

	@Override
	public FetchCardListResponse fetchCards(final String className, final QueryOptions queryOptions) {
		return delegate().fetchCards(className, queryOptions);
	}

	@Override
	public FetchCardListResponse fetchSQLCards(final String functionName, final QueryOptions queryOptions) {
		return delegate().fetchSQLCards(functionName, queryOptions);
	}

	@Override
	public CMCardWithPosition getCardPosition(final String className, final Long cardId, final QueryOptions queryOptions) {
		return delegate().getCardPosition(className, cardId, queryOptions);
	}

	@Override
	public Long createCard(final Card card) {
		return delegate().createCard(card);
	}

	@Override
	public Long createCard(final Card userGivenCard, final boolean manageAlsoDomainsAttributes) {
		return delegate().createCard(userGivenCard, manageAlsoDomainsAttributes);
	}

	@Override
	public void updateCard(final Card card) {
		delegate().updateCard(card);
	}

	@Override
	public void updateFetchedCard(final Card card, final Map<String, Object> attributes) {
		delegate().updateFetchedCard(card, attributes);
	}

	@Override
	public void deleteCard(final String className, final Long cardId) {
		delegate().deleteCard(className, cardId);
	}

	@Override
	public List<CMDomain> findDomainsForClassWithName(final String className) {
		return delegate().findDomainsForClassWithName(className);
	}

	@Override
	public boolean isProcess(final CMClass target) {
		return delegate().isProcess(target);
	}

	@Override
	public Iterable<Long> createRelations(final RelationDTO relationDTO) {
		return delegate().createRelations(relationDTO);
	}

	@Override
	public void updateRelation(final RelationDTO relationDTO) {
		delegate().updateRelation(relationDTO);

	}

	@Override
	public void deleteRelation(final String domainName, final Long relationId) {
		delegate().deleteRelation(domainName, relationId);

	}

	@Override
	public void deleteDetail(final Card master, final Card detail, final String domainName) {
		delegate().deleteDetail(master, detail, domainName);

	}

	@Override
	public void deleteRelation(final String srcClassName, final Long srcCardId, final String dstClassName,
			final Long dstCardId, final CMDomain domain) {
		delegate().deleteRelation(srcClassName, srcCardId, dstClassName, dstCardId, domain);

	}

	@Override
	public File exportClassAsCsvFile(final String className, final String separator) {
		return delegate().exportClassAsCsvFile(className, separator);
	}

	@Override
	public CSVData importCsvFileFor(final FileItem csvFile, final Long classId, final String separator)
			throws IOException, JSONException {
		return delegate().importCsvFileFor(csvFile, classId, separator);
	}

	@Override
	public CMCard resolveCardReferences(final CMClass entryType, final CMCard card) {
		return delegate().resolveCardReferences(entryType, card);
	}

	@Override
	public void lockCard(final Long cardId) {
		delegate().lockCard(cardId);

	}

	@Override
	public void unlockCard(final Long cardId) {
		delegate().unlockCard(cardId);

	}

	@Override
	public void unlockAllCards() {
		delegate().unlockAllCards();
	}

	Iterable<CMAttribute> proxy(final Iterable<? extends CMAttribute> attributes) {
		return from(attributes) //
				.transform(TO_LOCALIZED_ATTRIBUTE) //
				.filter(CMAttribute.class);
	}

}
