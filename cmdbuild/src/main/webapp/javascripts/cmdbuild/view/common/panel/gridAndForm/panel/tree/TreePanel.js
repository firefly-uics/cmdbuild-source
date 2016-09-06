(function () {

	/**
	 * @abstract
	 */
	Ext.define('CMDBuild.view.common.panel.gridAndForm.panel.tree.TreePanel', {
		extend: 'Ext.tree.Panel',

		/**
		 * @cfg {CMDBuild.controller.common.panel.gridAndForm.panel.tree.Tree}
		 */
		delegate: undefined,

		autoScroll: true,
		border: false,
		cls: 'cmdb-border-bottom',
		forceFit: true,
		frame: false,
		region: 'center',
		rootVisible: false,

		viewConfig: {
			stripeRows: true
		}
	});

})();
