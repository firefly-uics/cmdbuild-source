(function() {

	Ext.define('CMDBuild.view.management.common.widgets.manageEmail.Grid', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.widgets.ManageEmail',
			'CMDBuild.model.widget.ManageEmail'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.manageEmail.Grid}
		 */
		delegate: undefined,

		/**
		 * @cfg {Boolean}
		 */
		readOnly: undefined,

		/**
		 * @property {Boolean}
		 */
		storeLoaded: false,

		autoScroll: true,
		border: false,
		collapsible: false,
		frame: false,

		initComponent: function() {
			if (!this.readOnly) {
				var me = this;

				Ext.apply(this, {
					tbar: [
						{
							iconCls: 'add',
							text: '@@ Compose email',

							handler: function(values) {
								me.delegate.cmOn('onEmailAddButtonClick');
							}
						},
						{
							iconCls: 'x-tbar-loading',
							text: '@@ Regenerate e-mails',

							handler: function() {
								// Ask to the user if is sure to delete all the unsent e-mails before
								Ext.Msg.show({
									title: CMDBuild.Translation.common.confirmpopup.title,
									msg: tr.updateTemplateConfirm,
									buttons: Ext.Msg.OKCANCEL,
									icon: Ext.Msg.WARNING,

									fn: function(btn) {
										if (btn == 'ok')
											me.delegate.cmOn('onGlobalRegenerationButtonClick');
									}
								});
							}
						}
					]
				});
			}

			Ext.apply(this, {
				columns: [
					{
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.ID,
						hidden: true
					},
					{
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.ACCOUNT,
						hidden: true
					},
					{
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX,
						hidden: true
					},
					{
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.STATUS,
						hidden: true,
						sortable: true
					},
					{
						header: '@@ Archiving date',
						sortable: true,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.DATE,
						flex: 1
					},
					{
						header: CMDBuild.Translation.address,
						sortable: false,
						scope: this,
						renderer: this.renderAddress,
						flex: 1
					},
					{
						header: CMDBuild.Translation.subject,
						sortable: false,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.SUBJECT,
						flex: 1
					},
					{
						sortable: false,
						scope: this,
						renderer: this.renderEmailContent,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.CONTENT,
						menuDisabled: true,
						hideable: false,
						flex: 2
					},
					{
						xtype: 'checkcolumn',
						header: '@@ Auto-sync.',
						dataIndex: '@@ autoSync',
						width: 90,
						align: 'center',
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true
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
								tooltip: '@@ Manually regenerate',
								scope: this,

								handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmOn('onEmailRegeneration', record);
								},

								isDisabled: function(grid, rowIndex, colIndex, item, record) {
									return !this.delegate.recordIsEditable(record) || this.readOnly || !this.delegate.isRegenerable(record);
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
									this.delegate.cmOn('onEmailReply', record);
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
								tooltip: CMDBuild.Translation.editEmail,
								scope: this,

								handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmOn('onEmailEdit', record);
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
								tooltip: CMDBuild.Translation.viewEmail,
								scope: this,

								handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmOn('onEmailView', record);
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
								tooltip: CMDBuild.Translation.deleteEmail,
								scope: this,

								handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmOn('onEmailDelete', record);
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
								formatName: function(name) {
									return CMDBuild.Translation.management.modworkflow.extattrs.manageemail.lookup[name] || name;
								}
							}
						],
						hideGroupedHeader: true,
						enableGroupingMenu: false
					}
				],
				store: CMDBuild.core.proxy.widgets.ManageEmail.getStore()
			});

			this.callParent(arguments);

			this.getStore().on('load', function() {
				this.storeLoaded = true;
			}, this);
		},

		listeners: {
			itemdblclick: function(grid, record, item, index, e, eOpts) {
				this.delegate.cmOn('onItemDoubleClick', record);
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
			renderAddress: function(value, metadata, record) {
				if (this.delegate.recordIsReceived(record)) {
					return record.get(CMDBuild.core.proxy.CMProxyConstants.FROM_ADDRESS);
				} else {
					return record.get(CMDBuild.core.proxy.CMProxyConstants.TO_ADDRESSES);
				}
			},

			/**
			 * @param {Mixed} value
			 * @param {Object} metaData
			 * @param {CMDBuild.model.widget.ManageEmail.email} record
			 *
			 * @return {String}
			 */
			renderEmailContent: function(value, metadata, record) {
				return Ext.util.Format.stripTags(record.get(CMDBuild.core.proxy.CMProxyConstants.CONTENT));
			}
	});

})();