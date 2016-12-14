(function () {

	/**
	 * Manages all interactions from lookup fields
	 */
	Ext.define('CMDBuild.controller.common.field.comboBox.lookup.Lookup', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'controllerFieldsApplyFilterByParentValue = onFieldComboBoxLookupFocus',
			'controllerFieldsIsValid = fieldComboBoxLookupIsValid',
			'controllerFieldsReset = fieldComboBoxLookupReset',
			'controllerFieldsValueGet = fieldComboBoxLookupValueGet',
			'controllerFieldsValueSet = fieldComboBoxLookupValueSet',
			'onFieldComboBoxLookupParentUpdate',
			'onFieldComboBoxLookupReset = onFieldComboBoxLookupSelect'
		],

		/**
		 * @property {Array}
		 *
		 * @private
		 */
		controllerFields: [],

		/**
		 * @cfg {CMDBuild.view.common.field.comboBox.lookup.Lookup}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.view.common.field.comboBox.lookup.Lookup} configurationObject.view
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			// Error handling
				if (!Ext.isObject(this.view.attributeModel) || Ext.Object.isEmpty(this.view.attributeModel))
					return _error('constructor(): unmanaged attributeModel parameter', this, this.view.attributeModel);
			// END: Error handling

			var lookupTypeHierarky = this.view.attributeModel.get(CMDBuild.core.constants.Proxy.LOOKUP_TYPE_HIERARKY),
				fields = [];

			if (Ext.isArray(lookupTypeHierarky) && !Ext.isEmpty(lookupTypeHierarky))
				Ext.Array.each(lookupTypeHierarky, function (type, i, allTypes) { // Loop on array in reverse mode
					if (Ext.isString(type) && !Ext.isEmpty(type)) {
						var controller = Ext.create('CMDBuild.controller.common.field.comboBox.lookup.Field', {
							parentDelegate: this,
							type: type
						});

						this.controllerFieldsAdd(controller);

						fields.push(controller.getView());
					}
				}, this, true);

			this.view.removeAll();
			this.view.add(fields);
		},

		// ControllerFields property
			/**
			 * @param {CMDBuild.controller.common.field.comboBox.lookup.Field} controller
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			controllerFieldsAdd: function (controller) {
				// Error handling
					if (!Ext.isObject(controller) || Ext.Object.isEmpty(controller))
						return _error('controllerFieldsAdd(): unmanaged controller parameter', this, controller);
				// END: Error handling

				this.controllerFields.push(controller);
			},

			/**
			 * @param {CMDBuild.controller.common.field.comboBox.lookup.Field} controller
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			controllerFieldsApplyFilterByParentValue: function (controller) {
				// Error handling
					if (!Ext.isObject(controller) || Ext.Object.isEmpty(controller))
						return _error('controllerFieldsStoreFilterByParentValue(): unmanaged controller parameter', this, controller);
				// END: Error handling

				var parent = this.controllerFieldsParentGet(controller);

				if (Ext.isObject(parent) && !Ext.Object.isEmpty(parent))
					controller.cmfg('fieldComboBoxLookupFieldStoreFilterByValue', parent.cmfg('fieldComboBoxLookupFieldValueGet'));
			},

			/**
			 * @param {CMDBuild.controller.common.field.comboBox.lookup.Field} controller
			 *
			 * @returns {CMDBuild.controller.common.field.comboBox.lookup.Field or null}
			 *
			 * @private
			 */
			controllerFieldsChildGet: function (controller) {
				// Error handling
					if (!Ext.isObject(controller) || Ext.Object.isEmpty(controller))
						return _error('controllerFieldsChildGet(): unmanaged controller parameter', this, controller);
				// END: Error handling

				var index = Ext.Array.indexOf(this.controllerFields, controller);

				if (index >= 0)
					return this.controllerFields[index + 1];

				return null;
			},

			/**
			 * @param {CMDBuild.controller.common.field.comboBox.lookup.Field} controller
			 *
			 * @returns {CMDBuild.controller.common.field.comboBox.lookup.Field or null}
			 *
			 * @private
			 */
			controllerFieldsParentGet: function (controller) {
				// Error handling
					if (!Ext.isObject(controller) || Ext.Object.isEmpty(controller))
						return _error('controllerFieldsParentGet(): unmanaged controller parameter', this, controller);
				// END: Error handling

				var index = Ext.Array.indexOf(this.controllerFields, controller);

				if (index > 0)
					return this.controllerFields[index - 1];

				return null;
			},

			/**
			 * @param {CMDBuild.controller.common.field.comboBox.lookup.Field} controller
			 *
			 * @returns {CMDBuild.controller.common.field.comboBox.lookup.Field or null}
			 *
			 * @private
			 */
			controllerFieldsChildReset: function (controller) {
				// Error handling
					if (!Ext.isObject(controller) || Ext.Object.isEmpty(controller))
						return _error('controllerFieldsParentGet(): unmanaged controller parameter', this, controller);
				// END: Error handling

				var child = this.controllerFieldsChildGet(controller);

				if (Ext.isObject(child) && !Ext.Object.isEmpty(child))
					child.cmfg('fieldComboBoxLookupFieldReset');
			},

			/**
			 * @returns {Boolean}
			 *
			 * @private
			 */
			controllerFieldsIsValid: function () {
				if (Ext.isArray(this.controllerFields) && !Ext.isEmpty(this.controllerFields))
					return this.controllerFields[this.controllerFields.length - 1].cmfg('fieldComboBoxLookupFieldIsValid');

				return true;
			},

			/**
			 * @returns {Void}
			 *
			 * @private
			 */
			controllerFieldsReset: function () {
				Ext.Array.forEach(this.controllerFields, function (controller, i, allControllers) {
					if (Ext.isObject(controller) && !Ext.Object.isEmpty(controller))
						controller.cmfg('fieldComboBoxLookupFieldReset');
				}, this);
			},

			/**
			 * Returns last lookup field's value
			 *
			 * @returns {Number or null}
			 *
			 * @private
			 */
			controllerFieldsValueGet: function () {
				if (Ext.isArray(this.controllerFields) && !Ext.isEmpty(this.controllerFields))
					return this.controllerFields[this.controllerFields.length - 1].cmfg('fieldComboBoxLookupFieldValueGet');

				return null;
			},

			/**
			 * Setup last lookup field's value and others recursively
			 *
			 * @param {Number} value
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			controllerFieldsValueSet: function (value) {
				this.controllerFieldsReset();

				if (
					Ext.isArray(this.controllerFields) && !Ext.isEmpty(this.controllerFields)
					&& Ext.isNumber(value) && !Ext.isEmpty(value)
				) {
					return this.controllerFields[this.controllerFields.length - 1].cmfg('fieldComboBoxLookupFieldValueSet', value);
				}

				return null;
			},

		/**
		 * Recursive field's parent update
		 *
		 * @param {Object} parameters
		 * @param {CMDBuild.controller.common.field.comboBox.lookup.Field} parameters.controller
		 * @param {Number} parameters.value
		 *
		 * @returns {Void}
		 */
		onFieldComboBoxLookupParentUpdate: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};
			parameters.value = Ext.isNumber(parameters.value) ? parameters.value : null;

			var parent = this.controllerFieldsParentGet(parameters.controller);

			if (Ext.isObject(parent) && !Ext.Object.isEmpty(parent))
				parent.cmfg('fieldComboBoxLookupFieldValueSet', parameters.value);
		},

		/**
		 * Resets field's child
		 *
		 * @param {CMDBuild.controller.common.field.comboBox.lookup.Field} controller
		 *
		 * @returns {Void}
		 */
		onFieldComboBoxLookupReset: function (controller) {
			this.controllerFieldsChildReset(controller);
		}
	});

})();
