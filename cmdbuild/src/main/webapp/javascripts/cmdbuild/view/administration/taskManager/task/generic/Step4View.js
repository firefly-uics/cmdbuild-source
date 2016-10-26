(function () {

	Ext.define('CMDBuild.view.administration.taskManager.task.generic.Step4View', {
		extend: 'Ext.panel.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.administration.taskManager.task.Generic'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.generic.Step4}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		fieldEmailAccount: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		fieldEmailTemplate: undefined,

		bodyCls: 'cmdb-gray-panel-no-padding',
		border: false,
		frame: false,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				items: [
					this.fieldEmailAccount = Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.constants.Proxy.EMAIL_ACCOUNT,
						fieldLabel: CMDBuild.Translation.account,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						displayField: CMDBuild.core.constants.Proxy.NAME,
						valueField: CMDBuild.core.constants.Proxy.NAME,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
						forceSelection: true,

						store: CMDBuild.proxy.administration.taskManager.task.Generic.getStoreAccount(),
						queryMode: 'local'
					}),
					this.fieldEmailTemplate = Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.constants.Proxy.EMAIL_TEMPLATE,
						fieldLabel: CMDBuild.Translation.template,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						displayField: CMDBuild.core.constants.Proxy.NAME,
						valueField: CMDBuild.core.constants.Proxy.NAME,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
						forceSelection: true,

						store: CMDBuild.proxy.administration.taskManager.task.Generic.getStoreTemplate(),
						queryMode: 'local'
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
