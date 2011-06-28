(function() {

var ACTIVITY_CLOSE_COMPLETED = 'closed.completed';
var tr = CMDBuild.Translation.management.modcard;

Ext.define("CMAttachmentModel", {
	extend: "Ext.data.Model",

	fields: ['Category',
		{name:'CreationDate', type:'date', dateFormat:'d/m/y H:i:s'},
		{name:'ModificationDate', type:'date', dateFormat:'d/m/y H:i:s'},
		'Author','Version','Filename','Description','Fake']
});

Ext.define("CMDBuild.view.management.classes.attacchments.CMCardAttachmentsPanel", {
	extend: "Ext.grid.Panel",
	translation : CMDBuild.Translation.management.modcard,
	eventtype: 'card',
	eventmastertype: 'class',
	hideMode: "offsets",

	initComponent: function() {
		var col_tr = CMDBuild.Translation.management.modcard.attachment_columns;

		this.addAttachmentButton = new Ext.button.Button({
			iconCls : 'add',
			text : this.translation.add_attachment
		});

		this.store = buildStore();

		// TODO subclass this
		if( this.eventtype == 'activity' ) {
			this.backToActivityButton = new Ext.Button({
				text: CMDBuild.Translation.common.buttons.workflow.back,
				hideMode: "offsets",
				handler: function(){
					this.findParentByType('activitytabpanel').setActiveTab('activity_tab');
				},
				scope: this
			});
			this.buttonAlign = "center";
			this.buttons = [this.backToActivityButton];
		}

		Ext.apply(this, {
			loadMask: false,
			tbar:[this.addAttachmentButton],
			features: [{
				groupHeaderTpl: '{name} ({rows.length} {[values.rows.length > 1 ? CMDBuild.Translation.management.modcard.attachment_columns.items : CMDBuild.Translation.management.modcard.attachment_columns.item]})',
				ftype: 'groupingsummary'
			}],
			columns: [
				{header: col_tr.category, dataIndex: 'Category', hidden: true},
				{header: col_tr.creation_date, sortable: true, dataIndex: 'CreationDate', renderer: Ext.util.Format.dateRenderer('d/m/y H:i:s'), flex: 2},
				{header: col_tr.modification_date, sortable: true, dataIndex: 'ModificationDate', renderer: Ext.util.Format.dateRenderer('d/m/y H:i:s'), flex: 2},
				{header: col_tr.author, sortable: true, dataIndex: 'Author', flex: 2},
				{header: col_tr.version, sortable: true, dataIndex: 'Version', flex: 1},
				{header: col_tr.filename, sortable: true, dataIndex: 'Filename', flex: 4},
				{header: col_tr.description, sortable: true, dataIndex: 'Description', flex: 4},
				{header: '&nbsp;', width: 60, fixed: true, sortable: false, renderer: renderAttachmentActions, align: 'center', cellCls: 'grid-button', dataIndex: 'Fake'}
			]
		});

		this.callParent(arguments);

// TODO 3 to 4 move on the wf controller of the attachments
//		this.on('hide', function() {
//			if (this.eventtype == 'activity' && this.activityStatusCode != ACTIVITY_CLOSE_COMPLETED) {
//				this.disable();
//			}
//		}, this);

	},

	onCardSelected: function(card) {

		/*
		if (this.wfmodule) { // TODO aubclass workflow stuff
			this.activityStatusCode = this.wfmodule.getFlowStatusCodeById(eventParams.record.data.FlowStatus);
			if (this.activityStatusCode == ACTIVITY_CLOSE_COMPLETED) {
				this.enable();
				this.disableModify();
				this.getFooterToolbar().hide();
				this.doLayout();
				// FIXME remove modify and delete icons
			} else {
				this.disable();
				this.enableModify();
				this.getFooterToolbar().show();
			}
		} else {
			this.enable();
		}
		*/
	},

	reloadCard: function() {
		this.loaded = false;
		if (this.ownerCt.layout.getActiveItem().id == this.id) {
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
	}

});

function buildStore() {
	var s =  new Ext.data.Store({
		model: "CMAttachmentModel",
		proxy: {
			type: 'ajax',
			url: 'services/json/management/modcard/getattachmentlist',
			reader: {
				type: 'json',
				root: 'rows'
			}
		},

		remoteSort: false,
		groupField: 'Category',
		sorters: {property: 'Category', direction: "ASC"}
	});

	return s;
}

function renderAttachmentActions() {
	// FIXME delete and modify should be hidden on readonly classes
	var tr = CMDBuild.Translation.management.modcard;
	return '<img style="cursor:pointer" title="'+tr.download_attachment+'" class="action-attachment-download" src="images/icons/bullet_go.png"/>&nbsp;'
	     + '<img style="cursor:pointer" title="'+tr.edit_attachment+'" class="action-attachment-edit" src="images/icons/modify.png"/>&nbsp;'
	     + '<img style="cursor:pointer" title="'+tr.delete_attachment+'" class="action-attachment-delete" src="images/icons/delete.png"/>';
}

})();