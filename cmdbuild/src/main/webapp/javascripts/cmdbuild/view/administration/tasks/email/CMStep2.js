(function() {

	var tr = CMDBuild.Translation.administration.tasks.taskWorkflow;

	Ext.define('CMDBuild.view.administration.tasks.email.CMStep2Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		parentDelegate: undefined,
		filterWindow: undefined,
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

		isAdvancedEmpty: function() {
			return this.view.cronForm.delegate.isAdvancedEmpty();
		},

		setAdvancedValue: function(cronExpression) {
			this.view.cronForm.delegate.setAdvancedValue(cronExpression);
		},

		/**
		 * Try to find the correspondence of advanced cronExpression in baseCombo's store
		 *
		 * @param (String) value
		 */
		setBaseValue: function(value) {
			this.view.cronForm.delegate.setBaseValue(value);
		},
//
//		setDisabledAdvancedFields: function(value) {
//			for (var key in this.view.advancedFields)
//				this.view.advancedFields[key].setDisabled(value);
//		}
	});

	Ext.define('CMDBuild.view.administration.tasks.email.CMStep2', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,
		taskType: 'workflow',

		border: false,
		height: '100%',
		overflowY: 'auto',

		initComponent: function() {
			this.delegate = Ext.create('CMDBuild.view.administration.tasks.email.CMStep2Delegate', this);
			this.cronForm = Ext.create('CMDBuild.view.administration.tasks.common.cronForm.CMCronForm');

			Ext.apply(this, {
				items: [this.cronForm]
			});

			this.callParent(arguments);
		},

		listeners: {
			/**
			 * To correctly enable radio fields on tab show
			 */
			show: function(view, eOpts) {
				this.cronForm.fireEvent('show', view, eOpts);
			}
		}
	});

})();