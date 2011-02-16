package org.cmdbuild.connector;

import java.util.Properties;
import java.util.SortedSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.cmdbuild.connector.collections.ConnectorDataCollection;
import org.cmdbuild.connector.collections.ConnectorSchemaCollection;
import org.cmdbuild.connector.collections.IdDataCollection;
import org.cmdbuild.connector.configuration.ConfigurationException;
import org.cmdbuild.connector.configuration.ConnectorConfiguration;
import org.cmdbuild.connector.data.ConnectorCard;
import org.cmdbuild.connector.data.ConnectorClass;
import org.cmdbuild.connector.data.ConnectorDomain;
import org.cmdbuild.connector.data.ConnectorRelation;
import org.cmdbuild.connector.differ.ConnectorCardsDiffer;
import org.cmdbuild.connector.differ.ConnectorRelationsDiffer;
import org.cmdbuild.connector.differ.DifferAdapter;
import org.cmdbuild.connector.differ.DifferEvent;
import org.cmdbuild.connector.differ.DifferException;
import org.cmdbuild.connector.logger.Log;
import org.cmdbuild.connector.parser.Parser;
import org.cmdbuild.connector.parser.ParserAdapter;
import org.cmdbuild.connector.parser.ParserEvent;
import org.cmdbuild.connector.ws.CMDBuildParser;
import org.cmdbuild.connector.ws.CMDBuildSync;

public class Connector {

	protected static final Logger logger = Log.CONNECTOR;

	private ConnectorConfiguration configuration;

	public void setConfiguration(final ConnectorConfiguration configuration) {
		Validate.notNull(configuration, "null configuration");
		this.configuration = configuration;
	}

	public void execute() throws ConnectorException {
		final String className = configuration.getExternalModule();
		final ExternalModuleLoader<Parser> parserLoader = new ExternalModuleLoader<Parser>(className);
		final Parser externalParser = parserLoader.load();

		final ConnectorSchemaCollection externalSchemaCollection = new ConnectorSchemaCollection();
		final ConnectorDataCollection externalDataCollection = new ConnectorDataCollection();
		final ConnectorDataCollection cmdbuildDataCollection = new ConnectorDataCollection();
		final IdDataCollection idDataCollection = new IdDataCollection();

		externalParser.addListener(new DataListener(externalDataCollection));
		externalParser.addListener(new ParserAdapter() {

			@Override
			public void classFound(final ParserEvent<ConnectorClass> event) {
				final ConnectorClass connectorClass = event.getValue();
				externalSchemaCollection.addConnectorClass(connectorClass);
			}

			@Override
			public void domainFound(final ParserEvent<ConnectorDomain> event) {
				final ConnectorDomain connectorDomain = event.getValue();
				externalSchemaCollection.addConnectorDomain(connectorDomain);
			}

		});

		externalParser.parseSchema();

		final CMDBuildParser cmdbuildParser = new CMDBuildParser(configuration.getCMDBuildURL(), configuration
				.getCMDBuildUsername(), configuration.getCMDBuildPassword(), idDataCollection);
		cmdbuildParser.addListener(new DataListener(cmdbuildDataCollection, idDataCollection));
		cmdbuildParser.setSchema(externalSchemaCollection);
		externalParser.parse();
		cmdbuildParser.parse();

		final CMDBuildSync sync = new CMDBuildSync(configuration.getCMDBuildURL(), configuration.getCMDBuildUsername(),
				configuration.getCMDBuildPassword(), idDataCollection, externalDataCollection);
		syncCards(sync, externalSchemaCollection, externalDataCollection, cmdbuildDataCollection);
		syncRelations(sync, externalSchemaCollection, externalDataCollection, cmdbuildDataCollection);
	}

	private void syncCards(final CMDBuildSync sync, final ConnectorSchemaCollection externalSchemaCollection,
			final ConnectorDataCollection externalDataCollection, final ConnectorDataCollection cmdbuildDataCollection)
			throws DifferException {
		for (final ConnectorClass connectorClass : externalSchemaCollection.getConnectorClasses()) {
			logger.debug("calculating differences for class " + connectorClass.getName());
			final SortedSet<ConnectorCard> externalConnectorCards = externalDataCollection
					.getConnectorCards(connectorClass);
			logger.debug("externals cards #" + externalConnectorCards.size());
			final SortedSet<ConnectorCard> cmdbuildConnectorCards = cmdbuildDataCollection
					.getConnectorCards(connectorClass);
			logger.debug("cmdbuild cards #" + cmdbuildConnectorCards.size());

			final ConnectorCardsDiffer cardsDiffer = new ConnectorCardsDiffer(externalConnectorCards,
					cmdbuildConnectorCards);
			cardsDiffer.addListener(new DifferAdapter<ConnectorCard>() {

				@Override
				public void addItem(final DifferEvent<ConnectorCard> event) {
					final ConnectorCard value = event.getValue();
					logger.debug("card to be added " + value);
					sync.createCard(value);
				}

				@Override
				public void modifyItem(final DifferEvent<ConnectorCard> event) {
					final ConnectorCard value = event.getValue();
					logger.debug("card to be modified " + value);
					sync.modifyCard(value);
				}

				@Override
				public void removeItem(final DifferEvent<ConnectorCard> event) {
					final ConnectorCard value = event.getValue();
					logger.debug("card to be removed " + value);
					sync.deleteCard(value);
				}

			});
			cardsDiffer.diff();
		}
	}

	private void syncRelations(final CMDBuildSync sync, final ConnectorSchemaCollection externalSchemaCollection,
			final ConnectorDataCollection externalDataCollection, final ConnectorDataCollection cmdbuildDataCollection)
			throws DifferException {
		for (final ConnectorDomain connectorDomain : externalSchemaCollection.getConnectorDomains()) {
			logger.debug("calculating differences for domain " + connectorDomain.getName());
			final SortedSet<ConnectorRelation> externalConnectorRelations = externalDataCollection
					.getConnectorRelations(connectorDomain);
			logger.debug("externals relations #" + externalConnectorRelations.size());
			final SortedSet<ConnectorRelation> cmdbuildConnectorRelations = cmdbuildDataCollection
					.getConnectorRelations(connectorDomain);
			logger.debug("cmdbuild relations #" + cmdbuildConnectorRelations.size());

			final ConnectorRelationsDiffer differ = new ConnectorRelationsDiffer(externalConnectorRelations,
					cmdbuildConnectorRelations);
			differ.addListener(new DifferAdapter<ConnectorRelation>() {

				@Override
				public void addItem(final DifferEvent<ConnectorRelation> event) {
					final ConnectorRelation value = event.getValue();
					logger.debug("relation to be added " + value);
					sync.createRelation(value);
				}

				@Override
				public void removeItem(final DifferEvent<ConnectorRelation> event) {
					final ConnectorRelation value = event.getValue();
					logger.debug("relation to be removed " + value);
					sync.deleteRelation(value);
				}

			});
			differ.diff();
		}
	}

	private class DataListener extends ParserAdapter {

		private final ConnectorDataCollection collection;
		private final IdDataCollection idDataCollection;

		public DataListener(final ConnectorDataCollection collection) {
			this(collection, null);
		}

		public DataListener(final ConnectorDataCollection collection, final IdDataCollection idDataCollection) {
			Validate.notNull(collection, "null collection");
			this.collection = collection;
			this.idDataCollection = idDataCollection;
		}

		@Override
		public void cardFound(final ParserEvent<ConnectorCard> event) {
			final ConnectorCard value = event.getValue();
			logger.debug(value.getConnectorClass().getName() + ":" + value.toString());
			collection.addConnectorCard(value);
			if (idDataCollection != null) {
				idDataCollection.addCardId(value.getConnectorClass(), value.getId(), value.getKey());
			}
		}

		@Override
		public void relationFound(final ParserEvent<ConnectorRelation> event) {
			final ConnectorRelation value = event.getValue();
			logger.debug(value.getConnectorDomain().getName() + ":" + value.toString());
			collection.addConnectorRelation(value);
		}

	}

	public static void main(final String[] args) {
		final ConnectorConfiguration configuration = ConnectorConfiguration.getInstance();
		int status = 0;

		try {
			final Properties systemProperties = System.getProperties();
			if (!systemProperties.containsKey(ConnectorConfiguration.CONFIGURATION_PATH_PROPERTY)) {
				throw new ConfigurationException("missing property "
						+ ConnectorConfiguration.CONFIGURATION_PATH_PROPERTY);
			}

			final String configurationPath = systemProperties
					.getProperty(ConnectorConfiguration.CONFIGURATION_PATH_PROPERTY);

			configuration.load(configurationPath);

			final String message = StringUtils.defaultIfEmpty(configuration.getStartMessage(), "Program started");
			logger.info(message);

			final Connector connector = new Connector();
			connector.setConfiguration(configuration);
			connector.execute();
		} catch (final ConfigurationException e) {
			logger.fatal("Unable to load configuration " + e.getMessage());
			status = 1;
		} catch (final ConnectorException e) {
			logger.fatal(e.getMessage());
			status = 1;
		} catch (final Exception e) {
			logger.fatal(e.getMessage());
			status = 1;
		} finally {
			final String message = StringUtils.defaultIfEmpty(configuration.getEndMessage(), "Program finished");
			logger.info(message);
			System.exit(status);
		}
	}

}
