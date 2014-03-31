(function() {

	var tr = CMDBuild.Translation.administration.tasks;

	Ext.define('CMDBuild.view.administration.tasks.event.synchronous.CMStep1Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		parentDelegate: undefined,
		view: undefined,

		/**
		 * Gatherer function to catch events
		 *
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		checkWorkflowComboSelected: function() {
			if (this.getValueWorkflowCombo())
				return true;

			return false;
		},

		getWorkflowDelegate: function() {
			return this.view.workflowForm.delegate;
		},

		getValueAttributeGrid: function() {
			return this.getWorkflowDelegate().getValueGrid();
		},

		getValueId: function() {
			return this.view.idField.getValue();
		},

		getValueWorkflowCombo: function() {
			return this.getWorkflowDelegate().getValueCombo();
		},

		onWorkflowSelected: function(name, modify) {
			this.getWorkflowDelegate().onWorkflowSelected(name, modify);
		},

		setDisabledAttributesTable: function(state) {
			this.getWorkflowDelegate().setDisabledAttributesTable(state);
		},

		setDisabledTypeField: function(state) {
			this.view.typeField.setDisabled(state);
		},

		setValueActive: function(value) {
			this.view.activeField.setValue(value);
		},

		setValueAttributesGrid: function(data) {
			this.getWorkflowDelegate().setValueGrid(data);
		},

		setValueDescription: function(value) {
			this.view.descriptionField.setValue(value);
		},

		setValueId: function(value) {
			this.view.idField.setValue(value);
		},

		setValueWorkflowCombo: function(workflowName) {
			this.getWorkflowDelegate().setValueCombo(workflowName);
		}
	});

	Ext.define('CMDBuild.view.administration.tasks.event.synchronous.CMStep1', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,
		taskType: 'workflow',

		border: false,
		height: '100%',
		overflowY: 'auto',

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.workflow.CMStep1Delegate', this);

			this.typeField = Ext.create('Ext.form.field.Text', {
				fieldLabel: tr.type,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name: CMDBuild.ServiceProxy.parameter.TYPE,
				width: CMDBuild.CFG_BIG_FIELD_WIDTH,
				value: me.taskType,
				disabled: true,
				cmImmutable: true,
				readOnly: true
			});

			this.idField = Ext.create('Ext.form.field.Hidden', {
				name: CMDBuild.ServiceProxy.parameter.ID
			});

			this.descriptionField = Ext.create('Ext.form.field.Text', {
				name: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
				fieldLabel: CMDBuild.Translation.description_,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.CFG_BIG_FIELD_WIDTH,
				allowBlank: false
			});

			this.activeField = Ext.create('Ext.form.field.Checkbox', {
				name: CMDBuild.ServiceProxy.parameter.ACTIVE,
				fieldLabel: tr.startOnSave,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH
			});

			this.workflowForm = Ext.create('CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowForm', {
				name: CMDBuild.ServiceProxy.parameter.CLASS_NAME,
				allowBlank: false
			});

			Ext.apply(this, {
				items: [
					this.typeField,
					this.idField,
					this.descriptionField,
					this.activeField,
					this.workflowForm
				]
			});

			this.callParent(arguments);
		}
	});

})();