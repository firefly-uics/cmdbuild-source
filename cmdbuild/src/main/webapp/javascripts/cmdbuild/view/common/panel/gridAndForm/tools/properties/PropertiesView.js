(function () {

	Ext.define('CMDBuild.view.common.panel.gridAndForm.tools.properties.PropertiesView', {
		extend: 'Ext.panel.Tool',

		tooltip: CMDBuild.Translation.properties,
		type: 'properties',

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				menu: Ext.create('Ext.menu.Menu')
			});

			if (this.delegate.withSpacer)
				Ext.apply(this, {
					style: { // Emulation of spacer
						margin: '0px 5px 0px 0px'
					}
				});

			this.callParent(arguments);
		},

		/**
		 * @param {Ext.EventObject} event
		 * @param {Ext.Element} toolEl
		 * @param {Ext.panel.Header} owner
		 * @param {Ext.panel.Tool} tool
		 *
		 * @returns {Void}
		 */
		handler: function (event, toolEl, owner, tool) {
			this.delegate.cmfg('panelGridAndFormtoolsPropertiesUpdateAndShow');
		}
	});

})();