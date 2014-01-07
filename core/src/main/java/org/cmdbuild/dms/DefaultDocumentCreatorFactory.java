package org.cmdbuild.dms;

import java.util.Collection;
import java.util.List;

import org.cmdbuild.dao.entrytype.CMClass;

import com.google.common.collect.Lists;

public class DefaultDocumentCreatorFactory implements DocumentCreatorFactory {

	@Override
	public DocumentCreator create(final CMClass target) {
		return new DefaultDocumentCreator(buildSuperclassesPath(target));
	}

	private Collection<String> buildSuperclassesPath(final CMClass target) {
		final List<String> path = Lists.newArrayList();
		CMClass clazz = target;
		path.add(clazz.getIdentifier().getLocalName());
		while (clazz.getParent() != null && !clazz.getParent().getName().equals("Class")) {
			clazz = clazz.getParent();
			path.add(0, clazz.getIdentifier().getLocalName());
		}
		return path;
	}

}
