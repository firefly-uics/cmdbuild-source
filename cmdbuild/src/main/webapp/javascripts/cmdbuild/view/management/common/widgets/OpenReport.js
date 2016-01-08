(function() {

	Ext.define('CMDBuild.view.management.common.widgets.OpenReport', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.widget.OpenReport'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.OpenReport}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		formatCombo: undefined,

		/**
		 * @property {Ext.container.Container}
		 */
		fieldContainer: undefined,

		bodyCls: 'x-panel-body-default-framed',
		border: false,
		frame: false,

		bodyStyle: {
			padding: '5px'
		},

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

		initComponent: function() {
			Ext.apply(this, {
				items: [
					this.formatCombo = Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.constants.Proxy.EXTENSION,
						fieldLabel: CMDBuild.Translation.format,
						labelAlign: 'right',
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.MEDIUM_FIELD_WIDTH,
						valueField: CMDBuild.core.constants.Proxy.VALUE,
						displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
						value: CMDBuild.core.constants.Proxy.PDF,
						editable: false,
						forceSelection: true,

						store: CMDBuild.core.proxy.widget.OpenReport.getStoreFormats(),
						queryMode: 'local'
					}),
					this.fieldContainer = Ext.create('Ext.container.Container', { // To contains all non fixed fields
						frame: false,
						border: false,

						layout: {
							type: 'vbox',
							align: 'stretch'
						},

						defaults: {
							maxWidth: CMDBuild.BIG_FIELD_WIDTH
						},

						items: []
					})
				]
			});

			this.callParent(arguments);
		},

		/**
		 * @returns {Array}
		 *
		 * @override
		 */
		getExtraButtons: function() {
			return [
				Ext.create('CMDBuild.core.buttons.text.Confirm', {
					scope: this,

					handler: function(button, e) {
						this.delegate.cmfg('onOpenReportSaveButtonClick');
					}
				})
			];
		}
	});

})();