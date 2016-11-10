(function () {

	/**
	 * @link CMDBuild.view.management.workflow.panel.tree.filter.advanced.saveDialog.SaveDialogWindow
	 */
	Ext.define('CMDBuild.view.common.panel.gridAndForm.panel.common.filter.advanced.saveDialog.SaveDialogWindow', {
		extend: 'CMDBuild.core.window.AbstractCustomModal',

		/**
		 * @cfg {CMDBuild.controller.common.panel.gridAndForm.panel.common.filter.advanced.SaveDialog}
		 */
		delegate: undefined,

		/**
		 * @cfg {Object}
		 */
		dimensions: {
			width: 650
		},

		/**
		 * @cfg {String}
		 */
		dimensionsMode: 'absolute',

		/**
		 * @property {CMDBuild.view.common.panel.gridAndForm.panel.common.filter.advanced.saveDialog.FormPanel}
		 */
		form: undefined,

		border: true,
		closeAction: 'hide',
		frame: true,
		layout: 'fit',
		title: CMDBuild.Translation.filterParameters,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				items: [
					this.form = Ext.create('CMDBuild.view.common.panel.gridAndForm.panel.common.filter.advanced.saveDialog.FormPanel', { delegate: this.delegate })
				]
			});

			this.callParent(arguments);
		}
	});

})();
