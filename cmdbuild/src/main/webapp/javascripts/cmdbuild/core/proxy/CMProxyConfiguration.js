(function() {

	Ext.define('CMDBuild.core.proxy.CMProxyConfiguration', {
		alternateClassName: 'CMDBuild.ServiceProxy.configuration', // Legacy class name

		requires: [
			'CMDBuild.core.proxy.CMProxy',
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyUrlIndex'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		getLanguage: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.getLanguage,
				scope: parameters.scope || this,
				success: parameters.success || Ext.emptyFn,
				failure: parameters.failure || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		},

		/**
		 * @return {Ext.data.Store} classes and processes store
		 */
		getStartingClassStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: true,
				model: 'CMDBuild.model.CMSetupModels.startingClass',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.CMProxyUrlIndex.classes.read,
					reader: {
						type: 'json',
						root: 'classes'
					},
					extraParams: {
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				sorters: [{
					property: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
					direction: 'ASC'
				}]
			});
		},

		/**
		 * @param {Object} parameters
		 * @param {String} name
		 */
		read: function(parameters, name) {
			this.readConf(parameters, name);
		},

		/**
		 * @param {Object} parameters
		 */
		readAll: function(parameters) {
			parameters.method = 'GET';
			parameters.url = CMDBuild.core.proxy.CMProxyUrlIndex.configuration.getConfigurations;
			parameters.params = { names: Ext.JSON.encode(['cmdbuild', 'workflow', 'gis', 'bim', 'graph']) };

			CMDBuild.ServiceProxy.core.doRequest(parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		readBimConfiguration: function(parameters) {
			this.readConf(parameters, 'bim');
		},

		/**
		 * @param {Object} parameters
		 * @param {String} name
		 *
		 * @private
		 */
		readConf: function(parameters, name) {
			parameters.method = 'GET';
			parameters.url = CMDBuild.core.proxy.CMProxyUrlIndex.configuration.getConfiguration;
			parameters.params = { name: name };

			CMDBuild.core.proxy.CMProxy.doRequest(parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		readGisConfiguration: function(parameters) {
			this.readConf(parameters, 'gis');
		},

		/**
		 * @param {Object} parameters
		 */
		readMainConfiguration: function(parameters) {
			this.readConf(parameters, 'cmdbuild');
		},

		/**
		 * @param {Object} parameters
		 */
		readWFConfiguration: function(parameters) {
			this.readConf(parameters, 'workflow');
		},

		/**
		 * @param {Object} parameters
		 * @param {String} name
		 */
		save: function(parameters, name) {
			parameters.method = 'POST';
			parameters.url = CMDBuild.core.proxy.CMProxyUrlIndex.configuration.saveConfiguration;
			parameters.params.name = name;

			CMDBuild.ServiceProxy.core.doRequest(parameters);
		}
	});

})();