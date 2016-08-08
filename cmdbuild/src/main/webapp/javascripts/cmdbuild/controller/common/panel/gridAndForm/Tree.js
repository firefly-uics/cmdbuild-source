(function () {

	/**
	 * Required cmfg methods:
	 * 	- panelGridAndFormGridStoreGet
	 * 	- panelGridAndFormGridStoreLoad
	 *
	 * @abstract
	 */
	Ext.define('CMDBuild.controller.common.panel.gridAndForm.Tree', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.common.panel.gridAndForm.GridAndForm}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {CMDBuild.view.common.panel.gridAndForm.TreePanel}
		 */
		view: undefined
	});

})();
