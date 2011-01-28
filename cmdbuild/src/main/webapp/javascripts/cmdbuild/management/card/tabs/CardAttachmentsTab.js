(function() {

var ACTIVITY_CLOSE_COMPLETED = 'closed.completed';

CMDBuild.Management.CardAttachmentsTab = Ext.extend(Ext.grid.GridPanel, {
	translation : CMDBuild.Translation.management.modcard,
    eventtype: 'card',
    eventmastertype: 'class',
    hideMode: "offsets",
    
	initComponent: function() {
	    var col_tr = CMDBuild.Translation.management.modcard.attachment_columns;

    	this.addAttachmentAction = new Ext.Action({
      		iconCls : 'add',
      		text : this.translation.add_attachment,
			handler : this.addAttachment,
      		scope: this
    	});

    	function renderAttachmentActions() {
    		// FIXME delete and modify should be hidden on readonly classes
    		var tr = CMDBuild.Translation.management.modcard;
			return '<img style="cursor:pointer" title="'+tr.download_attachment+'" class="action-attachment-download" src="images/icons/bullet_go.png"/>&nbsp;'
			     + '<img style="cursor:pointer" title="'+tr.edit_attachment+'" class="action-attachment-edit" src="images/icons/modify.png"/>&nbsp;'
			     + '<img style="cursor:pointer" title="'+tr.delete_attachment+'" class="action-attachment-delete" src="images/icons/delete.png"/>';
		}

    	var reader = new Ext.data.JsonReader({
			root: "rows",
			fields: ['Category',
			         {name:'CreationDate', type:'date', dateFormat:'d/m/y H:i:s'},
			         {name:'ModificationDate', type:'date', dateFormat:'d/m/y H:i:s'},
			         'Author','Version','Filename','Description','Fake']
		});
		var proxy = new Ext.data.HttpProxy({
			url: 'services/json/management/modcard/getattachmentlist'
		});
		
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

		this.tbar = [
			this.addAttachmentAction,
			new CMDBuild.Management.GraphActionHandler().getAction()
		];
		
		Ext.apply(this, {
			loadMask: true,
			store: new Ext.data.GroupingStore({
				reader: reader,
				proxy: proxy,
				remoteSort: false,
				groupField: 'Category',
				sortInfo: {field: 'Category', direction: "ASC"}
	        }),
	        cm: new Ext.grid.ColumnModel([
	        	{header: col_tr.category, width: 20, sortable: true, dataIndex: 'Category', hidden: true},
	        	{header: col_tr.creation_date, width: 20, sortable: true, dataIndex: 'CreationDate', renderer: Ext.util.Format.dateRenderer('d/m/y H:i:s')},
	        	{header: col_tr.modification_date, width: 20, sortable: true, dataIndex: 'ModificationDate', renderer: Ext.util.Format.dateRenderer('d/m/y H:i:s')},
	        	{header: col_tr.author, width: 20, sortable: true, dataIndex: 'Author'},
	            {header: col_tr.version, width: 10, sortable: true, dataIndex: 'Version'},
	            {header: col_tr.filename, width: 40, sortable: true, dataIndex: 'Filename'},
	            {header: col_tr.description, width: 40, sortable: true, dataIndex: 'Description'},
	            {header: '&nbsp;', width: 60, fixed: true, sortable: false, renderer: renderAttachmentActions, align: 'center', cellCls: 'grid-button', dataIndex: 'Fake'}
	        ]),
	        view: new Ext.grid.GroupingView({
				forceFit: true,
				groupTextTpl: '{gvalue} ({[values.rs.length]} {[values.rs.length > 1 ? CMDBuild.Translation.management.modcard.attachment_columns.items : CMDBuild.Translation.management.modcard.attachment_columns.item]})'
			}),	        
	        animCollapse: false	        
		});

		CMDBuild.Management.CardAttachmentsTab.superclass.initComponent.apply(this, arguments);
		
		
		this.on('hide', function() {
			if (this.eventtype == 'activity' && this.activityStatusCode != ACTIVITY_CLOSE_COMPLETED) {
				this.disable();
			}
		}, this);
		
		if (CMDBuild.Config.dms.enabled == "false") {
			this.disable();
		} else {
			subscribeToEvents.call(this);				
		}
	},

	initForClass: function(eventParams) {
		this.disable();
		if (eventParams) {
			this.currentClassPrivileges = Ext.apply({
					create: false,
					write: false
				}, eventParams.privileges);
			this.enableModify();
			
		}
	},

	enableModify: function() {
		this.addAttachmentAction.setDisabled(!this.currentClassPrivileges.write);
	},
	
	disableModify: function() {
		this.addAttachmentAction.disable();
	},
	
	newCard: function(eventParams) {
		this.disable();
	},

	loadCard: function(eventParams) {
		var idClass = eventParams.record.data.IdClass;
		if (CMDBuild.Utils.isSimpleTable(idClass)) {
			this.disable();
			return;
		}
		
		this.currentCardId = eventParams.record.data.Id;
		this.currentClassId = eventParams.record.data.IdClass;
		this.currentCardPrivileges = {
			create: eventParams.record.data.priv_create,
			write: eventParams.record.data.priv_write
		};
		this.reloadCard();
		
		if (this.wfmodule) { // workflow stuff
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
	},

	reloadCard: function() {
		this.loaded = false;
		if (this.isVisible()) {
			this.loadCardAttachments();
		}
	},

	loadCardAttachments: function() {
		if (this.loaded)
			return;
		this.getStore().load({
			params : {
				IdClass: this.currentClassId,
				Id: this.currentCardId
			}
		});
		this.loaded = true;
	},

	addAttachment: function() {
		var addAttachmentWin = new CMDBuild.Management.AddAttachmentWindow({
			classId: this.currentClassId,
			cardId: this.currentCardId
		}).show();
		
		addAttachmentWin.on("saved", this.reloadCard, this);
	},

	editAttachment: function(jsonRow) {
		var editAttachmentWin = new CMDBuild.Management.EditAttachmentWindow({
			classId: this.currentClassId,
			cardId: this.currentCardId,
			category: jsonRow.Category,
			filename: jsonRow.Filename,
			description: jsonRow.Description
		}).show();
		
		editAttachmentWin.on("saved", this.reloadCard, this);
	},
	
	deleteAttachment: function(jsonRow) {
		Ext.Msg.confirm(
			this.translation.delete_attachment,
			this.translation.delete_attachment_confirm,
			function(btn) {
				if (btn != 'yes') {
					return;
				}
				CMDBuild.Ajax.request({
					url : 'services/json/management/modcard/deleteattachment',
					params : {
						"IdClass": this.currentClassId,
						"Id": this.currentCardId,
						"Filename": jsonRow.Filename
					},
					waitTitle : CMDBuild.Translation.common.wait_title,
					waitMsg : CMDBuild.Translation.common.wait_msg,
					method : 'POST',
					scope : this,
					success : function() {
	                	// Defer the call because Alfresco is not responsive
	                	function deferredCall() {
							this.reloadCard();
						};
                		deferredCall.defer(CMDBuild.Config.dms.delay, this);
					}
			 	});
	 		}, this);
	},

	downloadAttachment: function(jsonRow) {
		var params = {
				"IdClass": this.currentClassId,
				"Id": this.currentCardId,
				"Filename": jsonRow.Filename
			};
		var url = 'services/json/management/modcard/downloadattachment?'+Ext.urlEncode(params);
		window.open(url, "_blank");
	}
});

function subscribeToEvents() {
	this.on('rowdblclick', doubleclickHandler, this);
	this.on('cellclick', cellclickHandler);
	this.on('activate', this.loadCardAttachments, this);
	
	this.subscribe('cmdb-init-' + this.eventmastertype, this.initForClass, this);
	this.subscribe('cmdb-new-' + this.eventtype, this.newCard, this);
	this.subscribe('cmdb-load-' + this.eventtype, this.loadCard, this);
	
	this.destroy = function() {
		this.unsubscribe('cmdb-init-' + this.eventmastertype, this.initForClass, this);
		this.unsubscribe('cmdb-new-' + this.eventtype, this.newCard, this);
		this.unsubscribe('cmdb-load-' + this.eventtype, this.loadCard, this);
		
		CMDBuild.Management.CardAttachmentsTab.superclass.destroy.call(this);
	}
}

function cellclickHandler(grid, rowIndex, colIndex, event) {
	var className = event.target.className;
	var jsonRow = grid.getStore().getAt(rowIndex).json;
	var functionArray = {
		'action-attachment-delete': this.deleteAttachment,
		'action-attachment-edit': this.editAttachment,
		'action-attachment-download': this.downloadAttachment
	};
	if (functionArray[className]) {
		functionArray[className].call(grid,jsonRow);
	}
}

function doubleclickHandler(grid, rowIndex, event) {
	this.downloadAttachment(grid.getStore().getAt(rowIndex).json);
}

Ext.reg('cardattachmentstab', CMDBuild.Management.CardAttachmentsTab);

})();