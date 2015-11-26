package org.cmdbuild.service.rest.v2.cxf;

import static com.google.common.base.Predicates.equalTo;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Ordering.from;
import static org.cmdbuild.dao.entrytype.Predicates.functionId;
import static org.cmdbuild.service.rest.v2.model.Models.newAttribute;
import static org.cmdbuild.service.rest.v2.model.Models.newFunctionWithBasicDetails;
import static org.cmdbuild.service.rest.v2.model.Models.newFunctionWithFullDetails;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseSingle;

import java.util.Comparator;

import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.function.CMFunction.CMFunctionParameter;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.service.rest.v2.Functions;
import org.cmdbuild.service.rest.v2.cxf.serialization.AttributeTypeResolver;
import org.cmdbuild.service.rest.v2.model.Attribute;
import org.cmdbuild.service.rest.v2.model.FunctionWithBasicDetails;
import org.cmdbuild.service.rest.v2.model.FunctionWithFullDetails;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;

import com.google.common.base.Function;
import com.google.common.base.Optional;

public class CxfFunctions implements Functions {

	private static final Comparator<CMFunction> ID_ASC = new Comparator<CMFunction>() {

		@Override
		public int compare(final CMFunction o1, final CMFunction o2) {
			return o1.getId().compareTo(o2.getId());
		}

	};

	private final ErrorHandler errorHandler;
	/**
	 * @deprecated enclose within a logic
	 */
	@Deprecated
	private final CMDataView dataView;

	public CxfFunctions(final ErrorHandler errorHandler, final CMDataView dataView) {
		this.errorHandler = errorHandler;
		this.dataView = dataView;
	}

	@Override
	public ResponseMultiple<FunctionWithBasicDetails> readAll(final Integer limit, final Integer offset) {
		final Iterable<? extends CMFunction> functions = dataView.findAllFunctions();
		final Iterable<? extends CMFunction> ordered = from(ID_ASC).sortedCopy(functions);
		final Iterable<FunctionWithBasicDetails> elements = from(ordered) //
				.skip((offset == null) ? 0 : offset) //
				.limit((limit == null) ? Integer.MAX_VALUE : limit) //
				.transform(new Function<CMFunction, FunctionWithBasicDetails>() {

					@Override
					public FunctionWithBasicDetails apply(final CMFunction input) {
						return newFunctionWithBasicDetails() //
								.withId(input.getId()) //
								.withName(input.getName()) //
								.withDescription(input.getName()) //
								.build();
					}

				});
		return newResponseMultiple(FunctionWithBasicDetails.class) //
				.withElements(elements) //
				.withMetadata(newMetadata() //
						.withTotal(size(ordered)) //
						.build()) //
				.build();
	}

	@Override
	public ResponseSingle<FunctionWithFullDetails> read(final Long functionId) {
		final Optional<? extends CMFunction> function = from(dataView.findAllFunctions()) //
				.filter(functionId(equalTo(functionId))) //
				.first();
		if (!function.isPresent()) {
			errorHandler.functionNotFound(functionId);
		}
		final FunctionWithFullDetails element = new Function<CMFunction, FunctionWithFullDetails>() {

			@Override
			public FunctionWithFullDetails apply(final CMFunction input) {
				return newFunctionWithFullDetails() //
						.withId(input.getId()) //
						.withName(input.getName()) //
						.withDescription(input.getName()) //
						.build();
			}

		}.apply(function.get());
		return newResponseSingle(FunctionWithFullDetails.class) //
				.withElement(element) //
				.build();
	}

	@Override
	public ResponseMultiple<Attribute> readInputParameters(final Long functionId, final Integer limit,
			final Integer offset) {
		final Optional<? extends CMFunction> function = from(dataView.findAllFunctions()) //
				.filter(functionId(equalTo(functionId))) //
				.first();
		if (!function.isPresent()) {
			errorHandler.functionNotFound(functionId);
		}
		final Iterable<CMFunctionParameter> parameters = function.get().getInputParameters();
		return serialize(parameters, limit, offset);
	}

	@Override
	public ResponseMultiple<Attribute> readOutputParameters(final Long functionId, final Integer limit,
			final Integer offset) {
		final Optional<? extends CMFunction> function = from(dataView.findAllFunctions()) //
				.filter(functionId(equalTo(functionId))) //
				.first();
		if (!function.isPresent()) {
			errorHandler.functionNotFound(functionId);
		}
		final Iterable<CMFunctionParameter> parameters = function.get().getOutputParameters();
		return serialize(parameters, limit, offset);
	}

	private ResponseMultiple<Attribute> serialize(final Iterable<CMFunctionParameter> parameters, final Integer limit,
			final Integer offset) {
		final Iterable<Attribute> elements = from(parameters) //
				.skip((offset == null) ? 0 : offset) //
				.limit((limit == null) ? Integer.MAX_VALUE : limit) //
				.transform(new Function<CMFunctionParameter, Attribute>() {

					@Override
					public Attribute apply(final CMFunctionParameter input) {
						return newAttribute() //
								.withId(input.getName()) //
								.withName(input.getName()) //
								.withDescription(input.getName()) //
								.withType(new AttributeTypeResolver().resolve(input.getType()).asString()) //
								.build();
					}

				});
		return newResponseMultiple(Attribute.class) //
				.withElements(elements) //
				.withMetadata(newMetadata() //
						.withTotal(size(parameters)) //
						.build()) //
				.build();
	}

}
