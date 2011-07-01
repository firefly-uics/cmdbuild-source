package org.cmdbuild.dms.alfresco.webservice;

import java.util.Collection;
import java.util.Properties;

import org.alfresco.webservice.types.Reference;
import org.alfresco.webservice.types.ResultSetRow;
import org.alfresco.webservice.types.Store;
import org.alfresco.webservice.util.Constants;
import org.alfresco.webservice.util.WebServiceFactory;
import org.apache.commons.lang.Validate;
import org.cmdbuild.config.LegacydmsProperties;
import org.cmdbuild.dms.DocumentSearch;
import org.cmdbuild.dms.SingleDocumentSearch;
import org.cmdbuild.dms.alfresco.AlfrescoClient;
import org.cmdbuild.elements.TableImpl;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.logger.Log;

public class AlfrescoWebserviceClient implements AlfrescoClient {

	private final LegacydmsProperties properties;

	public AlfrescoWebserviceClient(final LegacydmsProperties properties) {
		Validate.notNull(properties, "null properties");
		this.properties = properties;

		final String address = properties.getServerURL();
		WebServiceFactory.setEndpointAddress(address);
	}

	private void executeWhithinSession(final AlfrescoWebserviceCommand<?> command) {
		final String username = properties.getAlfrescoUser();
		final String password = properties.getAlfrescoPassword();
		final AlfrescoSession session = new AlfrescoSession(username, password);
		session.start();
		if (session.isStarted()) {
			command.execute();
		} else {
			Log.DMS.warn("session could not be started");
		}
		session.end();
	}

	private static String baseSearchPath(final LegacydmsProperties properties) {
		return new StringBuilder() //
				.append(properties.getRepositoryWSPath()) //
				.append(properties.getRepositoryApp()) //
				.toString();
	}

	@Override
	public ResultSetRow[] search(final DocumentSearch search) {
		final SearchCommand command = new SearchCommand();
		command.setDocumentSearch(search);
		command.setBaseSearchPath(baseSearchPath(properties));
		executeWhithinSession(command);
		return command.getResult();
	}

	@Override
	public ResultSetRow search(final SingleDocumentSearch search) {
		final SingleSearchCommand command = new SingleSearchCommand();
		command.setDocumentSearch(search);
		command.setBaseSearchPath(baseSearchPath(properties));
		executeWhithinSession(command);
		if (command.isSuccessfull()) {
			return command.getResult();
		} else {
			throw NotFoundException.NotFoundExceptionType.ATTACHMENT_NOTFOUND.createException(search.getFileName(),
					search.getClassName(), Integer.toString(search.getCardId()));
		}
	}

	@Override
	public ResultSetRow searchRow(final String uuid) {
		final UuidSearchCommand command = new UuidSearchCommand();
		command.setUuid(uuid);
		executeWhithinSession(command);
		return command.getResult();
	}

	public String searchUuid(final SingleDocumentSearch search) {
		ResultSetRow resultSetRow = search(search);
		return resultSetRow.getNode().getId();
	}

	@Override
	public boolean update(final String uuid, final Properties updateProperties, final Properties aspectsProperties) {
		final UpdateCommand command = new UpdateCommand();
		command.setUuid(uuid);
		command.setUpdateProperties(updateProperties);
		command.setAspectsProperties(aspectsProperties);
		executeWhithinSession(command);
		return command.getResult();
	}

	@Override
	public Reference getCategoryReference(final String category) {
		final GetCategoryCommand command = new GetCategoryCommand();
		command.setCategory(category);
		executeWhithinSession(command);
		return command.getResult();
	}

	@Override
	public boolean createCategory(final String category) {
		final CreateCategoryCommand command = new CreateCategoryCommand();
		command.setCategoryRoot(properties.getCmdbuildCategory());
		command.setCategory(category);
		executeWhithinSession(command);
		return command.getResult();
	}

	@Override
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

	public static String[] path(final String className, final int cardId) throws NotFoundException {
		final Collection<String> classWithAncestors = TableImpl.tree().path(className);
		final String[] path = new String[classWithAncestors.size() + 1];
		classWithAncestors.toArray(path);
		path[classWithAncestors.size()] = "Id" + cardId;
		Log.DMS.debug("requested path " + path);
		return path;
	}

	public static String escapeQuery(final String query) {
		return query.replaceAll(" ", "_x0020_");
	}

}
