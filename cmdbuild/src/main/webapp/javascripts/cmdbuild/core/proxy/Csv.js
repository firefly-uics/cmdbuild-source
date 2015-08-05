(function() {

	Ext.define('CMDBuild.core.proxy.Csv', {

		requires: [
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * @return {Ext.data.ArrayStore}
		 */
		getImportModeStore: function() {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.proxy.Constants.DESCRIPTION, CMDBuild.core.proxy.Constants.VALUE],
				data: [
					[CMDBuild.Translation.add, 'add'],
					[CMDBuild.Translation.replace , 'replace']
				]
			});
		},

		/**
		 * @param {Object} parameters
		 */
		getRecords: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'GET',
				url: CMDBuild.core.proxy.Index.csv.getCsvRecords,
				scope: parameters.scope || this,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: function(records, operation, success) { // Clears server session data
					CMDBuild.Ajax.request({
						method: 'GET',
						url: CMDBuild.core.proxy.Index.csv.clearSession
					});

					CMDBuild.LoadMask.get().hide();
				}
			});
		},

		/**
		 * @return {Ext.data.ArrayStore}
		 */
		getSeparatorStore: function() {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.proxy.Constants.VALUE],
				data: [
					[';'],
					[','],
					['|']
				]
			});
		},

		/**
		 * @param {Object} parameters
		 */
		upload: function(parameters) {
			parameters.form.submit({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.csv.uploadCsv,
				scope: parameters.scope || this,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		}
	});

})();