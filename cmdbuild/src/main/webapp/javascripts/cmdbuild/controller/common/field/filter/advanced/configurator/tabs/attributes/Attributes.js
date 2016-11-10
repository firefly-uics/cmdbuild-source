(function () {

	Ext.define('CMDBuild.controller.common.field.filter.advanced.configurator.tabs.attributes.Attributes', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Utils',
			'CMDBuild.proxy.common.field.filter.advanced.configurator.tabs.Attributes'
		],

		/**
		 * @cfg {CMDBuild.controller.common.field.filter.advanced.configurator.Configurator}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'fieldFilterAdvancedConfiguratorConfigurationAttributesControllersConditionGroupRemove',
			'fieldFilterAdvancedConfiguratorConfigurationAttributesControllersConditionGroupReset = fieldFilterAdvancedConfiguratorReset',
			'fieldFilterAdvancedConfiguratorConfigurationAttributesEntryTypeSelect',
			'fieldFilterAdvancedConfiguratorConfigurationAttributesValueGet',
			'fieldFilterAdvancedConfiguratorConfigurationAttributesValueSet',
			'onFieldFilterAdvancedConfiguratorConfigurationAttributesAddButtonSelect'
		],

		/**
		 * Condition groups controllers
		 *
		 * @property {Object}
		 *
		 * @private
		 */
		controllersConditionGroup: {},

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.configurator.tabs.attributes.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {Array}
		 *
		 * @private
		 */
		selectedEntityAttributes: {},

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.configurator.tabs.attributes.AttributesView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.field.filter.advanced.configurator.Configurator} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.common.field.filter.advanced.configurator.tabs.attributes.AttributesView', { delegate: this });

			// Shorthands
			this.form = this.view.form;
		},

		// AttributeButton manage methods
			/**
			 * @returns {Void}
			 *
			 * @private
			 */
			attributeButtonBuild: function () {
				var buttonGroups = [],
					groupedAttributes = CMDBuild.core.Utils.groupAttributesObjects(this.selectedEntityAttributesGet());

				this.attributeButtonReset();

				Ext.Object.each(groupedAttributes, function (group, attributes, myself) {
					var groupItems = [];

					CMDBuild.core.Utils.objectArraySort(attributes, CMDBuild.core.constants.Proxy.DESCRIPTION); // Sort groups items

					Ext.Array.forEach(attributes, function (attribute, i, allAttributes) {
						groupItems.push({
							text: attribute.get(CMDBuild.core.constants.Proxy.DESCRIPTION),
							attribute: attribute,
							scope: this,

							handler: function (item, e) {
								this.cmfg('onFieldFilterAdvancedConfiguratorConfigurationAttributesAddButtonSelect', item[CMDBuild.core.constants.Proxy.ATTRIBUTE]);
							}
						});
					}, this);

					buttonGroups.push({
						text: group,
						menu: groupItems
					});
				}, this);

				CMDBuild.core.Utils.objectArraySort(buttonGroups, CMDBuild.core.constants.Proxy.TEXT); // Sort groups

				// If no groups display just attributes
				buttonGroups = (Ext.Object.getKeys(groupedAttributes).length == 1) ? buttonGroups[0].menu : buttonGroups;

				if (!Ext.isEmpty(buttonGroups)) {
					this.form.addAttributeButton.menu.add(buttonGroups);
					this.form.addAttributeButton.enable();
				}
			},

			/**
			 * @returns {Void}
			 *
			 * @private
			 */
			attributeButtonReset: function () {
				this.form.addAttributeButton.menu.removeAll();
			},

		/**
		 * @param {CMDBuild.model.common.field.filter.advanced.configurator.tabs.attributes.Attribute} attribute
		 * @param {Object} value
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		controllersConditionAdd: function (attribute, value) {
			if (Ext.isObject(attribute) && !Ext.Object.isEmpty(attribute)) {
				this.fieldFilterAdvancedConfiguratorConfigurationAttributesControllersConditionGroupAdd(attribute);

				if (this.fieldFilterAdvancedConfiguratorConfigurationAttributesControllersConditionGroupExists(attribute.get(CMDBuild.core.constants.Proxy.NAME))) {
					var fieldManager = Ext.create('CMDBuild.core.fieldManager.FieldManager', {
						parentDelegate: this,
						// FIXME: local implementation of 'reference', 'lookup', 'inet' managed types
						managedAttributesTypes: [
							'boolean', 'char', 'date', 'decimal', 'double', 'foreignkey', 'inet',
							'lookup', 'integer', 'reference', 'string', 'text', 'time', 'timestamp'
						]
					});
					fieldManager.attributeModelSet(attribute);

					var condition = fieldManager.buildFilterCondition(),
						conditionGroup = this.fieldFilterAdvancedConfiguratorConfigurationAttributesControllersConditionGroupGet(
							attribute.get(CMDBuild.core.constants.Proxy.NAME)
						);

					condition.setValue(value);

					conditionGroup.cmfg('fieldFilterAdvancedConfiguratorTabAttributesFieldsetConditionAdd', condition);
				}
			}
		},

		// ControllersConditionGroup manage methods
			/**
			 * @param {CMDBuild.model.common.field.filter.advanced.configurator.tabs.attributes.Attribute} attribute
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			fieldFilterAdvancedConfiguratorConfigurationAttributesControllersConditionGroupAdd: function (attribute) {
				if (
					Ext.isObject(attribute) && !Ext.Object.isEmpty(attribute)
					&& !this.fieldFilterAdvancedConfiguratorConfigurationAttributesControllersConditionGroupExists(attribute.get(CMDBuild.core.constants.Proxy.NAME))
				) {
					this.controllersConditionGroup[attribute.get(CMDBuild.core.constants.Proxy.NAME)] = Ext.create(
						'CMDBuild.controller.common.field.filter.advanced.configurator.tabs.attributes.FieldsetCondition',
						{
							parentDelegate: this,
							attribute: attribute
						}
					);

					this.form.add(this.controllersConditionGroup[attribute.get(CMDBuild.core.constants.Proxy.NAME)].getView());
				}
			},

			/**
			 * @param {String} name
			 *
			 * @returns {Boolean}
			 *
			 * @private
			 */
			fieldFilterAdvancedConfiguratorConfigurationAttributesControllersConditionGroupExists: function (name) {
				return !Ext.isEmpty(name) && !Ext.isEmpty(this.controllersConditionGroup[name]);
			},

			/**
			 * @param {String} name
			 *
			 * @returns
			 * 		{CMDBuild.controller.common.field.filter.advanced.configurator.tabs.attributes.FieldsetCondition} if group not empty (single group)
			 * 		{Object} if name is empty (all groups)
			 * 		{null} if group is empty
			 *
			 * @private
			 *
			 * TODO: simpler implementation
			 */
			fieldFilterAdvancedConfiguratorConfigurationAttributesControllersConditionGroupGet: function (name) {
				if (!Ext.isEmpty(name))
					if (this.fieldFilterAdvancedConfiguratorConfigurationAttributesControllersConditionGroupExists(name)) {
						return this.controllersConditionGroup[name];
					} else {
						return null;
					}

				return this.controllersConditionGroup;
			},

			/**
			 * @param {String} name
			 *
			 * @returns {Void}
			 */
			fieldFilterAdvancedConfiguratorConfigurationAttributesControllersConditionGroupRemove: function (name) {
				if (this.fieldFilterAdvancedConfiguratorConfigurationAttributesControllersConditionGroupExists(name)) {
					this.form.remove(this.fieldFilterAdvancedConfiguratorConfigurationAttributesControllersConditionGroupGet(name).getView());

					delete this.controllersConditionGroup[name];
				}
			},

			/**
			 * @returns {Void}
			 */
			fieldFilterAdvancedConfiguratorConfigurationAttributesControllersConditionGroupReset: function () {
				this.controllersConditionGroup = {};

				this.form.removeAll();
			},

		/**
		 * Recursive method to decode filter object and launch creation of form items
		 *
		 * @param {Object} filterConfigurationObject
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		decodeFilterConfigurationObject: function (filterConfigurationObject) {
			if (Ext.isObject(filterConfigurationObject) && !Ext.Object.isEmpty(filterConfigurationObject)) {
				var filterConfigurationAttribute = filterConfigurationObject.or || filterConfigurationObject.and || filterConfigurationObject;

				if (Ext.isArray(filterConfigurationAttribute)) {
					Ext.Array.forEach(filterConfigurationAttribute, function (objectProperty, i, allObjectProperties) {
						return this.decodeFilterConfigurationObject(objectProperty);
					}, this);
				} else if (
					Ext.isObject(filterConfigurationAttribute) && !Ext.Object.isEmpty(filterConfigurationAttribute)
					&& !Ext.isEmpty(filterConfigurationAttribute[CMDBuild.core.constants.Proxy.SIMPLE])
					&& !Ext.isEmpty(filterConfigurationAttribute[CMDBuild.core.constants.Proxy.SIMPLE][CMDBuild.core.constants.Proxy.ATTRIBUTE])
				) {
					var attribute = this.selectedEntityAttributesGetByName(
						filterConfigurationAttribute[CMDBuild.core.constants.Proxy.SIMPLE][CMDBuild.core.constants.Proxy.ATTRIBUTE]
					);

					if (!Ext.isEmpty(attribute)) {
						return this.controllersConditionAdd(attribute, filterConfigurationAttribute);
					} else {
						return _error(
							'decodeFilterConfigurationObject(): attribute not found',
							this,
							filterConfigurationAttribute[CMDBuild.core.constants.Proxy.SIMPLE][CMDBuild.core.constants.Proxy.ATTRIBUTE]
						);
					}
				}
			}
		},

		/**
		 * @param {Object} parameters
		 * @param {Function} parameters.callback
		 * @param {Object} parameters.scope
		 * @param {Boolean} parameters.visible
		 *
		 * @returns {Void}
		 */
		fieldFilterAdvancedConfiguratorConfigurationAttributesEntryTypeSelect: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};
			parameters.scope = Ext.isObject(parameters.scope) ? parameters.scope : this;
			parameters.visible = Ext.isBoolean(parameters.visible) ? parameters.visible : false;

			var className = this.cmfg('fieldFilterAdvancedConfiguratorConfigurationGet', CMDBuild.core.constants.Proxy.CLASS_NAME);

			// Error handling
				if (this.cmfg('fieldFilterAdvancedConfiguratorConfigurationIsEmpty'))
					return _error('fieldFilterAdvancedConfiguratorConfigurationAttributesEntryTypeSelect(): unmanaged configuration property', this, this.cmfg('fieldFilterAdvancedConfiguratorConfigurationGet'));

				if (!Ext.isString(className) || Ext.isEmpty(className))
					return _error('fieldFilterAdvancedConfiguratorConfigurationAttributesEntryTypeSelect(): unmanaged className property', this, className);
			// END: Error handling

			this.cmfg('fieldFilterAdvancedConfiguratorConfigurationAttributesControllersConditionGroupReset');

			var params = {};
			params[CMDBuild.core.constants.Proxy.ACTIVE] = true;
			params[CMDBuild.core.constants.Proxy.CLASS_NAME] = className;

			CMDBuild.proxy.common.field.filter.advanced.configurator.tabs.Attributes.read({
				params: params,
				loadMask: false,
				scope: this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.ATTRIBUTES];

					this.attributeButtonReset();

					if (Ext.isArray(decodedResponse) && !Ext.isEmpty(decodedResponse)) {
						this.selectedEntityAttributesSet(decodedResponse);
						this.attributeButtonBuild();

						this.view.tab.setVisible(parameters.visible); // Show/Hide tab

						if (Ext.isFunction(parameters.callback))
							Ext.callback(parameters.callback, parameters.scope);
					} else {
						_error('fieldFilterAdvancedConfiguratorConfigurationAttributesEntryTypeSelect(): unmanaged response', this, decodedResponse);
					}
				}
			});
		},

		/**
		 * @returns {Object}
		 */
		fieldFilterAdvancedConfiguratorConfigurationAttributesValueGet: function () {
			var data = [];

			Ext.Object.each(this.fieldFilterAdvancedConfiguratorConfigurationAttributesControllersConditionGroupGet(), function (name, controller, myself) {
				if (Ext.isObject(controller) && !Ext.Object.isEmpty(controller))
					data.push(controller.cmfg('fieldFilterAdvancedConfiguratorTabAttributesFieldsetConditionValueGet'));
			}, this);

			if (data.length == 1)
				return data[0];

			if (data.length > 1)
				return { and: data };

			return {};
		},

		/**
		 * @param {Object} filter
		 *
		 * @returns {Void}
		 */
		fieldFilterAdvancedConfiguratorConfigurationAttributesValueSet: function (filter) {
			this.cmfg('fieldFilterAdvancedConfiguratorConfigurationAttributesControllersConditionGroupReset');

			if (
				Ext.isObject(filter) && !Ext.Object.isEmpty(filter)
				&& Ext.isObject(filter[CMDBuild.core.constants.Proxy.ATTRIBUTE]) && !Ext.Object.isEmpty(filter[CMDBuild.core.constants.Proxy.ATTRIBUTE])
			) {
				this.decodeFilterConfigurationObject(filter[CMDBuild.core.constants.Proxy.ATTRIBUTE]);
			}
		},

		/**
		 * @param {Object} attribute
		 *
		 * @returns {Void}
		 */
		onFieldFilterAdvancedConfiguratorConfigurationAttributesAddButtonSelect: function (attribute) {
			this.controllersConditionAdd(attribute);
		},

		// SelectedEntityAttributes manage methods
			/**
			 * @returns {Array}
			 *
			 * @private
			 */
			selectedEntityAttributesGet: function () {
				return Ext.Object.getValues(this.selectedEntityAttributes);
			},

			/**
			 * @param {String} name
			 *
			 * @returns {CMDBuild.model.common.field.filter.advanced.configurator.tabs.attributes.Attribute or null}
			 *
			 * @private
			 */
			selectedEntityAttributesGetByName: function (name) {
				if (Ext.isString(name) && !Ext.isEmpty(name))
					return this.selectedEntityAttributes[name];

				return null;
			},

			/**
			 * @param {Array} attributes
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			selectedEntityAttributesSet: function (attributes) {
				this.selectedEntityAttributes = {};

				if (Ext.isArray(attributes) && !Ext.isEmpty(attributes))
					Ext.Array.forEach(attributes, function (attributeObject, i, allAttributeObjects) {
						if (Ext.isObject(attributeObject) && !Ext.Object.isEmpty(attributeObject))
							this.selectedEntityAttributes[attributeObject[CMDBuild.core.constants.Proxy.NAME]] = Ext.create(
								'CMDBuild.model.common.field.filter.advanced.configurator.tabs.attributes.Attribute',
								attributeObject
							);
					}, this);
			}
	});

})();
