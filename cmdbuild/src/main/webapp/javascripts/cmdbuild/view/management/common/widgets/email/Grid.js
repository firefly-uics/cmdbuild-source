(function() {

	var tr = CMDBuild.Translation.management.modworkflow.extattrs.manageemail;

	Ext.define('CMDBuild.view.management.common.widgets.email.Grid', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.widgets.ManageEmail',
			'CMDBuild.model.widget.ManageEmail'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.CMManageEmailController}
		 */
		delegate: undefined,

		/**
		 * @cfg {Array}
		 */
		deletedEmails: [],

		/**
		 * All email types this widget manages
		 *
		 * @cfg {Object}
		 */
		emailTypes: {
			draft: 'Draft',
			'new': 'New',
			outgoing: 'Outgoing',
			received: 'Received',
			sent: 'Sent'
		},

		/**
		 * @cfg {Boolean}
		 */
		readOnly: undefined,

		/**
		 * @cfg {Boolean}
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
							text: CMDBuild.Translation.management.modworkflow.extattrs.manageemail.compose,

							handler: function(values) {
								me.delegate.cmOn('onEmailAddButtonClick');
							}
						},
						{
							iconCls: 'x-tbar-loading',
							text: CMDBuild.Translation.management.modworkflow.extattrs.manageemail.regenerates,

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
						header: tr.datehdr,
						sortable: true,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.DATE,
						flex: 1
					},
					{
						header: tr.addresshdr,
						sortable: false,
						scope: this,
						renderer: this.renderAddress,
						flex: 1
					},
					{
						header: tr.subjecthdr,
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
									return !this.recordIsEditable(record) || this.readOnly || !this.isRegenerable(record);
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
									return !this.recordIsEditable(record) || this.readOnly;
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
									return !this.recordIsEditable(record) || this.readOnly;
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
									return tr.lookup[name] || name;
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

		/**
		 * @param {Object} values
		 */
		addTemplateToStore: function(values) {
			var record = this.createRecord(values);

			this.addToStoreIfNotInIt(record);
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 */
		addToStoreIfNotInIt: function(record) {
			var store = this.getStore();

			if (store.findBy(function(item) {
					return item.get(CMDBuild.core.proxy.CMProxyConstants.ID) == record.get(CMDBuild.core.proxy.CMProxyConstants.ID);
				}) == -1
			) {
				this.delegate.generateTemporaryId(record);

				// Use loadRecords because store.add does not update the grouping so the grid goes broken
				store.loadRecords([record], { addRecords: true });
			}
		},

		/**
		 * @param {Object} recordValues
		 *
		 * @return {CMDBuild.model.widget.ManageEmail.email}
		 */
		createRecord: function(recordValues) {
			recordValues = recordValues || {};
			recordValues[CMDBuild.core.proxy.CMProxyConstants.STATUS] = recordValues[CMDBuild.core.proxy.CMProxyConstants.STATUS] || this.emailTypes[CMDBuild.core.proxy.CMProxyConstants.NEW];
			recordValues[CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX] = (recordValues.hasOwnProperty(CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX)) ? recordValues[CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX] : this.delegate.widgetConf[CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX];

			return Ext.create('CMDBuild.model.widget.ManageEmail.email', recordValues);
		},

		/**
		 * @return {Array}
		 */
		getDraftEmails: function() {
			return this.getEmailsByGroup(this.emailTypes[CMDBuild.core.proxy.CMProxyConstants.DRAFT]);
		},

		/**
		 * @param {String} group
		 *
		 * @return {Array}
		 */
		getEmailsByGroup: function(group) {
			var out = this.store.getGroups(group);

			if (out)
				out = out.children; // ExtJS mystic output { name: group, children: [...] }

			return out || [];
		},

		/**
		 * @return {Array}
		 */
		getNewEmails: function() {
			return this.getEmailsByGroup(this.emailTypes[CMDBuild.core.proxy.CMProxyConstants.NEW]);
		},

		/**
		 * @return {Boolean}
		 */
		hasDraftEmails: function() {
			return this.getDraftEmails().length > 0;
		},

		/**
		 * @WIP TODO
		 *
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 *
		 * @return {Boolean}
		 */
		isRegenerable: function(record) {
			return true;
		},

		/**
		 * @return {Boolean}
		 */
		isStoreLoaded: function() {
			return this.storeLoaded;
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 *
		 * @return {Boolean}
		 */
		recordIsEditable: function(record) {
			var status = record.get(CMDBuild.core.proxy.CMProxyConstants.STATUS);

			return status == this.emailTypes[CMDBuild.core.proxy.CMProxyConstants.DRAFT] || status == this.emailTypes[CMDBuild.core.proxy.CMProxyConstants.NEW];
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