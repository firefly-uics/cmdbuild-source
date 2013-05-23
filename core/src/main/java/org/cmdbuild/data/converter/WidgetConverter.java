package org.cmdbuild.data.converter;

import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.data.store.DataViewStore.BaseStorableConverter;
import org.cmdbuild.model.widget.Widget;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.google.common.collect.Maps;

public class WidgetConverter extends BaseStorableConverter<Widget> {

	private static final String DESCRIPTION_ATTRIBUTE = "Description";
	private static final String CODE_ATTRIBUTE = "Code";

	@Override
	public Widget convert(final CMCard card) {
		Widget widget = null;
		try {
			final ObjectMapper mapper = new ObjectMapper();
			widget = mapper.readValue((String) card.get(DESCRIPTION_ATTRIBUTE), new TypeReference<Widget>() {
			});
			widget.setTargetClass((String) card.getCode());
			widget.setId(card.getId());
		} catch (final Exception ex) {
			// TODO: empty for now. Log it
		}
		return widget;
	}

	@Override
	public Map<String, Object> getValues(final Widget widget) {
		final Map<String, Object> result = Maps.newHashMap();
		final ObjectMapper mapper = new ObjectMapper();
		result.put(CODE_ATTRIBUTE, widget.getTargetClass());
		try {
			result.put(DESCRIPTION_ATTRIBUTE, mapper.writeValueAsString(widget));
		} catch (final Exception ex) {
			// TODO: empty for now. Log it
		}
		return result;
	}

	@Override
	public String getClassName() {
		return "_Widget";
	}

}
