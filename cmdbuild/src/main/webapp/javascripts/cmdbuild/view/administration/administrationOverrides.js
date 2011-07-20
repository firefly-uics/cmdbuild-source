Ext.override(Ext.form.field.Base, {
	disable: function(silent) {
		var me = this;

		if (me.rendered) {
			me.bodyEl.addCls(me.disabledCls);
			me.el.dom.disabled = true
			me.onDisable();
		}

		me.disabled = true;

		if (silent !== true) {
			me.fireEvent('disable', me);
		}

		return me;
	}
});