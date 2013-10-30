package org.cmdbuild.dms;

import java.util.Collection;
import java.util.List;

import org.cmdbuild.dao.entrytype.CMClass;

import com.google.common.collect.Lists;

public class DefaultDocumentCreatorFactory implements DocumentCreatorFactory {

	private CMClass target;

	@Override
	public void setClass(final CMClass target) {
		this.target = target;
	}

	@Override
	public DocumentCreator create() {
		return new DefaultDocumentCreator(buildSuperclassesPath());
	}

	private Collection<String> buildSuperclassesPath() {
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
