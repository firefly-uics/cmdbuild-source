(function() {

	Ext.define('CMDBuild.view.common.field.CMIconCombo', {
		extend: 'Ext.form.field.ComboBox',

		iconClsField: 'name', // could be changed on instantiation
		iconClsPrefix: 'ux-flag-', // could be changed on instantiation

		initComponent: function() {
			var me = this;
			var v = me.displayField;
			var pre = me.iconClsPrefix;
			var icon = me.iconClsField;
			var tpl = '<div class="x-combo-list-item ux-icon-combo-item '
					+ pre + '{'
					+ icon + '}">' + '{'
					+ v +'}' + '</div>';

			Ext.apply(this, {
				listConfig: {
					getInnerTpl: function() {return tpl;}
				}
			});

			this.callParent(arguments);

			this.on({
				render:{scope:this, fn:function() {
					this.inputEl.addCls('ux-icon-combo-input ux-icon-combo-item');
				}}
			});

			this.setValue = Ext.Function.createInterceptor(this.setValue, function(v) {
				if (this.lastFlagCls && !Ext.isEmpty(this.inputEl)) {
					this.inputEl.removeCls(this.lastFlagCls);
				}

				this.lastFlagCls = pre + v;

				if (!Ext.isEmpty(this.inputEl))
					this.inputEl.addCls(this.lastFlagCls);
			}, this);
		}
	});

})();