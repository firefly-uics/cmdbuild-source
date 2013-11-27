package org.cmdbuild.dms;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.cmdbuild.dao.entrytype.CMClass;

import com.google.common.collect.Lists;

public class DefaultDocumentCreatorFactory implements DocumentCreatorFactory {

	@Override
	public DocumentCreator create(final String name) {
		return new DefaultDocumentCreator(Arrays.asList(name));
	}

	@Override
	public DocumentCreator create(final CMClass target) {
		return new DefaultDocumentCreator(buildSuperclassesPath(target));
	}

	private Collection<String> buildSuperclassesPath(final CMClass targetClass) {
		final List<String> path = Lists.newArrayList();
		CMClass currentClass = targetClass;
		path.add(currentClass.getIdentifier().getLocalName());
		while (currentClass.getParent() != null && !currentClass.getParent().getName().equals("Class")) {
			currentClass = currentClass.getParent();
			path.add(0, currentClass.getIdentifier().getLocalName());
		}
		return path;
	}

}
