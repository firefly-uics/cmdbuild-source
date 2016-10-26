(function () {

	Ext.define('CMDBuild.view.administration.taskManager.task.common.field.stringList.window.EditWindow', {
		extend: 'CMDBuild.core.window.AbstractCustomModal',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.common.field.stringList.window.Edit}
		 */
		delegate: undefined,

		/**
		 * @cfg {Object}
		 */
		dimensions: {
			height: 300,
			width: 400
		},

		/**
		 * @cfg {String}
		 */
		dimensionsMode: 'absolute',

		/**
		 * @property {Ext.form.Panel}
		 */
		formPanel: undefined,

		border: true,
		closeAction: 'hide',
		frame: false,
		layout: 'fit',
		resizable: false,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.add.Add', {
								text: CMDBuild.Translation.addFilter,
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onTaskManagerCommonFieldStringListWindowAddButtonClick');
								}
							})
						]
					}),
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
							Ext.create('CMDBuild.core.buttons.text.Confirm', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onTaskManagerCommonFieldStringListWindowConfirmButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onTaskManagerCommonFieldStringListWindowAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.formPanel = Ext.create('Ext.form.Panel', {
						bodyCls: 'cmdb-gray-panel-no-padding',
						border: false,
						frame: false,
						overflowY: 'auto',

						layout: {
							type: 'vbox',
							align: 'stretch'
						},

						items: []
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
