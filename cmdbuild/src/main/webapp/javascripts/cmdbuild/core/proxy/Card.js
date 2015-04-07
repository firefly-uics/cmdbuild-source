(function() {

	Ext.define('CMDBuild.core.proxy.Card', {
		alternateClassName: 'CMDBuild.ServiceProxy.card', // Legacy class name

		requires: [
			'CMDBuild.core.proxy.CMProxy',
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyUrlIndex'
		],

		singleton: true,

		/**
		 * @property {Object} params
		 */
		getCardHistory: function(params) {
			return Ext.create('Ext.data.Store', {
				autoLoad: false,
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.CMProxyUrlIndex.card.getCardHistory,
					reader: {
						type: 'json',
						root: 'rows'
					}
				},
				sorters: [
					{
						property: 'BeginDate',
						direction: 'DESC'
					},
					{
						property: '_EndDate',
						direction: 'DESC'
					}
				],
				fields: params.fields,
				baseParams: params.baseParams
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
			params.url = CMDBuild.core.proxy.CMProxyUrlIndex.card.getPosition;

			CMDBuild.ServiceProxy.core.doRequest(params);
		},

		/**
		 * @param {Object} params
		 */
		get: function(params) {
			adaptGetCardCallParams(params);
			params.method = 'GET';
			params.url = CMDBuild.core.proxy.CMProxyUrlIndex.card.read;

			CMDBuild.ServiceProxy.core.doRequest(params);
		},

		/**
		 * @param {Object} params
		 */
		remove: function(params) {
			params.method = 'POST';
			params.url = CMDBuild.core.proxy.CMProxyUrlIndex.card.remove;
			params.important = true;

			CMDBuild.ServiceProxy.core.doRequest(params);
		},

		/**
		 * @param {Object} params
		 */
		bulkUpdate: function(params) {
			params.method = 'POST';
			params.url = CMDBuild.core.proxy.CMProxyUrlIndex.card.bulkUpdate;

			CMDBuild.ServiceProxy.core.doRequest(params);
		},

		/**
		 * @param {Object} params
		 */
		bulkUpdateFromFilter: function(params) {
			params.method = 'POST';
			params.url = CMDBuild.core.proxy.CMProxyUrlIndex.card.bulkUpdateFromFilter;

			CMDBuild.ServiceProxy.core.doRequest(params);
		},

		/**
		 *
		 * Id of the card to lock, className is not required because id is unique
		 *
		 * @param {Number} params.id
		 */
		lockCard: function(params) {
			params.method = 'POST';
			params.url = CMDBuild.core.proxy.CMProxyUrlIndex.card.lock;

			CMDBuild.ServiceProxy.core.doRequest(params);
		},

		/**
		 * Id of card to unlock
		 *
		 * @param {Number} params.id
		 */
		unlockCard: function(params) {
			params.method = 'POST';
			params.url = CMDBuild.core.proxy.CMProxyUrlIndex.card.unlock;

			CMDBuild.ServiceProxy.core.doRequest(params);
		},

		/**
		 * Unlock all cards that was locked
		 *
		 * @param {Object} parameters
		 */
		unlockAllCards: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.card.unlockAll,
				loadMask: true,
				params: parameters.params,
				scope: parameters.scope,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		}
	});

	function adaptGetCardCallParams(p) {
		if (p.params.Id && p.params.IdClass) {
			_deprecated();

			var parameters = {};
			parameters[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = _CMCache.getEntryTypeNameById(p.params.IdClass);
			parameters[CMDBuild.core.proxy.CMProxyConstants.CARD_ID] = p.params.Id;

			p.params = parameters;
		}
	}

})();