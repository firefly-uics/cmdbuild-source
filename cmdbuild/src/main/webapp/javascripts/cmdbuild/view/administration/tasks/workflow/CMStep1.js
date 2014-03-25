(function() {

	var tr = CMDBuild.Translation.administration.tasks;

	Ext.define('CMDBuild.view.administration.tasks.workflow.CMStep1Delegate', {
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
			if (this.getWorkflowComboValue())
				return true;

			return false;
		},

		getAttributeTableValues: function() {
			return this.view.attributesTable.getData();
		},

		getId: function() {
			return this.view.idField.getValue();
		},

		getWorkflowComboValue: function() {
			return this.view.workflowCombo.getValue();
		},

		fillActive: function(value) {
			this.view.activeField.setValue(value);
		},

		fillAttributesGrid: function(data) {
			this.view.attributesTable.fillWithData(data);
		},

		fillDescription: function(value) {
			this.view.descriptionField.setValue(value);
		},

		fillId: function(value) {
			this.view.idField.setValue(value);
		},

		fillWorkflowCombo: function(workflowName) {
			this.view.workflowCombo.setValue(workflowName);
		},

		onWorkflowSelected: function(name, modify) {
			this.view.workflowFormDelegate.onWorkflowSelected(name, modify);
		},

		setDisabledAttributesTable: function(state) {
			this.view.workflowFormDelegate.setDisabledAttributesTable(state);
		},

		setDisabledTypeField: function(state) {
			this.view.typeField.setDisabled(state);
		}
	});

	Ext.define('CMDBuild.view.administration.tasks.workflow.CMStep1', {
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

			// Workflow form configuration
				this.workflowCombo = Ext.create('CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowFormCombo', {
					name: CMDBuild.ServiceProxy.parameter.CLASS_NAME
				});
				this.attributesTable = Ext.create('CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowFormGrid');

				this.workflowFormDelegate = Ext.create('CMDBuild.controller.administration.tasks.common.workflowForm.CMWorkflowFormController');
				this.workflowFormDelegate.comboField = this.workflowCombo;
				this.workflowFormDelegate.gridField = this.attributesTable;
				this.workflowCombo.delegate = this.workflowFormDelegate;
				this.attributesTable.delegate = this.workflowFormDelegate;

			Ext.apply(this, {
				items: [
					this.typeField,
					this.idField,
					this.descriptionField,
					this.workflowCombo,
					this.activeField,
					this.attributesTable
				]
			});

			this.callParent(arguments);
		}
	});

})();