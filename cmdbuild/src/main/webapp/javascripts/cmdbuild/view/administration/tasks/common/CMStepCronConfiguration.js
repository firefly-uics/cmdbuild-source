(function() {

	Ext.define('CMDBuild.view.administration.tasks.common.CMStepCronConfigurationDelegate', {
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
		// overwrite
		cmOn: function(name, param, callBack) {
			switch (name) {
				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		// GETters functions
			/**
			 * @return (Object) delegate
			 */
			getCronDelegate: function() {
				return this.view.cronForm.delegate;
			},

		// SETters functions
			/**
			 * @param (String) cronExpression
			 */
			setValueAdvancedFields: function(cronExpression) {
				this.getCronDelegate().setValueAdvancedFields(cronExpression);
			},

			/**
			 * @param (String) value
			 */
			setValueBase: function(value) {
				this.getCronDelegate().setValueBase(value);
			}
	});

	Ext.define('CMDBuild.view.administration.tasks.common.CMStepCronConfiguration', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,

		border: false,
		height: '100%',
		overflowY: 'auto',

		initComponent: function() {
			this.delegate = Ext.create('CMDBuild.view.administration.tasks.common.CMStepCronConfigurationDelegate', this);
			this.cronForm = Ext.create('CMDBuild.view.administration.tasks.common.cronForm.CMCronForm');

			Ext.apply(this, {
				items: [this.cronForm]
			});

			this.callParent(arguments);
		},

		listeners: {
			// To correctly enable radio fields on tab show
			show: function(view, eOpts) {
				this.cronForm.fireEvent('show', view, eOpts);
			}
		}
	});

})();