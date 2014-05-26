package org.cmdbuild.logic.taskmanager;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.data.store.lookup.Predicates.lookupTypeWithName;
import static org.cmdbuild.data.store.lookup.Predicates.lookupWithDescription;

import java.util.Map;

import org.cmdbuild.dao.entry.LookupValue;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.NullAttributeTypeVisitor;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.services.sync.store.ClassType;
import org.cmdbuild.services.sync.store.internal.AttributeValueAdapter;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

public class DefaultAttributeValueAdapter implements AttributeValueAdapter {

	private final CMDataView dataView;
	private final LookupStore lookupStore;

	public DefaultAttributeValueAdapter(final CMDataView dataView, final LookupStore lookupStore) {
		this.dataView = dataView;
		this.lookupStore = lookupStore;
	}

	@Override
	public Iterable<Map.Entry<String, Object>> toInternal(final ClassType type,
			final Iterable<? extends Map.Entry<String, ? extends Object>> values) {
		final String typeName = type.getName();
		final CMClass targetType = dataView.findClass(typeName);
		// TODO handle class not found
		final Map<String, Object> adapted = Maps.newHashMap();
		for (final Map.Entry<String, ? extends Object> entry : values) {
			final String attributeName = entry.getKey();
			final Object attributeValue = entry.getValue();
			new NullAttributeTypeVisitor() {

				private Object adaptedValue;

				public void adapt() {
					adaptedValue = attributeValue;
					targetType.getAttribute(attributeName).getType().accept(this);
					adapted.put(attributeName, adaptedValue);
				}

				@Override
				public void visit(final LookupAttributeType attributeType) {
					final String lookupTypeName = attributeType.getLookupTypeName();
					final LookupType lookupType = from(lookupStore.readAllTypes()) //
							.filter(lookupTypeWithName(lookupTypeName)) //
							.first() //
							.get();
					final String shouldBeDescription = attributeValue.toString();
					final Optional<Lookup> lookup = from(lookupStore.readAll(lookupType)) //
							.filter(lookupWithDescription(shouldBeDescription)) //
							.first();
					adaptedValue = lookup.isPresent() ? lookup.get().getId() : null;
				}

			}.adapt();
		}
		return adapted.entrySet();
	}

	@Override
	public Iterable<Map.Entry<String, Object>> toSynchronizer(final ClassType type,
			final Iterable<? extends Map.Entry<String, ? extends Object>> values) {
		final String typeName = type.getName();
		final CMClass targetType = dataView.findClass(typeName);
		// TODO handle class not found
		final Map<String, Object> adapted = Maps.newHashMap();
		for (final Map.Entry<String, ? extends Object> entry : values) {
			final String attributeName = entry.getKey();
			final Object attributeValue = entry.getValue();
			new NullAttributeTypeVisitor() {

				private Object adaptedValue;

				public void adapt() {
					adaptedValue = attributeValue;
					targetType.getAttribute(attributeName).getType().accept(this);
					adapted.put(attributeName, adaptedValue);
				}

				@Override
				public void visit(final LookupAttributeType attributeType) {
					final LookupValue lookupValue = LookupValue.class.cast(attributeValue);
					adaptedValue = lookupValue.getDescription();
				}

			}.adapt();
		}
		return adapted.entrySet();
	}

}