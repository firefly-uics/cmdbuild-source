(function() {

	Ext.define('CMDBuild.core.proxy.reports.Jasper', {

		requires: [
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.reports.Grid'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		analize: function(parameters) {
			if (!Ext.isEmpty(parameters.form)) {
				parameters.form.submit({
					method: 'POST',
					url: CMDBuild.core.proxy.Index.reports.jasper.analyze,
					params: parameters.params,
					scope: parameters.scope || this,
					failure: parameters.failure || Ext.emptyFn(),
					success: parameters.success || Ext.emptyFn()
				});
			} else {
				_error('analizeReport form parameter not defined', this);
			}
		},

		/**
		 * @param {Object} parameters
		 */
		create: function(parameters) {
			CMDBuild.Ajax.request({
				url: CMDBuild.core.proxy.Index.reports.jasper.create,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @return {Ext.data.Store}
		 */
		getStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: false,
				model: 'CMDBuild.model.reports.Grid',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.reports.jasper.getReportsByType,
					reader: {
						type: 'json',
						root: 'rows',
						totalProperty: 'results'
					},
					extraParams: {
						type: CMDBuild.core.proxy.Constants.CUSTOM
					}
				},
				sorters: [
					{ property: CMDBuild.core.proxy.Constants.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {Object} parameters
		 */
		import: function(parameters) {
			if (!Ext.isEmpty(parameters.form)) {
				parameters.form.submit({
					method: 'POST',
					url: CMDBuild.core.proxy.Index.reports.jasper.import,
					params: parameters.params,
					scope: parameters.scope || this,
					failure: parameters.failure || Ext.emptyFn(),
					success: parameters.success || Ext.emptyFn()
				});
			} else {
				_error('importReport form parameter not defined', this);
			}
		},

		/**
		 * @param {Object} parameters
		 */
		remove: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.reports.jasper.remove,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @param {Object} parameters
		 */
		resetSession: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.reports.jasper.resetSession,
				scope: parameters.scope || this,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @param {Object} parameters
		 */
		save: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.reports.jasper.save,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		}
	});

})();