package unit.rest.model;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.cmdbuild.service.rest.model.Model;
import org.junit.Test;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.google.common.reflect.Reflection;

public class AllModelsTest {

	@Test
	public void allModelsMustHaveOneOnlyAndNotPublicContructor() throws Exception {
		final ClassLoader classLoader = AllModelsTest.class.getClassLoader();
		final String targetPackage = Reflection.getPackageName(Model.class);
		for (final ClassInfo classInfo : ClassPath.from(classLoader).getTopLevelClasses(targetPackage)) {
			final String name = classInfo.getName();
			if (name.endsWith("package-info")) {
				continue;
			}

			final Class<?> clazz = Class.forName(name);
			final Constructor<?>[] constructors = clazz.getDeclaredConstructors();
			assertThat(constructors.length, lessThanOrEqualTo(1));
			final Constructor<?> onlyConstructor = constructors[0];
			assertThat("invalid modifier at class" + name, Modifier.isPublic(onlyConstructor.getModifiers()),
					equalTo(false));
		}
	}

}
