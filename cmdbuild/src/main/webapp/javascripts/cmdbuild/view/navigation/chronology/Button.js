(function() {

	Ext.define('CMDBuild.view.navigation.chronology.Button', {
		extend: 'Ext.button.Split',

		/**
		 * @cfg {CMDBuild.controller.navigation.Chronology}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.menu.Menu}
		 */
		menu: undefined,

		iconCls: 'navigation-chronology',
		text: CMDBuild.Translation.navigationChronology,

		initComponent: function() {
			Ext.apply(this, {
				menu: Ext.create('Ext.menu.Menu'),
				scope: this,

				handler: function(button, e) {
					this.delegate.cmfg('onNavigationChronologyButtonShowMenu');
				}
			});

			this.callParent(arguments);
		}
	});

})();