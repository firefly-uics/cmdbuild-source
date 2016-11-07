(function () {

	Ext.define('CMDBuild.controller.common.field.filter.advanced.configurator.tabs.attributes.Condition', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Global',
			'CMDBuild.core.constants.Proxy'
		],

		/**
		 * @cfg {CMDBuild.controller.common.field.filter.advanced.configurator.tabs.attributes.FieldsetCondition}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'fieldFieldsetFilterConditionContainerBuildItems',
			'fieldFieldsetFilterConditionContainerLabelOrVisibleSet',
			'fieldFilterAdvancedConfiguratorTabAttributesFieldsetConditionValueGet',
			'fieldFieldsetFilterConditionContainerValueSet',
			'onFieldFieldsetFilterConditionContainerInputParameterSelectChange',
			'onFieldFieldsetFilterConditionContainerOperatorSelect'
		],

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.configurator.tabs.attributes.ConditionView}
		 */
		view: undefined,

		/**
		 * @param {Array} arrayToMerge
		 *
		 * @returns {Array} items
		 */
		fieldFieldsetFilterConditionContainerBuildItems: function (arrayToMerge) {
			// Error handling
				if (!Ext.isArray(arrayToMerge) || Ext.isEmpty(arrayToMerge))
					return _error('fieldFieldsetFilterConditionContainerBuildItems(): unmanaged arrayToMerge parameter', this, arrayToMerge);
			// END: Error handling

			var items = [];

			Ext.Array.forEach(arrayToMerge, function (array, i, allArrays) {
				if (Ext.isArray(array) && !Ext.isEmpty(array))
					Ext.Array.push(
						items,
						Ext.isArray(array) ? array : [array]
					);
			}, this);

			return items;
		},

		/**
		 * @param {Boolean} state
		 *
		 * @returns {Void}
		 */
		fieldFieldsetFilterConditionContainerLabelOrVisibleSet: function (state) {
			state = Ext.isBoolean(state) ? state : false;

			if (state)
				return this.view.labelOr.show();

			return this.view.labelOr.hide();
		},

		/**
		 * @returns {Object} conditionObject
		 */
		fieldFilterAdvancedConfiguratorTabAttributesFieldsetConditionValueGet: function () {
			// Error handling
				if (this.cmfg('fieldFilterAdvancedConfiguratorTabAttributesFieldsetConditionAttributeIsEmpty'))
					return _error(
						'fieldFilterAdvancedConfiguratorTabAttributesFieldsetConditionValueGet(): unmanaged attribute property',
						this,
						this.cmfg('fieldFilterAdvancedConfiguratorTabAttributesFieldsetConditionAttributeGet')
					);
			// END: Error handling

			var conditionObject = {},
				targetClass = this.cmfg('fieldFilterAdvancedConfiguratorTabAttributesFieldsetConditionAttributeGet', CMDBuild.core.constants.Proxy.TARGET_CLASS);

			var simplePropertyObject = {};
			simplePropertyObject[CMDBuild.core.constants.Proxy.ATTRIBUTE] = this.cmfg(
				'fieldFilterAdvancedConfiguratorTabAttributesFieldsetConditionAttributeGet',
				CMDBuild.core.constants.Proxy.NAME
			);
			simplePropertyObject[CMDBuild.core.constants.Proxy.OPERATOR] = this.view.fieldOperator.getValue();
			simplePropertyObject[CMDBuild.core.constants.Proxy.PARAMETER_TYPE] = this.view.fieldInputParameter.getValue()
				? CMDBuild.core.constants.Proxy.RUNTIME : CMDBuild.core.constants.Proxy.FIXED;
			simplePropertyObject[CMDBuild.core.constants.Proxy.VALUE] = [];

			if (Ext.isArray(this.view.fields) && !Ext.isEmpty(this.view.fields))
				Ext.Array.forEach(this.view.fields, function (field, i, allFields) {
					if (Ext.isObject(field) && !Ext.Object.isEmpty(field) && !field.isDisabled()) {
						simplePropertyObject[CMDBuild.core.constants.Proxy.VALUE].push(field.getValue());

						// Manage 'calculated values' for My User and My Group
						if (Ext.isString(targetClass) && !Ext.isEmpty(targetClass))
							if (targetClass == CMDBuild.core.constants.Global.getClassNameUser() && field.getValue() == -1) {
								simplePropertyObject[CMDBuild.core.constants.Proxy.PARAMETER_TYPE] = CMDBuild.core.constants.Proxy.CALCULATED;
								simplePropertyObject[CMDBuild.core.constants.Proxy.VALUE] = ['@MY_USER'];
							} else if (targetClass == CMDBuild.core.constants.Global.getClassNameGroup() && field.getValue() == -1) {
								simplePropertyObject[CMDBuild.core.constants.Proxy.PARAMETER_TYPE] = CMDBuild.core.constants.Proxy.CALCULATED;
								simplePropertyObject[CMDBuild.core.constants.Proxy.VALUE] = ['@MY_GROUP'];
							}
					}
				}, this);

			conditionObject[CMDBuild.core.constants.Proxy.SIMPLE] = simplePropertyObject; // Wrap in simple property

			return conditionObject;
		},

		/**
		 * @param {Object} value
		 *
		 * @returns {Void}
		 */
		fieldFieldsetFilterConditionContainerValueSet: function (value) {
			value = Ext.isObject(value) ? value : {};
			value = value[CMDBuild.core.constants.Proxy.SIMPLE];

			if (Ext.isObject(value) && !Ext.Object.isEmpty(value)) {
				// Operator field setup
				this.view.fieldOperator.setValue(value[CMDBuild.core.constants.Proxy.OPERATOR]);

				// Condition fields setup
				Ext.Array.forEach(this.view.fields, function (field, i, allFields) {
					if (Ext.isObject(field) && !Ext.Object.isEmpty(field) && !Ext.isEmpty(value[CMDBuild.core.constants.Proxy.VALUE][i]))
						field.setValue(value[CMDBuild.core.constants.Proxy.VALUE][i]);
				}, this);

				// Input parameter field setup
				this.view.fieldInputParameter.setValue(value[CMDBuild.core.constants.Proxy.PARAMETER_TYPE] == CMDBuild.core.constants.Proxy.RUNTIME);
			}

			this.setDisabledFields(false);
		},

		/**
		 * @param {Boolean} checked
		 *
		 * @returns {Void}
		 */
		onFieldFieldsetFilterConditionContainerInputParameterSelectChange: function (checked) {
			this.setDisabledFields(checked);
		},

		/**
		 * @returns {Void}
		 */
		onFieldFieldsetFilterConditionContainerOperatorSelect: function () {
			this.setDisabledFields(false);
		},

		/**
		 * @param {Boolean} state
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		setDisabledFields: function (state) {
			if (Ext.isArray(this.view.fields) && !Ext.isEmpty(this.view.fields)) {
				switch (this.view.fieldOperator.getValue()) {
					case 'between': {
						Ext.Array.forEach(this.view.fields, function (field, i, allFields) {
							if (Ext.isObject(field) && !Ext.Object.isEmpty(field) && Ext.isFunction(field.setDisabled))
								field.setDisabled(this.view.fieldInputParameter.getValue()); // Check inputParameter field value
						}, this);
					} break;

					case 'isnull':
					case 'isnotnull': {
						Ext.Array.forEach(this.view.fields, function (field, i, allFields) {
							if (Ext.isObject(field) && !Ext.Object.isEmpty(field) && Ext.isFunction(field.setDisabled))
								field.setDisabled(true);
						}, this);
					} break;

					// Only the between needs two fields
					default: {
						Ext.Array.forEach(this.view.fields, function (field, i, allFields) {
							if (Ext.isObject(field) && !Ext.Object.isEmpty(field) && Ext.isFunction(field.setDisabled))
								field.setDisabled(this.view.fieldInputParameter.getValue() || i != 0 ? true : state); // Check inputParameter field value and fields item
						}, this);
					}
				}
			}
		}
	});

})();
