(function() {

	Ext.define('CMDBuild.core.proxy.Configuration', {

		requires: [
			'CMDBuild.core.proxy.CMProxy',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * @return {Ext.data.Store} classes and processes store
		 */
		getStartingClassStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: true,
				model: 'CMDBuild.model.CMSetupModels.startingClass',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.classes.readAll,
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
					property: CMDBuild.core.constants.Proxy.DESCRIPTION,
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
			parameters.url = CMDBuild.core.proxy.Index.configuration.getConfigurations;
			parameters.params = { names: Ext.JSON.encode(['bim', 'cmdbuild', 'dms', 'gis', 'graph', 'workflow']) };

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
			parameters.url = CMDBuild.core.proxy.Index.configuration.getConfiguration;
			parameters.params = { name: name };
			parameters.loadMask = Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false;

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
			parameters.url = CMDBuild.core.proxy.Index.configuration.saveConfiguration;
			parameters.params.name = name;
			parameters.loadMask = Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false;

			CMDBuild.ServiceProxy.core.doRequest(parameters);
		}
	});

})();