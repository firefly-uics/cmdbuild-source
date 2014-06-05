(function() {

	var tr = CMDBuild.Translation.administration.tasks;

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

		/**
		 * @return (String)
		 */
		checkWorkflowComboSelected: function() {
			return this.getWorkflowDelegate().getValueCombo();
		},

		// GETters functions
			/**
			 * @return (Object) delegate
			 */
			getWorkflowDelegate: function() {
				return this.view.workflowForm.delegate;
			},

			/**
			 * @return (String)
			 */
			getValueWorkflowAttributeGrid: function() {
				return this.getWorkflowDelegate().getValueGrid();
			},

			/**
			 * @return (Boolean)
			 */
			getValueWorkflowFieldsetCheckbox: function() {
				return this.view.workflowFieldset.checkboxCmp.getValue();
			},

		// SETters functions
			/**
			 * @param (Boolean) state
			 */
			setDisabledWorkflowAttributesGrid: function(state) {
				this.getWorkflowDelegate().setDisabledAttributesGrid(state);
			},

			/**
			 * @param (Object) value
			 */
			setValueWorkflowAttributesGrid: function(value) {
				this.getWorkflowDelegate().setValueGrid(value);
			},

			/**
			 * @param (String) value
			 */
			setValueWorkflowCombo: function(value) {
				this.getWorkflowDelegate().setValueCombo(value);
			},

			/**
			 * @param (Boolean) state
			 */
			setValueWorkflowFieldsetCheckbox: function(state) {
				if (state) {
					this.view.workflowFieldset.expand();
				} else {
					this.view.workflowFieldset.collapse();
				}
			}
	});

	Ext.define('CMDBuild.view.administration.tasks.email.CMStep4', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,

		bodyCls: 'cmgraypanel',
		border: false,
		autoScroll: true,

		initComponent: function() {
			this.delegate = Ext.create('CMDBuild.view.administration.tasks.email.CMStep4Delegate', this);

			this.workflowForm = Ext.create('CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowForm', {
				combo: {
					name: CMDBuild.core.proxy.CMProxyConstants.WORKFLOW_CLASS_NAME
				}
			});

			this.workflowFieldset = Ext.create('Ext.form.FieldSet', {
				title: tr.startWorkflow,
				checkboxName: CMDBuild.core.proxy.CMProxyConstants.WORKFLOW_ACTIVE,
				checkboxToggle: true,
				collapsed: true,
				collapsible: true,
				toggleOnTitleClick: true,
				autoScroll: true,

				items: [this.workflowForm]
			});

			Ext.apply(this, {
				items: [this.workflowFieldset]
			});

			this.callParent(arguments);
		}
	});

})();