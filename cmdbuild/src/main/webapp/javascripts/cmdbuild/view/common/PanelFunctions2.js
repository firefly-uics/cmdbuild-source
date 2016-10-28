(function () {

	/**
	 * Service class to be used as mixin for Ext.form.Panel or some methods are compatible also with Ext.panel.Panel
	 *
	 * Specific managed properties:
	 * 	- {Boolean} disablePanelFunctions: disable PanelFunctions class actions on processed item (old name: considerAsFieldToDisable)
	 * 	- {Boolean} enablePanelFunctions: enable PanelFunctions class actions on processed item
	 * 	- {Boolean} forceDisabled: force item to be disabled
	 *
	 * @version 2
	 */
	Ext.define('CMDBuild.view.common.PanelFunctions2', {

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @param {Object} field
		 *
		 * @returns {Boolean}
		 *
		 * @private
		 */
		isPanelFunctionManagedField: function (field) {
			return (
				Ext.isObject(field) && !Ext.Object.isEmpty(field)
				&& !field.disablePanelFunctions
				&& (
					field instanceof Ext.button.Button
					|| field instanceof Ext.form.Field
					|| field instanceof Ext.form.field.Base
					|| field instanceof Ext.form.FieldContainer
					|| field instanceof Ext.form.FieldSet
					|| field instanceof Ext.ux.form.MultiSelect
					|| (Ext.isBoolean(field.enablePanelFunctions) && field.enablePanelFunctions)
					|| (Ext.isBoolean(field.considerAsFieldToDisable) && field.considerAsFieldToDisable) /** @deprecated */
				)
			);
		},

		/**
		 * @param {Object} parameters
		 * @param {Boolean} parameters.ignoreForceDisabled
		 * @param {Boolean} parameters.ignoreIsVisibleCheck
		 * @param {Boolean} parameters.state
		 * @param {Ext.component.Component} parameters.target
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		managedFieldDisableSet: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};
			parameters.ignoreForceDisabled = Ext.isBoolean(parameters.ignoreForceDisabled) ? parameters.ignoreForceDisabled : false;
			parameters.ignoreIsVisibleCheck = Ext.isBoolean(parameters.ignoreIsVisibleCheck) ? parameters.ignoreIsVisibleCheck : true;
			parameters.state = Ext.isBoolean(parameters.state) ? parameters.state : true;
			parameters.target = Ext.isObject(parameters.target) ? parameters.target : this;

			// Error handling
				if (!Ext.isFunction(parameters.target.cascade))
					return _error('managedFieldDisableSet(): unmanaged target component parameter', this, parameters.target);
			// END: Error handling

			parameters.target.cascade(function (item) {
				if (
					this.isPanelFunctionManagedField(item)
					&& (item.isVisible() || parameters.ignoreIsVisibleCheck)
					&& Ext.isFunction(item.setDisabled)
				) {
					item.setDisabled(Ext.isBoolean(item.forceDisabled) && item.forceDisabled && !parameters.ignoreForceDisabled ? true : parameters.state);
				}
			}, this);
		},

		/**
		 * @returns {Array} nonValidFields
		 */
		panelFunctionFieldInvalidGet: function () {
			var nonValidFields = [];

			this.cascade(function (item) {
				if (
					this.isPanelFunctionManagedField(item)
					&& Ext.isFunction(item.isValid) && !item.isValid()
					&& Ext.isFunction(item.isDisabled) && !item.isDisabled()
					&& Ext.isFunction(item.isHidden) && !item.isHidden()
				) {
					nonValidFields.push(item);
				}
			}, this);

			return nonValidFields;
		},

		/**
		 * @param {Object} parameters
		 * @param {Boolean} parameters.includeDisabled
		 * @param {Ext.component.Component} parameters.target
		 *
		 * @returns {Object}
		 */
		panelFunctionDataGet: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};
			parameters.includeDisabled = Ext.isBoolean(parameters.includeDisabled) ? parameters.includeDisabled : false;
			parameters.target = Ext.isObject(parameters.target) ? parameters.target : this;

			var values = Ext.isFunction(parameters.target.getForm) ? parameters.target.getForm().getValues() : {};

			if (parameters.includeDisabled) {
				var data = {};

				parameters.target.cascade(function (item) {
					if (
						this.isPanelFunctionManagedField(item)
						&& Ext.isFunction(item.getValue) && Ext.isFunction(item.getName)
						&& Ext.isBoolean(item.submitValue) && item.submitValue
					) {
						data[item.getName()] = item.getValue();
					}
				}, this);

				return Ext.apply(values, data);
			}

			return values;
		},

		/**
		 * @param {Object} parameters
		 * @param {Boolean} parameters.ignoreForceDisabled
		 * @param {Boolean} parameters.ignoreIsVisibleCheck
		 * @param {Boolean} parameters.state
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		panelFunctionFieldDisableStateSet: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};

			this.managedFieldDisableSet({
				ignoreForceDisabled: parameters.ignoreForceDisabled,
				ignoreIsVisibleCheck: parameters.ignoreIsVisibleCheck,
				state: parameters.state
			});
		},

		/**
		 * Change state of fieldset's contained fields
		 *
		 * @param {Object} parameters
		 * @param {Boolean} parameters.ignoreForceDisabled
		 * @param {Boolean} parameters.ignoreIsVisibleCheck
		 * @param {Ext.form.FieldSet} parameters.fieldset
		 * @param {Boolean} parameters.state
		 *
		 * @returns {Void}
		 */
		panelFunctionFieldSetDisableStateSet: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};
			parameters.state = Ext.isBoolean(parameters.state) ? parameters.state : true;

			// Error handling
				if (!parameters.fieldset instanceof Ext.form.FieldSet)
					return _error('panelFunctionFieldSetDisableStateSet(): unmanaged fieldset parameter', this, parameters.fieldset);
			// END: Error handling

			this.managedFieldDisableSet({
				ignoreForceDisabled: parameters.ignoreForceDisabled,
				ignoreIsVisibleCheck: parameters.ignoreIsVisibleCheck,
				state: parameters.state,
				target: parameters.fieldset
			});
		},

		/**
		 * Keeps in sync two fields, usually name and description. If the master field changes and the slave is empty, or it has the same
		 * value as the old value of the master, its value is updated with the new one.
		 *
		 * These function has to be used with the change listener, example:
		 * 		change: function (field, newValue, oldValue, eOpts) {
		 * 			this.fieldSynch(slaveField, newValue, oldValue);
		 * 		}
		 *
		 * @param {Object} parameters
		 * @param {Object} parameters.slaveField
		 * @param {Object} parameters.newValue
		 * @param {Object} parameters.oldValue
		 *
		 * @returns {Void}
		 */
		panelFunctionFieldSynch: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};

			// Error handling
				if (!Ext.isObject(parameters.slaveField) || Ext.isEmpty(parameters.slaveField))
					return _error('panelFunctionFieldSynch(): unmanaged slaveField parameter', this, parameters.slaveField);
			// END: Error handling

			if (this.isPanelFunctionManagedField(parameters.slaveField)) {
				var actualValue = parameters.slaveField.getValue();

				if (Ext.isEmpty(actualValue) || actualValue == parameters.oldValue)
					parameters.slaveField.setValue(parameters.newValue);
			}
		},

		/**
		 * Setup modify state of form
		 *
		 * @param {Object} parameters
		 * @param {Boolean} parameters.ignoreForceDisabled
		 * @param {Boolean} parameters.ignoreIsVisibleCheck
		 * @param {Boolean} parameters.forceToolbarBottomState
		 * @param {Boolean} parameters.forceToolbarTopState
		 * @param {Boolean} parameters.state
		 *
		 * @returns {Void}
		 */
		panelFunctionModifyStateSet: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};
			parameters.state = Ext.isBoolean(parameters.state) ? parameters.state : false;

			// Dependents from state value
			parameters.forceToolbarBottomState = Ext.isBoolean(parameters.forceToolbarBottomState) ? parameters.forceToolbarBottomState : !parameters.state;
			parameters.forceToolbarTopState = Ext.isBoolean(parameters.forceToolbarTopState) ? parameters.forceToolbarTopState : parameters.state;

			this.panelFunctionFieldDisableStateSet({
				ignoreForceDisabled: parameters.ignoreForceDisabled,
				ignoreIsVisibleCheck: parameters.ignoreIsVisibleCheck,
				state: !parameters.state
			});
			this.panelFunctionToolbarBottomDisableStateSet({ state: parameters.forceToolbarBottomState });
			this.panelFunctionToolbarTopDisableStateSet({ state: parameters.forceToolbarTopState });
		},

		/**
		 * @param {Object} parameters
		 * @param {Object} parameters.target
		 *
		 * @returns {Void}
		 */
		panelFunctionReset: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};
			parameters.target = Ext.isObject(parameters.target) ? parameters.target : this;

			// Error handling
				if (!Ext.isFunction(parameters.target.cascade))
					return _error('panelFunctionReset(): unmanaged cascade function', this, parameters.target);
			// END: Error handling

				parameters.target.cascade(function (item) {
				if (
					this.isPanelFunctionManagedField(item)
					&& Ext.isFunction(item.setValue) && Ext.isFunction(item.reset)
				) {
					item.setValue();
					item.reset();
				}
			}, this);
		},

		/**
		 * @param {Object} parameters
		 * @param {Boolean} parameters.ignoreForceDisabled
		 * @param {Boolean} parameters.state
		 *
		 * @returns {Void}
		 */
		panelFunctionToolbarBottomDisableStateSet: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};
			parameters.ignoreForceDisabled = Ext.isBoolean(parameters.ignoreForceDisabled) ? parameters.ignoreForceDisabled : false;
			parameters.state = Ext.isBoolean(parameters.state) ? parameters.state : true;

			var componentToolbar = this.getDockedComponent(CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM);

			if (Ext.isObject(componentToolbar) && !Ext.Object.isEmpty(componentToolbar))
				Ext.Array.each(componentToolbar.items.items, function (item, i, allItems) {
					if (
						Ext.isObject(item) && !Ext.Object.isEmpty(item)
						&& !item.disablePanelFunctions
						&& (
							item instanceof Ext.button.Button
							|| (Ext.isBoolean(item.enablePanelFunctions) && item.enablePanelFunctions)
							|| (Ext.isBoolean(item.considerAsFieldToDisable) && item.considerAsFieldToDisable) /** @deprecated */
						)
						&& Ext.isFunction(item.setDisabled)
					) {
						item.setDisabled(Ext.isBoolean(item.forceDisabled) && item.forceDisabled && !parameters.ignoreForceDisabled ? true : parameters.state);
					}
				}, this);
		},

		/**
		 * @param {Object} parameters
		 * @param {Boolean} parameters.ignoreForceDisabled
		 * @param {Boolean} parameters.state
		 *
		 * @returns {Void}
		 */
		panelFunctionToolbarTopDisableStateSet: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};
			parameters.ignoreForceDisabled = Ext.isBoolean(parameters.ignoreForceDisabled) ? parameters.ignoreForceDisabled : false;
			parameters.state = Ext.isBoolean(parameters.state) ? parameters.state : true;

			var componentToolbar = this.getDockedComponent(CMDBuild.core.constants.Proxy.TOOLBAR_TOP);

			if (Ext.isObject(componentToolbar) && !Ext.Object.isEmpty(componentToolbar))
				Ext.Array.each(componentToolbar.items.items, function (item, i, allItems) {
					if (
						Ext.isObject(item) && !Ext.Object.isEmpty(item)
						&& !item.disablePanelFunctions
						&& (
							item instanceof Ext.button.Button
							|| (Ext.isBoolean(item.enablePanelFunctions) && item.enablePanelFunctions)
							|| (Ext.isBoolean(item.considerAsFieldToDisable) && item.considerAsFieldToDisable) /** @deprecated */
						)
						&& Ext.isFunction(item.setDisabled)
					) {
						item.setDisabled(Ext.isBoolean(item.forceDisabled) && item.forceDisabled && !parameters.ignoreForceDisabled ? true : parameters.state);
					}
				}, this);
		},

		/**
		 * @param {Object} parameters
		 * @param {Boolean} parameters.includeDisabled
		 * @param {String} parameters.propertyName
		 *
		 * @returns {Mixed}
		 */
		panelFunctionValueGet: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};
			parameters.includeDisabled = Ext.isBoolean(parameters.includeDisabled) ? parameters.includeDisabled : true;

			// Error handling
				if (!Ext.isString(parameters.propertyName) || Ext.isEmpty(parameters.propertyName))
					return _error('panelFunctionValueGet(): unmanaged propertyName parameter', this, parameters.propertyName);
			// END: Error handling

			return this.panelFunctionDataGet({ includeDisabled: parameters.includeDisabled })[parameters.propertyName];
		}
	});

})();
