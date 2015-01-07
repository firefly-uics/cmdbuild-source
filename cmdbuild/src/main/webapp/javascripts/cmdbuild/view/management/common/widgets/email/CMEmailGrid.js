(function() {

	var tr = CMDBuild.Translation.management.modworkflow.extattrs.manageemail;

	Ext.define('CMDBuild.view.management.common.widgets.CMEmailGrid', {
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
		 * @cfg {Object}
		 */
		emailTypes: {
			draft: 'Draft',
			'new': 'New',
			received: 'Received'
		},

		/**
		 * @cfg {Int}
		 */
		processId: undefined,

		/**
		 * @cfg {Boolean}
		 */
		readOnly: undefined,

		collapsible: false,
		isLoaded: false,
		loadMask: false,

		initComponent: function() {
			if (this.readOnly) {
				var me = this;

				Ext.apply(this, {
					tbar: [
						{
							iconCls: 'add',
							text: CMDBuild.Translation.management.modworkflow.extattrs.manageemail.compose,

							handler: function(values) {
								me.delegate.onAddEmailButtonClick(me, me.createRecord({}));
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
										if (btn != 'ok')
											return;
										me.delegate.onUpdateTemplatesButtonClick();
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
						sortable: true,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.ACCOUNT,
						hidden: true
					},
					{
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX,
						hidden: true
					},
					{
						sortable: true,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.STATUS,
						hidden: true
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
						dataIndex: 'Fake',
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
						width: 90,
						fixed: true,
						sortable: false,
						renderer: this.renderEmailActions,
						align: 'center',
						tdCls: 'grid-button',
						dataIndex: 'Fake',
						menuDisabled: true,
						hideable: false
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

			this.mon(this.store, 'load', this.onStoreLoad, this);
			this.on('beforeitemclick', this.cellclickHandler, this);
			this.on('itemdblclick', this.doubleclickHandler, this);
		},

		/**
		 * @param {Object} values
		 */
		addTemplateToStore: function(values) {
			var record = this.createRecord(values);

			// Mark the record added by template to be able to delete it in removeTemplatesToStore
			record._cmTemplate = true;

			this.addToStoreIfNotInIt(record);
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.grid} record
		 */
		addToStoreIfNotInIt: function(record) {
			var store = this.getStore();

			if (store.findBy(
					function(item) {
						return item[CMDBuild.core.proxy.CMProxyConstants.ID] == record[CMDBuild.core.proxy.CMProxyConstants.ID];
					}
				) == -1
			) {
				// Use loadRecords because store.add does not update the grouping so the grid goes broken
				store.loadRecords([record], { addRecords: true });
			}
		},

		/**
		 * @param {Ext.grid.View} grid
		 * @param {CMDBuild.model.widget.ManageEmail.grid} record
		 * @param {String} item
		 * @param {Int} index
		 * @param {Ext.EventObject} e
		 * @param {Object} eOpts
		 */
		cellclickHandler: function(grid, record, item, index, e, eOpts) {
			var className = e.target.className;
			var functionArray = {
				'action-email-delete': this.onDeleteEmail,
				'action-email-edit': this.onEditEmail,
				'action-email-view': this.onViewEmail
			};

			if (functionArray[className])
				functionArray[className].call(this, record);
		},

		/**
		 * @param {Object} recordValues
		 *
		 * @return {CMDBuild.model.widget.ManageEmail.grid}
		 */
		createRecord: function(recordValues) {
			recordValues[CMDBuild.core.proxy.CMProxyConstants.STATUS] = recordValues[CMDBuild.core.proxy.CMProxyConstants.STATUS] || this.emailTypes[CMDBuild.core.proxy.CMProxyConstants.NEW];
			recordValues[CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX] = (recordValues.hasOwnProperty(CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX)) ? recordValues[CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX] : this.delegate.widgetConf[CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX];

			return new CMDBuild.model.widget.ManageEmail.grid(recordValues);
		},

		/**
		 * @param {Ext.grid.View} grid
		 * @param {CMDBuild.model.widget.ManageEmail.grid} record
		 * @param {String} item
		 * @param {Int} index
		 * @param {Ext.EventObject} e
		 * @param {Object} eOpts
		 */
		doubleclickHandler: function(grid, record, item, index, e, eOpts) {
			var fn = this.recordIsEditable(record) ? this.onEditEmail : this.onViewEmail;

			fn.call(this, record);
		},

		/**
		 * @return {Array}
		 */
		getDraftEmails: function() {
			return this.getEmailsByGroup(this.emailTypes[CMDBuild.core.proxy.CMProxyConstants.DRAFT]);
		},

		/**
		 * @param {String} g
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
		 * @param {CMDBuild.model.widget.ManageEmail.grid} record
		 */
		onDeleteEmail: function(record) {
			Ext.Msg.confirm(
				CMDBuild.Translation.common.confirmpopup.title,
				CMDBuild.Translation.common.confirmpopup.areyousure,
				function(btn) {
					if (btn != 'yes')
						return;

					this.removeRecord(record);
				},
				this
			);
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.grid} record
		 */
		onEditEmail: function(record) {
			this.delegate.onModifyEmailIconClick(this, record);
		},

		onStoreLoad: function() {
			this.isLoaded = true;
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.grid} record
		 */
		onViewEmail: function(record) {
			Ext.create('CMDBuild.view.management.common.widgets.CMEmailWindow', {
				delegate: this.delegate,
				emailGrid: this,
				readOnly: true,
				record: record
			}).show();
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.grid} record
		 *
		 * @return {Boolean}
		 */
		recordIsEditable: function(record) {
			var status = record.get(CMDBuild.core.proxy.CMProxyConstants.STATUS);

			return status == this.emailTypes[CMDBuild.core.proxy.CMProxyConstants.DRAFT] || status == this.emailTypes[CMDBuild.core.proxy.CMProxyConstants.NEW];
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.grid} record
		 *
		 * @return {Boolean}
		 */
		recordIsReceived: function(record) {
			return (record.get(CMDBuild.core.proxy.CMProxyConstants.STATUS) == this.emailTypes[CMDBuild.core.proxy.CMProxyConstants.RECEIVED]);
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.grid} record
		 */
		removeRecord: function(record) {
			// The email has an id only if it was returned by the server. So add it to the deletedEmails only if the server know it
			var id = record.getId();

			if (id)
				this.deletedEmails.push(id);

			this.getStore().remove(record);
		},

		removeTemplatesFromStore: function() {
			var data = this.store.data.clone();

			for (var i = 0; i < data.length; ++i) {
				var storeItem = data.getAt(i);

				if (storeItem && storeItem._cmTemplate)
					this.store.remove(storeItem);
			}
		},

		// Column renderers
			/**
			 * @param {Mixed} value
			 * @param {Object} metaData
			 * @param {CMDBuild.model.widget.ManageEmail.grid} record
			 *
			 * @return {String}
			 */
			renderAddress: function(value, metadata, record) {
				if (this.recordIsReceived(record)) {
					return record.get(CMDBuild.core.proxy.CMProxyConstants.FROM_ADDRESS);
				} else {
					return record.get(CMDBuild.core.proxy.CMProxyConstants.TO_ADDRESSES);
				}
			},

			/**
			 * @param {Mixed} value
			 * @param {Object} metaData
			 * @param {CMDBuild.model.widget.ManageEmail.grid} record
			 *
			 * @return {String}
			 */
			renderEmailActions: function(value, metadata, record) {
				if (this.recordIsEditable(record) && this.readOnly) {
					return '<img style="cursor:pointer" title="'+CMDBuild.Translation.management.modworkflow.extattrs.manageemail.deleteicon+'" class="action-email-delete" src="images/icons/delete.png"/>&nbsp;'
						+ '<img style="cursor:pointer" title="'+CMDBuild.Translation.management.modworkflow.extattrs.manageemail.editicon+'" class="action-email-edit" src="images/icons/modify.png"/>&nbsp;';
				} else {
					return '<span style="cursor:pointer; width: 16px; height: 16px" />'
						+ '<img style="cursor:pointer" title="'+CMDBuild.Translation.management.modworkflow.extattrs.manageemail.viewicon+'" class="action-email-view" src="images/icons/zoom.png"/>&nbsp;'
						+ '</span>';
				}
			},

			/**
			 * @param {Mixed} value
			 * @param {Object} metaData
			 * @param {CMDBuild.model.widget.ManageEmail.grid} record
			 *
			 * @return {String}
			 */
			renderEmailContent: function(value, metadata, record) {
				var htmlContent = record.get(CMDBuild.core.proxy.CMProxyConstants.CONTENT);

				if (htmlContent) {
					return htmlContent.replace(/\<[Bb][Rr][\/]?\>/g," ").replace(/\<[^\>]*\>/g,"");
				} else {
					return undefined;
				}
			}
	});

})();