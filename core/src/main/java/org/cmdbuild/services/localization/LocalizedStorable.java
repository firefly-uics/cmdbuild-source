package org.cmdbuild.services.localization;

public interface LocalizedStorable {
	
	void accept(LocalizedStorableVisitor visitor);

}
