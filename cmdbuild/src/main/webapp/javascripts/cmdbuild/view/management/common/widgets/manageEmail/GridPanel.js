(function() {

	Ext.define('CMDBuild.view.management.common.widgets.manageEmail.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.widgets.manageEmail.ManageEmail'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.manageEmail.Grid}
		 */
		delegate: undefined,

		/**
		 * @cfg {Boolean}
		 */
		readOnly: undefined,

		autoScroll: true,
		border: false,
		collapsible: false,
		frame: false,

		initComponent: function() {
			var me = this;

			Ext.apply(this, {
				tbar: [
					{
						iconCls: 'add',
						text: CMDBuild.Translation.composeEmail,
						disabled: this.readOnly,
						scope: this,

						handler: function(values) {
							this.delegate.cmOn('onGridAddEmailButtonClick');
						}
					},
					{
						iconCls: 'x-tbar-loading',
						text: CMDBuild.Translation.regenerateEmail,
						disabled: this.readOnly,
						scope: this,

						handler: function(button, e) {
							Ext.Msg.show({ // Ask to the user if is sure to delete all the unsent e-mails before
								title: CMDBuild.Translation.common.confirmpopup.title,
								msg: CMDBuild.Translation.emailRegenerationConfirmPopupText,
								buttons: Ext.Msg.OKCANCEL,
								icon: Ext.Msg.WARNING,

								fn: function(btn) {
									if (btn == 'ok')
										me.delegate.cmOn('onGlobalRegenerationButtonClick');
								}
							});
						}
					}
				],
				columns: [
					{
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.STATUS,
						hidden: true
					},
					{
						text: CMDBuild.Translation.archivingDate,
						sortable: true,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.DATE,
						flex: 1
					},
					{
						text: CMDBuild.Translation.address,
						sortable: false,
						scope: this,
						renderer: this.addressRenderer,
						flex: 1
					},
					{
						text: CMDBuild.Translation.subject,
						sortable: false,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.SUBJECT,
						flex: 1
					},
					{
						sortable: false,
						scope: this,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.BODY,
						menuDisabled: true,
						hideable: false,
						renderer: 'stripTags',
						flex: 2
					},
					{
						xtype: 'actioncolumn',
						align: 'center',
						width: 25,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,
						items: [
							{
								icon: 'images/icons/refresh.gif',
								tooltip: CMDBuild.Translation.manualRegeneration,
								scope: this,

								handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmOn('onGridRegenerationEmailButtonClick', record);
								},

								isDisabled: function(grid, rowIndex, colIndex, item, record) {
									return (
										this.readOnly
										|| !this.delegate.recordIsEditable(record)
										|| !this.delegate.isRegenerable(record)
										|| !record.get(CMDBuild.core.proxy.CMProxyConstants.KEEP_SYNCHRONIZATION)
									);
								}
							}
						]
					},
					{
						xtype: 'actioncolumn',
						align: 'center',
						width: 25,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,
						items: [
							{
								icon: 'images/icons/reply.png',
								tooltip: CMDBuild.Translation.reply,
								scope: this,

								isDisabled: function(grid, rowIndex, colIndex, item, record) {
									return this.readOnly || this.delegate.recordIsEditable(record);
								},

								handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmOn('onGridReplyEmailButtonClick', record);
								}
							}
						]
					},
					{
						xtype: 'actioncolumn',
						align: 'center',
						width: 25,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,
						items: [
							{
								icon: 'images/icons/email_go.png',
								tooltip: CMDBuild.Translation.send,
								scope: this,

								handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmOn('onGridSendEmailButtonClick', record);
								},

								isDisabled: function(grid, rowIndex, colIndex, item, record) {
									return !this.delegate.recordIsSendable(record);
								}
							}
						]
					},
					{
						xtype: 'actioncolumn',
						align: 'center',
						width: 25,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,
						items: [
							{
								icon: 'images/icons/modify.png',
								tooltip: CMDBuild.Translation.edit,
								scope: this,

								handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmOn('onGridEditEmailButtonClick', record);
								},

								isDisabled: function(grid, rowIndex, colIndex, item, record) {
									return !this.delegate.recordIsEditable(record) || this.readOnly;
								}
							}
						]
					},
					{
						xtype: 'actioncolumn',
						align: 'center',
						width: 25,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,
						items: [
							{
								icon: 'images/icons/zoom.png',
								tooltip: CMDBuild.Translation.view,
								scope: this,

								handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmOn('onGridViewEmailButtonClick', record);
								}
							}
						]
					},
					{
						xtype: 'actioncolumn',
						align: 'center',
						width: 25,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,
						items: [
							{
								icon: 'images/icons/cross.png',
								tooltip: CMDBuild.Translation.deleteLabel,
								scope: this,

								handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmOn('onGridDeleteEmailButtonClick', record);
								},

								isDisabled: function(grid, rowIndex, colIndex, item, record) {
									return !this.delegate.recordIsEditable(record) || this.readOnly;
								}
							}
						]
					}
				],
				features: [
					{
						ftype: 'groupingsummary',
						groupHeaderTpl: [
							'{name:this.formatName}',
							{
								formatName: function(name) { // TODO: use plain translation without emailLookupNames
									return CMDBuild.Translation.emailLookupNames[name];
								}
							}
						],
						hideGroupedHeader: true,
						enableGroupingMenu: false
					}
				],
				store: CMDBuild.core.proxy.widgets.manageEmail.ManageEmail.getStore()
			});

			this.callParent(arguments);
		},

		listeners: {
			itemdblclick: function(grid, record, item, index, e, eOpts) {
				this.delegate.cmOn('onGridItemDoubleClick', record);
			}
		},

		// Column renderers
			/**
			 * @param {Mixed} value
			 * @param {Object} metaData
			 * @param {CMDBuild.model.widget.ManageEmail.email} record
			 *
			 * @return {String}
			 */
			addressRenderer: function(value, metadata, record) {
				if (this.delegate.recordIsReceived(record)) {
					return record.get(CMDBuild.core.proxy.CMProxyConstants.FROM);
				} else {
					return record.get(CMDBuild.core.proxy.CMProxyConstants.TO);
				}
			}
	});

})();