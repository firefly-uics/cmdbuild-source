(function() {

	Ext.define('CMDBuild.core.proxy.Localizations', {

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyUrlIndex',
			'CMDBuild.core.proxy.Configuration',
			'CMDBuild.model.Localizations'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		createLocalization: function(parameters) { // TODO: future implementation of server-side parameter for sectionId
			var url = undefined;

			switch (parameters.params.sectionId) {
				case 'classes': {
					url = CMDBuild.core.proxy.CMProxyUrlIndex.localizations.classCreate;
				} break;

				case 'classesAttributes': {
					url = CMDBuild.core.proxy.CMProxyUrlIndex.localizations.classAttributeCreate;
				} break;

				case 'domains': {

				} break;

				case 'lookups': {

				} break;

				case 'menus': {

				} break;

				case 'reports': {

				} break;
			}

			CMDBuild.Ajax.request({
				method: 'POST',
				url: url,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: parameters.loadMask || false,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @return {Ext.data.Store}
		 */
		getCsvSeparatorStore: function() {
			return Ext.create('Ext.data.Store', {
				fields: [CMDBuild.core.proxy.CMProxyConstants.VALUE],
				data: [
					{ value: ';' },
					{ value: ',' },
					{ value: '|' }
				]
			});
		},

		/**
		 * @return {Ext.data.Store}
		 */
		getFileFormatStore: function() {
			return Ext.create('Ext.data.Store', {
				fields: [CMDBuild.core.proxy.CMProxyConstants.NAME, CMDBuild.core.proxy.CMProxyConstants.VALUE],
				data: [
					{ name: '@@ CSV', value: 'csv' }
				]
			});
		},

		/**
		 * @param {Object} parameters
		 */
		getLanguages: function(parameters) {
			CMDBuild.Ajax.request({
				url: CMDBuild.core.proxy.CMProxyUrlIndex.utils.listAvailableTranslations,
				scope: parameters.scope || this,
				loadMask: parameters.loadMask || false,
				success: parameters.success || Ext.emptyFn,
				failure: parameters.failure || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		},

		/**
		 * @return {Ext.data.Store}
		 */
		getLanguagesStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: true,
				model: 'CMDBuild.model.Localizations.translation',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.CMProxyUrlIndex.utils.listAvailableTranslations,
					reader: {
						type: 'json',
						root: 'translations'
					}
				},
				sorters: [
					{ property: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {Object} parameters
		 */
		readLocalization: function(parameters) { // TODO: future implementation of server-side parameter for sectionId
			var url = undefined;

			switch (parameters.params.sectionId) {
				case 'classes': {
					url = CMDBuild.core.proxy.CMProxyUrlIndex.localizations.classRead;
				} break;

				case 'classesAttributes': {
					url = CMDBuild.core.proxy.CMProxyUrlIndex.localizations.classAttributeRead;
				} break;

				case 'domains': {

				} break;

				case 'lookups': {

				} break;

				case 'menus': {

				} break;

				case 'reports': {

				} break;

				default: {
_debug('Error', parameters);
				}
			}

			CMDBuild.Ajax.request({
				method: 'POST',
				url: url,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: parameters.loadMask || false,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		updateLocalization: function(parameters) { // TODO: future implementation of server-side parameter for sectionId
			var url = undefined;

			switch (parameters.params.sectionId) {
				case 'classes': {
					url = CMDBuild.core.proxy.CMProxyUrlIndex.localizations.classUpdate;
				} break;

				case 'classesAttributes': {
					url = CMDBuild.core.proxy.CMProxyUrlIndex.localizations.classAttributeUpdate;
				} break;

				case 'domains': {

				} break;

				case 'lookups': {

				} break;

				case 'menus': {

				} break;

				case 'reports': {

				} break;
			}

			CMDBuild.Ajax.request({
				method: 'POST',
				url: url,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: parameters.loadMask || false,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},
	});

})();
