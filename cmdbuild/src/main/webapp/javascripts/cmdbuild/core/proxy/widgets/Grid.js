(function() {

	Ext.define('CMDBuild.core.proxy.widgets.Grid', {

		requires: ['CMDBuild.model.CMModelFunctions'],

		singleton: true,

		// CSV import
			/**
			 * @param {Object} parameters
			 */
			uploadCsv: function(parameters) {
				parameters.form.submit({
					method: 'POST',
					url: CMDBuild.core.proxy.CMProxyUrlIndex.widgets.grid.uploadCsv,
					scope: parameters.scope,
					success: parameters.success,
					failure: parameters.failure
				});
			},

			/**
			 * @param {Object} parameters
			 */
			getCsvRecords: function(parameters) {
				// Clears server session data
				parameters.callback = function() {
					CMDBuild.Ajax.request({
						method: 'GET',
						url: CMDBuild.core.proxy.CMProxyUrlIndex.widgets.grid.clearSession
					});

					CMDBuild.LoadMask.get().hide();
				};

				CMDBuild.Ajax.request({
					method: 'GET',
					url: CMDBuild.core.proxy.CMProxyUrlIndex.widgets.grid.getCsvRecords,
					scope: parameters.scope,
					success: parameters.success,
					callback: parameters.callback
				});
			},

			/**
			 * @return {Ext.data.SimpleStore}
			 */
			getCsvSeparatorStore: function() {
				return Ext.create('Ext.data.SimpleStore', {
					fields: [CMDBuild.core.proxy.CMProxyConstants.VALUE],
					data: [
						[';'],
						[','],
						['|']
					]
				});
			},

		// Function presets
			/**
			 * Validates presets function name
			 *
			 * @param {Object} parameters
			 */
			getFunctions: function(parameters) {
				CMDBuild.Ajax.request({
					method: 'POST',
					url: CMDBuild.core.proxy.CMProxyUrlIndex.functions.getFunctions,
					scope: parameters.scope,
					success: parameters.success,
					callback: parameters.callback
				});
			},

			/**
			 * @param {Object} parameters
			 *
			 * @return {Ext.data.Store}
			 */
			getStoreFromFunction: function(parameters) {
				// Avoid to send limit, page and start parameters in server calls
				parameters.extraParams.limitParam = undefined;
				parameters.extraParams.pageParam = undefined;
				parameters.extraParams.startParam = undefined;

				return Ext.create('Ext.data.Store', {
					autoLoad: true,
					fields: parameters.fields,
					proxy: {
						type: 'ajax',
						url: CMDBuild.core.proxy.CMProxyUrlIndex.widgets.grid.getSqlCardList,
						reader: {
							root: 'cards',
							type: 'json',
							totalProperty: 'results',
						},
						extraParams: parameters.extraParams
					}
				})
			}
	});

})();