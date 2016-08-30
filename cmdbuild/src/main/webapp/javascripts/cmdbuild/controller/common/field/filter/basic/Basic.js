(function () {

	Ext.define('CMDBuild.controller.common.field.filter.basic.Basic', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Utils'
		],

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.controller.common.field.filter.advanced.window.Window}
		 */
		controllerFilterWindow: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onFieldFilterBasicReset',
			'onFieldFilterBasicTrigger1Click',
			'onFieldFilterBasicTrigger2Click'
		],

		/**
		 * @property {CMDBuild.view.common.field.filter.basic.Basic}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {Object} configObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.common.field.filter.basic.Basic', { delegate: this });
		},

		/**
		 * @param {Boolean} applyToStore
		 *
		 * @returns {Void}
		 */
		onFieldFilterBasicReset: function (applyToStore) {
			applyToStore = Ext.isBoolean(applyToStore) ? applyToStore : true;

			this.view.setValue();

			if (applyToStore)
				this.setQueryToStoreFilterAndLoad();
		},

		/**
		 * @returns {Void}
		 */
		onFieldFilterBasicTrigger1Click: function () {
			this.setQueryToStoreFilterAndLoad();
		},

		/**
		 * @returns {Void}
		 */
		onFieldFilterBasicTrigger2Click: function () {
			if (!this.view.isDisabled())
				this.cmfg('onFieldFilterBasicReset');
		},

		/**
		 * Decodes store filter JSON string and inject query parameter
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		setQueryToStoreFilterAndLoad: function () {
			var filter = {};
			var store = this.cmfg('panelGridAndFormGridStoreGet');

			if (
				Ext.isObject(store) && !Ext.Object.isEmpty(store)
				&& !Ext.isEmpty(store.getProxy())
				&& !Ext.isEmpty(store.getProxy().extraParams)
				&& !Ext.isEmpty(store.getProxy().extraParams[CMDBuild.core.constants.Proxy.FILTER])
			) {
				filter = store.getProxy().extraParams[CMDBuild.core.constants.Proxy.FILTER];
			}

			if (CMDBuild.core.Utils.isJsonString(filter))
				filter = Ext.decode(filter);

			filter[CMDBuild.core.constants.Proxy.QUERY] = this.view.getValue();

			var params = {};
			params[CMDBuild.core.constants.Proxy.FILTER] = Ext.encode(filter);

			this.cmfg('panelGridAndFormGridStoreLoad', { params: params });
		}
	});

})();