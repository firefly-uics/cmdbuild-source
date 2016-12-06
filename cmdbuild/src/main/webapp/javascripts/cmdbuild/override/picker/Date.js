(function () {

	Ext.define('CMDBuild.override.picker.Date', {
		override: 'Ext.picker.Date',

		/**
		 * Override this methods to be able to handle also the time - 08/07/2013
		 *
		 * @returns {Ext.picker.Date}
		 */
		selectToday: function () {
			var me = this,
				btn = me.todayBtn,
				handler = me.handler;

			if (btn && !btn.disabled) {
				me.setValue(new Date());

				me.fireEvent('select', me, me.value);

				if (handler)
					handler.call(me.scope || me, me, me.value);

				me.onSelect();
			}

			return me;
		},

		/**
		 * Override this methods to be able to handle also the time - 08/07/2013
		 *
		 * @param {Date} value
		 *
		 * @returns {Ext.picker.Date}
		 */
		setValue: function(value){
			this.value = value;

			return this.update(this.value);
		}
	});

})();
