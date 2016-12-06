(function () {

	Ext.define('CMDBuild.controller.common.field.grid.localSearch.LocalSearch', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Utils'
		],

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'fieldGridLocalSearchReset',
			'fieldGridLocalSearchTrigger1Click = onFieldGridLocalSearchEnterKeyPress',
			'fieldGridLocalSearchTrigger2Click'
		],

		/**
		 * @property {CMDBuild.view.common.field.grid.localSearch.LocalSearch}
		 */
		view: undefined,

		/**
		 * @returns {Void}
		 */
		fieldGridLocalSearchReset: function (silently) {
			this.view.grid.getStore().clearFilter();

			this.view.setValue();
		},

		/**
		 * @returns {Void}
		 */
		fieldGridLocalSearchTrigger1Click: function () {
			var query = Ext.String.trim(this.view.getValue());

			if (Ext.isString(query) && !Ext.isEmpty(query)) { // Apply action on NON empty filter string
				this.view.grid.getStore().clearFilter();
				this.view.grid.getStore().filterBy(function (record, id) {
					var returnValue = false;

					Ext.Object.each(record.getData(), function (key, value, myself) {
						if (Ext.util.Format.lowercase(value).indexOf(Ext.util.Format.lowercase(query)) >= 0)
							returnValue = true;
					}, this);

					return returnValue;
				}, this);
			} else { // Reset action on empty filter string
				this.cmfg('fieldGridLocalSearchReset');
			}
		},

		/**
		 * @returns {Void}
		 */
		fieldGridLocalSearchTrigger2Click: function () {
			if (!this.view.isDisabled())
				this.cmfg('fieldGridLocalSearchReset');
		}
	});

})();
