package org.cmdbuild.dms.alfresco.webservice;

import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;

import org.alfresco.webservice.types.Reference;
import org.alfresco.webservice.types.ResultSetRow;
import org.alfresco.webservice.types.Store;
import org.alfresco.webservice.util.Constants;
import org.alfresco.webservice.util.WebServiceFactory;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.cmdbuild.dms.documents.DocumentSearch;
import org.cmdbuild.dms.documents.SingleDocumentSearch;
import org.cmdbuild.dms.exception.FileNotFoundException;
import org.cmdbuild.dms.properties.DmsProperties;

public class AlfrescoWebserviceClient {

	private static final Logger logger = Logger.getLogger(AlfrescoWebserviceClient.class);

	private static Map<String, AlfrescoWebserviceClient> cache = new WeakHashMap<String, AlfrescoWebserviceClient>();

	private final DmsProperties properties;

	private AlfrescoWebserviceClient(final DmsProperties properties) {
		Validate.notNull(properties, "null properties");
		this.properties = properties;

		final String address = properties.getServerURL();
		WebServiceFactory.setEndpointAddress(address);
	}

	public static AlfrescoWebserviceClient getInstance(final DmsProperties properties) {
		Validate.notNull(properties, "null dms properties");
		synchronized (cache) {
			final String address = properties.getServerURL();
			AlfrescoWebserviceClient client = cache.get(address);
			if (client == null) {
				logger.info(String.format("creating new webservice client for address '%s'", address));
				client = new AlfrescoWebserviceClient(properties);
				cache.put(address, client);
			}
			return client;
		}
	}

	private void executeWhithinSession(final AlfrescoWebserviceCommand<?> command) {
		final String username = properties.getAlfrescoUser();
		final String password = properties.getAlfrescoPassword();
		final AlfrescoSession session = new AlfrescoSession(username, password);
		session.start();
		if (session.isStarted()) {
			command.execute();
		} else {
			logger.warn("session could not be started");
		}
		session.end();
	}

	private static String baseSearchPath(final DmsProperties properties) {
		return new StringBuilder() //
				.append(properties.getRepositoryWSPath()) //
				.append(properties.getRepositoryApp()) //
				.toString();
	}

	public ResultSetRow[] search(final DocumentSearch search) {
		final SearchCommand command = new SearchCommand();
		command.setDocumentSearch(search);
		command.setBaseSearchPath(baseSearchPath(properties));
		executeWhithinSession(command);
		return command.getResult();
	}

	public ResultSetRow search(final SingleDocumentSearch search) throws FileNotFoundException {
		final SingleSearchCommand command = new SingleSearchCommand();
		command.setDocumentSearch(search);
		command.setBaseSearchPath(baseSearchPath(properties));
		executeWhithinSession(command);
		if (command.isSuccessfull()) {
			return command.getResult();
		}
		throw FileNotFoundException.newInstance(search.getFileName(), search.getClassName(), search.getCardId());
	}

	public ResultSetRow searchRow(final String uuid) {
		final UuidSearchCommand command = new UuidSearchCommand();
		command.setUuid(uuid);
		executeWhithinSession(command);
		return command.getResult();
	}

	public String searchUuid(final SingleDocumentSearch search) throws FileNotFoundException {
		final ResultSetRow resultSetRow = search(search);
		return resultSetRow.getNode().getId();
	}

	public boolean update(final String uuid, final Properties updateProperties, final Properties aspectsProperties) {
		final UpdateCommand command = new UpdateCommand();
		command.setUuid(uuid);
		command.setUpdateProperties(updateProperties);
		command.setAspectsProperties(aspectsProperties);
		executeWhithinSession(command);
		return command.getResult();
	}

	public Reference getCategoryReference(final String category) {
		final GetCategoryCommand command = new GetCategoryCommand();
		command.setCategory(category);
		executeWhithinSession(command);
		return command.getResult();
	}

	public boolean createCategory(final String category) {
		final CreateCategoryCommand command = new CreateCategoryCommand();
		command.setCategoryRoot(properties.getCmdbuildCategory());
		command.setCategory(category);
		executeWhithinSession(command);
		return command.getResult();
	}

	public boolean applyCategory(final Reference category, final String uuid) {
		final ApplyCategoryCommand command = new ApplyCategoryCommand();
		command.setCategory(category);
		command.setUuid(uuid);
		executeWhithinSession(command);
		return command.getResult();
	}

}

abstract class AlfrescoWebserviceCommand<T> {

	public static final String DEFAULT_STORE_ADDRESS = "SpacesStore";
	public static final Store STORE = new Store(Constants.WORKSPACE_STORE, DEFAULT_STORE_ADDRESS);

	protected final Logger logger = Logger.getLogger(getClass());

	private T result;

	public abstract void execute();

	public abstract boolean isSuccessfull();

	public abstract boolean hasResult();

	public T getResult() {
		return result;
	}

	protected void setResult(final T result) {
		this.result = result;
	}

	public static String escapeQuery(final String query) {
		return query.replaceAll(" ", "_x0020_");
	}

}
