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
		// overwrite
		cmOn: function(name, param, callBack) {
			switch (name) {
				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		getWorkflowDelegate: function() {
			return this.view.workflowForm.delegate;
		},

		getValueAttributeGrid: function() {
			return this.getWorkflowDelegate().getValueGrid();
		},

		setDisabledAttributesTable: function(state) {
			this.getWorkflowDelegate().setDisabledAttributesTable(state);
		},

		setValueAttributesGrid: function(data) {
			this.getWorkflowDelegate().setValueGrid(data);
		}
	});

	Ext.define('CMDBuild.view.administration.tasks.email.CMStep4', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,
		taskType: 'email',

		border: false,
		height: '100%',
		overflowY: 'auto',

		initComponent: function() {
			this.delegate = Ext.create('CMDBuild.view.administration.tasks.email.CMStep4Delegate', this);

			this.workflowForm = Ext.create('CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowForm', {
				combo: {
					name: CMDBuild.ServiceProxy.parameter.WORKFLOW
				}
			});

			this.workflowFieldset = Ext.create('Ext.form.FieldSet', {
				title: tr.startWorkflow,
				checkboxToggle: true,
				collapsed: true,

				layout: {
					type: 'vbox',
					align: 'stretch'
				},

				items: [this.workflowForm]
			});

			Ext.apply(this, {
				items: [this.workflowFieldset]
			});

			this.callParent(arguments);
		}
	});

})();