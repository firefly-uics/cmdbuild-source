(function () {

	/**
	 * Navigation chronology to store data and handler functions to use stored data and setup UI as selected historic state.
	 */
	Ext.define('CMDBuild.controller.navigation.Chronology', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		mixins: ['CMDBuild.controller.navigation.ButtonHandlers'],

		/**
		 * @property {Array}
		 *
		 * @private
		 */
		records: [],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'navigationChronologyButtonHandler', // From mixins
			'navigationChronologyGet',
			'navigationChronologyIsEmpty',
			'navigationChronologyRecordSave'
		],

		/**
		 * @returns {Array}
		 */
		navigationChronologyGet: function () {
			return this.records;
		},

		/**
		 * @returns {Boolean}
		 */
		navigationChronologyIsEmpty: function () {
			return Ext.isEmpty(this.records);
		},

		/**
		 * @param {Object} parameters
		 * @param {String} parameters.moduleId
		 * @param {String} parameters.entryType - selected entryType
		 * @param {Object} parameters.item - item selected from grid (card, instance, ...)
		 * @param {Object} parameters.section - form tab object
		 * @param {Object} parameters.subSection - form tab sub-section
		 *
		 * @returns {Void}
		 */
		navigationChronologyRecordSave: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};

			// Error handling
				if (!Ext.isString(parameters.moduleId) || Ext.isEmpty(parameters.moduleId))
					return _error('navigationChronologyRecordSave(): unmanaged moduleId parameter', this, parameters.moduleId);

				if (!Ext.isObject(parameters.entryType) || Ext.Object.isEmpty(parameters.entryType))
					return _error('navigationChronologyRecordSave(): unmanaged entryType parameter', this, parameters.entryType);
			// END: Error handling

			var record = Ext.create('CMDBuild.model.navigation.chronology.Record', parameters);

			// Filter double records save
			if (Ext.isEmpty(this.records) || !this.records[0].equals(record))
				this.records.unshift(record);

			// Resize array to referenceComboStoreLimit configuration parameter
			this.records = Ext.Array.slice(this.records, 0, CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.REFERENCE_COMBO_STORE_LIMIT));
		}
	});

})();
