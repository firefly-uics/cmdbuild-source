package org.cmdbuild.dms.alfresco.webservice;

import static org.apache.commons.lang.StringUtils.EMPTY;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.alfresco.webservice.repository.RepositoryServiceSoapBindingStub;
import org.alfresco.webservice.types.CML;
import org.alfresco.webservice.types.CMLAddAspect;
import org.alfresco.webservice.types.CMLUpdate;
import org.alfresco.webservice.types.NamedValue;
import org.alfresco.webservice.types.Predicate;
import org.alfresco.webservice.types.Reference;
import org.alfresco.webservice.util.Utils;
import org.alfresco.webservice.util.WebServiceFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.cmdbuild.logger.Log;

class UpdateCommand extends AlfrescoWebserviceCommand<Boolean> {

	private String uuid;
	private Properties updateProperties;
	private Properties aspectsProperties;

	private static CMLUpdate cmlUpdate(final Predicate predicate, final Properties properties) {
		final CMLUpdate update = new CMLUpdate();
		update.setWhere(predicate);
		final List<NamedValue> namedValues = new ArrayList<NamedValue>();
		for (final String name : properties.stringPropertyNames()) {
			final String value = properties.getProperty(name, EMPTY);
			namedValues.add(Utils.createNamedValue(name, value));
		}
		update.setProperty(namedValues.toArray(new NamedValue[namedValues.size()]));
		return update;
	}

	private static CMLAddAspect[] aspects(final Predicate predicate, final Properties properties) {
		final List<CMLAddAspect> aspects = new ArrayList<CMLAddAspect>();
		for (final String name : properties.stringPropertyNames()) {
			final String value = properties.getProperty(name, EMPTY);
			final CMLAddAspect aspect = new CMLAddAspect(name, null, predicate, value);
			aspects.add(aspect);
		}
		return aspects.toArray(new CMLAddAspect[aspects.size()]);
	}

	public void setUuid(final String uuid) {
		this.uuid = uuid;
	}

	public void setUpdateProperties(final Properties update) {
		this.updateProperties = update;
	}

	public void setAspectsProperties(final Properties aspects) {
		this.aspectsProperties = aspects;
	}

	@Override
	public void execute() {
		Validate.isTrue(StringUtils.isNotBlank(uuid), String.format("invalid uuid '%s'", uuid));
		Validate.notNull(updateProperties, "null properties");

		final Reference reference = new Reference(STORE, uuid, null);

		final Predicate predicate = new Predicate();
		predicate.setStore(STORE);
		predicate.setNodes(new Reference[] { reference });

		final CML cml = new CML();

		final CMLUpdate update = cmlUpdate(predicate, updateProperties);
		cml.setUpdate(new CMLUpdate[] { update });

		final CMLAddAspect[] aspects = aspects(predicate, aspectsProperties);
		cml.setAddAspect(aspects);

		try {
			final RepositoryServiceSoapBindingStub repository = WebServiceFactory.getRepositoryService();
			repository.update(cml);
			setResult(true);
		} catch (final Exception e) {
			final String message = String.format("error updating element '%s'", uuid);
			Log.DMS.error(message, e);
			setResult(false);
		}
	}

	@Override
	public boolean isSuccessfull() {
		final Boolean result = getResult();
		return (result == null) ? false : result.booleanValue();
	}

	@Override
	public boolean hasResult() {
		return true;
	}

}
