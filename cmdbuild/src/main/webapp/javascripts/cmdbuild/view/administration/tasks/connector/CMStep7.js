(function() {

	var tr = CMDBuild.Translation.administration.tasks;

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep7Delegate', {
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
		}
	});

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep7', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,

		border: false,
		height: '100%',
		overflowY: 'auto',

		initComponent: function() {
			this.delegate = Ext.create('CMDBuild.view.administration.tasks.connector.CMStep7Delegate', this);

			this.deletionTypeCombo = Ext.create('Ext.form.field.ComboBox', {
				name: CMDBuild.ServiceProxy.parameter.DELETION_TYPE,
				fieldLabel: tr.taskConnector.deletionType,
				labelWidth: CMDBuild.LABEL_WIDTH,
				store: CMDBuild.core.proxy.CMProxyTasks.getDeletionTypes(),
				displayField: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
				valueField: CMDBuild.ServiceProxy.parameter.VALUE,
				width: CMDBuild.CFG_BIG_FIELD_WIDTH,
				forceSelection: true,
				editable: false
			});

			this.operationsCombo = Ext.create('Ext.form.field.ComboBox', {
				name: CMDBuild.ServiceProxy.parameter.OPERATIONS,
				fieldLabel: tr.taskConnector.operations,
				labelWidth: CMDBuild.LABEL_WIDTH,
				store: CMDBuild.core.proxy.CMProxyTasks.getOperations(),
				displayField: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
				valueField: CMDBuild.ServiceProxy.parameter.VALUE,
				width: CMDBuild.CFG_BIG_FIELD_WIDTH,
				forceSelection: true,
				editable: false
			});

			Ext.apply(this, {
				items: [
					this.deletionTypeCombo,
					this.operationsCombo
				]
			});

			this.callParent(arguments);
		}
	});

})();
