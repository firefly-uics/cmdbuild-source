(function() {

	Ext.define('CMDBuild.controller.administration.localizations.advancedTable.SectionClasses', {
		extend: 'CMDBuild.controller.administration.localizations.advancedTable.SectionBase',

		requires: [
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.core.proxy.localizations.Localizations',
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.advancedTable.AdvancedTable}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onAdvancedTableClassesShow',
			'onAdvancedTableRowUpdateButtonClick'
		],

		/**
		 * @cfg {Array}
		 */
		entityAttributeFilter: ['Notes'],

		/**
		 * @cfg {String}
		 */
		sectionId: CMDBuild.core.proxy.Constants.CLASS,

		/**
		 * @property {CMDBuild.view.administration.localizations.common.AdvancedTableGrid}
		 */
		grid: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.localizations.advancedTable.SectionClassesPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.administration.localizations.advancedTable.AdvancedTable} configObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.localizations.advancedTable.SectionClassesPanel', {
				delegate: this
			});

			// Shorthand
			this.grid = this.view.grid;

			this.cmfg('onAdvancedTableTabCreation', this.view); // Add panel to parent tab panel
		},

		/**
		 * Fill grid store with classes data
		 */
		onAdvancedTableClassesShow: function() {
			var root = this.grid.getStore().getRootNode();
			root.removeAll();

			var params = {};
			params[CMDBuild.core.proxy.Constants.TYPE] = this.getSectionId();

			CMDBuild.core.proxy.localizations.Localizations.readStructure({
				params: params,
				scope: this,
				success: function(response, options, decodedResponse) {
					if (Ext.isArray(decodedResponse.response)) {
						// TODO: class order
						Ext.Array.forEach(decodedResponse.response, function(classObject, i, allClasses) {
							// Entity main node
							var entityMainNodeObject = { expandable: true };
							entityMainNodeObject[CMDBuild.core.proxy.Constants.LEAF] = false;
							entityMainNodeObject[CMDBuild.core.proxy.Constants.PARENT] = root;
							entityMainNodeObject[CMDBuild.core.proxy.Constants.TEXT] = classObject[CMDBuild.core.proxy.Constants.NAME];

							var classMainNode = root.appendChild(entityMainNodeObject);

							// Entity translatable fields
							if (Ext.isArray(classObject[CMDBuild.core.proxy.Constants.FIELDS])) {
								Ext.Array.forEach(classObject[CMDBuild.core.proxy.Constants.FIELDS], function(fieldObject, i, allFields) {
									var entityFieldNodeObject = {};
									entityFieldNodeObject[CMDBuild.core.proxy.Constants.DEFAULT] = fieldObject[CMDBuild.core.proxy.Constants.VALUE];
									entityFieldNodeObject[CMDBuild.core.proxy.Constants.FIELD] = fieldObject[CMDBuild.core.proxy.Constants.NAME];
									entityFieldNodeObject[CMDBuild.core.proxy.Constants.IDENTIFIER] = classObject[CMDBuild.core.proxy.Constants.NAME];
									entityFieldNodeObject[CMDBuild.core.proxy.Constants.LEAF] = true;
									entityFieldNodeObject[CMDBuild.core.proxy.Constants.PARENT] = classMainNode;
									entityFieldNodeObject[CMDBuild.core.proxy.Constants.TEXT] = fieldObject[CMDBuild.core.proxy.Constants.NAME];
									entityFieldNodeObject[CMDBuild.core.proxy.Constants.TYPE] = CMDBuild.core.proxy.Constants.CLASS;

									// Fill with translations translations
									if (Ext.isObject(fieldObject[CMDBuild.core.proxy.Constants.TRANSLATIONS])) {
										Ext.Object.each(fieldObject[CMDBuild.core.proxy.Constants.TRANSLATIONS], function(tag, translation, myself) {
											entityFieldNodeObject[tag] = translation;
										});
									}

									classMainNode.appendChild(entityFieldNodeObject);
								}, this);
							} else {
								classMainNode.appendChild({}); // FIX: expandable property is bugged so i must build a fake node to make attributes node expandable
							}

							// Entity attribute nodes
							if (Ext.isArray(classObject[CMDBuild.core.proxy.Constants.ATTRIBUTES])) {
								var entityAttributesNodeObject = { expandable: true };
								entityAttributesNodeObject[CMDBuild.core.proxy.Constants.LEAF] = false;
								entityAttributesNodeObject[CMDBuild.core.proxy.Constants.PARENT] = classMainNode;
								entityAttributesNodeObject[CMDBuild.core.proxy.Constants.TEXT] = CMDBuild.Translation.attributes;

								var classAttributesNode = classMainNode.appendChild(entityAttributesNodeObject);

								// Entity attributes
								// TODO: sort attributes with CMDBuild sort order
								Ext.Array.forEach(classObject[CMDBuild.core.proxy.Constants.ATTRIBUTES], function(attribute, i, allAttributes) {
									if (!Ext.Array.contains(this.entityAttributeFilter, attribute[CMDBuild.core.proxy.Constants.NAME])) { // Custom CMDBuild behaviour
										var attributeDescription = attribute[CMDBuild.core.proxy.Constants.FIELDS][0]; // In future could be more than only description to translate

										var attributeNodeObject = {};
										attributeNodeObject[CMDBuild.core.proxy.Constants.DEFAULT] = attributeDescription[CMDBuild.core.proxy.Constants.VALUE];
										attributeNodeObject[CMDBuild.core.proxy.Constants.FIELD] = attributeDescription[CMDBuild.core.proxy.Constants.NAME];
										attributeNodeObject[CMDBuild.core.proxy.Constants.IDENTIFIER] = attribute[CMDBuild.core.proxy.Constants.NAME];
										attributeNodeObject[CMDBuild.core.proxy.Constants.LEAF] = true;
										attributeNodeObject[CMDBuild.core.proxy.Constants.OWNER] = classObject[CMDBuild.core.proxy.Constants.NAME];
										attributeNodeObject[CMDBuild.core.proxy.Constants.PARENT] = classAttributesNode;
										attributeNodeObject[CMDBuild.core.proxy.Constants.TEXT] = attribute[CMDBuild.core.proxy.Constants.NAME];
										attributeNodeObject[CMDBuild.core.proxy.Constants.TYPE] = CMDBuild.core.proxy.Constants.ATTRIBUTE_CLASS;

										// Fill with translations translations
										if (Ext.isObject(attributeDescription[CMDBuild.core.proxy.Constants.TRANSLATIONS])) {
											Ext.Object.each(attributeDescription[CMDBuild.core.proxy.Constants.TRANSLATIONS], function(tag, translation, myself) {
												attributeNodeObject[tag] = translation;
											});
										}

										classAttributesNode.appendChild(attributeNodeObject);
									}
								}, this);
							}
						}, this);
					} else {
						_error('wrong readStructure with type "' + this.getSectionId() + '" response format ', this);
					}
				}
			});
		},

		/**
		 * @param {CMDBuild.model.localizations.advancedTable.TreeStore} node
		 */
		onAdvancedTableRowUpdateButtonClick: function(node) {
			if (!Ext.isEmpty(node)) {
				var params = {};
				params[CMDBuild.core.proxy.Constants.TYPE] = node.get(CMDBuild.core.proxy.Constants.TYPE);
				params[CMDBuild.core.proxy.Constants.IDENTIFIER] = node.get(CMDBuild.core.proxy.Constants.IDENTIFIER);
				params[CMDBuild.core.proxy.Constants.FIELD] = node.get(CMDBuild.core.proxy.Constants.FIELD);
				params[CMDBuild.core.proxy.Constants.TRANSLATIONS] = Ext.encode(node.getTranslations());

				if (!Ext.isEmpty(node.get(CMDBuild.core.proxy.Constants.OWNER)))
					params[CMDBuild.core.proxy.Constants.OWNER] = node.get(CMDBuild.core.proxy.Constants.OWNER);

				CMDBuild.core.proxy.localizations.Localizations.update({
					params: params,
					success: function(response, options, decodedResponse) {
						CMDBuild.core.Message.success();
					}
				});
			} else {
				_error('empty node on update action', this);
			}
		}
	});

})();