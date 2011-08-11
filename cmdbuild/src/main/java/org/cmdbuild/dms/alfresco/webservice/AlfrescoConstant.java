package org.cmdbuild.dms.alfresco.webservice;

import org.alfresco.webservice.types.NamedValue;
import org.alfresco.webservice.types.ResultSetRow;
import org.alfresco.webservice.util.Constants;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.dms.alfresco.AlfrescoClient;

public enum AlfrescoConstant {

	NULL("") {
		@Override
		public void setInBean(final StoredDocument storedDocument, final NamedValue namedValue,
				final AlfrescoClient client) {
			// nothing to do
		}
	},
	NAME(Constants.PROP_NAME) {
		@Override
		public void setInBean(final StoredDocument storedDocument, final NamedValue namedValue,
				final AlfrescoClient client) {
			storedDocument.setName(namedValue.getValue());
		}
	},
	CREATED(Constants.PROP_CREATED) {
		@Override
		public void setInBean(final StoredDocument storedDocument, final NamedValue namedValue,
				final AlfrescoClient client) {
			storedDocument.setCreated(DateUtils.parse(namedValue.getValue()));
		}
	},
	DESCR(Constants.PROP_DESCRIPTION) {
		@Override
		public void setInBean(final StoredDocument storedDocument, final NamedValue namedValue,
				final AlfrescoClient client) {
			storedDocument.setDescription(namedValue.getValue());
		}
	},
	MODIFIED("{http://www.alfresco.org/model/content/1.0}modified") {
		@Override
		public void setInBean(final StoredDocument storedDocument, final NamedValue namedValue,
				final AlfrescoClient client) {
			storedDocument.setModified(DateUtils.parse(namedValue.getValue()));
		}
	},
	CATEGORIES("{http://www.alfresco.org/model/content/1.0}categories") {
		@Override
		public void setInBean(final StoredDocument storedDocument, final NamedValue namedValue,
				final AlfrescoClient client) {
			final String[] paths = namedValue.getValues();
			final String strip = "workspace://SpacesStore/";
			for (String uuid : paths) {
				final int idx = uuid.indexOf(strip);
				uuid = uuid.substring(idx + strip.length());
					final ResultSetRow row = client.searchRow(uuid);
					if (row != null) {
						final NamedValue[] namedValues = row.getColumns();
						for (final NamedValue nv : namedValues) {
							if (NAME.isName(nv.getName())) {
								storedDocument.setCategory(nv.getValue());
							}
						}
					}
			}
		}
	},
	VERSION("{http://www.alfresco.org/model/content/1.0}versionLabel") {
		@Override
		public void setInBean(final StoredDocument storedDocument, final NamedValue namedValue,
				final AlfrescoClient client) {
			storedDocument.setVersion(namedValue.getValue());
		}
	},
	PATH("{http://www.alfresco.org/model/content/1.0}path") {
		@Override
		public void setInBean(final StoredDocument storedDocument, final NamedValue namedValue,
				final AlfrescoClient client) {
			storedDocument.setPath(namedValue.getValue());
		}
	},
	AUTHOR("{http://www.alfresco.org/model/content/1.0}author") {
		@Override
		public void setInBean(final StoredDocument storedDocument, final NamedValue namedValue,
				final AlfrescoClient client) {
			storedDocument.setAuthor(namedValue.getValue());
		}
	},
	UUID("{http://www.alfresco.org/model/system/1.0}node-uuid") {
		@Override
		public void setInBean(final StoredDocument storedDocument, final NamedValue namedValue,
				final AlfrescoClient client) {
			storedDocument.setUuid(namedValue.getValue());
		}
	};

	private final String name;

	private AlfrescoConstant(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public boolean isName(final String name) {
		return this.name.equals(name);
	}

	public static AlfrescoConstant fromName(final String name) {
		for (final AlfrescoConstant ac : AlfrescoConstant.values()) {
			if (ac.isName(name))
				return ac;
		}
		return NULL;
	}

	public abstract void setInBean(StoredDocument storedDocument, NamedValue namedValue, AlfrescoClient client);

}
