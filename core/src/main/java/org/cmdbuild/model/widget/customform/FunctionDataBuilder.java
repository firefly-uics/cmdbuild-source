package org.cmdbuild.model.widget.customform;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.newHashMap;
import static org.cmdbuild.dao.guava.Functions.toValueSet;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.FunctionCall.call;
import static org.cmdbuild.dao.query.clause.alias.Aliases.name;

import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.entry.CMValueSet;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.function.CMFunction.CMFunctionParameter;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.view.CMDataView;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.base.Function;

public class FunctionDataBuilder implements DataBuilder {

	private static final ObjectMapper mapper = new ObjectMapper();

	private final CMDataView dataView;
	private final String functionName;
	private final Map<String, Object> variables;

	public FunctionDataBuilder(final CMDataView dataView, final String functionName, final Map<String, Object> variables) {
		this.dataView = dataView;
		this.functionName = functionName;
		this.variables = variables;
	}

	@Override
	public String build() {
		final CMFunction function = dataView.findFunctionByName(functionName);
		final Iterable<Object> parameters = from(function.getInputParameters()) //
				.transform(new Function<CMFunction.CMFunctionParameter, Object>() {

					@Override
					public Object apply(final CMFunctionParameter input) {
						return variables.get(input.getName());
					}

				});
		final Alias f = name("f");
		final CMQueryResult result = dataView.select(anyAttribute(function, f)) //
				.from(call(function, parameters), f) //
				.run();
		final List<Map<String, Object>> _result = from(result) //
				.transform(toValueSet(f)) //
				.transform(new Function<CMValueSet, Map<String, Object>>() {

					private final List<CMFunctionParameter> outputParameters = function.getOutputParameters();

					@Override
					public Map<String, Object> apply(final CMValueSet input) {
						final Map<String, Object> output = newHashMap();
						for (final CMFunctionParameter element : outputParameters) {
							final CMAttributeType<?> type = element.getType();
							final String name = element.getName();
							final Object value = input.get(name);
							output.put(name, convert(type, value));
						}
						return output;
					}

					private Object convert(final CMAttributeType<?> type, final Object value) {
						// TODO convert
						return value;
					}

				}) //
				.toList();
		return toJsonString(_result);
	}

	private String toJsonString(final List<Map<String, Object>> object) {
		try {
			return mapper.writeValueAsString(object);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

}
