package org.cmdbuild.workflow;

import org.cmdbuild.shark.util.CmdbuildUtils;
import org.enhydra.shark.api.internal.working.CallbackUtilities;

/**
 *
 * Needed only for configuring {@link CmdbuildUtils}.
 *
 */
public class CmdbuildLegacyScriptingManager extends CmdbuildScriptingManager {

	@Override
	public void configure(final CallbackUtilities cus) throws Exception {
		super.configure(cus);
		CmdbuildUtils.getInstance().configure(cus);
	}

}
