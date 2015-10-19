(function() {

	Ext.require('CMDBuild.core.constants.Proxy');

	/**
	 * TODO: waiting for refactor (rename)
	 */
	Ext.define('CMDBuild.model.configuration.alfresco.Form', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.DELAY, type: 'int', defaultValue: 1000, useNull: true },
			{ name: CMDBuild.core.constants.Proxy.ENABLED, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.constants.Proxy.FILE_SERVER_PORT, type: 'int', defaultValue: 1121, useNull: true },
			{ name: CMDBuild.core.constants.Proxy.FILE_SERVER_TYPE, type: 'string', defaultValue: 'AlfrescoFTP' },
			{ name: CMDBuild.core.constants.Proxy.FILE_SERVER_URL, type: 'string', defaultValue: 'localhost' },
			{ name: CMDBuild.core.constants.Proxy.LOOKUP_CATEGORY, type: 'string', defaultValue: 'AlfrescoCategory' },
			{ name: CMDBuild.core.constants.Proxy.PASSWORD, type: 'string', defaultValue: 'admin' },
			{ name: CMDBuild.core.constants.Proxy.REPOSITORY_APPLICATION, type: 'string', defaultValue: 'cm:cmdbuild' },
			{ name: CMDBuild.core.constants.Proxy.REPOSITORY_FILE_SERVER_PATH, type: 'string', defaultValue: '/Alfresco/User Homes/cmdbuild' },
			{ name: CMDBuild.core.constants.Proxy.REPOSITORY_WEB_SERVICE_PATH, type: 'string', defaultValue: '/app:company_home/app:user_homes/' },
			{ name: CMDBuild.core.constants.Proxy.SERVER_URL, type: 'string', defaultValue: 'http://localhost:10080/alfresco/api' },
			{ name: CMDBuild.core.constants.Proxy.USER, type: 'string', defaultValue: 'admin' },
		],

		statics: {
			/**
			 * Static function to convert from legacy object to model's one
			 *
			 * @param {Object} data
			 *
			 * @returns {Object} data
			 */
			convertFromLegacy: function(data) {
				data = data || {};
				data[CMDBuild.core.constants.Proxy.FILE_SERVER_PORT] = data['fileserver.port'];
				data[CMDBuild.core.constants.Proxy.FILE_SERVER_TYPE] = data['fileserver.type'];
				data[CMDBuild.core.constants.Proxy.FILE_SERVER_URL] = data['fileserver.url'];
				data[CMDBuild.core.constants.Proxy.LOOKUP_CATEGORY] = data['category.lookup'];
				data[CMDBuild.core.constants.Proxy.PASSWORD] = data['credential.password'];
				data[CMDBuild.core.constants.Proxy.REPOSITORY_APPLICATION] = data['repository.app'];
				data[CMDBuild.core.constants.Proxy.REPOSITORY_FILE_SERVER_PATH] = data['repository.fspath'];
				data[CMDBuild.core.constants.Proxy.REPOSITORY_WEB_SERVICE_PATH] = data['repository.wspath'];
				data[CMDBuild.core.constants.Proxy.SERVER_URL] = data['server.url'];
				data[CMDBuild.core.constants.Proxy.USER] = data['credential.user'];

				return data;
			},

			/**
			 * Static function to convert from model's object to legacy one
			 *
			 * @returns {Object}
			 */
			convertToLegacy: function(data) {
				return {
					'category.lookup': data[CMDBuild.core.constants.Proxy.LOOKUP_CATEGORY],
					'credential.password': data[CMDBuild.core.constants.Proxy.PASSWORD],
					'credential.user': data[CMDBuild.core.constants.Proxy.USER],
					'fileserver.port': data[CMDBuild.core.constants.Proxy.FILE_SERVER_PORT],
					'fileserver.type': data[CMDBuild.core.constants.Proxy.FILE_SERVER_TYPE],
					'fileserver.url': data[CMDBuild.core.constants.Proxy.FILE_SERVER_URL],
					'repository.app': data[CMDBuild.core.constants.Proxy.REPOSITORY_APPLICATION],
					'repository.fspath': data[CMDBuild.core.constants.Proxy.REPOSITORY_FILE_SERVER_PATH],
					'repository.wspath': data[CMDBuild.core.constants.Proxy.REPOSITORY_WEB_SERVICE_PATH],
					'server.url': data[CMDBuild.core.constants.Proxy.SERVER_URL],
					delay: data[CMDBuild.core.constants.Proxy.DELAY],
					enabled: data[CMDBuild.core.constants.Proxy.ENABLED]
				};
			}
		}
	});

})();