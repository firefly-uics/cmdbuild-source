package org.cmdbuild.workflow;

import org.cmdbuild.elements.AttributeValue;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.NotFoundException;

public class ActivityVariable {
	WorkflowVariableType type;
	AttributeValue variable;
	int clientIndex;
	
	public ActivityVariable(ActivityVariableDef def, ITable schema, int clientIndex) throws NotFoundException{
		this.type = def.type;
		this.variable = new AttributeValue(schema.getAttribute(def.name));
		this.clientIndex = clientIndex;
	}
	
	//to be used to show, ie., baseDSP variables
	public ActivityVariable(IAttribute attribute ) {
		this.type = WorkflowVariableType.VIEW;
		this.variable = new AttributeValue(attribute);
		this.clientIndex = -1;
	}

	public WorkflowVariableType getType() {
		return type;
	}

	private AttributeValue getVariable() {
		return variable;
	}
	
	public int getClientIndex() {
		return clientIndex;
	}

	public String getName() {
		return variable.getSchema().getName();
	}

	public void setValue(String value) {
		getVariable().setValue(value);
	}

	public IAttribute getAttribute() {
		return getVariable().getSchema();
	}

	public Object getValue() {
		return getVariable().getObject();
	}

	public Integer getId() {
		return getVariable().getId();
	}

	public Object getStringValue() {
		return getVariable().toString();
	}
}
