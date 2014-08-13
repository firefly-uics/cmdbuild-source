(function() {

	Ext.define('CMDBuild.core.proxy.widgets.CMProxyWidgetGrid', {
		statics: {

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
				// To clear server session data
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
		}
	});

})();