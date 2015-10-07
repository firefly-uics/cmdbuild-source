(function() {

	Ext.define('CMDBuild.core.buttons.iconized.state.Double', {
		extend: 'Ext.button.Cycle',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {String}
		 */
		state1text: CMDBuild.Translation.disable,

		/**
		 * @cfg {String}
		 */
		state2text: CMDBuild.Translation.enable,

		arrowCls: '', // Disable menu arrow
		showText: true,

		initComponent: function() {
			var me = this;

			Ext.apply(this, {
				menu: {
					items: [
						{
							text: me.state1text,
							iconCls: 'delete',
							clickedStateIdentifier: CMDBuild.core.proxy.CMProxyConstants.ENABLE // Identifier of clicked state
						},
						{
							text: me.state2text,
							iconCls: 'ok',
							clickedStateIdentifier: CMDBuild.core.proxy.CMProxyConstants.DISABLE // Identifier of clicked state
						}
					]
				}
			});

			this.callParent(arguments);
		},

		/**
		 * @param {Boolean} currentState
		 */
		setActiveState: function(currentState) {
			if (Ext.isBoolean(currentState))
				this.setActiveItem(currentState ? this.menu.items.items[0] : this.menu.items.items[1]);
		},

		/**
		 * @returns {String} clickedStateIdentifier
		 */
		getClickedState: function() {
			return this.getActiveItem().clickedStateIdentifier;
		}
	});

})();