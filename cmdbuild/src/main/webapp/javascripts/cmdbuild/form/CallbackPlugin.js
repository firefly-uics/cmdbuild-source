Ext.ns('CMDBuild');

CMDBuild.CallbackPlugin = function(config) {
    Ext.apply(this, config);
};

Ext.extend(CMDBuild.CallbackPlugin, Ext.util.Observable, {
    init: function(formPanel) {
    	var basicForm = formPanel.getForm();
		function addErrorHandling(o) {
			// add a callback after form submit
			if (o.callback) {
				if (o.failure)
					o.failure = o.failure.createSequence(o.callback, o.scope);
				else
					o.failure = o.callback;
				if (o.success)
					o.success = o.success.createSequence(o.callback, o.scope);
				else
					o.success = o.callback;
			}
		};
		basicForm.submit = basicForm.submit.createInterceptor(addErrorHandling, this);
		basicForm.load = basicForm.load.createInterceptor(addErrorHandling, this);
    }
});