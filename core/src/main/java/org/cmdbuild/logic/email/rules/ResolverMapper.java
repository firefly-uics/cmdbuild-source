package org.cmdbuild.logic.email.rules;

import static org.apache.commons.lang3.StringUtils.contains;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.email.rules.StartWorkflow.Mapper;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class ResolverMapper extends ForwardingMapper {

	private static final Marker marker = MarkerFactory.getMarker(ResolverMapper.class.getName());

	private static final String SEPARATOR = ":";

	private final Mapper secondary;
	private final CMDataView dataView;

	public ResolverMapper(final Mapper primary, final Mapper secondary, final CMDataView dataView) {
		super(primary);
		this.secondary = secondary;
		this.dataView = dataView;
	}

	@Override
	public Object getValue(final String name) {
		final Object resolved;
		final Object value = super.getValue(name);
		if (value instanceof String) {
			final String stringValue = String.class.cast(value);
			resolved = contains(stringValue, SEPARATOR) ? resolve(stringValue) : secondary.getValue(stringValue);
		} else {
			logger.warn(marker, "cannot resolve non-string value '{}'", value);
			resolved = NULL_VALUE;
		}
		return resolved;
	}

	private Object resolve(final String value) {
		logger.debug(marker, "resolving '{}'", value);
		final String[] tokens = value.split(SEPARATOR);
		final String targetValue = tokens[0];
		final String targetClassName = tokens[1];
		final String targetAttributeName = tokens[2];
		final Object mappedTargetValue = secondary.getValue(targetValue);
		final CMClass targetClass = dataView.findClass(targetClassName);
		try {
			// target class can be null
			final String message = String.format("getting card from class '%s' whose attribute '%s' equals '%s'", //
					targetClassName, targetAttributeName, mappedTargetValue);
			logger.debug(marker, message);
			return dataView.select(attribute(targetClass, targetAttributeName)) //
					.from(targetClass) //
					.where(condition(attribute(targetClass, targetAttributeName), eq(mappedTargetValue))) //
					.run() //
					.getOnlyRow() //
					.getCard(targetClass) //
					.getId();
		} catch (final Exception e) {
			logger.error(marker, "cannot get card", e);
		}
		return NULL_VALUE;
	}

}
