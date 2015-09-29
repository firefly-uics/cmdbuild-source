(function() {

	Ext.define('CMDBuild.view.management.dataView.sql.FormPanel', {
		extend: 'Ext.tab.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.management.dataView.Sql}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.management.dataView.sql.tabs.CardPanel}
		 */
		cardPanel: undefined,

		bodyCls: 'x-panel-body-default-framed cmbordertop',
		border: false,
		frame: false,

		initComponent: function() {
			Ext.apply(this, {
				items: [
					this.cardPanel = Ext.create('CMDBuild.view.management.dataView.sql.tabs.CardPanel', { delegate: this.delegate }),
					Ext.create('Ext.panel.Panel', {
						title: CMDBuild.Translation.management.modcard.tabs.detail,
						border: false,
						disabled: true
					}),
					Ext.create('Ext.panel.Panel', {
						title: CMDBuild.Translation.management.modcard.tabs.notes,
						border: false,
						disabled: true
					}),
					Ext.create('Ext.panel.Panel', {
						title: CMDBuild.Translation.management.modcard.tabs.relations,
						border: false,
						disabled: true
					}),
					Ext.create('Ext.panel.Panel', {
						title: CMDBuild.Translation.management.modcard.tabs.history,
						border: false,
						disabled: true
					}),
					Ext.create('Ext.panel.Panel', {
						title: CMDBuild.Translation.management.modcard.tabs.attachments,
						border: false,
						disabled: true
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();