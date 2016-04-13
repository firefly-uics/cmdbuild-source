(function () {

	/**
	 * REST proxy
	 *
	 * FIXME: future refactor for a correct implementation
	 */
	Ext.define('CMDBuild.core.proxy.classes.Icon', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.interfaces.FormSubmit',
			'CMDBuild.core.interfaces.Rest',
			'CMDBuild.core.proxy.index.Rest'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		createImage: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				url: 'services/json/file/upload?' // TODO: use rest index
					+ CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY
					+ '=' + Ext.util.Cookies.get(CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY) // Headers not supported in form submit
			});

			CMDBuild.core.interfaces.FormSubmit.submit(parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		getFolders: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				method: 'GET',
				url: CMDBuild.core.proxy.index.Rest.fileStores + '/images/folders/'
			});

			CMDBuild.core.interfaces.Rest.request(parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		readAllIcons: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				method: 'GET',
				url: CMDBuild.core.proxy.index.Rest.icons + '/'
			});

			CMDBuild.core.interfaces.Rest.request(parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		remove: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			CMDBuild.core.interfaces.Rest.request({
				method: 'DELETE',
				url: CMDBuild.core.proxy.index.Rest.icons + '/' + parameters.urlParams['iconId'],
				scope: Ext.isEmpty(parameters.scope) ? this : parameters.scope,
				success: function (response, options, decodedResponse) {
					parameters = Ext.isEmpty(parameters) ? {} : parameters;

					Ext.apply(parameters, {
						method: 'DELETE',
						url: CMDBuild.core.proxy.index.Rest.fileStores + '/folders/'
							+ parameters.urlParams['folderId']
							+ '/files/'
							+ parameters.urlParams['imageId'] + '/'
					});

					CMDBuild.core.interfaces.Rest.request(parameters);
				}
			});
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		update: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				method: 'POST',
				url: CMDBuild.core.proxy.index.Rest.icons + '/'
			});

			CMDBuild.core.interfaces.Rest.request(parameters);
		}
	});

})();
