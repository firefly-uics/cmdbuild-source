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

public class RelationQueryImpl implements RelationQuery {

	private Set<ICard> cards;
	private Set<DirectedDomain> domains;
	private boolean history;
	private boolean straightened;
	private boolean domainCounted;
	private boolean fullCards;
	private int domainLimit;
	private LimitFilter limit;
	private boolean orderedByDomain;

	public RelationQueryImpl(ICard card) {
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

	public RelationQuery history() {
		this.history = true;
		return this;
	}

	public RelationQuery straightened() {
		this.straightened = true;
		return this;
	}

	public RelationQuery card(ICard card) {
		this.cards.add(card);
		return this;
	}

	public RelationQuery domain(DirectedDomain directedDomain) {
		return domain(directedDomain, false);
	}

	public RelationQuery domain(DirectedDomain directedDomain, boolean fullCards) {
		if (fullCards) {
			if (!this.domains.isEmpty()) {
				throw ORMExceptionType.ORM_FULLCARDS_MULTIPLEDOMAINS.createException();
			}
			this.fullCards = fullCards;
		}
		this.domains.add(directedDomain);
		return this;
	}

	public RelationQuery domain(IDomain domain) {
		this.domains.add(DirectedDomain.create(domain, DomainDirection.D));
		this.domains.add(DirectedDomain.create(domain, DomainDirection.I));
		return this;
	}

	public RelationQuery domainLimit(int domainLimit) {
		this.domainLimit = domainLimit;
		this.domainCounted = true;
		return this;
	}

	public RelationQuery orderByDomain() {
		this.orderedByDomain = true;
		return this;
	}
	
	public RelationQuery subset(int offset, int limit) {
		if (offset >= 0 && limit > 0)
			this.limit = new LimitFilter(offset, limit);
		return this;
	};

	public Set<ICard> getCards() {
		return cards;
	}

	public Set<DirectedDomain> getDomains() {
		return domains;
	}
	
	public DirectedDomain getFullCardsDomain() {
		if (isFullCards())
			return domains.iterator().next();
		else
			throw ORMExceptionType.ORM_GENERIC_ERROR.createException();
	}

	public boolean isHistory() {
		return history;
	}

	public boolean isDomainCounted() {
		return domainCounted;
	}

	public int getDomainLimit() {
		return domainLimit;
	}

	public boolean isStraightened() {
		return straightened;
	}

	public boolean isDomainLimited() {
		return (domainLimit != 0);
	}

	public boolean isOrderedByDomain() {
		return orderedByDomain;
	}

	public LimitFilter getLimit() {
		return this.limit;
	}

	public boolean isFullCards() {
		return fullCards;
	}

	public Iterator<IRelation> iterator() {
		return getIterable().iterator();
	}

	public Iterable<IRelation> getIterable() {
		this.domainCounted = false;
		return new NotCountedIterable(this);
	}

	public Iterable<CountedValue<IRelation>> getCountedIterable() {
		this.domainCounted = true;
		return new CountedIterable(this);
	}

	private ICard buildDestCard(ResultSet rs, QueryComponents queryComponents) throws SQLException {
		ICard destCard = new CardImpl(rs.getInt("idclass2"));
		if (isFullCards()) {
			buildFullDestCard(destCard, rs, queryComponents);
		} else {
			buildBasicDestCard(destCard, rs);				
		}
		return destCard;
	}

	private void buildFullDestCard(ICard destCard, ResultSet rs, QueryComponents queryComponents) throws SQLException {
		ITable destinationClass = this.getFullCardsDomain().getDestTable();
		Map<String, QueryAttributeDescriptor> queryMapping = queryComponents.getQueryMapping();
		for (String attributeName: destinationClass.getAttributes().keySet()) {
			QueryAttributeDescriptor qad = queryMapping.get(attributeName);
			Log.SQL.trace("Setting value for attribute " + attributeName);
			destCard.setValue(attributeName, rs, qad);
		}
	}

	private void buildBasicDestCard(ICard destCard, ResultSet rs) throws SQLException {
		destCard.setValue("Id", rs.getObject("idobj2"));
		destCard.setValue("Code", rs.getObject("fieldcode"));
		destCard.setValue("Description", rs.getObject("fielddescription"));
	}

	private abstract class BaseIterable<T> implements Iterable<T> {
		private boolean straightened;
		private List<T> list = new LinkedList<T>();
		private RelationQuery relationQuery;

		BaseIterable(RelationQuery relationQuery) {
			this.relationQuery = relationQuery;
			this.straightened = relationQuery.isStraightened();
			executeQuery();
		}

		private void executeQuery() {
			Connection con = DBService.getConnection();
			Statement stm = null;
			ResultSet rs = null; 
			RelationQueryBuilder qb = new RelationQueryBuilder();
			String query = qb.buildSelectQuery(relationQuery);
			try {
				stm = con.createStatement();
				rs = stm.executeQuery(query);
				while(rs.next()) {
					try {
						list.add(createElement(rs, qb));
					} catch (NotFoundException e) {
						Log.PERSISTENCE.debug("Relation domain not found", e);
					}
				}
			} catch (SQLException ex) {
				Log.PERSISTENCE.error("Errors finding card relations", ex);
			} finally {
				DBService.close(rs, stm);
			}
		}

		abstract T createElement(ResultSet rs, RelationQueryBuilder qb) throws SQLException;

		protected IRelation createRelation(ResultSet rs, RelationQueryBuilder qb) throws SQLException {
			IRelation relation;
			IDomain domain = UserContext.systemContext().domains().get(rs.getInt("iddomain"));
			ICard srcCard = new LazyCard(rs.getInt("idclass1"), rs.getInt("idobj1"));
			ICard destCard;
			try {
				destCard = buildDestCard(rs, qb.getQueryComponents());							
			} catch (NotFoundException e) {
				destCard = null;
			}
			Integer id = rs.getInt("id");
			boolean isDirect = rs.getBoolean("direct");
			boolean reversedRelation = straightened && !isDirect;
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

		public Iterator<T> iterator() {
			return list.iterator();
		}
	}

	public class NotCountedIterable extends BaseIterable<IRelation> {

		NotCountedIterable(RelationQuery relationQuery) {
			super(relationQuery);
		}

		IRelation createElement(ResultSet rs, RelationQueryBuilder qb) throws SQLException {
			return createRelation(rs, qb);
		}
	}

	public class CountedIterable extends BaseIterable<CountedValue<IRelation>> {

		CountedIterable(RelationQuery relationQuery) {
			super(relationQuery);
		}

		CountedValue<IRelation> createElement(ResultSet rs, RelationQueryBuilder qb) throws SQLException {
			IRelation relation = createRelation(rs, qb);
			return new CountedValue<IRelation>(rs.getInt("count"), relation);
		}
	}

}
