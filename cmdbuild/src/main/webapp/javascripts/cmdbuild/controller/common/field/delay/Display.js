(function() {

	Ext.define('CMDBuild.controller.common.field.delay.Display', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'translateValue'
		],

		/**
		 * @property {CMDBuild.view.common.field.delay.Display}
		 */
		view: undefined,

		/**
		 * Use Delay field to translate values
		 *
		 * @param {Number} value
		 *
		 * @return {String}
		 */
		translateValue: function(value) {
			return Ext.create('CMDBuild.view.common.field.delay.Delay', {
				value: value,
			}).getRawValue();
		}

	});

})();