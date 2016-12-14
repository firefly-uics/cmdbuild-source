(function() {

	/**
	 * Customization of CMDBuild.controller.common.field.translatable.Window, mainly used in menu translations tree. Included/customized some
	 * CMDBuild.controller.common.field.translatable.Translatable methods
	 *
	 * @link CMDBuild.controller.common.field.translatable.Translatable
	 */
	Ext.define('CMDBuild.controller.common.field.translatable.NoFieldWindow', {
		extend: 'CMDBuild.controller.common.field.translatable.Window',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.common.field.translatable.Translatable'
		],

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * Buffer object where save translatable values
		 *
		 * @property {Object}
		 *
		 * @private
		 */
		bufferTranslations: {},

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'fieldTranslatableBufferTranslationsBatchSave',
			'fieldTranslatableConfigurationGet',
			'fieldTranslatableConfigurationIsEmpty',
			'fieldTranslatableConfigurationSet',
			'fieldTranslatableParamsGet',
			'onFieldTranslatableWindowAbortButtonClick',
			'onFieldTranslatableWindowConfigureAndShow',
			'onFieldTranslatableWindowConfirmButtonClick'
		],

		/**
		 * @property {CMDBuild.model.common.field.translatable.Configuration}
		 *
		 * @private
		 */
		configuration: undefined,

		/**
		 * @property {CMDBuild.view.common.field.translatable.window.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.common.field.translatable.window.Window}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Object} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.common.field.translatable.window.Window', { delegate: this });

			// Shorthands
			this.form = this.view.form;
		},

		// BufferTranslations methods
			/**
			 * @param {Object} translationObject
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			fieldTranslatableBufferTranslationsUpdate: function (translationObject) {
				// Error handling
					if (this.cmfg('fieldTranslatableConfigurationIsEmpty'))
						return _error('fieldTranslatableBufferTranslationsUpdate(): unmanaged configuration property', this, this.cmfg('fieldTranslatableConfigurationGet'));
				// END: Error handling

				var identifier = this.cmfg('fieldTranslatableConfigurationGet', CMDBuild.core.constants.Proxy.IDENTIFIER);

				this.bufferTranslations[identifier] = this.cmfg('fieldTranslatableParamsGet', { includeTranslations : true });
			},

			/**
			 * @returns {Void}
			 *
			 * @private
			 */
			fieldTranslatableBufferTranslationsReset: function () {
				this.bufferTranslations = {};
			},

			/**
			 * Customization of CMDBuild.controller.common.field.translatable.Utils.commit
			 *
			 * @returns {Void}
			 */
			fieldTranslatableBufferTranslationsBatchSave: function () {
				Ext.Object.each(this.bufferTranslations, function (identifier, params, myself) {
					if (
						Ext.isObject(params) && !Ext.Object.isEmpty(params)
						&& Ext.isString(params[CMDBuild.core.constants.Proxy.TRANSLATIONS]) && !Ext.isEmpty(params[CMDBuild.core.constants.Proxy.TRANSLATIONS])
					) {
						CMDBuild.proxy.common.field.translatable.Translatable.update({ params: params });
					}
				}, this);

				this.fieldTranslatableBufferTranslationsReset();
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
		 * @returns {Object} parameters
		 * @returns {Object} parameters.config
		 * @returns {String} parameters.title
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		onFieldTranslatableWindowConfigureAndShow: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};

			this.cmfg('fieldTranslatableConfigurationSet', { value: parameters.config }); // Setup configurations

			// Error handling
				if (this.cmfg('fieldTranslatableConfigurationIsEmpty') || !this.cmfg('fieldTranslatableConfigurationGet').isValid())
					return _error('constructor(): unmanaged configuration property', this, this.cmfg('fieldTranslatableConfigurationGet'));
			// END: Error handling

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

					this.setViewTitle(parameters.title);

					this.buildTranslationsFields();

					this.form.loadRecord(this.cmfg('fieldTranslatableConfigurationGet', CMDBuild.core.constants.Proxy.TRANSLATIONS));

					// Show window
					if (!Ext.isEmpty(this.view))
						this.view.show();
				}
			});
		},

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		onFieldTranslatableWindowConfirmButtonClick: function() {
			this.cmfg('fieldTranslatableConfigurationSet', {
				propertyName: CMDBuild.core.constants.Proxy.TRANSLATIONS,
				value: Ext.create('CMDBuild.model.common.field.translatable.Window', this.form.panelFunctionDataGet())
			});

			this.fieldTranslatableBufferTranslationsUpdate();

			this.cmfg('onFieldTranslatableWindowAbortButtonClick');
		}
	});

})();