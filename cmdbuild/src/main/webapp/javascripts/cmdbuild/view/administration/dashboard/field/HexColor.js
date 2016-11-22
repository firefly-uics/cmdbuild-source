(function () {

	/**
	 * @deprecated (CMDBuild.view.common.field.picker.Color)
	 */
	Ext.define('CMDBuild.view.administration.dashboard.field.HexColor', {
		extend: 'Ext.form.ColorField',

		editable: false,

		setValue: function (value) {
			if (value && value[0] == '#')
				value = value.slice(1);

			this.callParent(arguments);
		},

		getValue: function () {
			var value = this.value;

			if (value && value[0] != '#')
				value = '#'+value;

			return value;
		}
	});

})();
