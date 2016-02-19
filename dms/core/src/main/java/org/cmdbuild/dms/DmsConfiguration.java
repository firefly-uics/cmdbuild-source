package org.cmdbuild.dms;

import java.util.EventListener;

public interface DmsConfiguration {

	static interface ChangeListener extends EventListener {

		void configurationChanged();

	}

	void addListener(ChangeListener listener);

	boolean isEnabled();

	String getCmdbuildCategory();

}
