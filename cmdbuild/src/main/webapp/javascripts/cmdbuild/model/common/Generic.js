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
		}
	});

})();