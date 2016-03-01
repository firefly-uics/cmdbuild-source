(function() {

	Ext.define('CMDBuild.core.proxy.workflow.Xpdl', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.interfaces.FormSubmit',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		download: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				buildRuntimeForm: true,
				url: CMDBuild.core.proxy.Index.workflow.xpdl.download
			});

			CMDBuild.core.interfaces.FormSubmit.submit(parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		downloadTemplate: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				buildRuntimeForm: true,
				url: CMDBuild.core.proxy.Index.workflow.xpdl.downloadTemplate
			});

			CMDBuild.core.interfaces.FormSubmit.submit(parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		readVersions: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.workflow.xpdl.versions });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.XPDL, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		upload: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.workflow.xpdl.upload });

			CMDBuild.core.interfaces.FormSubmit.submit(parameters);
		}
	});

})();
