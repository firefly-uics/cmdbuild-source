package org.cmdbuild.services.scheduler.job;

import org.cmdbuild.elements.interfaces.ProcessType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.workflow.operation.ActivityDO;
import org.cmdbuild.workflow.operation.SharkFacade;

public class StartProcessJob extends AbstractJob {

	public StartProcessJob(int id) {
		super(id);
	}

	@Override
	public void execute() {
		if (isValidJob()) {
			Log.WORKFLOW.info(String.format("Starting process %s", detail));
			for (String key : params.keySet()) {
				Log.WORKFLOW.info(String.format("  %s -> %s", key, params.get(key)));
			}
			UserContext systemCtx = UserContext.systemContext();
			SharkFacade mngt = new SharkFacade(systemCtx);
			ProcessType processType = systemCtx.processTypes().get(detail);
			ActivityDO activity = mngt.startProcess(processType.getName());
			activity.setCmdbuildClassId(processType.getId());
			mngt.updateActivity(activity.getProcessInstanceId(), activity.getWorkItemId(), params, true);
		} else {
			Log.WORKFLOW.info(String.format("Invalid process"));
		}
	}

	private boolean isValidJob() {
		return ((detail != null) && (params != null));
	}
}
