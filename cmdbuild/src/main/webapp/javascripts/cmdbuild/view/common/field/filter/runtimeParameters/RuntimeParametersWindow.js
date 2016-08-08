(function () {

	Ext.define('CMDBuild.view.common.field.filter.runtimeParameters.RuntimeParametersWindow', {
		extend: 'CMDBuild.core.window.AbstractCustomModal',

		/**
		 * @cfg {CMDBuild.controller.common.field.filter.runtimeParameters.RuntimeParameters}
		 */
		delegate: undefined,

		/**
		 * @cfg {Object}
		 */
		dimensions: {
			height: 'auto',
			width: 60
		},

		/**
		 * @cfg {String}
		 */
		dimensionsMode: 'percentage',

		/**
		 * @property {Ext.form.Panel}
		 */
		form: undefined,

		baseTitle: '@@ Filter\'s input parameters', // TODO: translations (CMDBuild.Translation)
		border: true,
		closeAction: 'hide',
		frame: false,
		layout: 'fit',

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'bottom',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							this.saveButton = Ext.create('CMDBuild.core.buttons.text.Apply', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onFieldFilterRuntimeParametersApplyButtonClick');
								}
							}),
							this.saveButton = Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onFieldFilterRuntimeParametersAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.form = Ext.create('Ext.form.Panel', {
						bodyCls: 'cmdb-blue-panel',
						border: false,
						frame: false,
						labelAlign: 'right',

						layout: {
							type: 'vbox',
							align: 'stretch'
						}
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
