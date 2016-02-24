(function() {

	Ext.define('CMDBuild.view.administration.navigationTree.tree.TreeView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.navigationTree.Tree}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.navigationTree.tree.FormPanel}
		 */
		form: undefined,

		border: false,
		frame: false,
		layout: 'fit',
		title: CMDBuild.Translation.tree,

		initComponent: function () {
			Ext.apply(this, {
				items: [
					this.form = Ext.create('CMDBuild.view.administration.navigationTree.tree.FormPanel', { delegate: this.delegate })
				]
			});

			this.callParent(arguments);
		}
	});

})();
