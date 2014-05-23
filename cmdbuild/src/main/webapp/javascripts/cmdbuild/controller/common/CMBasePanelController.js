(function() {

	Ext.define('CMDBuild.controller.common.CMBasePanelController', {
		alternateClassName: 'CMDBuild.controller.CMBasePanelController', // Legacy class name

		/**
		 * @param (Object) view
		 */
		constructor: function(view) {
			this.view = view;
			this.view.on('CM_iamtofront', this.onViewOnFront, this);
		},

		callback: function() {
			CMDBuild.LoadMask.get().hide();
		},

		/**
		 * @param (String) method
		 * @param (Object) args
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
		 * @param (Object) parameters
		 */
		onViewOnFront: function(parameters) {
			CMDBuild.log.info('onPanelActivate ' + this.view.title, this, parameters);
		},

		/**
		 * Validation input form
		 *
		 * @param (Object) form
		 *
		 * @return (Boolean)
		 */
		validate: function(form) {
			// Check for invalid fields
			if (!Ext.isEmpty(form) && (form.getNonValidFields().length > 0)) {
				CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);
				return false;
			}

			return true;
		}
	});

})();