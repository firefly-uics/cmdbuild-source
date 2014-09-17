(function() {

	Ext.define('CMDBuild.model.widget.ModelLinkCards', {
		extend: 'Ext.util.Observable',

		/**
		 * @param {Object} config - { singleSelect: true/false }
		 */
		constructor: function(config) {
			config = config || {};

			this.selections = {};
			this._freezed = {};
			this.singleSelect = config.singleSelect;

			this.addEvents({
				'select': true,
				'deselect': true
			});

			this.callParent(arguments);
		},

		/**
		 * @param {Int} selection - card id
		 */
		select: function(selection) {
			if (!this._silent) {
				if (this.isSelected(selection)) {
					return;
				} else {
					if (this.singleSelect)
						this.reset();

					this.selections[selection] = true;
					this.fireEvent('select', selection);
				}
			}
		},

		/**
		 * @param {Int} selection - card id
		 */
		deselect: function(selection) {
			if (!this._silent) {
				if (this.isSelected(selection)) {
					delete this.selections[selection];
					this.fireEvent('deselect', selection);
				}
			}
		},

		/**
		 * @param {Array} selections
		 */
		getSelections: function() {
			var selections = [];

			for (var selection in this.selections)
				selections.push(selection);

			return selections;
		},

		/**
		 * @param {Int} selection - card id
		 */
		isSelected: function(selection) {
			return this.selections[selection];
		},

		freeze: function() {
			this._freezed = Ext.apply({}, this.selections);
		},

		defreeze: function() {
			this.selections = Ext.apply({}, this._freezed);
		},

		reset: function() {
			for (var selection in this.selections)
				this.deselect(selection);
		},

		/**
		 * @param {Boolean}
		 */
		hasSelection: function() {
			return this.getSelections().length > 0;
		},

		/**
		 * @param {Int}
		 */
		length: function() {
			return this.getSelections().length;
		}
	});

})();