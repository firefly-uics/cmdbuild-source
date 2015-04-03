(function () {

	/**
	 * Abstract class to be extended in controllers witch implements new CMDBuild algorithms where controller creates view
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
		 * @property {Object}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 */
		constructor: function(configurationObject) {
			Ext.apply(this, configurationObject); // Apply configuration to class
		},

		/**
		 * Default implementation of controller managed functions gatherer (CMFG)
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 */
		cmfg: function(name, param, callBack) {
			if (
				!Ext.isEmpty(name)
				&& Ext.isArray(this.cmfgCatchedFunctions)
				&& Ext.Array.contains(this.cmfgCatchedFunctions, name)
				&& typeof this[name] == 'function'
			) {
				return this[name](param, callBack);
			}

			// If function is not managed from this controller forward to parentDelegate
			if (!Ext.isEmpty(this.parentDelegate) && typeof this.parentDelegate.cmfg == 'function')
				return this.parentDelegate.cmfg(name, param, callBack);

			_debug('CMDBuild.controller.common.AbstractController WARNING: unmanaged function with name "' + name + '"');
		},

		/**
		 * @return {Object}
		 */
		getView: function() {
			return this.view;
		},

		/**
		 * Validation input form
		 *
		 * @param {Ext.form.Panel} form
		 *
		 * @return {Boolean}
		 */
		validate: function(form) {
			var invalidFieldsArray = form.getNonValidFields();

			// Check for invalid fields and builds errorMessage
			if (!Ext.isEmpty(form) && (invalidFieldsArray.length > 0)) {
				var errorMessage = CMDBuild.Translation.errors.invalid_fields + '<ul style="text-align: left;">';

				for (index in invalidFieldsArray)
					errorMessage += '<li>' + invalidFieldsArray[index].fieldLabel + '</li>';

				errorMessage += '<ul>';

				CMDBuild.Msg.error(CMDBuild.Translation.common.failure, errorMessage, false);

				return false;
			}

			return true;
		}
	});

})();