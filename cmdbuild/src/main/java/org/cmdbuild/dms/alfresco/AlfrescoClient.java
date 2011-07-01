package org.cmdbuild.dms.alfresco;

import java.util.Properties;

import org.alfresco.webservice.types.Reference;
import org.alfresco.webservice.types.ResultSetRow;
import org.cmdbuild.dms.DocumentSearch;
import org.cmdbuild.dms.SingleDocumentSearch;

public interface AlfrescoClient {

	ResultSetRow[] search(DocumentSearch search);

	ResultSetRow search(SingleDocumentSearch search);

	ResultSetRow searchRow(String uuid);

	boolean update(String uuid, Properties updateProperties, Properties aspectsProperties);

	Reference getCategoryReference(final String category);

	boolean createCategory(String category);

	boolean applyCategory(final Reference category, final String uuid);

}
