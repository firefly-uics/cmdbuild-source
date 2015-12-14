(function() {

	Ext.define('CMDBuild.override.form.field.FixCheckbox', {
		override: 'Ext.form.field.Checkbox',

		/**
		 * To fix problem that don't set checkbox value using mixin value property - 17/02/2015
		 */
		constructor: function(cfg) {
			cfg = cfg || {};

			if (Ext.isBoolean(cfg.value))
				cfg.checked = cfg.value;

			this.callParent([cfg]);
		}
	});

})();
