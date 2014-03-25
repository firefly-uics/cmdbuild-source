(function() {

	var tr = CMDBuild.Translation.administration.tasks.taskEmail;

	Ext.define('CMDBuild.view.administration.tasks.email.CMStep4Delegate', {
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

//		checkWorkflowComboSelected: function() {
//			if (this.getWorkflowComboValue())
//				return true;
//
//			return false;
//		},

		getAttributeTableValues: function() {
			return this.view.attributesTable.getData();
		},

//		getWorkflowComboValue: function() {
//			return this.view.workflowCombo.getValue();
//		},

		fillAttributesGrid: function(data) {
			this.view.attributesTable.fillWithData(data);
		},
//
//		fillWorkflowCombo: function(workflowName) {
//			this.view.workflowCombo.setValue(workflowName);
//		}
	});

	Ext.define('CMDBuild.view.administration.tasks.email.CMStep4', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,
		taskType: 'email',

		border: false,
		height: '100%',
		overflowY: 'auto',

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.email.CMStep4Delegate', this);

			// Workflow form configuration
			this.workflowCombo = Ext.create('CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowFormCombo', {
				name: CMDBuild.ServiceProxy.parameter.CLASS_NAME
			});

			this.attributesTable = Ext.create('CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowFormGrid', {
				margin: '0 0 5 0', // To fix Fieldset bottom padding problem
			});

			this.workflowFormDelegate = Ext.create('CMDBuild.controller.administration.tasks.common.workflowForm.CMWorkflowFormController');
			this.workflowFormDelegate.comboField = this;
			this.workflowFormDelegate.gridField = this.attributesTable;
			this.workflowCombo.delegate = this.workflowFormDelegate;
			this.attributesTable.delegate = this.workflowFormDelegate;

			this.workflowFieldset = Ext.create('Ext.form.FieldSet', {
				title: tr.startWorkflow,
				checkboxToggle: true,
				collapsed: true,
				layout: {
					type: 'vbox',
					align: 'stretch'
				},
				items: [
					{
						xtype: 'container',
						layout: {
							type: 'vbox'
						},
						items: [this.workflowCombo]
					},
					this.attributesTable
				]
			});

			Ext.apply(this, {
				items: [this.workflowFieldset]
			});

			this.callParent(arguments);
		}
	});

})();