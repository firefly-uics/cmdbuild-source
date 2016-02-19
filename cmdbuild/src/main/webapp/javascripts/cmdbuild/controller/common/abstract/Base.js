(function () {

	// External implementation to avoid overrides
	Ext.require([
		'CMDBuild.core.constants.Global',
		'CMDBuild.core.Message'
	]);

	/**
	 * Class to be extended in controllers witch implements new CMDBuild pattern where controller creates view
	 *
	 * Usage and wild cards:
	 * 	'=' - creates method alias
	 * 		Ex. 'functionName = aliasFunctionName'
	 * 	'->' - forwards method to sub-controller without a value return (sub-controller could be also multiple as list separated by commas)
	 * 		Ex. 'functionName -> controllerOne, controllerTwo, controllerThree, ...'
	 *
	 * Default managed functions:
	 * 	- identifierGet: only if identifier property is configured
	 *
	 * @abstract
	 */
	Ext.define('CMDBuild.controller.common.abstract.Base', {

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * Array of controller managed function
		 *
		 * @cfg {Array}
		 *
		 * @abstract
		 */
		cmfgCatchedFunctions: [],

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * Map to bind string to functions names
		 *
		 * @property {Object}
		 *
		 * @private
		 */
		stringToFunctionNameMap: {},

		/**
		 * @property {Object}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 */
		constructor: function(configurationObject) {
			this.stringToFunctionNameMap = {};

			Ext.apply(this, configurationObject); // Apply configuration to class

			// Apply default managed functions
			if (!Ext.isEmpty(this.identifier))
				this.cmfgCatchedFunctions.push('identifierGet');

			this.decodeCatchedFunctionsArray();
		},

		/**
		 * Default implementation of Controller Managed Functions Gatherer (CMFG), should be the only access point to class functions
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 */
		cmfg: function(name, param, callBack) {
			if (
				!Ext.isEmpty(name)
				&& Ext.isArray(this.cmfgCatchedFunctions)
				&& this.stringToFunctionNameMap.hasOwnProperty(name)
				&& !Ext.isEmpty(this.stringToFunctionNameMap[name])
			) {
				// Normal function manage
				if (Ext.isString(this.stringToFunctionNameMap[name]) && Ext.isFunction(this[this.stringToFunctionNameMap[name]]))
					return this[this.stringToFunctionNameMap[name]](param, callBack);

				// Wildcard manage
				if (Ext.isObject(this.stringToFunctionNameMap[name])) {
					switch (this.stringToFunctionNameMap[name].action) {
						// Forwarded function manage with multiple controller forwarding management (doesn't return values)
						case 'forward': {
							if (Ext.isArray(this.stringToFunctionNameMap[name].target))
								Ext.Array.forEach(this.stringToFunctionNameMap[name].target, function(controller, i, allControllers) {
									if (Ext.isEmpty(this[controller])) {
										_warning('undefined class property "this.' + controller + '"', this);
									} else {
										this[controller].cmfg(name, param, callBack); // Use cmfg() access point to manage aliases
									}
								}, this);

							return;
						}
					}
				}
			}

			// If function is not managed from this controller forward to parentDelegate
			if (!Ext.isEmpty(this.parentDelegate) && Ext.isFunction(this.parentDelegate.cmfg))
				return this.parentDelegate.cmfg(name, param, callBack);

			_warning('unmanaged function with name "' + name + '"', this);
		},

		/**
		 * Decodes array string inline tags (forward: '->', alias: '=')
		 *
		 * @private
		 */
		decodeCatchedFunctionsArray: function() {
			Ext.Array.forEach(this.cmfgCatchedFunctions, function(managedFnString, i, allManagedFnString) {
				if (Ext.isString(managedFnString)) {
					// Forward inline tag
					if (managedFnString.indexOf('->') >= 0) {
						var splittedString = managedFnString.split('->');

						if (splittedString.length == 2 && Ext.String.trim(splittedString[0]).indexOf(' ') < 0) {
							var targetsArray = Ext.String.trim(splittedString[1]).split(',');

							Ext.Array.forEach(targetsArray, function(controller, i, allControllers) {
								targetsArray[i] = Ext.String.trim(controller);
							}, this);

							this.stringToFunctionNameMap[Ext.String.trim(splittedString[0])] = {
								action: 'forward',
								target: targetsArray
							};
						}
					}

					// Alias inline tag
					if (managedFnString.indexOf('=') >= 0) {
						var splittedString = managedFnString.split('=');

						this.stringToFunctionNameMap[Ext.String.trim(splittedString[0])] = Ext.String.trim(splittedString[0]); // Main function

						// Build aliases binds
						if (splittedString.length == 2 && Ext.String.trim(splittedString[0]).indexOf(' ') < 0) {
							var aliasesArray = Ext.String.trim(splittedString[1]).split(',');

							Ext.Array.forEach(aliasesArray, function(alias, i, allAliases) {
								this.stringToFunctionNameMap[Ext.String.trim(alias)] = Ext.String.trim(splittedString[0]);
							}, this);
						}
					}

					// Plain string
					var trimmedString = Ext.String.trim(managedFnString);

					if (trimmedString.indexOf(' ') < 0)
						this.stringToFunctionNameMap[trimmedString] = trimmedString;
				}
			}, this);
		},

		/**
		 * @returns {String}
		 */
		getBaseTitle: function() {
			if (!Ext.isEmpty(this.view) && !Ext.isEmpty(this.view.baseTitle))
				return this.view.baseTitle;

			return '';
		},

		/**
		 * @return {Object}
		 */
		getView: function() {
			return this.view;
		},

		/**
		 * @returns {String or null}
		 */
		identifierGet: function() {
			if (!Ext.isEmpty(this.identifier))
				return this.identifier;

			return null;
		},

		/**
		 * Method to manage module initialization (to localize)
		 *
		 * @abstract
		 */
		onModuleInit: Ext.emptyFn,

		/**
		 * Property manage methods
		 *
		 * Parameters in a single object to be compatible with cmfg functions.
		 * These methods operates only with local variables (this...) hasn't able to manage other classe's variables. A good implementation with cmfg functionalities
		 * is to use these method's alieas.
		 */
			/**
			 * @param {Object} parameters
			 * @param {Array or String} parameters.attributePath
			 * @param {String} parameters.targetVariableName
			 *
			 * @returns {Mixed} full model object or single property
			 */
			propertyManageGet: function(parameters) {
				if (
					!Ext.Object.isEmpty(parameters)
					&& !Ext.isEmpty(parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME]) && Ext.isString(parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME])
				) {
					var attributePath = undefined;
					var requiredAttribute = this[parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME]];

					// AttributePath variable setup (only Array or String are managed)
					if (!Ext.isEmpty(parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH]) && Ext.isArray(parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH])) {
						attributePath = parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH];
					} else if (!Ext.isEmpty(parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH]) && Ext.isString(parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH])) {
						attributePath = [parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH]];
					}

					if (!Ext.isEmpty(attributePath) && Ext.isArray(attributePath))
						Ext.Array.forEach(attributePath, function(attributeName, i, allAttributeNames) {
							if (
								!Ext.isEmpty(attributeName) && Ext.isString(attributeName)
								&& !Ext.isEmpty(requiredAttribute) && Ext.isObject(requiredAttribute) && Ext.isFunction(requiredAttribute.get)
							) {
								requiredAttribute = requiredAttribute.get(attributeName);
							}
						}, this);

					return requiredAttribute;
				}

				_error('malformed propertyManageGet parameters', this);
			},

			/**
			 * @param {Object} parameters
			 * @param {Array or String} parameters.attributePath
			 * @param {String} parameters.targetVariableName
			 *
			 * @returns {Boolean}
			 */
			propertyManageIsEmpty: function(parameters) {
				var requiredValue = this.propertyManageGet(parameters);

				if (Ext.isObject(requiredValue) && Ext.isFunction(requiredValue.getData)) { // Model manage
					var result = true;

					Ext.Object.each(requiredValue.getData(), function(key, value, myself) {
						result = Ext.isEmpty(value);

						return result;
					}, this);

					return result;
				} else if (Ext.isObject(requiredValue)) { // Simple object manage
					return Ext.Object.isEmpty(requiredValue);
				}

				// Other variable types manage
				return Ext.isEmpty(requiredValue);
			},

			/**
			 * @param {String} targetVariableName
			 */
			propertyManageReset: function(targetVariableName) {
				if (!Ext.isEmpty(targetVariableName) && Ext.isString(targetVariableName))
					this[targetVariableName] = null;
			},

			/**
			 * @param {Object} parameters
			 * @param {String} parameters.modelName
			 * @param {String} parameters.targetVariableName
			 * @param {Object} parameters.value
			 *
			 * @returns {Mixed}
			 */
			propertyManageSet: function(parameters) {
				if (
					!Ext.Object.isEmpty(parameters)
					&& !Ext.isEmpty(parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME]) && Ext.isString(parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME])
					&& !Ext.isEmpty(parameters[CMDBuild.core.constants.Proxy.MODEL_NAME])
					&& !Ext.isEmpty(parameters[CMDBuild.core.constants.Proxy.VALUE])
				) {
					var modelName = parameters[CMDBuild.core.constants.Proxy.MODEL_NAME];
					var value = parameters[CMDBuild.core.constants.Proxy.VALUE];

					// Single property management
					if (!Ext.isEmpty(parameters[CMDBuild.core.constants.Proxy.PROPERTY_NAME]) && Ext.isString(parameters[CMDBuild.core.constants.Proxy.PROPERTY_NAME])) {
						// Create empty model if not existing (or is not a model class)
						if (
							Ext.isEmpty(this[parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME]])
							|| Ext.getClassName(this[parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME]]) != modelName
						) {
							this[parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME]] = Ext.create(modelName);
						}

						return this[parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME]].set(parameters[CMDBuild.core.constants.Proxy.PROPERTY_NAME], value);
					} else if (!Ext.isEmpty(value) && Ext.isObject(value)) { // Full object management
						if (Ext.getClassName(value) == modelName) {
							this[parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME]] = value;
						} else {
							this[parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME]] = Ext.create(modelName, value);
						}
					}
				}
			},

		/**
		 * Setup view panel title as a breadcrumbs component joining array items with titleSeparator.
		 *
		 * @param {Array or String} titlePart
		 */
		setViewTitle: function(titlePart) {
			if (Ext.isEmpty(titlePart))
				titlePart = [];

			titlePart = Ext.isArray(titlePart) ? titlePart : [titlePart];

			if (!Ext.isEmpty(this.view)) {
				if (Ext.isEmpty(titlePart)) {
					this.view.setTitle(this.getBaseTitle());
				} else {
					this.view.setTitle(
						this.getBaseTitle() + CMDBuild.core.constants.Global.getTitleSeparator() + titlePart.join(CMDBuild.core.constants.Global.getTitleSeparator())
					);
				}
			}
		},

		/**
		 * Validation input form
		 *
		 * @param {Ext.form.Panel} form
		 * @param {Boolean} showPopup - enable popup error message
		 *
		 * @return {Boolean}
		 */
		validate: function(form, showPopup) {
			showPopup = Ext.isBoolean(showPopup) ? showPopup : true;

			var invalidFieldsArray = form.getNonValidFields();

			// Check for invalid fields and builds errorMessage
			if (!Ext.isEmpty(form) && !Ext.isEmpty(invalidFieldsArray)) {
				var errorMessage = CMDBuild.Translation.errors.invalid_fields + '<ul style="text-align: left;">';

				for (index in invalidFieldsArray)
					errorMessage += '<li>' + invalidFieldsArray[index].getFieldLabel() + '</li>';

				errorMessage += '<ul>';

				if (showPopup)
					CMDBuild.core.Message.error(CMDBuild.Translation.common.failure, errorMessage, false);

				return false;
			}

			return true;
		}
	});

})();