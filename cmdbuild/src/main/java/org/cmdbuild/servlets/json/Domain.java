package org.cmdbuild.servlets.json;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESTINATION;
import static org.cmdbuild.servlets.json.CommunicationConstants.DOMAIN;
import static org.cmdbuild.servlets.json.CommunicationConstants.SOURCE;
import static org.cmdbuild.servlets.json.schema.Utils.toIterable;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.json.JSONArray;

public class Domain extends JSONBaseWithSpringContext {

	private static class DisabledClasses {

		private final List<String> source;
		private final List<String> destination;

		public DisabledClasses(final List<String> source, final List<String> destination) {
			this.source = source;
			this.destination = destination;
		}

		@JsonProperty(SOURCE)
		public List<String> getSource() {
			return source;
		}

		@JsonProperty(DESTINATION)
		public List<String> getDestination() {
			return destination;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof DisabledClasses)) {
				return false;
			}
			final DisabledClasses other = DisabledClasses.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.source, other.source) //
					.append(this.destination, other.destination) //
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(this.source) //
					.append(this.destination) //
					.toHashCode();
		}

		@Override
		public final String toString() {
			return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	private static final Map<String, DisabledClasses> map = newHashMap();

	public JsonResponse getDisabledClasses( //
			@Parameter(value = DOMAIN) final String domain //
	) {
		// TODO call logic
		return JsonResponse.success(map.get(domain));
	}

	public JsonResponse setDisabledClasses( //
			@Parameter(value = DOMAIN) final String domain, //
			@Parameter(value = SOURCE) final JSONArray source, //
			@Parameter(value = DESTINATION) final JSONArray destination //
	) {
		// TODO call logic
		map.put(domain, new DisabledClasses(newArrayList(toIterable(source)), newArrayList(toIterable(source))));
		return JsonResponse.success();
	}
}
