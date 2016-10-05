/**
 * 
 */
package org.cmdbuild.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotates a program element that exists, or is more widely visible than
 * otherwise necessary, only for use in test code.
 */
@Retention(CLASS)
@Target({ TYPE, METHOD, FIELD })
@Documented
public @interface VisibleForTesting {

}
