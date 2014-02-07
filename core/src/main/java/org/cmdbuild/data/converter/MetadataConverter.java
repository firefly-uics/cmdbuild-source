package org.cmdbuild.data.converter;

import static java.lang.String.format;
import static org.cmdbuild.dao.driver.postgres.Const.ID_ATTRIBUTE;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.Code;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.Description;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.Notes;

import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.data.store.DataViewStore.StorableConverter;
import org.cmdbuild.data.store.Store.Storable;
import org.cmdbuild.model.data.Metadata;
import org.cmdbuild.services.meta.MetadataService;

import com.google.common.collect.Maps;

public class MetadataConverter implements StorableConverter<Metadata> {

	private final String groupAttributeValue;

	public MetadataConverter(final CMAttribute attribute) {
		final StringBuilder sb = new StringBuilder();
		final CMEntryType owner = attribute.getOwner();
		if (owner != null) {
			final CMIdentifier identifier = owner.getIdentifier();
			if (identifier.getNameSpace() != CMIdentifier.DEFAULT_NAMESPACE) {
				sb.append(format("%s.", identifier.getNameSpace()));
			}
			sb.append(format("%s.", identifier.getLocalName()));
			sb.append(attribute.getName());
		}

		this.groupAttributeValue = sb.toString();
	}

	@Override
	public String getClassName() {
		return MetadataService.METADATA_CLASS_NAME;
	}

	@Override
	public String getGroupAttributeName() {
		return Code.getDBName();
	}

	@Override
	public Object getGroupAttributeValue() {
		return groupAttributeValue;
	}

	@Override
	public String getIdentifierAttributeName() {
		return Description.getDBName();
	}

	@Override
	public Storable storableOf(final CMCard card) {
		return new Storable() {

			@Override
			public String getIdentifier() {
				final String attributeName = getIdentifierAttributeName();
				final String value;
				if (ID_ATTRIBUTE.equals(attributeName)) {
					value = Long.toString(card.getId());
				} else {
					value = card.get(getIdentifierAttributeName(), String.class);
				}
				return value;
			}

		};
	}

	@Override
	public Metadata convert(final CMCard card) {
		final String name = (String) card.getDescription();
		final String value = (String) card.get(Notes.getDBName());
		return new Metadata(name, value);
	}

	@Override
	public Map<String, Object> getValues(final Metadata storable) {
		final Map<String, Object> values = Maps.newHashMap();
		values.put(Code.getDBName(), groupAttributeValue);
		values.put(Description.getDBName(), storable.name);
		values.put(Notes.getDBName(), storable.value);
		return values;
	}

	@Override
	public String getUser(final Metadata storable) {
		return SYSTEM_USER;
	}

}
