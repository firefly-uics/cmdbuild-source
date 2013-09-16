package org.cmdbuild.bim.mapper.xml;

import java.io.File;
import java.util.List;

import org.cmdbuild.bim.mapper.Parser;
import org.cmdbuild.bim.model.Catalog;
import org.cmdbuild.bim.model.CatalogFactory;
import org.cmdbuild.bim.model.EntityDefinition;
import org.cmdbuild.bim.model.implementation.EntityDefinitionExportImpl;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.bim.utils.LoggerSupport;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

public class XmlCatalogExportFactory implements CatalogFactory {

	private static class XmlCatalog implements Catalog {

		/** all the entries of the catalog */
		private final List<EntityDefinition> entities;

		/**
		 * @param entities
		 *            : a set of EntityDefinitions to assign to the field
		 *            entities of the catalog
		 * */
		public XmlCatalog(final List<EntityDefinition> entities) {
			this.entities = entities;
		}

		@Override
		public Iterable<EntityDefinition> getEntitiesDefinitions() {
			return entities;
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
			// TODO Auto-generated method stub
			throw new BimError("NON IMPLEMENTATO!");
		}

		@Override
		public List<Integer> getPositionsOf(String entityDefintionName) {
			// TODO Auto-generated method stub
			throw new BimError("NON IMPLEMENTATO!");    
		}
	}
	
	private static final Logger logger = LoggerSupport.logger;
	private final Parser parser;
	private final List<EntityDefinition> entities;

	/**
	 * Constructor: it returns an object with the parser ready to read the
	 * xmlFile and an empty set of entities
	 * 
	 * @param xmlFile
	 *            : the XML file from which the parser will build the catalog
	 * */
	public XmlCatalogExportFactory(final File xmlFile) {
		parser = new XmlParser(xmlFile);
		entities = Lists.newArrayList();
	}

	
	public XmlCatalogExportFactory(final String xmlString) {
		parser = new XmlParser(xmlString);
		entities = Lists.newArrayList();
	}
	
	@Override
	public Catalog create() {
		parseEntities();
		return new XmlCatalog(entities);
	}
	
	/**
	 * This method populates the catalog according to the XML file of the the
	 * parser
	 * */
	private void parseEntities() {
		String path = XmlParser.ROOT;
		try {
			int numberOfTypesToRead = parser.getNumberOfNestedEntities(path);
			logger.info("" + numberOfTypesToRead);
			for (int i = 1; i <= numberOfTypesToRead; i++) {
				path = XmlParser.ROOT + "/entity[" + i + "]";
				String name = parser.getEntityName(path);
				EntityDefinition entityDefinition = new EntityDefinitionExportImpl(name);
				String label = parser.getEntityLabel(path);
				String shape = parser.getEntityShape(path);
				logger.info("Reading class  " + name + " corresponding to " + label + " with shape " + shape);
				entityDefinition.setLabel(label);
				entityDefinition.setShape(shape);
				entities.add(entityDefinition);
			}
		} catch (BimError e) {
			logger.error(e.getMessage());
		}
	}

}
