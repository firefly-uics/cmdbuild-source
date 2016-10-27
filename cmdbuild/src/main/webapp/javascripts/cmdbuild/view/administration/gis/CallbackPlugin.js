(function () {

	/**
	 * @deprecated CMDBuild.core.interfaces.FormSubmit
	 */
	Ext.define('CMDBuild.view.administration.gis.CallbackPlugin', {
		extend: 'Ext.util.Observable',

		/**
		 * @param {Ext.panel.Form} formPanel
		 *
		 * @returns {Void}
		 */
		init: function (formPanel) {
			var basicForm = formPanel.getForm();
			function addErrorHandling(o) {
				// add a callback after form submit
				if (o.callback) {
					if (o.failure)
						o.failure = Ext.Function.createSequence(o.failure, o.callback, o.scope);
					else
						o.failure = o.callback;
					if (o.success)
						o.success = Ext.Function.createSequence(o.success, o.callback, o.scope);
					else
						o.success = o.callback;
				}
			};
			basicForm.submit = Ext.Function.createInterceptor(basicForm.submit, addErrorHandling, this);
			basicForm.load = Ext.Function.createInterceptor(basicForm.load, addErrorHandling, this);
		}
	});

})();
