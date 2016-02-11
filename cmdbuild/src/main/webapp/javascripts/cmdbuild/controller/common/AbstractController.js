(function () {

	Ext.require(['CMDBuild.core.Message']);

	/**
	 * Class to be extended in controllers witch implements new CMDBuild algorithms where controller creates view
	 *
	 * Usage and wild cards:
	 * 	'=' - creates method alias
	 * 		Ex. 'functionName = aliasFunctionName'
	 * 	'->' - forwards method to sub-controller (sub-controller could be also multiple as list separated by commas)
	 * 		Ex. 'functionName -> controllerOne, controllerTwo, controllerThree, ...'
	 *
	 * @abstract
	 */
	Ext.define('CMDBuild.controller.common.AbstractController', {

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
		 * Map to bind string to functions names
		 *
		 * @property {Object}
		 *
		 * @private
		 */
		stringToFunctionNameMap: {},

		/**
		 * @cfg {String}
		 */
		titleSeparator: ' - ',

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

			this.decodeCatchedFunctionsArray();
		},

		/**
		 * Default implementation of controller managed functions gatherer (CMFG), should be the only access point to class functions
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
						// Forwarded function manage with multiple controller forwarding management
						case 'forward': {
							if (Ext.isArray(this.stringToFunctionNameMap[name].target))
								Ext.Array.forEach(this.stringToFunctionNameMap[name].target, function(controller, i, allControllers) {
									this[controller].cmfg(name, param, callBack); // Use cmfg() access point to manage aliases
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
		 * @return {Object}
		 */
		getView: function() {
			return this.view;
		},

		/**
		 * Setup view panel title as a breadcrumbs component
		 *
		 * @param {String} titlePart
		 */
		setViewTitle: function(titlePart) {
			if (
				!Ext.isEmpty(this.view)
				&& !Ext.isEmpty(this.view.baseTitle)
			) {
				if (Ext.isEmpty(titlePart)) {
					this.view.setTitle(this.view.baseTitle);
				} else {
					this.view.setTitle(this.view.baseTitle + this.titleSeparator + titlePart);
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