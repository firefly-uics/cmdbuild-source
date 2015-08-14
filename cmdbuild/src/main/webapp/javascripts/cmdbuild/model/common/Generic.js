(function() {

	/**
	 * Plain generic model adapted to data object
	 */
	Ext.define('CMDBuild.model.common.Generic', {
		extend: 'Ext.data.Model',

		fields:[],

		/**
		 * @param {Object} data
		 */
		constructor: function(data) {
			data = data || {};

			this.self.setFields(Ext.Object.getKeys(data));

			this.callParent(arguments);
		},

		/**
		 * Override to get real data values because of a strange behaviour that for multiple model instances on getData returns only fields that where setup on last instance
		 *
		 * @returns {Object}
		 *
		 * @override
		 */
		getData: function() {
			return this.data;
		}
	});

})();