(function () {

	/**
	 * @abstract
	 */
	Ext.define('CMDBuild.view.common.panel.gridAndForm.panel.grid.GridPanel', {
		extend: 'Ext.grid.Panel',

		/**
		 * @cfg {CMDBuild.controller.common.panel.gridAndForm.panel.grid.Grid}
		 */
		delegate: undefined,

		border: false,
		cls: 'cmdb-border-bottom',
		forceFit: true,
		frame: false,
		region: 'center',

		viewConfig: {
			stripeRows: true
		}
	});

})();
