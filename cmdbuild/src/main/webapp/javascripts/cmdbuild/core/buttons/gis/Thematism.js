(function () {

	/**
	 * TODO: clean button code moving login inside controller classes
	 *
	 * @deprecated CMDBuild.core.buttons.icon.add.Add
	 */
	Ext.define('CMDBuild.core.buttons.gis.Thematism', {
		extend: 'Ext.button.Split',

		iconCls: 'add',
		text: CMDBuild.Translation.thematismTitle,

		callback: undefined,
		interactionDocument: undefined,

		initComponent: function () {
			this.menu = this.getMenuItem();
			this.callParent(arguments);
		},

		add: function (values) {
			var me = this;
			for (var i = 0; i < values.length; i++) {
				this.menu.add([
					{
						text: values[i],
					}
				]);
			}
		},

		removeAll: function () {
			this.menu.removeAll();
		},

		getMenuItem: function () {
			var me = this;
			return new Ext.menu.Menu({
				items: [],
				listeners: {
					click: function (menu, item, e, eOpts) {
						me.callback(item.text);
					}
				}
			});
		}
	});

})();
