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
						title: CMDBuild.Translation.detail,
						border: false,
						disabled: true
					}),
					Ext.create('Ext.panel.Panel', {
						title: CMDBuild.Translation.notes,
						border: false,
						disabled: true
					}),
					Ext.create('Ext.panel.Panel', {
						title: CMDBuild.Translation.relations,
						border: false,
						disabled: true
					}),
					Ext.create('Ext.panel.Panel', {
						title: CMDBuild.Translation.history,
						border: false,
						disabled: true
					}),
					Ext.create('Ext.panel.Panel', {
						title: CMDBuild.Translation.attachments,
						border: false,
						disabled: true
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();