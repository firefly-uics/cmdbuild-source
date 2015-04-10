(function() {

	Ext.define('CMDBuild.view.management.common.tabs.email.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.common.tabs.email.Email'
		],

		mixins: {
			panelFunctions: 'CMDBuild.view.common.PanelFunctions'
		},

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.email.Grid}
		 */
		delegate: undefined,

		autoScroll: true,
		border: false,
		collapsible: false,
		frame: false,

		initComponent: function() {
			var me = this;

			Ext.apply(this, {
				dockedItems: [
					{
						xtype: 'toolbar',
						dock: 'top',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,

						items: [
							{
								iconCls: 'add',
								text: CMDBuild.Translation.composeEmail,
								disabled: (
									this.delegate.cmfg('configurationGet')[CMDBuild.core.proxy.CMProxyConstants.READ_ONLY]
									|| !this.delegate.cmfg('editModeGet')
								),

								handler: function(button, e) {
									me.delegate.cmfg('onGridAddEmailButtonClick');
								}
							},
							{
								iconCls: 'x-tbar-loading',
								text: CMDBuild.Translation.regenerateEmail,
								disabled: (
									this.delegate.cmfg('configurationGet')[CMDBuild.core.proxy.CMProxyConstants.READ_ONLY]
									|| !this.delegate.cmfg('editModeGet')
								),

								handler: function(button, e) {
									Ext.Msg.show({ // Ask to the user if is sure to delete all the unsent e-mails before
										title: CMDBuild.Translation.common.confirmpopup.title,
										msg: CMDBuild.Translation.emailRegenerationConfirmPopupText,
										buttons: Ext.Msg.OKCANCEL,
										icon: Ext.Msg.WARNING,

										fn: function(btn) {
											if (btn == 'ok')
												me.delegate.cmfg('onGlobalRegenerationButtonClick');
										}
									});
								}
							}
						]
					}
				],
				columns: [
					{
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.STATUS,
						hidden: true
					},
					{
						text: CMDBuild.Translation.archivingDate,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.DATE,
						flex: 1
					},
					{
						text: CMDBuild.Translation.from,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.FROM,
						flex: 1
					},
					{
						text: CMDBuild.Translation.to,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.TO,
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
									this.delegate.cmfg('onGridRegenerationEmailButtonClick', record);
								},

								isDisabled: function(grid, rowIndex, colIndex, item, record) {
									return (
										this.delegate.cmfg('configurationGet')[CMDBuild.core.proxy.CMProxyConstants.READ_ONLY]
										|| !this.delegate.cmfg('editModeGet')
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

								handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmfg('onGridReplyEmailButtonClick', record);
								},

								isDisabled: function(grid, rowIndex, colIndex, item, record) {
									return (
										this.delegate.cmfg('configurationGet')[CMDBuild.core.proxy.CMProxyConstants.READ_ONLY]
										|| !this.delegate.cmfg('editModeGet')
										|| this.delegate.recordIsEditable(record)
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
								icon: 'images/icons/email_go.png',
								tooltip: CMDBuild.Translation.send,
								scope: this,

								handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmfg('onGridSendEmailButtonClick', record);
								},

								isDisabled: function(grid, rowIndex, colIndex, item, record) {
									return (
										this.delegate.cmfg('configurationGet')[CMDBuild.core.proxy.CMProxyConstants.READ_ONLY]
										|| !this.delegate.cmfg('editModeGet')
										|| !this.delegate.recordIsSendable(record)
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
								icon: 'images/icons/modify.png',
								tooltip: CMDBuild.Translation.edit,
								scope: this,

								handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmfg('onGridEditEmailButtonClick', record);
								},

								isDisabled: function(grid, rowIndex, colIndex, item, record) {
									return (
										this.delegate.cmfg('configurationGet')[CMDBuild.core.proxy.CMProxyConstants.READ_ONLY]
										|| !this.delegate.cmfg('editModeGet')
										|| !this.delegate.recordIsEditable(record)
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
								icon: 'images/icons/zoom.png',
								tooltip: CMDBuild.Translation.view,
								scope: this,

								handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmfg('onGridViewEmailButtonClick', record);
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
									this.delegate.cmfg('onGridDeleteEmailButtonClick', record);
								},

								isDisabled: function(grid, rowIndex, colIndex, item, record) {
									return (
										this.delegate.cmfg('configurationGet')[CMDBuild.core.proxy.CMProxyConstants.READ_ONLY]
										|| !this.delegate.cmfg('editModeGet')
										|| !this.delegate.recordIsEditable(record)
									);
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
				store: CMDBuild.core.proxy.common.tabs.email.Email.getStore()
			});

			this.callParent(arguments);
		},

		listeners: {
			itemdblclick: function(grid, record, item, index, e, eOpts) {
				this.delegate.cmfg('onGridItemDoubleClick', record);
			}
		}
	});

})();