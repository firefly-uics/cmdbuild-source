(function() {

	Ext.define('CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowForm', {
		extend: 'Ext.panel.Panel',

		border: false,

		/**
		 * To acquire informations to setup fields before creation
		 *
		 * @param (Object) comboSetup
		 * @param (Object) gridSetup
		 */
		constructor: function(comboSetup, gridSetup) {
			this.delegate = Ext.create('CMDBuild.controller.administration.tasks.common.workflowForm.CMWorkflowFormController', this);

			if (typeof comboSetup == 'undefined') {
				this.comboSetup = { delegate: this.delegate };
			} else {
				this.comboSetup = comboSetup;
				this.comboSetup.delegate = this.delegate;
			}

			if (typeof gridSetup == 'undefined') {
				this.gridSetup = { delegate: this.delegate };
			} else {
				this.gridSetup = gridSetup;
				this.gridSetup.delegate = this.delegate;
			}

			this.callParent(arguments);
		},

		initComponent: function() {
			this.combo = Ext.create('CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowFormCombo', this.comboSetup);
			this.grid = Ext.create('CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowFormGrid', this.gridSetup);

			this.delegate.comboField = this.combo;
			this.delegate.gridField = this.grid;

			Ext.apply(this, {
				items: [this.combo, this.grid]
			});

			this.callParent(arguments);
		}
	});

})();