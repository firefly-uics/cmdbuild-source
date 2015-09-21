(function() {

	Ext.define('CMDBuild.view.common.field.filter.advanced.window.panels.functions.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.common.field.filter.advanced.window.Functions'
		],

		/**
		 * @cfg {CMDBuild.controller.common.field.filter.advanced.window.panels.Functions}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.common.field.CMErasableCombo}
		 */
		functionComboBox: undefined,

		bodyCls: 'x-panel-default-framed',
		border: false,
		frame: false,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

		initComponent: function() {
			Ext.apply(this, {
				items: [
					this.functionComboBox = Ext.create('CMDBuild.view.common.field.CMErasableCombo', {
						name: CMDBuild.ServiceProxy.parameter.FUNCTION,
						fieldLabel: CMDBuild.Translation.management.findfilter.functions,
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
						displayField: CMDBuild.ServiceProxy.parameter.NAME,
						valueField: CMDBuild.ServiceProxy.parameter.NAME,
						editable: false,
						forceSelection: true,

						store: CMDBuild.core.proxy.common.field.filter.advanced.window.Functions.getStore(),
						queryMode: 'local'
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();