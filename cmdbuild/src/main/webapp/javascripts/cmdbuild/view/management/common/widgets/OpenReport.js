(function() {

	Ext.define('CMDBuild.view.management.common.widgets.OpenReport', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.core.proxy.widgets.OpenReport'
		],

		statics: {
			WIDGET_NAME: '.OpenReport'
		},

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
						name: CMDBuild.core.proxy.Constants.EXTENSION,
						fieldLabel: CMDBuild.Translation.format,
						labelAlign: 'right',
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.MEDIUM_FIELD_WIDTH,
						valueField: CMDBuild.core.proxy.Constants.VALUE,
						displayField: CMDBuild.core.proxy.Constants.DESCRIPTION,
						value: CMDBuild.core.proxy.Constants.PDF,
						editable: false,
						forceSelection: true,

						store: CMDBuild.core.proxy.widgets.OpenReport.getFormatsStore(),
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