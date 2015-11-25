(function() {

	Ext.define('CMDBuild.core.proxy.Csv', {

		requires: [
			'CMDBuild.core.interfaces.Ajax',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.LoadMask',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * @return {Ext.data.ArrayStore}
		 */
		getImportModeStore: function() {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.DESCRIPTION, CMDBuild.core.constants.Proxy.VALUE],
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
			CMDBuild.core.interfaces.Ajax.request({
				method: 'GET',
				url: CMDBuild.core.proxy.Index.csv.getCsvRecords,
				scope: parameters.scope || this,
				failure: parameters.failure || Ext.emptyFn,
				success: parameters.success || Ext.emptyFn,
				callback: function(options, success, response) { // Clears server session data
					CMDBuild.core.interfaces.Ajax.request({
						method: 'GET',
						url: CMDBuild.core.proxy.Index.csv.clearSession
					});

					CMDBuild.core.LoadMask.hide();
				}
			});
		},

		/**
		 * @return {Ext.data.ArrayStore}
		 */
		getSeparatorStore: function() {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.VALUE],
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
		decode: function(parameters) {
			parameters.form.submit({
				form: parameters.form,
				isUpload: true,
				method: 'POST',
				url: CMDBuild.core.proxy.Index.csv.readCsv,
				scope: parameters.scope || this,
				failure: parameters.failure || Ext.emptyFn,
				success: parameters.success || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
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
				failure: parameters.failure || Ext.emptyFn,
				success: parameters.success || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		}
	});

})();