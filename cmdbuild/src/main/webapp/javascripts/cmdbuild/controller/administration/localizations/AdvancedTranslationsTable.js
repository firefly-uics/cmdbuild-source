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
						delegate: this,

						title: record.get(CMDBuild.core.proxy.CMProxyConstants.NAME),
//						sectionId: record.get(CMDBuild.core.proxy.CMProxyConstants.VALUE), // TODO "id" sezione traduzioni

						bodyCls: 'cmgraypanel',
						layout: 'fit',

						items: [ // TODO: dynamic setup of AdvancedTranslationsTableGrid columns and store
							Ext.create('CMDBuild.view.administration.localizations.panels.AdvancedTranslationsTableGrid', {
								delegate: this,

								sectionId: record.get(CMDBuild.core.proxy.CMProxyConstants.VALUE), // TODO "id" sezione traduzioni

								columns: this.buildColumns(),
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
				case 'onAdvancedTableNodeExpand':
					return this.onAdvancedTableNodeExpand(param);

				case 'onAdvancedTableRowUpdateButtonClick':
					return this.onAdvancedTableRowUpdateButtonClick(param);

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		buildColumns: function() {
			var columnsArray = [
				{
					xtype: 'treecolumn',
					text: '@@ Translation object',
					dataIndex: CMDBuild.core.proxy.CMProxyConstants.OBJECT,
					width: 300,
					// locked: true, // There is a performance issue in ExtJs 4.2.0 without locked columns all is fine
					sortable: false,
					draggable: false
				},
				{
					text: '@@ defaultTranslation',
					dataIndex: CMDBuild.core.proxy.CMProxyConstants.DEFAULT_LOCALIZATION,
					width: 300,
					sortable: false,
					draggable: false,

					editor: { xtype: 'textfield' }
				}
			];

// TODO real implementation
//			CMDBuild.core.proxy.Localizations.getLanguagesToTranslate({
//				scope: this,
//				success: function(response, options, decodedResponse) {
//_debug('Attributes decodedResponse', decodedResponse);
//
//					Ext.Array.forEach(decodedResponse, function(language, index, allLanguages) {
//						columnsArray.push({ name: language.get(CMDBuild.core.proxy.CMProxyConstants.TAG), type: 'string' });
//					}, this);
//
//					return columnsArray;
//				}
//			});

			Ext.Array.forEach(CMDBuild.Config.localization.get(CMDBuild.core.proxy.CMProxyConstants.LANGUAGES_WITH_LOCALIZATIONS), function(language, i, allLanguages) { // TODO
				columnsArray.push(this.view.buildColumn(language));
			}, this);
_debug('columnsArray', columnsArray);
			return columnsArray;
		},

		/**
		 * Gatherer function for store build
		 *
		 * @param {String} sectionId
		 */
		buildStore: function(sectionId) { // TODO
			switch (sectionId) {
				case 'classes':
					return this.buildStoreClasses();

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
		 * @return {Ext.data.TreeStore} treeStore
		 */
		buildStoreClasses: function() {
_debug('CMDBuild.Config', CMDBuild.Config);
			var treeStore =  Ext.create('Ext.data.TreeStore', {
				fields: this.buildTreeStoreFields(),
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
				scope: this,
				success: function(response, options, decodedResponse) {
_debug('Classes decodedResponse', decodedResponse);
					Ext.Array.forEach(decodedResponse.classes, function(classObject, index, allClasses) {
						if (
							classObject[CMDBuild.core.proxy.CMProxyConstants.TYPE] == 'class' // Discard processes from visualization
							&& classObject[CMDBuild.core.proxy.CMProxyConstants.NAME] != 'Class' // Discard root class of all classes
						) {
							// Class main node
							var classMainNodeObject = { expandable: true, };
							classMainNodeObject[CMDBuild.core.proxy.CMProxyConstants.LEAF] = false;
							classMainNodeObject[CMDBuild.core.proxy.CMProxyConstants.OBJECT] = classObject[CMDBuild.core.proxy.CMProxyConstants.NAME];

							var classMainNode = root.appendChild(classMainNodeObject);

							// Class description property object
							var classDescriptionNodeObject = {};
							classDescriptionNodeObject[CMDBuild.core.proxy.CMProxyConstants.DEFAULT_LOCALIZATION] = classObject[CMDBuild.core.proxy.CMProxyConstants.TEXT];
							classDescriptionNodeObject[CMDBuild.core.proxy.CMProxyConstants.LEAF] = true;
							classDescriptionNodeObject[CMDBuild.core.proxy.CMProxyConstants.OBJECT] = '@@ Description';
							classDescriptionNodeObject[CMDBuild.core.proxy.CMProxyConstants.PROPERTY] = CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION;

							classMainNode.appendChild(classDescriptionNodeObject);

							// Class attributes node
							var classAttributeNodeObject = {
								expandable: true,
								parent: classMainNode
							};
							classAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.LEAF] = false;
							classAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.OBJECT] = '@@ Attributes';
							classAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.PROPERTY] = CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTES;

							var classAttributesNode = classMainNode.appendChild(classAttributeNodeObject);

							classAttributesNode.appendChild({}); // FIX: expandable property is bugged so i must build a fake node to make attributes node expandable
						}
					}, this);
				},
				callback: function(records, operation, success) {
					CMDBuild.LoadMask.get().hide();
				}
			});

			return treeStore;
		},

		/**
		 * @return {Array}
		 */
		buildTreeStoreFields: function() {
			var fieldsArray = [
				{ name: CMDBuild.core.proxy.CMProxyConstants.DEFAULT_LOCALIZATION, type: 'string'},
				{ name: CMDBuild.core.proxy.CMProxyConstants.OBJECT, type: 'string'},
				{ name: CMDBuild.core.proxy.CMProxyConstants.PARENT, type: 'auto' },
				{ name: CMDBuild.core.proxy.CMProxyConstants.PROPERTY, type: 'string' }
			];

			Ext.Array.forEach(CMDBuild.Config.localization.get(CMDBuild.core.proxy.CMProxyConstants.LANGUAGES), function(language, index, allLanguages) {
				fieldsArray.push({ name: language.get(CMDBuild.core.proxy.CMProxyConstants.TAG), type: 'string' });
			}, this);

			return fieldsArray;
		},

		/**
		 * @return {CMDBuild.view.administration.localizations.AdvancedTranslationsTablePanel}
		 */
		getView: function() {
			return this.view;
		},

		/**
		 * Gatherer function for node expand events
		 *
		 * @param {Object} inputObject
		 * 	Ex. {
		 * 			{String} sectionId
		 * 			{Ext.data.NodeInterface} node
		 * 		}
		 */
		onAdvancedTableNodeExpand: function(inputObject) { // TODO
			var node = inputObject.node;
			var sectionId = inputObject.sectionId;

			switch (sectionId) {
				case 'classes':
					return this.onAdvancedTableNodeExpandClasses(node);

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
		 * @param {Ext.data.NodeInterface} node
		 */
		onAdvancedTableNodeExpandClasses: function(node) {
_debug('onAdvancedTableNodeExpand', node);
			if (!Ext.isEmpty(node) && node.getDepth() == 1) { // I'm on first level node (class node)
				// Refresh all child node filling them with translations
				CMDBuild.LoadMask.get().show();
				node.eachChild(function(childNode) {
					if (childNode.isLeaf()) {
						var params = {};
						params[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = node.get(CMDBuild.core.proxy.CMProxyConstants.OBJECT);
						params['field'] = childNode.get(CMDBuild.core.proxy.CMProxyConstants.PROPERTY); // TODO: proxyCostants
						params['sectionId'] = 'classes'; // TODO proxy costants and get sectionId

						CMDBuild.core.proxy.Localizations.readLocalization({ // TODO probabilmente da implementare semaforo per la callback
							params: params,
							scope: this,
							success: function(response, options, decodedResponse) {
								// Class property object
								var classPropertyNodeObject = {};
								classPropertyNodeObject[CMDBuild.core.proxy.CMProxyConstants.PROPERTY] = childNode.get(CMDBuild.core.proxy.CMProxyConstants.PROPERTY);
								classPropertyNodeObject[CMDBuild.core.proxy.CMProxyConstants.DEFAULT_LOCALIZATION] = childNode.get(CMDBuild.core.proxy.CMProxyConstants.DEFAULT_LOCALIZATION);
								classPropertyNodeObject[CMDBuild.core.proxy.CMProxyConstants.LEAF] = true;
								classPropertyNodeObject[CMDBuild.core.proxy.CMProxyConstants.OBJECT] = childNode.get(CMDBuild.core.proxy.CMProxyConstants.OBJECT);
_debug('localizations decodedResponse', decodedResponse);
								if (!Ext.Object.isEmpty(decodedResponse.response))
									Ext.Object.each(decodedResponse.response, function(tag, translation, myself) {
										classPropertyNodeObject[tag] = translation;
									});

								node.replaceChild(classPropertyNodeObject, childNode);
							},
							callback: function(records, operation, success) {
								CMDBuild.LoadMask.get().hide();
							}
						});
					}
				}, this);
			} else if (!Ext.isEmpty(node) && node.getDepth() == 2) { // I'm on second level node (attributes node)
_debug('on expand second level');
				node.removeAll();
				// Refresh all child node filling them with translations
//				node.eachChild(function(childNode) {
//					if (childNode.isLeaf()) { // TODO END LINE
						var params = {};
						params[CMDBuild.core.proxy.CMProxyConstants.ACTIVE] = true;
						params[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = node.get(CMDBuild.core.proxy.CMProxyConstants.PARENT).get(CMDBuild.core.proxy.CMProxyConstants.OBJECT);

						CMDBuild.LoadMask.get().show();
						CMDBuild.core.proxy.Attributes.read({
							params: params,
							scope: this,
							success: function(response, options, decodedResponse) {
_debug('Attributes decodedResponse', decodedResponse);
								Ext.Array.forEach(decodedResponse.attributes, function(attributeObject, index, allAttributes) {
									if (attributeObject[CMDBuild.core.proxy.CMProxyConstants.NAME] != 'Notes') { // Custom CMDBuild behaviour
										var localizationParams = {};
										localizationParams[CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTE_NAME] = attributeObject[CMDBuild.core.proxy.CMProxyConstants.NAME];
										localizationParams[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = node.get(CMDBuild.core.proxy.CMProxyConstants.OBJECT);
										localizationParams['field'] = CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION; // TODO: proxyCostants
										localizationParams['sectionId'] = 'classesAttribute'; // TODO proxy costants and get sectionId

										CMDBuild.core.proxy.Localizations.readLocalization({
											params: localizationParams,
											scope: this,
											success: function(response, options, decodedResponse) {
_debug('localizations decodedResponse', decodedResponse);
												var childAttributeNodeObject = {};
												childAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.DEFAULT_LOCALIZATION] = attributeObject[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION];
												childAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.LEAF] = true;
												childAttributeNodeObject[CMDBuild.core.proxy.CMProxyConstants.OBJECT] = attributeObject[CMDBuild.core.proxy.CMProxyConstants.NAME];

												if (!Ext.Object.isEmpty(decodedResponse.response))
													Ext.Object.each(decodedResponse.response, function(tag, translation, myself) {
														childAttributeNodeObject[tag] = translation;
													});

												node.appendChild(childAttributeNodeObject);
											}
										});
									}
								}, this);
							},
							callback: function(records, operation, success) {
								CMDBuild.LoadMask.get().hide();
							}
						});
//					}
//				}, this);
			}
		},

		/**
		 * @param {Ext.data.Model} record
		 */
		onAdvancedTableRowUpdateButtonClick: function(record) {
_debug('CMDBuild.controller.administration.localizations.AdvancedTranslationsTable UPDATE');
			// TODO fare chiamata per salvataggio traduzioni a partire dai dati del record
		}
	});

})();