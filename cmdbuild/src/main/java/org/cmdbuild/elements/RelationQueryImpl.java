package org.cmdbuild.elements;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cmdbuild.common.annotations.OldDao;
import org.cmdbuild.dao.backend.postgresql.QueryComponents;
import org.cmdbuild.dao.backend.postgresql.QueryComponents.QueryAttributeDescriptor;
import org.cmdbuild.dao.backend.postgresql.RelationQueryBuilder;
import org.cmdbuild.elements.DirectedDomain.DomainDirection;
import org.cmdbuild.elements.filters.LimitFilter;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.IRelation;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.RelationQuery;
import org.cmdbuild.elements.proxy.LazyCard;
import org.cmdbuild.elements.utils.CountedValue;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.DBService;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;

@OldDao
@Deprecated
public class RelationQueryImpl implements RelationQuery {

	private final Set<ICard> cards;
	private final Set<DirectedDomain> domains;
	private boolean history;
	private boolean straightened;
	private boolean domainCounted;
	private boolean fullCards;
	private int domainLimit;
	private LimitFilter limit;
	private boolean orderedByDomain;

	public RelationQueryImpl(final ICard card) {
		this();
		this.cards.add(card);
	}

	public RelationQueryImpl() {
		this.cards = new HashSet<ICard>();
		this.domains = new HashSet<DirectedDomain>();
		this.history = false;
		this.straightened = false;
		this.domainCounted = false;
		this.domainLimit = 0;
		this.fullCards = false;
	}

	@Override
	public RelationQuery history() {
		this.history = true;
		return this;
	}

	@Override
	public RelationQuery straightened() {
		this.straightened = true;
		return this;
	}

	@Override
	public RelationQuery card(final ICard card) {
		this.cards.add(card);
		return this;
	}

	@Override
	public RelationQuery domain(final DirectedDomain directedDomain) {
		return domain(directedDomain, false);
	}

	@Override
	public RelationQuery domain(final DirectedDomain directedDomain, final boolean fullCards) {
		if (fullCards) {
			if (!this.domains.isEmpty()) {
				throw ORMExceptionType.ORM_FULLCARDS_MULTIPLEDOMAINS.createException();
			}
			this.fullCards = fullCards;
		}
		this.domains.add(directedDomain);
		return this;
	}

	@Override
	public RelationQuery domain(final IDomain domain) {
		this.domains.add(DirectedDomain.create(domain, DomainDirection.D));
		this.domains.add(DirectedDomain.create(domain, DomainDirection.I));
		return this;
	}

	@Override
	public RelationQuery domainLimit(final int domainLimit) {
		this.domainLimit = domainLimit;
		this.domainCounted = true;
		return this;
	}

	@Override
	public RelationQuery orderByDomain() {
		this.orderedByDomain = true;
		return this;
	}

	@Override
	public RelationQuery subset(final int offset, final int limit) {
		if (offset >= 0 && limit > 0)
			this.limit = new LimitFilter(offset, limit);
		return this;
	};

	@Override
	public Set<ICard> getCards() {
		return cards;
	}

	@Override
	public Set<DirectedDomain> getDomains() {
		return domains;
	}

	@Override
	public DirectedDomain getFullCardsDomain() {
		if (isFullCards())
			return domains.iterator().next();
		else
			throw ORMExceptionType.ORM_GENERIC_ERROR.createException();
	}

	@Override
	public boolean isHistory() {
		return history;
	}

	@Override
	public boolean isDomainCounted() {
		return domainCounted;
	}

	@Override
	public int getDomainLimit() {
		return domainLimit;
	}

	@Override
	public boolean isStraightened() {
		return straightened;
	}

	@Override
	public boolean isDomainLimited() {
		return (domainLimit != 0);
	}

	@Override
	public boolean isOrderedByDomain() {
		return orderedByDomain;
	}

	@Override
	public LimitFilter getLimit() {
		return this.limit;
	}

	@Override
	public boolean isFullCards() {
		return fullCards;
	}

	@Override
	public Iterator<IRelation> iterator() {
		return getIterable().iterator();
	}

	@Override
	public Iterable<IRelation> getIterable() {
		this.domainCounted = false;
		return new NotCountedIterable(this);
	}

	@Override
	public Iterable<CountedValue<IRelation>> getCountedIterable() {
		this.domainCounted = true;
		return new CountedIterable(this);
	}

	private ICard buildDestCard(final ResultSet rs, final QueryComponents queryComponents) throws SQLException {
		final ICard destCard = new CardImpl(rs.getInt("idclass2"));
		if (isFullCards()) {
			buildFullDestCard(destCard, rs, queryComponents);
		} else {
			buildBasicDestCard(destCard, rs);
		}
		return destCard;
	}

	private void buildFullDestCard(final ICard destCard, final ResultSet rs, final QueryComponents queryComponents)
			throws SQLException {
		final ITable destinationClass = this.getFullCardsDomain().getDestTable();
		final Map<String, QueryAttributeDescriptor> queryMapping = queryComponents.getQueryMapping();
		for (final String attributeName : destinationClass.getAttributes().keySet()) {
			final QueryAttributeDescriptor qad = queryMapping.get(attributeName);
			Log.SQL.trace("Setting value for attribute " + attributeName);
			destCard.setValue(attributeName, rs, qad);
		}
	}

	private void buildBasicDestCard(final ICard destCard, final ResultSet rs) throws SQLException {
		destCard.setValue("Id", rs.getObject("idobj2"));
		destCard.setValue("Code", rs.getObject("fieldcode"));
		destCard.setValue("Description", rs.getObject("fielddescription"));
	}

	private abstract class BaseIterable<T> implements Iterable<T> {
		private final boolean straightened;
		private final List<T> list = new LinkedList<T>();
		private final RelationQuery relationQuery;

		BaseIterable(final RelationQuery relationQuery) {
			this.relationQuery = relationQuery;
			this.straightened = relationQuery.isStraightened();
			executeQuery();
		}

		private void executeQuery() {
			final Connection con = DBService.getConnection();
			Statement stm = null;
			ResultSet rs = null;
			final RelationQueryBuilder qb = new RelationQueryBuilder();
			final String query = qb.buildSelectQuery(relationQuery);
			try {
				stm = con.createStatement();
				rs = stm.executeQuery(query);
				while (rs.next()) {
					try {
						list.add(createElement(rs, qb));
					} catch (final NotFoundException e) {
						Log.PERSISTENCE.debug("Relation domain not found", e);
					}
				}
			} catch (final SQLException ex) {
				Log.PERSISTENCE.error("Errors finding card relations", ex);
			} finally {
				DBService.close(rs, stm, con);
			}
		}

		abstract T createElement(ResultSet rs, RelationQueryBuilder qb) throws SQLException;

		protected IRelation createRelation(final ResultSet rs, final RelationQueryBuilder qb) throws SQLException {
			IRelation relation;
			final IDomain domain = UserOperations.from(UserContext.systemContext()).domains()
					.get(rs.getInt("iddomain"));
			final ICard srcCard = new LazyCard(rs.getInt("idclass1"), rs.getInt("idobj1"));
			ICard destCard;
			try {
				destCard = buildDestCard(rs, qb.getQueryComponents());
			} catch (final NotFoundException e) {
				destCard = null;
			}
			final Integer id = rs.getInt("id");
			final boolean isDirect = rs.getBoolean("direct");
			final boolean reversedRelation = straightened && !isDirect;
			if (!straightened && !isDirect) {
				relation = new RelationImpl(id, domain, destCard, srcCard, reversedRelation);
			} else {
				relation = new RelationImpl(id, domain, srcCard, destCard, reversedRelation);
			}
			relation.setValue("BeginDate", rs.getObject("begindate"));
			if (isHistory()) {
				relation.setValue("EndDate", rs.getObject("enddate"));
				relation.setValue("User", rs.getObject("username"));
			} else {
				relation.setValue("Status", "A");
			}
			relation.resetAttributes();
			return relation;
		}

		@Override
		public Iterator<T> iterator() {
			return list.iterator();
		}
	}

	public class NotCountedIterable extends BaseIterable<IRelation> {

		NotCountedIterable(final RelationQuery relationQuery) {
			super(relationQuery);
		}

		@Override
		IRelation createElement(final ResultSet rs, final RelationQueryBuilder qb) throws SQLException {
			return createRelation(rs, qb);
		}
	}

	public class CountedIterable extends BaseIterable<CountedValue<IRelation>> {

		CountedIterable(final RelationQuery relationQuery) {
			super(relationQuery);
		}

		@Override
		CountedValue<IRelation> createElement(final ResultSet rs, final RelationQueryBuilder qb) throws SQLException {
			final IRelation relation = createRelation(rs, qb);
			return new CountedValue<IRelation>(rs.getInt("count"), relation);
		}
	}

}
