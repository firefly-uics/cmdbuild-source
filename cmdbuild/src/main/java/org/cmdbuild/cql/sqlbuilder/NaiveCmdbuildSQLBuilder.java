package org.cmdbuild.cql.sqlbuilder;

import static java.lang.String.format;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.BeginsWithOperatorAndValue.beginsWith;
import static org.cmdbuild.dao.query.clause.where.ContainsOperatorAndValue.contains;
import static org.cmdbuild.dao.query.clause.where.EndsWithOperatorAndValue.endsWith;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.GreaterThanOperatorAndValue.gt;
import static org.cmdbuild.dao.query.clause.where.InOperatorAndValue.in;
import static org.cmdbuild.dao.query.clause.where.LessThanOperatorAndValue.lt;
import static org.cmdbuild.dao.query.clause.where.NotWhereClause.not;
import static org.cmdbuild.dao.query.clause.where.NullOperatorAndValue.isNull;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.cmdbuild.common.Builder;
import org.cmdbuild.cql.CQLBuilderListener.FieldInputValue;
import org.cmdbuild.cql.CQLBuilderListener.FieldValueType;
import org.cmdbuild.cql.compiler.impl.DomainDeclarationImpl;
import org.cmdbuild.cql.compiler.impl.DomainObjectsReferenceImpl;
import org.cmdbuild.cql.compiler.impl.FieldImpl;
import org.cmdbuild.cql.compiler.impl.FieldSelectImpl;
import org.cmdbuild.cql.compiler.impl.GroupImpl;
import org.cmdbuild.cql.compiler.impl.QueryImpl;
import org.cmdbuild.cql.compiler.impl.SelectImpl;
import org.cmdbuild.cql.compiler.impl.WhereImpl;
import org.cmdbuild.cql.compiler.select.ClassSelect;
import org.cmdbuild.cql.compiler.select.FieldSelect;
import org.cmdbuild.cql.compiler.select.SelectElement;
import org.cmdbuild.cql.compiler.select.SelectItem;
import org.cmdbuild.cql.compiler.where.DomainObjectsReference;
import org.cmdbuild.cql.compiler.where.Field.FieldValue;
import org.cmdbuild.cql.compiler.where.Group;
import org.cmdbuild.cql.compiler.where.WhereElement;
import org.cmdbuild.cql.compiler.where.fieldid.LookupFieldId;
import org.cmdbuild.cql.compiler.where.fieldid.LookupFieldId.LookupOperatorTree;
import org.cmdbuild.cql.compiler.where.fieldid.SimpleFieldId;
import org.cmdbuild.cql.sqlbuilder.CqlFilterMapper.CqlFilterMapperBuilder;
import org.cmdbuild.cql.sqlbuilder.attribute.CMFakeAttribute;
import org.cmdbuild.dao.driver.postgres.Const;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.NullAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.mapping.FilterMapper;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

public class NaiveCmdbuildSQLBuilder implements Builder<FilterMapper> {

	private static final Logger logger = Log.CMDBUILD;
	private static final Marker marker = MarkerFactory.getMarker(NaiveCmdbuildSQLBuilder.class.getName());

	public static FilterMapper build(final QueryImpl q, final Map<String, Object> vars) {
		return new NaiveCmdbuildSQLBuilder(q, vars).build();
	}

	private final DataSource dataSource = applicationContext().getBean(DataSource.class);
	private final CMDataView dataView = applicationContext().getBean(DBDataView.class);
	private final LookupStore lookupStore = applicationContext().getBean(LookupStore.class);

	private final QueryImpl query;
	private final Map<String, Object> vars;

	private final CqlFilterMapperBuilder builder = CqlFilterMapper.newInstance();

	private CMClass fromClass;

	public NaiveCmdbuildSQLBuilder(final QueryImpl query, final Map<String, Object> vars) {
		this.query = query;
		this.vars = vars;
	}

	@Override
	public FilterMapper build() {
		_build();
		return builder.build();
	}

	private void _build() {
		fromClass = query.getFrom().mainClass().getClassTable(dataView);
		builder.withEntryType(fromClass);

		final WhereImpl where = query.getWhere();
		for (final WhereElement element : where.getElements()) {
			handleWhereElement(element, fromClass);
		}

		final SelectImpl select = query.getSelect();
		if (!select.isDefault()) {
			for (@SuppressWarnings("rawtypes")
			final SelectElement selectElement : select.getElements()) {
				if (selectElement instanceof ClassSelect) {
					final ClassSelect classSelect = ClassSelect.class.cast(selectElement);
					for (final SelectItem item : classSelect.getElements()) {
						if (item instanceof FieldSelect) {
							final FieldSelect field = FieldSelectImpl.class.cast(item);
						} else {
							logger.warn(marker, "unsupported select item '{}'", selectElement.getClass()
									.getSimpleName());
						}
					}
				} else {
					logger.warn("unsupported select element '{}'", selectElement.getClass().getSimpleName());
				}
			}
		}
	}

	private void handleWhereElement(final WhereElement whereElement, final CMClass table) {
		if (whereElement instanceof FieldImpl) {
			logger.debug(marker, "adding field");
			handleField((FieldImpl) whereElement, table);
		} else if (whereElement instanceof DomainObjectsReference) {
			logger.debug(marker, "add domain objs");
			final DomainObjectsReferenceImpl domainObjectReference = DomainObjectsReferenceImpl.class
					.cast(whereElement);
			final DomainDeclarationImpl domainDeclaration = DomainDeclarationImpl.class.cast(domainObjectReference
					.getScope());
			final CMDomain domain = domainDeclaration.getDirectedDomain(dataView);
			final CMClass target = domain.getClass1().isAncestorOf(fromClass) ? domain.getClass2() : domain.getClass1();
			builder.add(FilterMapper.JoinElement.newInstance( //
					domain.getName(), //
					fromClass.getName(), //
					target.getName(), //
					false));
			for (final WhereElement element : domainObjectReference.getElements()) {
				handleWhereElement(element, target);
			}
		} else if (whereElement instanceof GroupImpl) {
			logger.debug(marker, "add group");
			final Group group = Group.class.cast(whereElement);
			for (final WhereElement element : group.getElements()) {
				handleWhereElement(element, table);
			}
		} else {
			logger.warn(marker, "unsupported type '{}'", whereElement.getClass());
		}
	}

	private void handleField(final FieldImpl field, final CMClass table) {
		if (field.getId() instanceof SimpleFieldId) {
			handleSimpleField(SimpleFieldId.class.cast(field.getId()), field, table);
		} else if (field.getId() instanceof LookupFieldId) {
			handleLookupField((LookupFieldId) field.getId(), field, table);
		} else {
			throw new RuntimeException("Complex field ids are not supported!");
		}
	}

	private void handleSimpleField(final SimpleFieldId simpleFieldId, final FieldImpl field, final CMClass table) {
		CMAttribute attribute = handleSystemAttributes(simpleFieldId.getId(), table);

		if (attribute == null) {
			attribute = table.getAttribute(simpleFieldId.getId());
		}

		final QueryAliasAttribute attributeForQuery = attribute(fromClass, attribute.getName());
		final Object[] values = values(field, table, attribute);
		final WhereClause whereClause;
		switch (field.getOperator()) {
		case LTEQ:
			whereClause = and(condition(attributeForQuery, eq(values[0])), condition(attributeForQuery, lt(values[0])));
			break;
		case GTEQ:
			whereClause = and(condition(attributeForQuery, eq(values[0])), condition(attributeForQuery, gt(values[0])));
			break;
		case LT:
			whereClause = condition(attributeForQuery, lt(values[0]));
			break;
		case GT:
			whereClause = condition(attributeForQuery, gt(values[0]));
			break;
		case EQ:
			whereClause = condition(attributeForQuery, eq(values[0]));
			break;
		case CONT:
			whereClause = condition(attributeForQuery, contains(values[0]));
			break;
		case BGN:
			whereClause = condition(attributeForQuery, beginsWith(values[0]));
			break;
		case END:
			whereClause = condition(attributeForQuery, endsWith(values[0]));
			break;
		case BTW:
			whereClause = and(condition(attributeForQuery, gt(values[0])), condition(attributeForQuery, lt(values[0])));
			break;
		case IN:
			whereClause = condition(attributeForQuery, in(values));
			break;
		case ISNULL:
			whereClause = condition(attributeForQuery, isNull());
			break;
		default:
			throw new IllegalArgumentException(format("invalid operator '%s'", field.getOperator()));
		}
		builder.add(field.isNot() ? not(whereClause) : whereClause);
	}

	private CMAttribute handleSystemAttributes(final String attributeName, final CMEntryType entryType) {
		CMAttribute attribute = null;
		if (Const.SystemAttributes.Id.getDBName().equals(attributeName)) {
			attribute = new CMFakeAttribute(attributeName, entryType, new IntegerAttributeType());
		} else if (Const.SystemAttributes.IdClass.getDBName().equals(attributeName)) {
			attribute = new CMFakeAttribute(attributeName, entryType, new IntegerAttributeType());
		} else if (Const.SystemAttributes.BeginDate.getDBName().equals(attributeName)) {
			attribute = new CMFakeAttribute(attributeName, entryType, new DateAttributeType());
		} else if (Const.SystemAttributes.Status.getDBName().equals(attributeName)) {
			attribute = new CMFakeAttribute(attributeName, entryType, new StringAttributeType());
		}

		return attribute;
	}

	private void handleLookupField(final LookupFieldId fid, final FieldImpl field, final CMClass table) {
		final LookupOperatorTree node = fid.getTree();
		if (node.getOperator().equalsIgnoreCase("parent")) {
			Object value = 0;
			final FieldValue fieldValue = field.getValues().iterator().next();
			if (node.getAttributeName() == null) {
				if (fieldValue.getType() == FieldValueType.INT) {
					value = fieldValue.getValue();
				} else if (fieldValue.getType() == FieldValueType.STRING) {
					for (final Lookup lookupDto : lookupStore.list()) {
						if (lookupDto.description.equals(fieldValue.getValue().toString())) {
							value = lookupDto.id;
						}
					}
				} else {
					try {
						final Field lookupDtoField = Lookup.class.getField(node.getAttributeName());
						for (final Lookup lookupDto : lookupStore.list()) {
							if (lookupDtoField.get(lookupDto).equals(fieldValue.getValue().toString())) {
								value = lookupDto.id;
							}
						}
					} catch (final Exception e) {
						logger.error(marker, "error getting field");
					}
				}
				final CMAttribute attribute = table.getAttribute(node.getAttributeName());
				final Object __value = attribute.getType().convertValue(value);
				final QueryAliasAttribute attributeForQuery = attribute(fromClass, attribute.getName());
				final WhereClause whereClause = condition(attributeForQuery, eq(__value));
				builder.add(field.isNot() ? not(whereClause) : whereClause);
			} else {
				throw new RuntimeException("unsupported lookup operator: " + node.getOperator());
			}
		}
	}

	private Object[] values(final FieldImpl field, final CMClass table, final CMAttribute attribute) {
		final List<Object> values = new ArrayList<Object>();
		for (final FieldValue v : field.getValues()) {
			convert(attribute, v, vars, new ConvertedCallback() {

				@Override
				public void addValue(Object object) {
					logger.debug(marker, "converted value '{}'" + object);
					values.add(attribute.getType().convertValue(object));
				}

			});
		}

		final Object firstValue = values.get(0);
		final String firstStringValue = (firstValue instanceof String) ? (String) firstValue : null;

		if (firstStringValue != null) {
			attribute.getType().accept(new NullAttributeTypeVisitor() {
				@Override
				public void visit(LookupAttributeType attributeType) {
					if (field.getValues().iterator().next().getType() != FieldValueType.NATIVE) {
						try {
							Integer.getInteger(firstStringValue);
						} catch (final NumberFormatException e) {
							final String lookupTypeName = attributeType.getLookupTypeName();
							Lookup lookup = null;
							for (final Lookup element : lookupStore.listForType(LookupType.newInstance() //
									.withName(lookupTypeName) //
									.build())) {
								if (element.description.equals(firstStringValue)) {
									lookup = element;
								}
							}
							if (lookup == null) {
								throw new RuntimeException("invalid lookup value: " + values.get(0));
							}
							values.clear();
							values.add(lookup.id);
						}
					}
				}

				@Override
				public void visit(ReferenceAttributeType attributeType) {
					if (field.getValues().iterator().next().getType() != FieldValueType.NATIVE) {
						try {
							Integer.parseInt(firstStringValue);
						} catch (final NumberFormatException e) {
							final String domainName = attributeType.getDomainName();
							final CMDomain domain = dataView.findDomain(domainName);
							final CMClass target;
							if (domain.getClass1().isAncestorOf(table)) {
								target = domain.getClass2();
							} else {
								target = domain.getClass1();
							}
							builder.add(condition(attribute(target, "Description"), eq(firstStringValue)));
							builder.add(FilterMapper.JoinElement.newInstance(domainName, table.getName(),
									target.getName(), true));
						}
					}
				}
			});
		}

		return values.toArray(new Object[values.size()]);
	}

	private interface ConvertedCallback {

		void addValue(Object object);

	}

	private void convert(final CMAttribute attribute, final FieldValue fieldValue, final Map<String, Object> context,
			final ConvertedCallback callback) {
		switch (fieldValue.getType()) {
		case BOOL:
		case DATE:
		case FLOAT:
		case INT:
		case STRING:
		case TIMESTAMP:
			callback.addValue(fieldValue.getValue().toString());
			break;
		case NATIVE:
			sqlQuery(fieldValue.getValue().toString(), callback);
			break;
		case INPUT:
			final FieldInputValue fieldInputValue = FieldInputValue.class.cast(fieldValue.getValue());
			final String variableName = fieldInputValue.getVariableName();
			final Object value = context.get(variableName);
			if (value instanceof java.util.Date) {
				callback.addValue(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format((Date) value));
			} else {
				callback.addValue(value.toString());
			}
			break;
		case SUBEXPR:
			throw new RuntimeException("subqueries are not supported");
		default:
			throw new RuntimeException("cannot convert value " + fieldValue.getType().name() + ": "
					+ fieldValue.getValue() + " to string!");
		}
	}

	private void sqlQuery(final String sql, final ConvertedCallback callback) {
		new JdbcTemplate(dataSource).query(sql, new RowCallbackHandler() {
			@Override
			public void processRow(final ResultSet rs) throws SQLException {
				callback.addValue(rs.getObject(0));
			}
		});
	}

}
