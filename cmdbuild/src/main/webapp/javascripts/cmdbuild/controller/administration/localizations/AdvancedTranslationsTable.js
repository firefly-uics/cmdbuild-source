(function() {

	Ext.define('CMDBuild.controller.administration.localizations.AdvancedTranslationsTable', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		requires: [
			'CMDBuild.core.proxy.Attributes',
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Classes',
			'CMDBuild.core.proxy.Localizations'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.Main}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.localizations.AdvancedTranslationsTablePanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.administration.localizations.Main} configObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configObject) {
			Ext.apply(this, configObject); // Apply config

			this.view = Ext.create('CMDBuild.view.administration.localizations.AdvancedTranslationsTablePanel', {
				delegate: this
			});

			// Build tabs
			CMDBuild.core.proxy.Localizations.getSectionsStore().each(function(record, id) { // TODO implementare la funzionalità con la creazione del pannello dinamica sull'onSuccess
				this.view.add(
					Ext.create('Ext.panel.Panel', {
						title: record.get(CMDBuild.core.proxy.CMProxyConstants.NAME),
						translationValue: record.get(CMDBuild.core.proxy.CMProxyConstants.VALUE), // TODO "id" sezione traduzioni
						bodyCls: 'cmgraypanel',

						layout: 'fit',

						items: [ // TODO: dynamic setup of AdvancedTranslationsTableGrid columns and store
							Ext.create('CMDBuild.view.administration.localizations.panels.AdvancedTranslationsTableGrid', {
								store: this.buildStore(record.get(CMDBuild.core.proxy.CMProxyConstants.VALUE))
							})
						]
					})
				);
			}, this);

			this.view.setActiveTab(0);
		},

		/**
		 * Gatherer function to catch events
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onAdvancedTableAbortButtonClick':
					return this.onAdvancedTableAbortButtonClick();

				case 'onAdvancedTableSaveButtonClick':
					return this.onAdvancedTableSaveButtonClick();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * @return {Array}
		 */
		buildStoreFields: function() {
			var fieldsArray = [
				{ name: 'expanded', type: 'boolean', defaultValue: true, persist: false }, // To expand all tree
				{ name: CMDBuild.core.proxy.CMProxyConstants.NAME, type: 'string'},
				{ name: CMDBuild.core.proxy.CMProxyConstants.DEFAULT, type: 'string'},
			];

			Ext.Array.forEach(CMDBuild.Config.localization.get(CMDBuild.core.proxy.CMProxyConstants.LANGUAGES), function(language, index, allLanguages) {
				fieldsArray.push({ name: language.get(CMDBuild.core.proxy.CMProxyConstants.TAG), type: 'string' });
			}, this);

			return fieldsArray;
		},

		/**
		 * @param {String} sectionId
		 *
		 * @return {Ext.data.TreeStore}
		 */
		buildStore: function(sectionId) { // TODO rifinire isolando un po' le cose ed annidando tutto (buildClassStore(), build....)
			switch (sectionId) {
				case 'classes': {
_debug('CMDBuild.Config', CMDBuild.Config);
					var treeStore =  Ext.create('Ext.data.TreeStore', {
						fields: this.buildStoreFields(),
						folderSort: true,
						root: {
							text: 'ROOT',
							expanded: true,
							children: []
						}
					});
					var root = treeStore.getRootNode();

					// GetAllClasses data to get default translations
					CMDBuild.LoadMask.get().show();
					CMDBuild.core.proxy.Classes.read({
						params: {
							active: true
						},
						success: function(response, options, decodedResponse) {
_debug('Classes decodedResponse', decodedResponse);
							Ext.Array.forEach(decodedResponse.classes, function(classObject, index, allClasses) {
								if (
									classObject[CMDBuild.core.proxy.CMProxyConstants.TYPE] == 'class' // Discard processes from visualization
									&& classObject[CMDBuild.core.proxy.CMProxyConstants.NAME] != 'Class' // Discard root class of all classes
								) {
									var childClassObject = {};
									childClassObject[CMDBuild.core.proxy.CMProxyConstants.NAME] = classObject[CMDBuild.core.proxy.CMProxyConstants.NAME];
									childClassObject[CMDBuild.core.proxy.CMProxyConstants.DEFAULT] = classObject[CMDBuild.core.proxy.CMProxyConstants.TEXT];
									childClassObject[CMDBuild.core.proxy.CMProxyConstants.LEAF] = false;

									var classNode = root.appendChild(childClassObject);

									var params = {};
									params[CMDBuild.core.proxy.CMProxyConstants.ACTIVE] = true;
									params[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = classObject[CMDBuild.core.proxy.CMProxyConstants.NAME];

									CMDBuild.core.proxy.Attributes.read({
										params: params,
										success: function(response, options, decodedResponse) {
_debug('Attributes decodedResponse', decodedResponse);
											Ext.Array.forEach(decodedResponse.attributes, function(attributeObject, index, allAttributes) {
												var childAttributeObject = {};
												childAttributeObject[CMDBuild.core.proxy.CMProxyConstants.NAME] = attributeObject[CMDBuild.core.proxy.CMProxyConstants.NAME];
												childAttributeObject[CMDBuild.core.proxy.CMProxyConstants.DEFAULT] = attributeObject[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION];
												childAttributeObject[CMDBuild.core.proxy.CMProxyConstants.LEAF] = true;

												classNode.appendChild(childAttributeObject);
											}, this);
										},
										callback: function(records, operation, success) {
											CMDBuild.LoadMask.get().hide();
										}
									});
								}
							}, this);
						}
					});
_debug('treeStore', treeStore);
_debug('root', root);
					return treeStore;
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
					// TODO error message pop-up
				}
			}
		},

		/**
		 * @return {CMDBuild.view.administration.localizations.AdvancedTranslationsTablePanel}
		 */
		getView: function() {
			return this.view;
		},

		onAdvancedTableAbortButtonClick: function() {
_debug('CMDBuild.controller.administration.localizations.AdvancedTranslationsTable ABORT');
		},

		onAdvancedTableSaveButtonClick: function() {
_debug('CMDBuild.controller.administration.localizations.AdvancedTranslationsTable SAVE');
		}
	});

})();