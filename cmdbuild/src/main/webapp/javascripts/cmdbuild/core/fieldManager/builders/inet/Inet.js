(function () {

	/**
	 * Specific field attributes:
	 * 		- {String} ipType: ipv4 or ipv6
	 */
	Ext.define('CMDBuild.core.fieldManager.builders.inet.Inet', {
		extend: 'CMDBuild.core.fieldManager.builders.Abstract',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.core.fieldManager.FieldManager}
		 */
		parentDelegate: undefined,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Ext.grid.column.Column or Object}
		 *
		 * @override
		 */
		buildColumn: function (parameters) {
			return this.buildSubFieldClass().buildColumn(parameters);
		},

		/**
		 * @returns {Object}
		 *
		 * @override
		 */
		buildEditor: function () {
			return this.buildSubFieldClass().buildEditor();
		},

		/**
		 * @returns {Object}
		 *
		 * @override
		 */
		buildField: function () {
			return this.buildSubFieldClass().buildField();
		},

		/**
		 * @returns {Object}
		 *
		 * @override
		 */
		buildFieldReadOnly: function () {
			return this.buildSubFieldClass().buildFieldReadOnly();
		},

		/**
		 * @returns {CMDBuild.view.common.field.filter.advanced.configurator.tabs.attributes.ConditionView}
		 *
		 * @override
		 */
		buildFilterCondition: function () {
			return this.buildSubFieldClass().buildFilterCondition();
		},

		/**
		 * @returns {Object}
		 *
		 * @private
		 */
		buildSubFieldClass: function () {
			switch (this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.IP_TYPE)) {
				case 'ipv6':
					return Ext.create('CMDBuild.core.fieldManager.builders.inet.V6', { parentDelegate: this });

				case 'ipv4':
				default:
					return Ext.create('CMDBuild.core.fieldManager.builders.inet.V4', { parentDelegate: this });
			}
		}
	});

})();
