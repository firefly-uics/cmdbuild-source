(function () {

	Ext.define('CMDBuild.controller.common.field.translatable.Translatable', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.common.field.translatable.Translatable'
		],

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.model.common.field.translatable.Configuration}
		 *
		 * @private
		 */
		configuration: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'fieldTranslatableBuildField',
			'fieldTranslatableConfigurationGet',
			'fieldTranslatableConfigurationIsEmpty',
			'fieldTranslatableConfigurationReadTranslations',
			'fieldTranslatableConfigurationSet',
			'fieldTranslatableFieldLabelGet',
			'fieldTranslatableIsValid',
			'fieldTranslatableParamsGet',
			'fieldTranslatableReset',
			'fieldTranslatableValueGet',
			'fieldTranslatableValueSet',
			'onFieldTranslatableButtonClick'
		],

		/**
		 * @cfg {CMDBuild.view.common.field.translatable.Translatable}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.view.common.field.translatable.Translatable} configurationObject.view
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			// If valid setup configuration object on creation, otherwise call fieldTranslatableConfigurationSet() field method
			if (Ext.isObject(this.view.config) && !Ext.Object.isEmpty(this.view.config))
				this.cmfg('fieldTranslatableConfigurationSet', { value: this.view.config });

			// Build sub controllers
			this.controllerWindow = Ext.create('CMDBuild.controller.common.field.translatable.Window', { parentDelegate: this });
		},

		// Configuration methods
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 */
			fieldTranslatableConfigurationGet: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'configuration';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Boolean}
			 */
			fieldTranslatableConfigurationIsEmpty: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'configuration';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},

			/**
			 * @param {Object} parameters
			 *
			 * @returns {Void}
			 */
			fieldTranslatableConfigurationReadTranslations: function () {
				CMDBuild.proxy.common.field.translatable.Translatable.read({
					params: this.cmfg('fieldTranslatableParamsGet'),
					loadMask: false,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

						this.cmfg('fieldTranslatableConfigurationSet', {
							propertyName: CMDBuild.core.constants.Proxy.TRANSLATIONS,
							value: Ext.create('CMDBuild.model.common.field.translatable.Window', decodedResponse)
						});
					}
				});
			},

			/**
			 * @param {Object} parameters
			 *
			 * @returns {Void}
			 */
			fieldTranslatableConfigurationSet: function (parameters) {
				if (!Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.common.field.translatable.Configuration';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'configuration';

					this.propertyManageSet(parameters);
				}
			},

		/**
		 * @param {Object} value
		 *
		 * @returns {String or null}
		 *
		 * @private
		 */
		decodeConfigurationValue: function (value) {
			if (Ext.typeOf(value) == 'string')
				return value;

			// Error handling
				if (!Ext.isString(value.key) || Ext.isEmpty(value.key))
					return _error('decodeConfigurationValue(): unmanaged key property', this, value.key);

				if (!Ext.isString(value.sourceType) || Ext.isEmpty(value.sourceType))
					return _error('decodeConfigurationValue(): unmanaged sourceType property', this, value.sourceType);

				if (!Ext.isObject(value.source) || Ext.Object.isEmpty(value.source))
					return _error('decodeConfigurationValue(): unmanaged source property', this, value.source);
			// END: Error handling

			switch (value.sourceType) {
				case 'form': {
					// Error handling
						if (!Ext.isObject(value.source) || Ext.Object.isEmpty(value.source) || !Ext.isFunction(value.source.getData))
							return _error('decodeConfigurationValue(): unmanaged source parameter', this, value.source);
					// END: Error handling

					return value.source.getData(true)[value.key];
				} break;

				case 'model': {
					// Error handling
						if (!Ext.isObject(value.source) || Ext.Object.isEmpty(value.source) || !Ext.isFunction(value.source.get))
							return _error('decodeConfigurationValue(): unmanaged source parameter', this, value.source);
					// END: Error handling

					return value.source.get(value.key);
				} break;

				case 'object':
					return value.source[value.key];

				default:
					return _error('decodeConfigurationValue(): unmanaged type property', this);
			}
		},

		/**
		 * @returns {Object}
		 */
		fieldTranslatableBuildField: function () {
			switch (this.view.config.fieldMode) {
				case 'text':
				default:
					return Ext.create('Ext.form.field.Text', {
						name: this.view.name,
						allowBlank: this.view.allowBlank,
						vtype: this.view.vtype,
						disablePanelFunctions: true,
						flex: 1
					});
			}
		},

		/**
		 * @returns {String}
		 */
		fieldTranslatableFieldLabelGet: function () {
			return this.view.getFieldLabel();
		},

		/**
		 * Forwarder method
		 *
		 * @returns {String}
		 */
		fieldTranslatableIsValid: function () {
			return this.view.field.isValid();
		},

		/**
		 * @param {Object} parameters
		 * @param {Boolean} parameters.includeTranslations
		 *
		 * @returns {Object} params
		 */
		fieldTranslatableParamsGet: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};
			parameters.includeTranslations = Ext.isBoolean(parameters.includeTranslations) ? parameters.includeTranslations : false;

			// Error handling
				if (this.cmfg('fieldTranslatableConfigurationIsEmpty'))
					return _error('fieldTranslatableParamsSet(): unmanaged configuration property', this, this.cmfg('fieldTranslatableConfigurationGet'));
			// END: Error handling

			var params = {};
			params[CMDBuild.core.constants.Proxy.TYPE] = this.cmfg('fieldTranslatableConfigurationGet', CMDBuild.core.constants.Proxy.TYPE);
			params[CMDBuild.core.constants.Proxy.IDENTIFIER] = this.decodeConfigurationValue(
				this.cmfg('fieldTranslatableConfigurationGet', CMDBuild.core.constants.Proxy.IDENTIFIER)
			);
			params[CMDBuild.core.constants.Proxy.FIELD] = this.cmfg('fieldTranslatableConfigurationGet', CMDBuild.core.constants.Proxy.FIELD);

			if (!this.cmfg('fieldTranslatableConfigurationIsEmpty', CMDBuild.core.constants.Proxy.OWNER))
				params[CMDBuild.core.constants.Proxy.OWNER] = this.decodeConfigurationValue(
					this.cmfg('fieldTranslatableConfigurationGet', CMDBuild.core.constants.Proxy.OWNER)
				);

			if (parameters.includeTranslations) {
				var translationsModel = this.cmfg('fieldTranslatableConfigurationGet', CMDBuild.core.constants.Proxy.TRANSLATIONS);

				params[CMDBuild.core.constants.Proxy.TRANSLATIONS] = Ext.encode(translationsModel.getData());
			}

			return params;
		},

		/**
		 * Forwarder method
		 *
		 * @returns {Void}
		 */
		fieldTranslatableReset: function () {
			this.view.field.reset();
		},

		/**
		 * Forwarder method
		 *
		 * @returns {String}
		 */
		fieldTranslatableValueGet: function () {
			return this.view.field.getValue();
		},

		/**
		 * Forwarder method
		 *
		 * @param {String} value
		 *
		 * @returns {Void}
		 */
		fieldTranslatableValueSet: function (value) {
			this.view.field.setValue(value);
		},

		/**
		 * @returns {Void}
		 */
		onFieldTranslatableButtonClick: function () {
			this.controllerWindow.cmfg('onFieldTranslatableWindowConfigureAndShow', { parentDelegate: this });
		}
	});

})();
