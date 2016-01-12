(function () {

	/**
	 * Class to be extended in controllers witch creates a substrate to adapt CMDBuild.controller.common.abstract.Base functionalities
	 * with old CMDBuild panel creation through CMMainViewport
	 *
	 * @abstract
	 */
	Ext.define('CMDBuild.controller.common.abstract.BasePanel', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		/**
		 * @property {Object}
		 */
		view: undefined,

		/**
		 * @param {Object} view
		 */
		constructor: function(view) {
			this.callParent([{ view: view }]);

			this.cmName = this.view.cmName;
			this.view.delegate = this; // Apply delegate to view
		},

		/**
		 * Must be implemented a method with right name localization to manage onModuleInit cmfg() event
		 *
		 * @abstract
		 */
		onModuleInit: Ext.emptyFn
	});

})();