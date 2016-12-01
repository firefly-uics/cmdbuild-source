(function () {

	Ext.require([
		'CMDBuild.core.interfaces.messages.Error',
		'CMDBuild.core.interfaces.messages.Warning'
	]);

	Ext.define('CMDBuild.override.data.Store', {
		override: 'Ext.data.Store',

		/**
		 * Creates callback interceptor to print error message on store load - 02/10/2015
		 *
		 * @param {Array} records
		 * @param {Ext.data.Operation} operation
		 * @param {Boolean} success
		 *
		 * @returns {Boolean}
		 *
		 * @private
		 */
		callbackInterceptor: function (records, operation, success) {
			var decodedResponse = {};

			if (!Ext.isEmpty(operation) && !Ext.isEmpty(operation.response) && !Ext.isEmpty(operation.response.responseText))
				decodedResponse = CMDBuild.core.interfaces.Ajax.decodeJson(operation.response.responseText);

			if (Ext.isFunction(CMDBuild.global.interfaces.Configurations.get) && !CMDBuild.global.interfaces.Configurations.get('disableAllMessages')) {
				if (!CMDBuild.global.interfaces.Configurations.get('disableWarnings'))
					CMDBuild.core.interfaces.messages.Warning.display(decodedResponse);

				if (!CMDBuild.global.interfaces.Configurations.get('disableErrors'))
					CMDBuild.core.interfaces.messages.Error.display(decodedResponse, operation.request);
			}

			return true;
		},

		/**
		 * Creates callback interceptor to print error message on store load - 02/10/2015
		 *
		 * @param {Object} options
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		load: function (options) {
			options = Ext.isObject(options) ? options : {};
			options.callback = Ext.isFunction(options.callback) ? options.callback : Ext.emptyFn;

			Ext.ns('CMDBuild.global.Data');
			Ext.ns('CMDBuild.global.interfaces.Configurations');

			if (Ext.isFunction(CMDBuild.global.Data.dataDefaultHeadersUpdate))
				CMDBuild.global.Data.dataDefaultHeadersUpdate();

			options.callback = Ext.Function.createInterceptor(options.callback, this.callbackInterceptor, this);

			this.callParent([options]);
		}
	});

})();
