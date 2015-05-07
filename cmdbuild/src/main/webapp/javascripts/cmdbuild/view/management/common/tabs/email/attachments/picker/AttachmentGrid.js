(function() {

	Ext.define('CMDBuild.view.management.common.tabs.email.attachments.picker.AttachmentGrid', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.proxy.Attachment',
			'CMDBuild.core.proxy.CMProxyConstants'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.email.attachments.Picker}
		 */
		delegate: undefined,

		border: false,
		split: true,

		initComponent: function() {
			Ext.apply(this, {
				columns: [
					{
						text: CMDBuild.Translation.fileName,
						dataIndex: 'Filename', // TODO: use proxy constants
						flex: 1
					},
					{
						text: CMDBuild.Translation.descriptionLabel,
						dataIndex: 'Description', // TODO: use proxy constants
						flex: 1
					}
				],
				selModel: Ext.create('Ext.selection.CheckboxModel', {
					injectCheckbox: 'first'
				}),
				store: CMDBuild.core.proxy.Attachment.getStore()
			});

			this.callParent(arguments);
		}
	});

})();