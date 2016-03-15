(function() {

	Ext.define('CMDBuild.core.proxy.widgets.CustomForm', {

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		singleton: true,
		
		/**
		 * @param {Object} parameters
		 */
		getCardList: function(parameters) {
			CMDBuild.Ajax.request({
				url: CMDBuild.core.proxy.CMProxyUrlIndex.card.getList,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false,
				failure: parameters.failure || Ext.emptyFn,
				success: parameters.success || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		},

		/**
		 * @return {Ext.data.ArrayStore}
		 */
		getStoreExportFileFormat: function() {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, CMDBuild.core.proxy.CMProxyConstants.NAME],
				data: [
					[CMDBuild.Translation.csv, CMDBuild.core.proxy.CMProxyConstants.CSV]
				],
				sorters: [
					{ property: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @return {Ext.data.ArrayStore}
		 */
		getStoreImportFileFormat: function() {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, CMDBuild.core.proxy.CMProxyConstants.NAME],
				data: [
					[CMDBuild.Translation.csv, CMDBuild.core.proxy.CMProxyConstants.CSV]
				],
				sorters: [
					{ property: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {Object} parameters
		 */
		readFromFunctions: function(parameters) {
			CMDBuild.Ajax.request({
				url: CMDBuild.core.proxy.CMProxyUrlIndex.functions.readCards,
				params: parameters.params,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				scope: parameters.scope || this,
				failure: parameters.failure || Ext.emptyFn,
				success: parameters.success || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		}
	});

})();