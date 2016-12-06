(function() {

	Ext.define('CMDBuild.override.form.field.Display', {
		override: 'Ext.form.field.Display',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Utils'
		],

		/**
		 * @cfg {Boolean}
		 */
		allowBlank: true,

		/**
		 * Validate also display field
		 *
		 * @returns {Boolean}
		 *
		 * @override
		 */
		isValid: function() {
			if (this.allowBlank)
				return true;

			return !Ext.isEmpty(this.getValue());
		},

		/**
		 * Avoids Display fields to strip \n on contents
		 *
		 * @param {Mixed} rawValue
		 * @param {Mixed} fieldObject
		 *
		 * @return {String}
		 */
		renderer: function(rawValue, fieldObject) {
			if (!CMDBuild.core.Utils.hasHtmlTags(rawValue))
				return rawValue.replace(/(\r\n|\n|\r)/gm, '<br />');

			return rawValue;
		},

		/**
		 * @param {Object or String} value
		 *
		 * @returns {Void}
		 */
		setValue: function (value) {
			value = Ext.isObject(value) && Ext.Object.isEmpty(value) ? '' : value;

			// ADAPTER: attributes like lookup and reference that value is an object like { id: '', description: '' }
			if (Ext.isObject(value) && !Ext.Object.isEmpty(value))
				if (!Ext.isEmpty(value[CMDBuild.core.constants.Proxy.DESCRIPTION])) {
					value = value[CMDBuild.core.constants.Proxy.DESCRIPTION];
				} else if (!Ext.isEmpty(value[CMDBuild.core.constants.Proxy.CODE])) {
					value = value[CMDBuild.core.constants.Proxy.CODE];
				} else if (!Ext.isEmpty(value[CMDBuild.core.constants.Proxy.ID])) {
					value = value[CMDBuild.core.constants.Proxy.ID];
				}


			this.callParent([value]);
		}
	});

})();