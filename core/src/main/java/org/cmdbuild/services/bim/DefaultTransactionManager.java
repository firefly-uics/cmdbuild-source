package org.cmdbuild.services.bim;

import org.cmdbuild.bim.service.BimService;

public class DefaultTransactionManager implements TransactionManager {
	
	private final BimService service;
	private long transactionId;
	
	public DefaultTransactionManager(final BimService service){
		this.service = service;
		this.transactionId = -1;
	}
	
	@Override
	public void open(String projectId) {
		if(!hasTransaction()){
			transactionId = Long.parseLong(service.openTransaction(projectId));
		}
	}

	@Override
	public boolean hasTransaction() {
		return transactionId != -1;
	}

	@Override
	public String getId() {
		return String.valueOf(transactionId);
	}

	@Override
	public String commit() {
		try{
			return service.commitTransaction(getId());
		}catch(Throwable t){
			abort();
			throw new RuntimeException("Unable to perform the commit", t);
		}
	}

	@Override
	public void abort() {
		service.abortTransaction(getId());
	}

}
