package org.cmdbuild.servlets.json.serializers;

import java.util.Collection;
import java.util.List;

import org.cmdbuild.dms.DocumentTypeDefinition;
import org.cmdbuild.dms.MetadataDefinition;
import org.cmdbuild.dms.MetadataGroupDefinition;
import org.cmdbuild.elements.Lookup;

import com.google.common.collect.Lists;

public class Attachments {

	private Attachments() {
		// prevents instantiation
	}

	public static final class JsonCategoryDefinition {

		private final Lookup lookup;
		private final DocumentTypeDefinition documentTypeDefinition;

		private JsonCategoryDefinition(final Lookup lookup, final DocumentTypeDefinition documentTypeDefinition) {
			this.lookup = lookup;
			this.documentTypeDefinition = documentTypeDefinition;
		}

		public String getName() {
			return lookup.getCode();
		}

		public String getDescription() {
			return lookup.getDescription();
		}

		public Iterable<JsonMetadataGroupDefinition> getMetadataGroups() {
			final List<JsonMetadataGroupDefinition> jsonDefinitions = Lists.newArrayList();
			for (final MetadataGroupDefinition definition : documentTypeDefinition.getMetadataGroupDefinitions()) {
				jsonDefinitions.add(JsonMetadataGroupDefinition.from(definition));
			}
			return jsonDefinitions;
		}

		public static JsonCategoryDefinition from(final Lookup lookup,
				final DocumentTypeDefinition documentTypeDefinition) {
			return new JsonCategoryDefinition(lookup, documentTypeDefinition);
		}

	}

	public static final class JsonMetadataGroupDefinition {

		private final MetadataGroupDefinition metadataGroupDefinition;

		private JsonMetadataGroupDefinition(final MetadataGroupDefinition definition) {
			this.metadataGroupDefinition = definition;
		}

		public String getName() {
			return metadataGroupDefinition.getName();
		}

		public Iterable<JsonMetadataDefinition> getMetadata() {
			final List<JsonMetadataDefinition> jsonDefinitions = Lists.newArrayList();
			for (final MetadataDefinition definition : metadataGroupDefinition.getMetadataDefinitions()) {
				jsonDefinitions.add(JsonMetadataDefinition.from(definition));
			}
			return jsonDefinitions;
		}

		public static JsonMetadataGroupDefinition from(final MetadataGroupDefinition metadataGroupDefinition) {
			return new JsonMetadataGroupDefinition(metadataGroupDefinition);
		}

	}

	public static class JsonMetadataDefinition {

		private final MetadataDefinition metadataDefinition;

		private JsonMetadataDefinition(final MetadataDefinition metadataDefinition) {
			this.metadataDefinition = metadataDefinition;
		}

		public String getName() {
			return metadataDefinition.getName();
		}

		public static JsonMetadataDefinition from(final MetadataDefinition metadataDefinition) {
			return new JsonMetadataDefinition(metadataDefinition);
		}

	}

	public static final class JsonAttachmentsContext {

		private final Iterable<JsonCategoryDefinition> categoriesDefinition;

		private JsonAttachmentsContext(final Iterable<JsonCategoryDefinition> categoriesDefinition) {
			this.categoriesDefinition = categoriesDefinition;
		}

		public Iterable<JsonCategoryDefinition> getCategories() {
			return categoriesDefinition;
		}

		public static JsonAttachmentsContext from(final Collection<JsonCategoryDefinition> categoriesDefinition) {
			return new JsonAttachmentsContext(categoriesDefinition);
		}

	}

}