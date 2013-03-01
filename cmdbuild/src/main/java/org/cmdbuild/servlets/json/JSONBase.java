package org.cmdbuild.servlets.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cmdbuild.elements.interfaces.DomainFactory;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.TranslationService;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;

public class JSONBase {

	public static final String PARAMETER_ACTIVE = "active", //
			PARAMETER_ATTRIBUTES = "attributes", //
			PARAMETER_CARDS = "cards", //
			PARAMETER_CARD_ID = "cardId", //
			PARAMETER_CLASS_NAME = "className", //
			PARAMETER_CONFIRMED = "confirmed", //
			PARAMETER_COUNT = "count", //
			PARAMETER_DESCRIPTION = "description",
			PARAMETER_DEFAULT_VALUE = "defaultvalue", //
			PARAMETER_DOMAIN_CARDINALITY = "cardinality",
			PARAMETER_DOMAIN_DESCRIPTION_STARTING_AT_THE_FIRST_CLASS = "descr_1", //
			PARAMETER_DOMAIN_DESCRIPTION_STARTING_AT_THE_SECOND_CLASS = "descr_2", //
			PARAMETER_DOMAIN_FIRST_CLASS_ID = "idClass1", //
			PARAMETER_DOMAIN_ID = "domainId", //
			PARAMETER_DOMAIN_IS_MASTER_DETAIL = "isMasterDetail", //
			PARAMETER_DOMAIN_LIMIT = "domainlimit", //
			PARAMETER_DOMAIN_MASTER_DETAIL_LABEL = "md_label", //
			PARAMETER_DOMAIN_NAME = "domainName", //
			PARAMETER_DOMAIN_SECOND_CLASS_ID = "idClass2", //
			PARAMETER_DOMAIN_SOURCE = "src", //
			PARAMETER_EDITOR_TYPE = "editorType", //
			PARAMETER_FIELD_MODE = "fieldmode", //
			PARAMETER_FILE_CSV = "filecsv", //
			PARAMETER_FILTER = "filter", //
			PARAMETER_FUNCTION = "function", //
			PARAMETER_FK_DESTINATION = "fkDestination", //
			PARAMETER_GROUP = "group", //
			PARAMETER_GROUP_NAME = "groupName", //
			PARAMETER_ID = "id", //
			PARAMETER_INDEX = "index", //
			PARAMETER_INHERIT = "inherits", //
			PARAMETER_INHERITED = "inherited", PARAMETER_IS_PROCESS = "isprocess", //
			PARAMETER_LENGTH = "len", //
			PARAMETER_LIMIT = "limit", //
			PARAMETER_LOOKUP = "lookup", //
			PARAMETER_MENU = "menu", //
			PARAMETER_MASTER = "master", //
			PARAMETER_META_DATA = "meta", //
			PARAMETER_NAME = "name", //
			PARAMETER_NOT_NULL = "isnotnull", //
			PARAMETER_PRECISION = "precision", //
			PARAMETER_RELATION_ID = "relationId", //
			PARAMETER_SCALE = "scale", //
			PARAMETER_TABLE_TYPE = "tableType", //
			PARAMETER_TYPE = "type", //
			PARAMETER_RETRY_WITHOUT_FILTER = "retryWithoutFilter", //
			PARAMETER_SHOW_IN_GRID = "isbasedsp", //
			PARAMETER_SOURCE_CLASS_NAME = "sourceClassName", //
			PARAMETER_SOURCE_FUNCTION = "sourceClassFunction", //
			PARAMETER_SORT = "sort", // 
			PARAMETER_START = "start", // 
			PARAMETER_SEPARATOR = "separator", //			
			PARAMETER_SUPERCLASS = "superclass", //
			PARAMETER_UNIQUE = "isunique", //
			PARAMETER_USER_STOPPABLE = "userstoppable", //
			PARAMETER_VIEWS = "views", //
			PARAMETER_WIDGET = "widget", //
			PARAMETER_WIDGET_ID = "widgetId", //

			SERIALIZATION_ATTRIBUTE = "attribute", //
			SERIALIZATION_ATTRIBUTES = "attributes", //
			SERIALIZATION_ATTRIBUTE_TYPES = "types", //
			SERIALIZATION_CARD = "card", //
			SERIALIZATION_COUNT = "count", //
			SERIALIZATION_DOMAINS = "domains", //
			SERIALIZATION_DOMAIN = "domain", //
			SERIALIZATION_FILTER = "filter", //
			SERIALIZATION_OUT_OF_FILTER = "outOfFilter", //
			SERIALIZATION_POSITION = "position", //
			SERIALIZATION_TABLE = "table"; //

	public static class MultipleException extends Exception {

		private static final long serialVersionUID = 6540036977691254944L;

		private final List<Exception> exceptions;

		public MultipleException() {
			this.exceptions = new ArrayList<Exception>();
		}

		public MultipleException(final Exception e) {
			this();
			this.exceptions.add(e);
		}

		public Iterable<Exception> getExceptions() {
			return exceptions;
		}

		public void addException(final Exception e) {
			exceptions.add(e);
		}
	}

	public static class PartialFailureException extends Exception {

		private static final long serialVersionUID = 4651384443077293725L;

		private final JSONObject out;
		private final Exception e;

		public PartialFailureException(final JSONObject out, final Exception e) {
			this.out = out;
			this.e = e;
		}

		public JSONObject getPartialOutput() {
			return out;
		}

		public Exception getOriginalException() {
			return e;
		}
	}

	/**
	 * Marker interface for exported JSON service methods
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface JSONExported {
		String contentType() default ("application/json");
	}

	/**
	 * Execute in a transaction
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Transacted {
	}

	/**
	 * Available also without being authenticated
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Unauthorized {
	}

	/**
	 * Available ony to super users
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Admin {
		enum AdminAccess {
			FULL, DEMOSAFE
		};

		AdminAccess value() default (AdminAccess.FULL);
	}

	/**
	 * Available only if not configured
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Configuration {
	}

	/**
	 * Don't add the success field, needed by ExtJs, to the response
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface SkipExtSuccess {
	}

	protected ApplicationContext applicationContext;

	public JSONBase() {
	};

	public void init(final HttpServletRequest request, final HttpServletResponse response) {
	}

	protected String getTraslation(final String key) {
		final String lang = new SessionVars().getLanguage();
		return TranslationService.getInstance().getTranslation(lang, key);
	}

	public void setSpringApplicationContext(final ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	// Methods to generate a ICard
	// This is temporary code to allow the passing to a new DAO
	// Is used in the JSONBase's subclass to generate
	// ICard from Id and ClassName or ClassId
	// This would be replaced with a Logic that is able to
	// build the CMCard (The new DAO cards)

	@Deprecated
	protected ICard buildCard(final int classId, final int cardId) {
		final UserContext userCtx = new SessionVars().getCurrentUserContext();
		return buildCard(UserOperations.from(userCtx).tables().get(classId), cardId);
	}

	@Deprecated
	protected ICard buildCard(final String className, final Number cardId) {
		final UserContext userCtx = new SessionVars().getCurrentUserContext();
		return buildCard(UserOperations.from(userCtx).tables().get(className), cardId.intValue());
	}

	private ICard buildCard(final ITable table, final int cardId) {
		Log.JSONRPC.debug("build card className:" + table.getName() + ", id:" + cardId);
		if (cardId > 0) {
			return table.cards().get(cardId);
		} else {
			return table.cards().create();
		}
	}

	// The same for the ITable

	@Deprecated
	public ITable buildTable(final String className) throws Exception {
		final UserContext userCtx = new SessionVars().getCurrentUserContext();
		if (className != null) {
			return UserOperations.from(userCtx).tables().get(className);
		}

		return UserOperations.from(userCtx).tables().create();
	}

	@Deprecated
	public ITable buildTable(final int classId) throws Exception {
		final UserContext userCtx = new SessionVars().getCurrentUserContext();
		if (classId > 0) {
			return UserOperations.from(userCtx).tables().get(classId);
		}

		return UserOperations.from(userCtx).tables().create();
	}

	// The same for IDomain

	@Deprecated
	public IDomain build(final int domainId) throws AuthException, ORMException, NotFoundException {
		final UserContext userCtx = new SessionVars().getCurrentUserContext();
		final DomainFactory df = UserOperations.from(userCtx).domains();

		if (domainId > 0) {
			return df.get(domainId);
		} else {
			return df.create();
		}
	}

	public IDomain build(final String domainName) throws AuthException, ORMException, NotFoundException {
		final UserContext userCtx = new SessionVars().getCurrentUserContext();
		final DomainFactory df = UserOperations.from(userCtx).domains();

		return df.get(domainName);
	}
}
