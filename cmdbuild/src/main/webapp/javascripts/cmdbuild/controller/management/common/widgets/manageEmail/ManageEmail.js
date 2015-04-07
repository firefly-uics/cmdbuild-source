(function () {

	/**
	 * Main widget controller which manage email regeneration methods
	 */
	Ext.define('CMDBuild.controller.management.common.widgets.manageEmail.ManageEmail', {
		extend: 'CMDBuild.controller.management.common.widgets.CMWidgetController',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		mixins: {
			observable: 'Ext.util.Observable'
		},

		card: undefined, // TODO da configurare

		/**
		 * @property {Ext.form.Basic}
		 */
		clientForm: undefined,

		/**
		 * @cfg {CMDBuild.controller.management.common.CMWidgetManagerController}
		 */
		ownerController: undefined,

		/**
		 * @property {CMDBuild.view.management.common.widgets.manageEmail.MainPanel}
		 */
		view: undefined,

		/**
		 * @cfg {Object}
		 */
		widgetConf: undefined,

		/**
		 * @param {CMDBuild.view.management.common.widgets.manageEmail.MainPanel} view
		 * @param {CMDBuild.controller.management.common.CMWidgetManagerController} ownerController
		 * @param {Object} widgetConf
		 * @param {Ext.form.Basic} clientForm
		 * @param {CMDBuild.model.CMActivityInstance} card
		 *
		 * @override
		 */
		constructor: function(view, ownerController, widgetConf, clientForm, card) {
			this.mixins.observable.constructor.call(this);

			this.callParent(arguments);
		},

//		/**
//		 * @override
//		 */
//		beforeActiveView: function() {},

		/**
		 * @return {Object}
		 *
		 * @override
		 */
		getData: function() {
			var out = {};
			out[CMDBuild.core.proxy.CMProxyConstants.OUTPUT] = this.getActivityId();

			return out;
		},

//		/**
//		 * Used to mark widget as busy during regenerations, especially useful for getData() regeneration
//		 *
//		 * @return {Boolean}
//		 *
//		 * @override
//		 */
//		isBusy: function() {
//			return true;
//		},

//		/**
//		 * @return {Boolean}
//		 *
//		 * @override
//		 */
//		isValid: function() {
//			return this.callParent(arguments);
//		},

//		/**
//		 * @param {Array} callbackChainArray
//		 *
//		 * @override
//		 */
//		onBeforeSave: function(callbackChainArray, i) {
//			if (!Ext.isEmpty(callbackChainArray[i])) {
//				this.globalLoadMask = false;
//
//				this.beforeSaveCallbackObject = {
//					array: callbackChainArray,
//					index: i
//				};
//
//				this.controllerGrid.storeLoad(true);
//			}
//		},

//		/**
//		 * Initialize widget on widget configuration to apply all events on form fields
//		 *
//		 * @override
//		 */
//		onEditMode: function() {
//			this.setActivityId();
//
//			if (!this.grid.getStore().isLoading())
//				this.controllerGrid.storeLoad(true, true);
//		}
	});

})();