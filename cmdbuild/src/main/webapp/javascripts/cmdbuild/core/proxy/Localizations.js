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
				sorters: {
					property: CMDBuild.core.proxy.CMProxyConstants.NAME,
					direction: 'ASC'
				}
			});
		},

		/**
		 * @param {Object} parameters
		 */
		getLanguagesToTranslate: function(parameters) { // TODO forse non serve
			CMDBuild.Ajax.request({
				url: '#############',
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: parameters.loadMask || false,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
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

//		/**
//		 * @param {String} sectionName  // TODO
//		 * @param {Array} languages // TODO
//		 *
//		 * @return {Ext.data.TreeStore}
//		 */
//		getSectionTranslationsStore: function(sectionName, languages) {
////			return Ext.create('Ext.data.Store', {
////				fields: ['@@ key', '@@ defaultTranslation', '@@ langTag1', '@@ langTag2', '@@ langTag3'],
////				data: [
////					{ '@@ key': 'asdasd1', '@@ defaultTranslation': 'translation1', '@@ langTag1': 'translationTag1 1', '@@ langTag2': 'translationTag2 1', '@@ langTag3': 'translationTag3 1' },
////					{ '@@ key': 'asdasd2', '@@ defaultTranslation': 'translation2', '@@ langTag1': 'translationTag1 2', '@@ langTag2': 'translationTag2 3', '@@ langTag3': 'translationTag3 2' },
////					{ '@@ key': 'asdasd3', '@@ defaultTranslation': 'translation3', '@@ langTag1': 'translationTag1 3', '@@ langTag2': 'translationTag2 4', '@@ langTag3': 'translationTag3 3' },
////					{ '@@ key': 'asdasd4', '@@ defaultTranslation': 'translation4', '@@ langTag1': 'translationTag1 4', '@@ langTag2': 'translationTag2 5', '@@ langTag3': 'translationTag3 4' },
////					{ '@@ key': 'asdasd5', '@@ defaultTranslation': 'translation5', '@@ langTag1': 'translationTag1 5', '@@ langTag2': 'translationTag2 6', '@@ langTag3': 'translationTag3 5' },
////					{ '@@ key': 'asdasd6', '@@ defaultTranslation': 'translation6', '@@ langTag1': 'translationTag1 6', '@@ langTag2': 'translationTag2 7', '@@ langTag3': 'translationTag3 6' },
////					{ '@@ key': 'asdasd7', '@@ defaultTranslation': 'translation7', '@@ langTag1': 'translationTag1 7', '@@ langTag2': 'translationTag2 8', '@@ langTag3': 'translationTag3 7' },
////				]
////			});
//
//			Ext.define('Task', {
//				extend: 'Ext.data.Model',
//
//				fields: [
//					{ name: 'expanded', type: 'boolean', defaultValue: true, persist: false }, // To expand all tree
//					{ name: 'task', type: 'string'},
//					{ name: 'user', type: 'string'},
//					{ name: 'duration', type: 'string'},
//				]
//			});
//
//			return Ext.create('Ext.data.TreeStore', {
//				autoLoad: true,
//				model: 'Task',
//				proxy: {
//					type: 'ajax',
//					url: 'http://localhost:8080/cmdbuild/treegrid.json'
//				},
//				folderSort: true
//			});
//		},

		/**
		 * @return {Ext.data.Store}
		 */
		getSectionsStore: function() { // TODO non è necessario fare uno store ma una chiamata normale
			return Ext.create('Ext.data.Store', {
				fields: [CMDBuild.core.proxy.CMProxyConstants.NAME, CMDBuild.core.proxy.CMProxyConstants.VALUE],
				data: [
					{ name: '@@ Classes', value: 'classes' },
					{ name: '@@ Domains', value: 'domains' },
					{ name: '@@ Lookups', value: 'lookups' },
					{ name: '@@ Menus', value: 'menus' },
					{ name: '@@ Reports', value: 'reports' }
				]
			});
		}
	});

})();