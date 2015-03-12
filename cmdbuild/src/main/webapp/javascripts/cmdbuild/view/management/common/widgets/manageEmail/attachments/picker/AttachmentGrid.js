(function() {

	Ext.define('CMDBuild.view.management.common.widgets.manageEmail.attachments.picker.AttachmentGrid', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.widgets.ManageEmail'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.manageEmail.AttachmentsPicker}
		 */
		delegate: undefined,

		border: false,
		split: true,

		initComponent: function() {
			Ext.apply(this, {
				columns: [
					{
						xtype: 'checkcolumn',
						dataIndex: 'Checked', // TODO
						width: 40,

						listeners: {
							scope: this,
							checkchange: function(column, rowIndex, checked, eOpts) { // TODO probabilmente sarà inutile perchè prendo quelli checcati e li spedisco
								var record = this.getStore().getAt(rowIndex);

								if (!Ext.isEmpty(record))
									this.cmOn('onPickerWindowAttachmentGridCheckChange', {
										checked: checked,
										fileName: record.get('Filename')
									});
							}
						}
					},
					{
						text: CMDBuild.Translation.fileName,
						dataIndex: 'Filename', // TODO
						flex: 1
					},
					{
						text: CMDBuild.Translation.descriptionLabel,
						dataIndex: 'Description', // TODO
						flex: 1
					}
				],
				store: CMDBuild.core.proxy.widgets.ManageEmail.attachmentGetStore()
			});

			this.callParent(arguments);
		}

	});

})();