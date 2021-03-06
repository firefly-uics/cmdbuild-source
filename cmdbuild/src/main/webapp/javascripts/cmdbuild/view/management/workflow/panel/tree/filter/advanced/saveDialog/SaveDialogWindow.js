(function () {

	Ext.define('CMDBuild.view.management.workflow.panel.tree.filter.advanced.saveDialog.SaveDialogWindow', {
		extend: 'CMDBuild.core.window.AbstractModal',

		/**
		 * @cfg {CMDBuild.controller.management.workflow.panel.tree.filter.advanced.SaveDialog}
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
		 * @property {CMDBuild.view.management.workflow.panel.tree.filter.advanced.saveDialog.FormPanel}
		 */
		form: undefined,

		border: true,
		closeAction: 'hide',
		frame: true,
		title: CMDBuild.Translation.filterParameters,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				items: [
					this.form = Ext.create('CMDBuild.view.management.workflow.panel.tree.filter.advanced.saveDialog.FormPanel', { delegate: this.delegate })
				]
			});

			this.callParent(arguments);
		}
	});

})();
