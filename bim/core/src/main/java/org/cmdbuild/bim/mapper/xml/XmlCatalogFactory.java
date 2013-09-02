package org.cmdbuild.bim.mapper.xml;

import java.io.File;
import java.util.List;

import org.cmdbuild.bim.mapper.Parser;
import org.cmdbuild.bim.model.AttributeDefinition;
import org.cmdbuild.bim.model.Catalog;
import org.cmdbuild.bim.model.CatalogFactory;
import org.cmdbuild.bim.model.EntityDefinition;
import org.cmdbuild.bim.model.implementation.AttributeDefinitionFactory;
import org.cmdbuild.bim.model.implementation.EntityDefinitionImpl;
import org.cmdbuild.bim.model.implementation.ListAttributeDefinition;
import org.cmdbuild.bim.model.implementation.ReferenceAttributeDefinition;
import org.cmdbuild.bim.model.implementation.SimpleAttributeDefinition;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.bim.utils.LoggerSupport;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

public class XmlCatalogFactory implements CatalogFactory {

	private static class XmlCatalog implements Catalog {

		/** all the entries of the catalog */
		private final List<EntityDefinition> entities;
		private final List<String> names;

		/**
		 * @param entities
		 *            : a set of EntityDefinitions to assign to the field
		 *            entities of the catalog
		 * */
		public XmlCatalog(List<EntityDefinition> entities, List<String> names) {
			this.entities = entities;
			this.names = names;
		}

		@Override
		public Iterable<EntityDefinition> getEntitiesDefinitions() {
			return entities;
		}

		@Override
		public void printSummary() {
			for (EntityDefinition entity : entities) {
				logger.info("ENTITY " + entity.getTypeName().toUpperCase());
				Iterable<AttributeDefinition> attributes = entity.getAttributes();
				for (AttributeDefinition attribute : attributes) {
					if (attribute instanceof ListAttributeDefinition && attribute.getReference().isValid()) {
						logger.info("LIST OF --> " + attribute.getName());
					} else if (attribute instanceof ReferenceAttributeDefinition) {
						logger.info("--> " + attribute.getName());
					} else if (attribute instanceof SimpleAttributeDefinition) {
						logger.info(attribute.getName());
					} else {
						throw new BimError("Error printing attribute " + attribute);
					}
				}
			}
		}

		@Override
		public EntityDefinition getEntityDefinition(int i) {
			return entities.get(i);
		}

		@Override
		public int getSize() {
			return entities.size();
		}

		@Override
		public boolean contains(String entityDefintionName) {
			return names.contains(entityDefintionName);
		}
		
		@Override
		public List<Integer> getPositionsOf(String entityDefintionName){
			int maxsize = getSize();
			List<Integer> indices = Lists.newArrayList();
			for(int i=0; i < maxsize; i++){
				String name = names.get(i);
				if(name.equals(entityDefintionName)){
					indices.add(i);
				}
			}
			return indices;
		}
	}
	
	private static final Logger logger = LoggerSupport.logger;
	private final Parser parser;
	private final List<EntityDefinition> entities;
	private final List<String> names = Lists.newArrayList();

	public XmlCatalogFactory(final File xmlFile) {
		parser = new XmlParser(xmlFile);
		entities = Lists.newArrayList();
	}

	public XmlCatalogFactory(final String xmlString) {
		parser = new XmlParser(xmlString);
		entities = Lists.newArrayList();
	}

	@Override
	public Catalog create() {
		parseEntities();
		return new XmlCatalog(entities, names);
	}
	
	
	
	
	/**
	 * This method populates the catalog according to the XML file of the the
	 * parser
	 * */
	private void parseEntities() {
		String path = XmlParser.ROOT;
		try {
			int numberOfTypesToRead = parser.getNumberOfNestedEntities(path);
			logger.info(numberOfTypesToRead + " entries");
			for (int i = 1; i <= numberOfTypesToRead; i++) {
				path = XmlParser.ROOT + "/entity[" + i + "]";
				String name = parser.getEntityName(XmlParser.ROOT + "/entity[" + i + "]");
				EntityDefinition entityDefinition = new EntityDefinitionImpl(name);
				String label = parser.getEntityLabel(path);
				logger.info("{} - {}", name, label);
				entityDefinition.setLabel(label);
				readEntity(entityDefinition, path);
				entities.add(entityDefinition);
				names.add(name);
			}
		} catch (BimError e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * This method sets the attributes of entityDefinition according to the XML
	 * file of the parser. If a nested entity is found the method is called
	 * recursively.
	 * 
	 * @param entityDefinition
	 *            : the entry of the catalog which has to be built
	 * @param path
	 *            : a string which the parser uses in order to read the right
	 *            piece of the XML file
	 * */
	private void readEntity(EntityDefinition entityDefinition, String path) {
		//Iterable<String> attributesNames = parser.getAttributesNames(path);
		//for (String attributeName : attributesNames) {
		for(int i = 1; i <= parser.getNumberOfAttributes(path); i++){
			String type = parser.getAttributeType(path, i);
			String label = parser.getAttributeLabel(path, i);
			String value = parser.getAttributeValue(path, i);
			String attributeName = parser.getAttributeName(path, i);
			AttributeDefinitionFactory factory = new AttributeDefinitionFactory(type);
			AttributeDefinition attributeDefinition = factory.createAttribute(attributeName);
			attributeDefinition.setLabel(label);
			if (!value.equals("")) {
				((SimpleAttributeDefinition) attributeDefinition).setValue(value);
			}
			entityDefinition.getAttributes().add(attributeDefinition);
			if (attributeDefinition instanceof ReferenceAttributeDefinition) {
				String path_tmp = path;
				path = path + "/attributes/attribute[@name = '" + attributeName + "']";
				int numberOfNestedEntities = parser.getNumberOfNestedEntities(path);
				if (numberOfNestedEntities != 1) {
					throw new BimError("Expected 1 nested entity, found " + numberOfNestedEntities);
				}
				EntityDefinition referencedEntityDefinition = new EntityDefinitionImpl("");
				((ReferenceAttributeDefinition) attributeDefinition).setReference(referencedEntityDefinition);
				path = path + "/entity";
				readEntity(referencedEntityDefinition, path);
				path = path_tmp;
			} else if (attributeDefinition instanceof ListAttributeDefinition) {
				String path_tmp = path;
				path = path + "/attributes/attribute[@name = '" + attributeName + "']";
				int numberOfNestedEntities = parser.getNumberOfNestedEntities(path);
				if (numberOfNestedEntities == 0) {
				} else if (numberOfNestedEntities > 0) {
					for (int j = 1; j <= numberOfNestedEntities; j++) {
						String path0 = path;
						path = path + "/entity[" + j + "]";
						EntityDefinition referencedEntityDefinition = new EntityDefinitionImpl("");
						((ListAttributeDefinition) attributeDefinition).setReference(referencedEntityDefinition);
						((ListAttributeDefinition) attributeDefinition).getAllReferences().add(
								referencedEntityDefinition);
						readEntity(referencedEntityDefinition, path);
						path = path0;
					}
				} else {
					throw new BimError("error reading reference list " + attributeName);
				}
				path = path_tmp;
			}
		}
	}

}
