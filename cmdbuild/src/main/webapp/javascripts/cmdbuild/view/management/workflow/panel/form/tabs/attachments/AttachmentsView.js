(function () {

	/**
	 * @link CMDBuild.view.management.common.widgets.CMOpenAttachment
	 * @link CMDBuild.view.management.classes.attachments.CMCardAttachmentsPanel
	 *
	 * @legacy
	 */
	Ext.define('CMDBuild.view.management.workflow.panel.form.tabs.attachments.AttachmentsView', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.management.workflow.panel.form.tabs.Attachment'
		],

		/**
		 * @cfg {CMDBuild.controller.management.workflow.panel.form.tabs.Note}
		 */
		delegate: undefined,

		border: false,
		frame: false,
		title: CMDBuild.Translation.attachments,

		eventtype: 'card',
		eventmastertype: 'class',
		hideMode: "offsets",

		initComponent: function() {
			this.backToActivityButton = Ext.create('CMDBuild.core.buttons.text.Back');

			Ext.apply(this, {
				buttonAlign: "center",
				buttons: [this.backToActivityButton],
				cls: "x-panel-body-default-framed"
			});

			this.addAttachmentButton = Ext.create('CMDBuild.core.buttons.iconized.add.Add', {
				text: CMDBuild.Translation.management.modcard.add_attachment
			});

			Ext.apply(this, {
				loadMask: false,
				tbar:[this.addAttachmentButton],
				features: [{
					groupHeaderTpl: '{name} ({rows.length} {[values.rows.length > 1 ? CMDBuild.Translation.management.modcard.attachment_columns.items : CMDBuild.Translation.management.modcard.attachment_columns.item]})',
					ftype: 'groupingsummary'
				}],
				columns: [
					{header: CMDBuild.Translation.management.modcard.attachment_columns.category, dataIndex: 'Category', hidden: true},
					{header: CMDBuild.Translation.management.modcard.attachment_columns.creation_date, sortable: true, dataIndex: 'CreationDate', renderer: Ext.util.Format.dateRenderer('d/m/Y H:i:s'), flex: 2},
					{header: CMDBuild.Translation.management.modcard.attachment_columns.modification_date, sortable: true, dataIndex: 'ModificationDate', renderer: Ext.util.Format.dateRenderer('d/m/Y H:i:s'), flex: 2},
					{header: CMDBuild.Translation.management.modcard.attachment_columns.author, sortable: true, dataIndex: 'Author', flex: 2},
					{header: CMDBuild.Translation.management.modcard.attachment_columns.version, sortable: true, dataIndex: 'Version', flex: 1},
					{header: CMDBuild.Translation.management.modcard.attachment_columns.filename, sortable: true, dataIndex: 'Filename', flex: 4},
					{header: CMDBuild.Translation.management.modcard.attachment_columns.description, sortable: true, dataIndex: 'Description', flex: 4},
					{header: '&nbsp;', width: 80, sortable: false, renderer: this.renderAttachmentActions, align: 'center', tdCls: 'grid-button', dataIndex: 'Fake'}
				],
				store: CMDBuild.proxy.management.workflow.panel.form.tabs.Attachment.getStore()
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function(panel, eOpts) {
				// History record save
				if (!Ext.isEmpty(_CMWFState.getProcessClassRef()) && !Ext.isEmpty( _CMWFState.getProcessInstance()))
					CMDBuild.global.navigation.Chronology.cmfg('navigationChronologyRecordSave', {
						moduleId: 'workflow',
						entryType: {
							description: _CMWFState.getProcessClassRef().get(CMDBuild.core.constants.Proxy.TEXT),
							id: _CMWFState.getProcessClassRef().get(CMDBuild.core.constants.Proxy.ID),
							object: _CMWFState.getProcessClassRef()
						},
						item: {
							description: _CMWFState.getProcessInstance().get(CMDBuild.core.constants.Proxy.TEXT),
							id: _CMWFState.getProcessInstance().get(CMDBuild.core.constants.Proxy.ID),
							object: _CMWFState.getProcessInstance()
						},
						section: {
							description: this.title,
							object: this
						}
					});
			}
		},

		configure: function(c) {
			this.widgetConf = c.widget;
			var ai = _CMWFState.getActivityInstance();
			var pi = _CMWFState.getProcessInstance();

			this.readOnly = c.widget.ReadOnly;

			this.setExtraParams({
				className: _CMCache.getEntryTypeNameById(pi.getClassId()),
				cardId: pi.getId()
			});

			this.writePrivileges = ai.isWritable() && !this.readOnly;
			this.addAttachmentButton.setDisabled(!this.writePrivileges);

			this.loaded = false;
		},

		cmActivate: function() {
			this.enable();

			// rendering issues, call showBackButton only after
			// that the  panel did actually activated
			this.mon(this, "activate", this.showBackButton, this, {single: true});

			this.ownerCt.setActiveTab(this);
		},

		reloadCard: function() {
			this.loaded = false;
			if (this.ownerCt.layout.getActiveItem) {
				if (this.ownerCt.layout.getActiveItem().id == this.id) {
					this.loadCardAttachments();
				}
			} else {
				// it is not in a tabPanel
				this.loadCardAttachments();
			}
		},

		loadCardAttachments: function() {
			if (this.loaded) {
				return;
			}

			this.getStore().load();

			this.loaded = true;
		},

		setExtraParams: function(p) {
			this.store.proxy.extraParams = p;
		},

		clearStore: function() {
			this.store.removeAll();
		},

		renderAttachmentActions: function() {
			var out = '<img style="cursor:pointer" title="'+CMDBuild.Translation.management.modcard.download_attachment+'" class="action-attachment-download" src="images/icons/bullet_go.png"/>&nbsp;';

			if (this.writePrivileges) {
				out += '<img style="cursor:pointer" title="'+CMDBuild.Translation.management.modcard.edit_attachment+'" class="action-attachment-edit" src="images/icons/modify.png"/>&nbsp;'
				+ '<img style="cursor:pointer" title="'+CMDBuild.Translation.management.modcard.delete_attachment+'" class="action-attachment-delete" src="images/icons/delete.png"/>';
			}

			return out;
		},

		hideBackButton: function() {
			this.backToActivityButton.hide();
		},

		showBackButton: function() {
			this.backToActivityButton.show();
			this.ownerCt.doLayout();
		},

		/**
		 * @param {Boolean} writePrivilege
		 *
		 * @override
		 */
		updateWritePrivileges: function(writePrivilege) {
			this.writePrivileges = writePrivilege;
			this.addAttachmentButton.setDisabled(
				!writePrivilege
				|| !CMDBuild.configuration.workflow.get(CMDBuild.core.constants.Proxy.ENABLE_ADD_ATTACHMENT_ON_CLOSED_ACTIVITIES)
			);
		},

		/**
		 * @deprecated
		 */
		onAddCardButtonClick: function() {
			_deprecated('onAddCardButtonClick', this);

			this.disable();
		},

		/**
		 * @deprecated
		 */
		onCardSelected: function(card) {
			_deprecated('onCardSelected', this);

			this.updateWritePrivileges(card.raw.priv_write);
		}
	});

})();