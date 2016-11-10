(function () {

	Ext.define('CMDBuild.core.plugin.SetValueOnLoad', {
		extend: 'Ext.util.Observable',

		/**
		 * @param {Object} field
		 *
		 * @returns {Void}
		 */
		init: function (field) {
			field.valueNotFoundText = '';

			if (Ext.isFunction(field.getStore) && !Ext.isEmpty(field.getStore()))
				field.getStore().on('load', function (store, records, successful, eOpts) {
					this.valueNotFoundText = this.initialConfig.valueNotFoundText;

					if (this.getStore()) // Store is null if the field is not rendered
						this.setValue(this.getValue());
				}, field);
		}
	});

})();
