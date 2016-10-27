(function () {

	Ext.define('CMDBuild.override.form.field.Hidden', {
		override: 'Ext.form.field.Hidden',

		/**
		 * 08/07/2013
		 *
		 *	@param {Mixed} value
		 *
		 * @returns {Boolean}
		 */
		validateValue: function (value) {
			if (this.allowBlank === false)
				return (value.length > 0);

			return true;
		}
	});

})();
