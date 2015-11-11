package org.cmdbuild.workflow.user;

import static com.google.common.collect.FluentIterable.from;

import java.util.List;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.ForwardingAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.NullAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMProcessClass;
import org.cmdbuild.workflow.CMProcessInstance;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.ForwardingActivity;
import org.cmdbuild.workflow.ForwardingProcessDefinitionManager;
import org.cmdbuild.workflow.ProcessDefinitionManager;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess.Type;

import com.google.common.base.Function;

public class UserProcessDefinitionManager extends ForwardingProcessDefinitionManager {

	private final ProcessDefinitionManager delegate;
	private final CMDataView dataView;

	public UserProcessDefinitionManager(final ProcessDefinitionManager delegate, final CMDataView dataView) {
		this.delegate = delegate;
		this.dataView = dataView;
	}

	@Override
	protected ProcessDefinitionManager delegate() {
		return delegate;
	}

	@Override
	public CMActivity getManualStartActivity(final CMProcessClass process, final String groupName)
			throws CMWorkflowException {
		return wrap(process, super.getManualStartActivity(process, groupName));
	}

	@Override
	public CMActivity getActivity(final CMProcessInstance processInstance, final String activityDefinitionId)
			throws CMWorkflowException {
		final CMProcessClass type = processInstance.getType();
		return wrap(type, super.getActivity(processInstance, activityDefinitionId));
	}

	private CMActivity wrap(final CMProcessClass type, final CMActivity delegate) {
		return new ForwardingActivity() {

			@Override
			protected CMActivity delegate() {
				return delegate;
			}

			@Override
			public List<CMActivityVariableToProcess> getVariables() {
				return from(super.getVariables()) //
						.transform(new Function<CMActivityVariableToProcess, CMActivityVariableToProcess>() {

							@Override
							public CMActivityVariableToProcess apply(final CMActivityVariableToProcess input) {
								final CMActivityVariableToProcess output;
								switch (input.getType()) {

								case READ_WRITE:
								case READ_WRITE_REQUIRED:
									output = new ForwardingAttributeTypeVisitor() {

										private final CMAttributeTypeVisitor delegate = NullAttributeTypeVisitor
												.getInstance();

										private CMActivityVariableToProcess output;

										@Override
										protected CMAttributeTypeVisitor delegate() {
											return delegate;
										}

										public CMActivityVariableToProcess output() {
											output = input;
											final CMAttribute attribute = type.getAttribute(input.getName());
											if (attribute != null) {
												attribute.getType().accept(this);
											}
											return output;
										}

										@Override
										public void visit(final ReferenceAttributeType attributeType) {
											final String domainName = attributeType.getDomainName();
											final CMDomain domain = dataView.findDomain(domainName);
											if (domain == null) {
												output = new CMActivityVariableToProcess(input.getName(),
														Type.READ_ONLY);
											}
										};

									}.output();
									break;

								default:
									output = input;
									break;

								}
								return output;
							}

						}).toList();
			}

		};
	}

}
