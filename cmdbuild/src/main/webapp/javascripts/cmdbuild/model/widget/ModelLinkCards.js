(function() {

	Ext.define('CMDBuild.model.widget.ModelLinkCards', {
		extend: 'Ext.util.Observable',

		/**
		 * @property {Boolean}
		 */
		_freezed: {},

		/**
		 * @property {Object}
		 * 	{
		 * 		cardId: {
		 * 			metadata1: value1,
		 * 			metadata2: value2,
		 * 			...
		 * 		},
		 * 		...
		 * 	}
		 */
		selections: {},

		/**
		 * @property {Boolean}
		 */
		singleSelect: undefined,

		/**
		 * @param {Object} configuration - { singleSelect: true/false }
		 *
		 * @override
		 */
		constructor: function(configuration) {
			configuration = configuration || {};

			this.singleSelect = configuration[CMDBuild.core.proxy.CMProxyConstants.SINGLE_SELECT];

			this.addEvents({
				'select': true,
				'deselect': true
			});

			this.callParent(arguments);
		},

		/**
		 * @param {Int} selection - card id
		 */
		deselect: function(selection) {
			if (!this._silent)
				if (this.isSelected(selection)) {
					delete this.selections[selection];

					this.fireEvent('deselect', selection);
				}
		},

		defreeze: function() {
			this.selections = Ext.apply({}, this._freezed);
		},

		freeze: function() {
			this._freezed = Ext.apply({}, this.selections);
		},

		/**
		 * @return {Array} selections - each element is a cardId
		 */
		getSelections: function() {
			return this.selections;
		},

		/**
		 * @return {Boolean}
		 */
		hasSelection: function() {
			return this.getSelections().length > 0;
		},

		/**
		 * @param {Int} selection - card id
		 */
		isSelected: function(selection) {
			return this.selections.hasOwnProperty(selection);
		},

		reset: function() {
			for (var selection in this.selections)
				this.deselect(selection);
		},

		/**
		 * @param {Int} selection - card id
		 * @param {Object} metadata
		 */
		select: function(selection, metadata) {
			metadata = metadata || {};

			if (!this._silent && !this.isSelected(selection)) {
				if (this.singleSelect)
					this.reset();

				this.selections[selection] = metadata;

				this.fireEvent('select', selection);
			}
		}
	});

})();