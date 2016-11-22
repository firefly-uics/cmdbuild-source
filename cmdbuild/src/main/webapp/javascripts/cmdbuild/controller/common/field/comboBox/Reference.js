(function () {

	Ext.define('CMDBuild.controller.common.field.comboBox.Reference', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Global',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Utils',
			'CMDBuild.proxy.common.field.comboBox.Reference'
		],

		/**
		 * @cfg {Mixed}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.controller.common.field.searchWindow.SearchWindow}
		 */
		controllerSearchWindow: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'fieldComboBoxReferenceExternalValueSet = fieldValueSet',
			'fieldComboBoxReferenceNormalizeValue',
			'fieldComboBoxReferenceStoreExceedsLimit',
			'fieldComboBoxReferenceStoreGet = fieldStoreGet',
			'fieldComboBoxReferenceValueFieldGet = fiedlValueFieldGet',
			'fieldComboBoxReferenceValueGet = fieldValueGet',
			'fieldComboBoxReferenceValueSet',
			'onFieldComboBoxReferenceKeyUp',
			'onFieldComboBoxReferenceTrigger1Click',
			'onFieldComboBoxReferenceTrigger2Click',
			'onFieldComboBoxReferenceTrigger3Click'
		],

		/**
		 * @property {CMDBuild.model.common.field.comboBox.reference.Configuration}
		 *
		 * @private
		 */
		configuration: undefined,

		/**
		 * @property {CMDBuild.view.common.field.comboBox.Reference}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.view.common.field.comboBox.Reference} configurationObject.view
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.configurationSet({ value: this.view.configuration });

			// Error handling
				if (!Ext.isObject(this.view.attributeModel) || Ext.Object.isEmpty(this.view.attributeModel))
					return _error('constructor(): unmanaged attributeModel parameter', this, this.view.attributeModel);

				if (!Ext.isObject(this.view.getStore()) || Ext.Object.isEmpty(this.view.getStore()))
					return _error('constructor(): unmanaged store property', this, this.view.getStore());
			// END: Error handling

			// Build sub-controller
			this.controllerSearchWindow = Ext.create('CMDBuild.controller.common.field.searchWindow.SearchWindow', { parentDelegate: this });

			if (!this.view.getStore().isLoading())
				this.view.getStore().on('load', this.onStoreLoad, this);
		},

		// Configuration property methods
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 *
			 * @private
			 */
			configurationGet: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'configuration';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Object} parameters
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			configurationSet: function (parameters) {
				if (!Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.common.field.comboBox.reference.Configuration';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'configuration';

					this.propertyManageSet(parameters);
				}
			},

		/**
		 * Forwarder method (usually from Search window)
		 *
		 * @param {Ext.data.Model} selectedRecord
		 *
		 * @returns {Void}
		 */
		fieldComboBoxReferenceExternalValueSet: function (selectedRecord) {
			if (!Ext.isEmpty(selectedRecord)) {
				this.view.blur(); // Allow 'change' event that occurs on blur
				this.view.setValue(selectedRecord.get(this.view.valueField));
			}
		},

		/**
		 * Recursive normalization of value
		 *
		 * @param {Mixed} value
		 *
		 * @returns {Mixed}
		 */
		fieldComboBoxReferenceNormalizeValue: function (value) {
			if (!Ext.isEmpty(value)) {
				switch (Ext.typeOf(value)) {
					case 'array':
						return this.cmfg('fieldComboBoxReferenceNormalizeValue', value[0]);

					case 'string': {
						if (value == '@MY_GROUP' || value == '@MY_USER')
							return -1;

						return isNaN(parseInt(value)) ? value : parseInt(value);
					}

					case 'object': {
						if (Ext.isFunction(value.get))
							return this.cmfg('fieldComboBoxReferenceNormalizeValue', value.get(this.view.valueField));

						return this.cmfg('fieldComboBoxReferenceNormalizeValue', value[this.view.valueField]);
					}

					default:
						return value;
				}
			}

			return '';
		},

		/**
		 * @returns {Boolean}
		 */
		fieldComboBoxReferenceStoreExceedsLimit: function () {
			return this.view.getStore().getTotalCount() > CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.REFERENCE_COMBO_STORE_LIMIT);
		},

		/**
		 * @returns {Ext.data.Store}
		 */
		fieldComboBoxReferenceStoreGet: function () {
			return this.view.getStore();
		},

		/**
		 * @returns {String}
		 */
		fieldComboBoxReferenceValueFieldGet: function () {
			return this.view.valueField;
		},

		/**
		 * @returns {Number}
		 */
		fieldComboBoxReferenceValueGet: function () {
			return this.view.getValue();
		},

		/**
		 * Adds values in store if not already inside
		 *
		 * @param {Mixed} value
		 *
		 * @returns {Array}
		 */
		fieldComboBoxReferenceValueSet: function (value) {
			value = this.cmfg('fieldComboBoxReferenceNormalizeValue', value);

			if (Ext.isNumber(value) && !Ext.isEmpty(value)) {
				if (!this.view.getStore().isLoading())
					if (this.view.getStore().find(this.view.valueField, value) >= 0) { // Value in store
						return [value];
					} else if (value > 0) { // Value not in store, avoids to execute call for 'calculated values' for My User and My Group
						var params = {};
						params[CMDBuild.core.constants.Proxy.CARD_ID] = value;
						params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.view.attributeModel.get(CMDBuild.core.constants.Proxy.TARGET_CLASS);

						CMDBuild.proxy.common.field.comboBox.Reference.readCard({
							params: params,
							scope: this,
							success: function (response, options, decodedResponse) {
								decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CARD];

								if (Ext.isObject(decodedResponse) && !Ext.Object.isEmpty(decodedResponse)) {
									this.view.getStore().add(
											Ext.create('CMDBuild.model.common.field.comboBox.reference.StoreRecord', {
												Id: decodedResponse['Id'],
												Description: decodedResponse['Description']
											})
									);

									this.view.setValue(decodedResponse[this.view.valueField]);
								}

								this.view.validate();
							}
						});

						return [value];
					}

				// Defer value set because store is loading
				this.view.getStore().on('load', function (store, records, successful, eOpts) {
					this.view.setValue(value);
				}, this, { single: true });

				return [value];
			}

			return [];
		},

		/**
		 * @returns {Void}
		 *
		 * @private
		 */
		manageMetadata: function () {
			var metadata = this.view.attributeModel.get(CMDBuild.core.constants.Proxy.METADATA);

			if (
				Ext.isObject(metadata) && !Ext.Object.isEmpty(metadata)
				&& CMDBuild.core.Utils.decodeAsBoolean(metadata['system.type.reference.' + CMDBuild.core.constants.Proxy.PRESELECT_IF_UNIQUE])
				&& this.view.getStore().getCount() == 1
			) {
				this.view.setValue(this.view.getStore().getAt(0).get('Id'));
			}
		},

		/**
		 * @returns {Void}
		 */
		onFieldComboBoxReferenceKeyUp: function () {
			this.cmfg('onFieldComboBoxReferenceTrigger3Click', this.view.getRawValue());
		},

		/**
		 * If store has more than configuration limit records, no drop down but opens searchWindow
		 *
		 * @returns {Void}
		 */
		onFieldComboBoxReferenceTrigger1Click: function () {
			if (this.view.getStore().isLoading()) {
				this.view.getStore().on('load', this.trigger1Manager, this, { single: true });
			} else {
				this.trigger1Manager();
			}
		},

		/**
		 * @returns {Void}
		 */
		onFieldComboBoxReferenceTrigger2Click: function () {
			if (!this.view.isDisabled())
				this.view.setValue();
		},

		/**
		 * @param {String} value
		 *
		 * @returns {Void}
		 */
		onFieldComboBoxReferenceTrigger3Click: function (value) {
			value = Ext.isString(value) ? value : '';

			if (!this.view.isDisabled()) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.NAME] = this.view.attributeModel.get(CMDBuild.core.constants.Proxy.TARGET_CLASS);

				CMDBuild.proxy.common.field.comboBox.Reference.readClassByName({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

						if (Ext.isObject(decodedResponse) && !Ext.Object.isEmpty(decodedResponse)) {
							var configurationObject = {};
							configurationObject[CMDBuild.core.constants.Proxy.ENTRY_TYPE] = Ext.create('CMDBuild.cache.CMEntryTypeModel', decodedResponse);
							configurationObject[CMDBuild.core.constants.Proxy.GRID_CONFIGURATION] = { presets: { quickSearch: value } };
							configurationObject[CMDBuild.core.constants.Proxy.READ_ONLY] = this.configurationGet(CMDBuild.core.constants.Proxy.READ_ONLY_SEARCH_WINDOW);

							this.controllerSearchWindow.cmfg('fieldSearchWindowConfigurationSet', { value: configurationObject });
							this.controllerSearchWindow.getView().show();
						} else {
							_error('onFieldComboBoxReferenceTrigger3Click(): unmanaged response', this, decodedResponse);
						}
					}
				});
			}
		},

		/**
		 * @param {Ext.data.Store or CMDBuild.core.cache.Store} store
		 * @param {Array} records
		 * @param {Boolean} successful
		 * @param {Object} eOpts
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		onStoreLoad: function (store, records, successful, eOpts) {
			this.manageMetadata();

			// Manage 'calculated values' for My User and My Group
			var targetClass = this.view.attributeModel.get(CMDBuild.core.constants.Proxy.TARGET_CLASS);

			if (
				Ext.isString(targetClass) && !Ext.isEmpty(targetClass)
				&& this.view.getStore().find(this.view.valueField, -1) < 0 // Avoid duplicates
			) {
				switch (targetClass) {
					case CMDBuild.core.constants.Global.getClassNameUser(): {
						this.view.getStore().insert(0, Ext.create('CMDBuild.model.common.field.comboBox.reference.StoreRecord', {
							Id: -1,
							Description: '* ' + CMDBuild.Translation.loggedUser + ' *'
						}));
					} break;

					case CMDBuild.core.constants.Global.getClassNameGroup(): {
						this.view.getStore().insert(0, Ext.create('CMDBuild.model.common.field.comboBox.reference.StoreRecord', {
							Id: -1,
							Description: '* ' + CMDBuild.Translation.loggedGroup + ' *'
						}));
					} break;
				}
			}

			this.view.setValue(this.view.getValue());
		},

		/**
		 * @returns {Void}
		 *
		 * @private
		 */
		trigger1Manager: function () {
			if (this.cmfg('fieldComboBoxReferenceStoreExceedsLimit')) {
				this.cmfg('onFieldComboBoxReferenceTrigger3Click');
			} else {
				this.view.onTriggerClick();
			}
		}
	});

})();
