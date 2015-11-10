(function() {

	/**
	 * Use CMDBuild.controller.common.AbstractBasePanelController instead
	 *
	 * @deprecated
	 */
	Ext.define('CMDBuild.controller.common.CMBasePanelController', {
		alternateClassName: 'CMDBuild.controller.CMBasePanelController', // Legacy class name

		/**
		 * @param {Object} view
		 */
		constructor: function(view) {
			this.view = view;
			this.view.on('CM_iamtofront', this.onViewOnFront, this);
		},

		callback: function() {
			CMDBuild.core.LoadMask.hide();
		},

		/**
		 * @param {String} method
		 * @param {Object} args
		 */
		callMethodForAllSubcontrollers: function(method, args) {
			if (!Ext.isEmpty(this.subcontrollers)) {
				for (var item in this.subcontrollers) {
					var subController = this.subcontrollers[item];

					if (subController && typeof subController[method] == 'function')
						subController[method].apply(subController, args);
				}
			}
		},

		/**
		 * @param {Object} parameters
		 */
		onViewOnFront: function(parameters) {
			CMDBuild.log.info('onPanelActivate ' + this.view.title, this, parameters);
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