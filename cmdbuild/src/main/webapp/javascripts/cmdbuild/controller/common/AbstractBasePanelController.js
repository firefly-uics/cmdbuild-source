(function () {

	/**
	 * Class to be extended in controllers witch creates a substrate to adapt AbstractController functionalities with old CMDBuild panel creation through CMMainViewport
	 *
	 * @abstract
	 */
	Ext.define('CMDBuild.controller.common.AbstractBasePanelController', {
		extend: 'CMDBuild.controller.common.AbstractController',

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

			this.view.on('CM_iamtofront', this.onViewOnFront, this);
		},

		/**
		 * @abstract
		 */
		onViewOnFront: Ext.emptyFn
	});

})();