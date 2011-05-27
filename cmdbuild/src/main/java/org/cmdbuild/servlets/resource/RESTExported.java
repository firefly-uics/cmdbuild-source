package org.cmdbuild.servlets.resource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Methods exposed to the web in a Resource must be annotated
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RESTExported {
	
	/**
	 * Enum with the standard HTTP methods
	 */
	public enum RestMethod {
		GET,POST,PUT,DELETE,HEAD,OPTIONS,TRACE;
	}

	/**
	 * which method to use
	 * @return
	 */
	RestMethod httpMethod() default(RestMethod.GET);
	/**
	 * identify a subresource (ie. {Resource.baseURI()}/someOtherIdentifier)
	 * @return
	 */
	String subResource() default("");
	/**
	 * The contentType returned to the client. Default application/xml
	 * @return
	 */
	String contentType() default("application/xml");
	/**
	 * The success code returned to the client. default 200 (OK)
	 * @return
	 */
	int successCode() default(200); // default 200 OK
	/**
	 * In case of exception, the code returned to the client. default 500 (INTERNAL SERVER ERROR)
	 * @return
	 */
	int failureCode() default(500); // default 500 INTERNAL SERVER ERROR
}
