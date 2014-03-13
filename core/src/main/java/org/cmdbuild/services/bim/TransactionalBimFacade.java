package org.cmdbuild.services.bim;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.cmdbuild.bim.model.Entity;

public class TransactionalBimFacade extends ForwardingBimFacade implements InvocationHandler {

	@interface BimTransactional {

	}

	private final TransactionManager transactionManager;
	private final BimFacade nonTransactionalDelegate;
	private final BimFacade transactionalDelegate;

	public TransactionalBimFacade(final BimFacade delegate, final TransactionManager transactionManager) {
		super(delegate);
		this.nonTransactionalDelegate = delegate;
		this.transactionManager = transactionManager;

		final Object proxy = Proxy.newProxyInstance( //
				TransactionalBimFacade.class.getClassLoader(), //
				new Class<?>[] { BimFacade.class }, //
				this);
		this.transactionalDelegate = BimFacade.class.cast(proxy);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {
		if (method.getAnnotation(BimTransactional.class) != null) {
			if (!transactionManager.hasTransaction()) {
				throw new UnsupportedOperationException("TO DO");
			}
		}
		try {
			return method.invoke(nonTransactionalDelegate, arguments);
		} catch (final Throwable e) {
			transactionManager.abort();
			throw e;
		}
	}

	@Override
	@BimTransactional
	public String createCard(Entity entityToCreate, String targetProjectId) {
		return transactionalDelegate.createCard(entityToCreate, targetProjectId);
	}

	@Override
	public void openTransaction(String projectId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void abortTransaction() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public BimFacadeProject createBaseAndExportProject(BimFacadeProject bimProject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void checkin(String targetId, File file) {
		// TODO Auto-generated method stub
		
	}

}
