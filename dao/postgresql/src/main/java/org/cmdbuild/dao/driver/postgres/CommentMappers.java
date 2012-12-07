package org.cmdbuild.dao.driver.postgres;

import org.cmdbuild.dao.entrytype.DBAttribute.AttributeMetadata;
import org.cmdbuild.dao.entrytype.DBClass.ClassMetadata;
import org.cmdbuild.dao.entrytype.DBDomain.DomainMetadata;
import org.cmdbuild.dao.entrytype.DBEntryType.EntryTypeMetadata;

class CommentMappers {

	private CommentMappers() {
		// prevents instantiation
	}

	public static final CommentMapper CLASS_COMMENT_MAPPER = new EntryTypeCommentMapper() {

		private static final String SUPERCLASS = "SUPERCLASS";
		private static final String TYPE = "TYPE";

		public static final String TYPE_CLASS = "class";
		public static final String TYPE_SIMPLECLASS = "simpleclass";

		{
			define(DESCR, EntryTypeMetadata.DESCRIPTION);
			define(SUPERCLASS, ClassMetadata.SUPERCLASS);
			define(TYPE, EntryTypeMetadata.HOLD_HISTORY, new CommentValueConverter() {
				@Override
				public String getMetaValueFromComment(final String commentValue) {
					return Boolean.valueOf(!TYPE_SIMPLECLASS.equals(commentValue)).toString();
				}

				@Override
				public String getCommentValueFromMeta(final String metaValue) {
					return Boolean.valueOf(metaValue) ? TYPE_CLASS : TYPE_SIMPLECLASS;
				}
			});
		}
	};

	public static final CommentMapper DOMAIN_COMMENT_MAPPER = new EntryTypeCommentMapper() {
		{
			// Excellent name choice!
			define("LABEL", EntryTypeMetadata.DESCRIPTION);
			define("CLASS1", DomainMetadata.CLASS_1);
			define("CLASS2", DomainMetadata.CLASS_2);
			/*
			 * The descriptions should be the attribute descriptions to support
			 * n-ary domains
			 */
			define("DESCRDIR", DomainMetadata.DESCRIPTION_1);
			define("DESCRINV", DomainMetadata.DESCRIPTION_2);
		}
	};

	public static final CommentMapper ATTRIBUTE_COMMENT_MAPPER = new EntryTypeCommentMapper() {
		{
			define(DESCR, EntryTypeMetadata.DESCRIPTION);
			define("BASEDSP", AttributeMetadata.BASEDSP);
			define("GROUP", AttributeMetadata.GROUP);
			define("INDEX", AttributeMetadata.INDEX);
			define("LOOKUP", AttributeMetadata.LOOKUP_TYPE);
			define("NOTNULL", AttributeMetadata.MANDATORY);
			define("UNIQUE", AttributeMetadata.UNIQUE);
		}
	};

}
