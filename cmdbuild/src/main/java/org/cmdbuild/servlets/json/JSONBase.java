package org.cmdbuild.servlets.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cmdbuild.elements.interfaces.ICard;
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

	public static class MultipleException extends Exception {
		
		private static final long serialVersionUID = 6540036977691254944L;

		private final List<Exception> exceptions;

		public MultipleException() {
			this.exceptions = new ArrayList<Exception>();
		}

		public MultipleException(Exception e) {
			this();
			this.exceptions.add(e);
		}

		public Iterable<Exception> getExceptions() {
			return exceptions;
		}

		public void addException(Exception e) {
			exceptions.add(e);
		}
	}

	public static class PartialFailureException extends Exception {

		private static final long serialVersionUID = 4651384443077293725L;

		private final JSONObject out;
		private Exception e;

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
		String contentType() default("application/json");
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
		enum AdminAccess { FULL, DEMOSAFE };
		AdminAccess value() default(AdminAccess.FULL);
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

	public JSONBase() {	};

	public void init(HttpServletRequest request, HttpServletResponse response) {
	}

	protected String getTraslation(String key) {
		String lang = new SessionVars().getLanguage();
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
		UserContext userCtx = new SessionVars().getCurrentUserContext();
		return buildCard(UserOperations.from(userCtx).tables().get(classId), cardId);
	}

	@Deprecated
	protected ICard buildCard(final String className, final int cardId) {
		UserContext userCtx = new SessionVars().getCurrentUserContext();
		return buildCard(UserOperations.from(userCtx).tables().get(className), cardId);
	}

	private ICard buildCard(final ITable table, final int cardId) {
		Log.JSONRPC.debug("build card className:"+table.getName()+", id:"+cardId);
		if(cardId > 0){
			return table.cards().get(cardId);
		} else {
			return table.cards().create();
		}
	} 

	// The same for the ITable

	@Deprecated
	public ITable buildTable(String className) throws Exception {
		UserContext userCtx = new SessionVars().getCurrentUserContext();
		if (className != null) {
			return UserOperations.from(userCtx).tables().get(className);
		}

		return UserOperations.from(userCtx).tables().create();
	}

	@Deprecated
	public ITable buildTable(int classId) throws Exception {
		UserContext userCtx = new SessionVars().getCurrentUserContext();
		if (classId > 0) {
			return UserOperations.from(userCtx).tables().get(classId);
		}

		return UserOperations.from(userCtx).tables().create();
	}
}
