package org.cmdbuild.service.rest.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MultivaluedMap;

import com.google.common.collect.Lists;

/**
 * Filters all {@link MultivaluedMap} arguments removing all keys that appears
 * in the same method within a {@link FormParam} annotation.
 */
public class MultivaluedMapFilter<T> implements InvocationHandler {

	public static <T> MultivaluedMapFilter<T> of(final T target) {
		return new MultivaluedMapFilter<T>(target);
	}

	private final T target;

	private MultivaluedMapFilter(final T target) {
		this.target = target;
	}

	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		final List<MultivaluedMap> maps = Lists.newArrayList();
		final List<String> keys = Lists.newArrayList();
		for (int i = 0; i < args.length; i++) {
			final Object arg = args[i];
			if (arg instanceof MultivaluedMap) {
				maps.add(MultivaluedMap.class.cast(arg));
			} else {
				for (final Annotation annotation : method.getParameterAnnotations()[i]) {
					if (annotation instanceof FormParam) {
						final FormParam formParam = FormParam.class.cast(annotation);
						final String name = formParam.value();
						keys.add(name);
					}
				}
			}
		}
		for (final MultivaluedMap element : maps) {
			for (final String key : keys) {
				element.remove(key);
			}
		}
		return method.invoke(target, args);
	}

}
