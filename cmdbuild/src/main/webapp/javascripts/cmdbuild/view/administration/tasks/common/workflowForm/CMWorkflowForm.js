(function() {

	Ext.require('CMDBuild.core.proxy.CMProxyTasks');

	Ext.define('CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowForm', {
		extend: 'Ext.panel.Panel',

		border: false,

		/**
		 * To acquire informations to setup fields before creation
		 *
		 * @param (Object) configuration
		 * @param (Object) configuration.combo
		 * @param (Object) configuration.grid
		 */
		constructor: function(configuration) {
			this.delegate = Ext.create('CMDBuild.controller.administration.tasks.common.workflowForm.CMWorkflowFormController', this);

			if (typeof configuration == 'undefined' || typeof configuration.combo == 'undefined') {
				this.comboConfig = { delegate: this.delegate };
			} else {
				this.comboConfig = configuration.combo;
				this.comboConfig.delegate = this.delegate;
			}

			if (typeof configuration == 'undefined' || typeof configuration.grid == 'undefined') {
				this.gridConfig = { delegate: this.delegate };
			} else {
				this.gridConfig = configuration.grid;
				this.gridConfig.delegate = this.delegate;
			}

			this.callParent(arguments);
		},

		initComponent: function() {
			this.combo = Ext.create('CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowFormCombo', this.comboConfig);
			this.grid = Ext.create('CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowFormGrid', this.gridConfig);

			this.delegate.comboField = this.combo;
			this.delegate.gridField = this.grid;

			Ext.apply(this, {
				items: [this.combo, this.grid]
			});

			this.callParent(arguments);
		}
	});

})();