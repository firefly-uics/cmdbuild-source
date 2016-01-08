(function() {

	Ext.define('CMDBuild.core.proxy.Card', {
		alternateClassName: 'CMDBuild.ServiceProxy.card', // Legacy class name

		requires: [
			'CMDBuild.core.interfaces.Ajax',
			'CMDBuild.core.proxy.CMProxy',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * @param {Object} params
		 */
		bulkUpdate: function(params) {
			params.method = 'POST';
			params.url = CMDBuild.core.proxy.Index.card.bulkUpdate;

			CMDBuild.ServiceProxy.core.doRequest(params);
		},

		/**
		 * @param {Object} params
		 */
		bulkUpdateFromFilter: function(params) {
			params.method = 'POST';
			params.url = CMDBuild.core.proxy.Index.card.bulkUpdateFromFilter;

			CMDBuild.ServiceProxy.core.doRequest(params);
		},

		/**
		 * @param {Object} parameters
		 */
		getList: function(parameters) {
			CMDBuild.core.interfaces.Ajax.request({
				method: 'GET',
				url: CMDBuild.core.proxy.Index.card.getList,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false,
				failure: parameters.failure || Ext.emptyFn,
				success: parameters.success || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		},

		/**
		 * Retrieve the position on the DB of the required card, considering the sorting and current filter applied on the grid
		 *
		 * @param {Object} p
		 * 		Ex: {
		 * 			params: {
		 * 				{Number} cardId
		 * 				{String} className
		 * 				{Object} filter
		 * 				{Object} sort
		 * 			}
		 * 		}
		 */
		getPosition: function(params) {
			params.method = 'GET';
			params.url = CMDBuild.core.proxy.Index.card.getPosition;

			CMDBuild.ServiceProxy.core.doRequest(params);
		},

		/**
		 * @param {Object} params
		 */
		get: function(params) {
			adaptGetCardCallParams(params);
			params.method = 'GET';
			params.url = CMDBuild.core.proxy.Index.card.read;

			CMDBuild.ServiceProxy.core.doRequest(params);
		},

		/**
		 * @param {Object} params
		 */
		remove: function(parameters) {
			parameters.important = true;

			CMDBuild.core.interfaces.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.card.remove,
				headers: parameters.headers,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: parameters.loadMask || true,
				failure: parameters.failure || Ext.emptyFn,
				success: parameters.success || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		},

		/**
		 * @property {Object} parameters
		 */
		update: function(parameters) {
			CMDBuild.core.interfaces.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.card.update,
				headers: parameters.headers,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				failure: parameters.failure || Ext.emptyFn,
				success: parameters.success || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		},

		// Lock/Unlock methods
			/**
			 * @param {Object} parameters
			 */
			lock: function(parameters) {
				CMDBuild.core.interfaces.Ajax.request({
					method: 'POST',
					url: CMDBuild.core.proxy.Index.classes.cards.lock,
					headers: parameters.headers,
					params: parameters.params,
					loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false,
					scope: parameters.scope || this,
					failure: parameters.failure || Ext.emptyFn,
					success: parameters.success || Ext.emptyFn,
					callback: parameters.callback || Ext.emptyFn
				});
			},

			/**
			 * @param {Object} parameters
			 */
			unlock: function(parameters) {
				CMDBuild.core.interfaces.Ajax.request({
					method: 'POST',
					url: CMDBuild.core.proxy.Index.classes.cards.unlock,
					headers: parameters.headers,
					params: parameters.params,
					loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false,
					scope: parameters.scope || this,
					failure: parameters.failure || Ext.emptyFn,
					success: parameters.success || Ext.emptyFn,
					callback: parameters.callback || Ext.emptyFn
				});
			},

			/**
			 * @param {Object} parameters
			 */
			unlockAll: function(parameters) {
				CMDBuild.core.interfaces.Ajax.request({
					method: 'POST',
					url: CMDBuild.core.proxy.Index.classes.cards.unlockAll,
					headers: parameters.headers,
					params: parameters.params,
					loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
					scope: parameters.scope || this,
					failure: parameters.failure || Ext.emptyFn,
					success: parameters.success || Ext.emptyFn,
					callback: parameters.callback || Ext.emptyFn
				});
			}
	});

	function adaptGetCardCallParams(p) {
		if (p.params.Id && p.params.IdClass) {
			_deprecated('adaptGetCardCallParams', 'CMDBuild.core.proxy.Card');

			var parameters = {};
			parameters[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(p.params.IdClass);
			parameters[CMDBuild.core.constants.Proxy.CARD_ID] = p.params.Id;

			p.params = parameters;
		}
	}

})();