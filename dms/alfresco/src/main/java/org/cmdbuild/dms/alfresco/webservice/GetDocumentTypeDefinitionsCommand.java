package org.cmdbuild.dms.alfresco.webservice;

import static com.google.common.collect.Collections2.filter;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.startsWith;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.alfresco.webservice.dictionary.DictionaryFault;
import org.alfresco.webservice.dictionary.DictionaryServiceSoapPort;
import org.alfresco.webservice.types.ClassDefinition;
import org.alfresco.webservice.types.PropertyDefinition;
import org.alfresco.webservice.util.Constants;
import org.alfresco.webservice.util.WebServiceFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.cmdbuild.dms.DocumentTypeDefinition;
import org.cmdbuild.dms.MetadataDefinition;
import org.cmdbuild.dms.MetadataGroupDefinition;
import org.cmdbuild.dms.alfresco.utils.CustomModelParser;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

class GetDocumentTypeDefinitionsCommand extends AlfrescoWebserviceCommand<Map<String, DocumentTypeDefinition>> {

	private static class ClassDefinitionWithUriInName implements Predicate<ClassDefinition> {

		private final String uri;

		private ClassDefinitionWithUriInName(final String uri) {
			this.uri = uri;
		}

		@Override
		public boolean apply(final ClassDefinition classDefinition) {
			final String name = classDefinition.getName();
			return startsWith(name, uriInCurlyBrackets());
		}

		private String uriInCurlyBrackets() {
			return Constants.createQNameString(uri, EMPTY);
		}

		public static Predicate<ClassDefinition> of(final String uri) {
			return new ClassDefinitionWithUriInName(uri);
		}

	}

	private static class AspectProperty implements MetadataDefinition {

		private final PropertyDefinition propertyDefinition;

		private AspectProperty(final PropertyDefinition propertyDefinition) {
			this.propertyDefinition = propertyDefinition;
		}

		@Override
		public String getName() {
			return stripValueFromNamespace(propertyDefinition.getName());
		}

		@Override
		public String getDescription() {
			return propertyDefinition.getTitle();
		}

		@Override
		public String getType() {
			return stripValueFromNamespace(propertyDefinition.getDataType());
		}

		@Override
		public boolean isMandatory() {
			return propertyDefinition.isMandatory();
		}

		public static MetadataDefinition of(final PropertyDefinition propertyDefinitions) {
			return new AspectProperty(propertyDefinitions);
		}

		@Override
		public boolean equals(final Object object) {
			if (object == this) {
				return true;
			}
			if (!(object instanceof AspectProperty)) {
				return false;
			}
			final AspectProperty metadataDefinition = AspectProperty.class.cast(object);
			return getName().equals(metadataDefinition.getName());
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(getName()) //
					.toHashCode();
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this) //
					.append("name", getName()) //
					.append("type", getType()) //
					.toString();
		}

		private String stripValueFromNamespace(final String name) {
			return name.replaceAll(ANYTHING_BETWEEN_CURLY_BRACERS, EMPTY);
		}

	}

	private static class AspectDefinition implements MetadataGroupDefinition {

		private final ClassDefinition classDefinition;

		private AspectDefinition(final ClassDefinition classDefinition) {
			this.classDefinition = classDefinition;
		}

		@Override
		public String getName() {
			return strip(classDefinition.getName());
		}

		private String strip(final String name) {
			return name.replaceAll(ANYTHING_BETWEEN_CURLY_BRACERS, EMPTY);
		}

		@Override
		public Iterable<MetadataDefinition> getMetadataDefinitions() {
			final List<MetadataDefinition> metadataDefinitions = Lists.newArrayList();
			for (final PropertyDefinition propertyDefinition : allPropertyDefinitions()) {
				metadataDefinitions.add(AspectProperty.of(propertyDefinition));
			}
			return metadataDefinitions;
		}

		/**
		 * Null-safe
		 */
		private List<PropertyDefinition> allPropertyDefinitions() {
			final PropertyDefinition[] propertyDefinitions = classDefinition.getProperties();
			return (propertyDefinitions == null) ? Collections.<PropertyDefinition> emptyList()
					: asList(propertyDefinitions);
		}

		@Override
		public boolean equals(final Object object) {
			if (object == this) {
				return true;
			}
			if (!(object instanceof AspectDefinition)) {
				return false;
			}
			final AspectDefinition metadataGroupDefinition = AspectDefinition.class.cast(object);
			return getName().equals(metadataGroupDefinition.getName());
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(getName()) //
					.toHashCode();
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this) //
					.append("name", getName()) //
					.toString();
		}

		public static MetadataGroupDefinition of(final ClassDefinition classDefinition) {
			return new AspectDefinition(classDefinition);
		}

	}

	private static class DocumentTypeDefinitionBuilder {

		private String name;
		private final List<MetadataGroupDefinition> metadataGroupDefinitions = Lists.newArrayList();

		public DocumentTypeDefinition build() {
			return new DocumentTypeDefinition() {

				@Override
				public String getName() {
					return name;
				}

				@Override
				public Iterable<MetadataGroupDefinition> getMetadataGroupDefinitions() {
					return metadataGroupDefinitions;
				}

			};
		}

		public void setName(final String name) {
			this.name = name;

		}

		public void add(final MetadataGroupDefinition metadataGroupDefinition) {
			metadataGroupDefinitions.add(metadataGroupDefinition);
		}

	}

	private static final String ANYTHING_BETWEEN_CURLY_BRACERS = "\\{.*\\}";

	private final DictionaryServiceSoapPort dictionaryService;

	private String uri;
	private String prefix;
	private String content;

	public GetDocumentTypeDefinitionsCommand() {
		dictionaryService = WebServiceFactory.getDictionaryService();
	}

	public void setUri(final String uri) {
		this.uri = uri;
	}

	public void setPrefix(final String prefix) {
		this.prefix = prefix;
	}

	public void setCustomModelContent(final String content) {
		this.content = content;
	}

	@Override
	public boolean isSuccessfull() {
		return getResult() != null;
	}

	@Override
	public void execute() {
		Validate.isTrue(StringUtils.isNotBlank(uri), "invalid uri root '%s'", uri);
		Validate.isTrue(StringUtils.isNotBlank(prefix), "invalid prefix '%s'", prefix);
		try {
			final Map<String, DocumentTypeDefinition> result = Maps.newLinkedHashMap();

			final Map<String, MetadataGroupDefinition> aspectDefinitions = aspectDefinitions();
			final Map<String, List<String>> aspectNamesByType = aspectNamesByType();
			for (final String type : aspectNamesByType.keySet()) {
				final DocumentTypeDefinitionBuilder builder = new DocumentTypeDefinitionBuilder();
				builder.setName(type);
				for (final String aspectName : aspectNamesByType.get(type)) {
					final MetadataGroupDefinition aspectDefinition = aspectDefinitions.get(aspectName);
					if (aspectDefinition == null) {
						logger.warn("no aspect definition for expected name '{}'", aspectName);
					} else {
						builder.add(aspectDefinition);
					}
				}
				final DocumentTypeDefinition typeDefinition = builder.build();
				result.put(type, typeDefinition);
			}

			setResult(result);
		} catch (final Exception e) {
			logger.error("error getting class definitions", e);
		}
	}

	private Map<String, MetadataGroupDefinition> aspectDefinitions() throws RemoteException, DictionaryFault {
		final Map<String, MetadataGroupDefinition> aspectDefinitions = Maps.newHashMap();
		for (final ClassDefinition classDefinition : cmdbuildClassDefinitions()) {
			if (classDefinition.isIsAspect()) {
				final MetadataGroupDefinition aspectDefinition = AspectDefinition.of(classDefinition);
				aspectDefinitions.put(aspectDefinition.getName(), aspectDefinition);
			} else /* is custom content type */{
				// we don't need this kind of informations
			}
		}
		return aspectDefinitions;
	}

	private Collection<ClassDefinition> cmdbuildClassDefinitions() throws RemoteException, DictionaryFault {
		final List<ClassDefinition> allClassDefinitions = allClassDefinitions();
		final Collection<ClassDefinition> filteredClassDefinitions = filter(allClassDefinitions,
				ClassDefinitionWithUriInName.of(uri));
		return filteredClassDefinitions;
	}

	/**
	 * null-safe
	 */
	private List<ClassDefinition> allClassDefinitions() throws RemoteException, DictionaryFault {
		final ClassDefinition[] classDefinitions = dictionaryService.getClasses(null, null);
		return (classDefinitions == null) ? Collections.<ClassDefinition> emptyList() : asList(classDefinitions);
	}

	private Map<String, List<String>> aspectNamesByType() {
		return new CustomModelParser(content, prefix).getAspectsByType();
	}

}
