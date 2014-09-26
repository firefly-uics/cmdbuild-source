(function() {

	Ext.define('CMDBuild.model.widget.ModelLinkCards', {
		extend: 'Ext.util.Observable',

		_freezed: {},
		selections: {},
		singleSelect: undefined,

		/**
		 * @param {Object} configuration - { singleSelect: true/false }
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
			if (!this._silent) {
				if (this.isSelected(selection)) {
					delete this.selections[selection];
					this.fireEvent('deselect', selection);
				}
			}
		},

		defreeze: function() {
			this.selections = Ext.apply({}, this._freezed);
		},

		freeze: function() {
			this._freezed = Ext.apply({}, this.selections);
		},

		/**
		 * @return {Array} selections
		 */
		getSelections: function() {
			var selections = [];

			for (var selection in this.selections)
				selections.push(selection);

			return selections;
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
			return this.selections[selection];
		},

		reset: function() {
			for (var selection in this.selections)
				this.deselect(selection);
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
		}
	});

})();