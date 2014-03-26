(function() {

	Ext.define('CMDBuild.view.administration.tasks.workflow.CMStep2Delegate', {
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

		getCronDelegate: function() {
			return this.view.cronForm.delegate;
		},

		isEmptyAdvanced: function() {
			return this.getCronDelegate().isEmptyAdvanced();
		},

		setValueAdvancedFields: function(cronExpression) {
			this.getCronDelegate().setValueAdvancedFields(cronExpression);
		},

		/**
		 * Try to find the correspondence of advanced cronExpression in baseCombo's store
		 */
		setValueBase: function(value) {
			this.getCronDelegate().setValueBase(value);
		}
	});

	Ext.define('CMDBuild.view.administration.tasks.workflow.CMStep2', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,
		taskType: 'workflow',

		border: false,
		height: '100%',
		overflowY: 'auto',

		initComponent: function() {
			this.delegate = Ext.create('CMDBuild.view.administration.tasks.workflow.CMStep2Delegate', this);
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