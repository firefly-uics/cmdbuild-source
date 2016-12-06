(function () {

	Ext.require([
		'CMDBuild.core.constants.Proxy',
		'CMDBuild.core.proxy.common.field.comboBox.WorkflowSelector'
	]);

	Ext.define('CMDBuild.view.common.field.comboBox.WorflowSelector', {
		extend: 'Ext.form.field.ComboBox',

		/**
		 * @cfg {String}
		 *
		 * @required
		 */
		name: undefined,

		fieldLabel: CMDBuild.Translation.workflow,
		valueField: CMDBuild.core.constants.Proxy.NAME,
		displayField: CMDBuild.core.constants.Proxy.TEXT, // TODO: waiting for refactor (rename description)
		queryMode: 'local',

		/**
		 * @override
		 */
		initComponent: function () {
			if (Ext.isEmpty(this.store))
				Ext.apply(this, {
					store: CMDBuild.core.proxy.common.field.comboBox.WorkflowSelector.getStoreWorkflow()
				});

			this.callParent(arguments);
		}
	});

})();
