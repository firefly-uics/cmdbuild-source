(function () {

	Ext.define('CMDBuild.controller.common.field.filter.advanced.configurator.tabs.attributes.FieldsetCondition', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.common.field.filter.advanced.configurator.tabs.attributes.Attributes}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {CMDBuild.model.common.field.filter.advanced.configurator.tabs.attributes.Attribute}
		 */
		attribute: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'fieldFilterAdvancedConfiguratorTabAttributesFieldsetConditionAdd',
			'fieldFilterAdvancedConfiguratorTabAttributesFieldsetConditionAttributeGet',
			'fieldFilterAdvancedConfiguratorTabAttributesFieldsetConditionAttributeIsEmpty',
			'fieldFilterAdvancedConfiguratorTabAttributesFieldsetConditionRemoveButtonClick',
			'fieldFieldsetFilterConditionContainerValueGet',
			'fieldFilterAdvancedConfiguratorTabAttributesFieldsetConditionVisibilityAndOrFlagManage'
		],

		/**
		 * @cfg {CMDBuild.view.common.field.filter.advanced.configurator.tabs.attributes.FieldsetConditionView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.field.filter.advanced.configurator.tabs.attributes.Attributes} configurationObject.parentDelegate
		 * @param {CMDBuild.model.common.field.filter.advanced.configurator.tabs.attributes.Attribute} configurationObject.attribute
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			// Error handling
				if (!Ext.isObject(this.attribute) || Ext.Object.isEmpty(this.attribute))
					return _error('constructor(): unmanaged attribute property', this, this.attribute);
			// END: Error handling

			this.fieldFilterAdvancedConfiguratorTabAttributesFieldsetConditionAttributeSet({ value: this.attribute });

			this.view = Ext.create('CMDBuild.view.common.field.filter.advanced.configurator.tabs.attributes.FieldsetConditionView', {
				delegate: this,

				title: this.attribute.get(CMDBuild.core.constants.Proxy.DESCRIPTION)
			});
		},

		/**
		 * @param {Array} items
		 *
		 * @returns {Void}
		 */
		fieldFilterAdvancedConfiguratorTabAttributesFieldsetConditionAdd: function (items) {
			items = Ext.isArray(items) ? items : [items];

			var validItems = [];

			Ext.Array.forEach(items, function (item, i, allItems) {
				if (
					Ext.isObject(item) && !Ext.Object.isEmpty(item)
					&& item instanceof CMDBuild.view.common.field.filter.advanced.configurator.tabs.attributes.ConditionView
				) {
					item.delegate.parentDelegate = this;

					validItems.push(item);
				}
			}, this);

			this.view.add(validItems);

			this.cmfg('fieldFilterAdvancedConfiguratorTabAttributesFieldsetConditionVisibilityAndOrFlagManage');
		},

		// Attribute property functions
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 */
			fieldFilterAdvancedConfiguratorTabAttributesFieldsetConditionAttributeGet: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'attribute';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Boolean}
			 */
			fieldFilterAdvancedConfiguratorTabAttributesFieldsetConditionAttributeIsEmpty: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'attribute';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},

			/**
			 * @param {Object} parameters
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			fieldFilterAdvancedConfiguratorTabAttributesFieldsetConditionAttributeSet: function (parameters) {
				if (!Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.common.field.filter.advanced.configurator.tabs.attributes.Attribute';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'attribute';

					this.propertyManageSet(parameters);
				}
			},

		/**
		 * @param {CMDBuild.view.common.field.filter.advanced.configurator.tabs.attributes.ConditionView} item
		 *
		 * @returns {Void}
		 */
		fieldFilterAdvancedConfiguratorTabAttributesFieldsetConditionRemoveButtonClick: function (item) {
			if (Ext.isObject(item) && !Ext.Object.isEmpty(item))
				this.view.remove(item);

			this.cmfg('fieldFilterAdvancedConfiguratorTabAttributesFieldsetConditionVisibilityAndOrFlagManage');
		},

		/**
		 * @returns {Object}
		 */
		fieldFieldsetFilterConditionContainerValueGet: function () {
			if (this.view.items.getCount() > 0) {
				var data = [];

				this.view.items.each(function (item, i, len) {
					if (Ext.isObject(item) && !Ext.Object.isEmpty(item) && Ext.isFunction(item.getValue))
						data.push(item.getValue());
				});

				// Or clause omitted if there is only one condition
				if (data.length == 1)
					return data[0];

				return { or: data };
			}

			return {};
		},

		/**
		 * @returns {Void}
		 */
		fieldFilterAdvancedConfiguratorTabAttributesFieldsetConditionVisibilityAndOrFlagManage: function () {
			if (this.view.items.getCount() > 0)
				return this.view.items.each(function (item, i, len) {
					item.setLabelOrVisible(i < len - 1);
				}, this);

			// Remove entire container if have no items
			return this.cmfg(
				'fieldFilterAdvancedConfiguratorConfigurationAttributesControllersConditionGroupRemove',
				this.cmfg('fieldFilterAdvancedConfiguratorTabAttributesFieldsetConditionAttributeGet', CMDBuild.core.constants.Proxy.NAME)
			);
		}
	});

})();
