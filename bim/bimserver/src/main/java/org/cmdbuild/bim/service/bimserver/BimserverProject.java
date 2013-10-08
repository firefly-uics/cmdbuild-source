package org.cmdbuild.bim.service.bimserver;

import org.bimserver.interfaces.objects.SObjectState;
import org.bimserver.interfaces.objects.SProject;
import org.cmdbuild.bim.service.BimProject;

public class BimserverProject implements BimProject {

	private static final String ACTIVE = "ACTIVE";
	private static final String NULL_TRANSACTION = "-1";
	private final SProject project;
	private String transactionId;

	protected BimserverProject(final SProject project) {
		this.project = project;
		transactionId = NULL_TRANSACTION;
	}

	@Override
	public String getIdentifier() {
		final long poid = project.getOid();
		return String.valueOf(poid);
	}

	@Override
	public String getLastRevisionId() {
		final long roid = project.getLastRevisionId();
		return String.valueOf(roid);
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public boolean isActive() {
		final SObjectState state = project.getState();
		return state.name().equals(ACTIVE);
	}

	@Override
	public String getTransactionId() {
		return transactionId;
	}

	@Override
	public boolean hasOpenTransaction() {
		return !transactionId.equals(NULL_TRANSACTION);
	}

	@Override
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
		
	}

	@Override
	public void resetTransaction() {
		this.transactionId = NULL_TRANSACTION;
		
	}

}
