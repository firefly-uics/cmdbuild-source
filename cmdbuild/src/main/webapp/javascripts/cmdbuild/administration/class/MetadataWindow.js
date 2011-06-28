CMDBuild.Administration.MetadataWindow = Ext.extend(CMDBuild.PopupWindow, { 
	height: 300,
	width: 300,
	translation: CMDBuild.Translation.administration.modClass.attributeProperties.meta,
	initComponent: function() {
		this.deletedMeta = [];
		this.saveBtn = new CMDBuild.buttons.SaveButton({
			handler: this.onSave,
			scope: this
		});
		
		this.abortBtn = new CMDBuild.buttons.AbortButton({
			handler: this.onAbort,
			scope: this
		});
	
		this.cm = new Ext.grid.ColumnModel([{
				header: this.translation.key,
				dataIndex: 'key',
				editor: new Ext.form.TextField({
					allowBlank: false
				})
			},{
				header: this.translation.value,
				dataIndex: 'value',
				editor:new Ext.form.TextField({
					allowBlank: false
				})
			},{
				header: '&nbsp', 
				width: 40, 
				fixed: true, 
				sortable: false, 
				renderer: this.renderDeleteActions, 
				align: 'center', 
				cellCls: 'grid-button', 
				dataIndex: 'delete',
				menuDisabled: true,
    			id: 'imagecolumn',
    			hideable: false
			}
		]);
		 
		this.recordTemplate = Ext.data.Record.create ([
		    {name: 'key', mapping: 'key'},
		    {name: 'value', mapping: 'value'},
		    {name: 'status', mapping: 'status'}
		]);
		
		this.store = new Ext.data.SimpleStore({
			fields: ['key','value', 'status'],
			data: []
		});
		
		this.addMetaToStore();
		// TODO extjs 3 to 4 migration @@ editor
		this.grid = new Ext.grid.Panel({
	    	layout: 'fit',
	    	stripeRows: true,
	        frame:true,
	        clicksToEdit: 1,
	        cm: this.cm,
	        sm: this.sm,
	        store: this.store,
	        viewConfig: { forceFit:true },
	        tbar: [{
	        	text: this.translation.add,
	        	handler: this.addMetadata,
	        	scope: this,
	        	iconCls: 'add'
	        }]
	    });
		this.grid.on('cellclick', this.onCellClick, this);
		
		Ext.apply(this, {
			title: this.translation.title,
			items: [this.grid],
			buttonAlign: 'center',
			buttons: [this.saveBtn, this.abortBtn]
		});
		
	CMDBuild.Administration.MetadataWindow.superclass.initComponent.apply(this, arguments);
	},
	
	onCellClick: function(grid, rowIndex, colIndex, event) {
		var className = event.target.className;
		if (className == "action-meta-delete") {
			var record = this.store.getAt(rowIndex);
			if (record.data.status != "NEW") {
				record.data.status = "DELETED";
				this.deletedMeta.push(record);
			}
			this.store.remove(record);
		}
	},
	
	renderDeleteActions: function() {
		return '<img style="cursor:pointer" title="' +
		CMDBuild.Translation.administration.modClass.attributeProperties.meta.remove + 
		'" class="action-meta-delete" src="images/icons/cross.png"/>&nbsp;';
	},
	
	addMetadata: function() {
		var r = new this.recordTemplate({
			key: this.translation.key,
			value: this.translation.value,
			status: 'NEW'
		});
		this.grid.stopEditing();
		this.store.insert(0, r);
		this.grid.startEditing(0, 0);
	},
	
	onSave: function() {
		var metaAsMap = this.getMetaAsMap();
		this.owner.formPanel.getForm().findField('meta').setValue(Ext.util.JSON.encode(metaAsMap));
		this.owner.record.data.meta = metaAsMap;
		this.destroy();
	},
	
	getMetaAsMap: function() {
		var meta = {};
		this.changeStatusToModifiedRecords();
		this.store.add(this.deletedMeta);
		this.store.each(function(r){			
			var k = r.data.key;
			var v = r.data.value;
			var s = r.data.status;
			//if the key is changed, delete the record and add a new one
			if (r.modified && r.modified["key"] && s != "NEW") {
				meta[this.ns+r.modified["key"]] = {
					value: v,
					status: "DELETED"
				};
				meta[this.ns+k] = {
						value: v,
						status: "NEW"
				};
			} else {
				meta[this.ns+k] = {
						value: v,
						status: s
				};
			}
		}, this);
		CMDBuild.log.debug('getMetaAsMap', meta);
		return meta;
	},
	
	changeStatusToModifiedRecords: function() {
		var modified = this.store.modified;
		for (var i=0, len=modified.length; i<len; ++i) {
			var modifiedRec = modified[i];			
			this.store.each(function(rec){
				if (rec.id == modifiedRec.id && rec.data.status != "NEW") {
					rec.data.status = "MODIFIED";
				}
			}, this);
		}
	},
	
	onAbort: function() {
		this.destroy();
	},
	
	addMetaToStore: function() {
		var tmpMeta = CMDBuild.Utils.Metadata.extractMetaByNS(this.meta, this.ns);
		
		for (var i in  tmpMeta) {
			var m = tmpMeta[i];
			if (m.status) {
				//is not the first time that the window is open
				var r = new this.recordTemplate({
					key: i,
					value: m.value,
					status: m.status
				});
				if (r.data.status == "DELETED") {
					this.deletedMeta.push(r);
				} else {
					this.store.add(r);
				}
			} else {
				var r = new this.recordTemplate({
					key: i,
					value: m,
					status: 'NOT_MODIFIED'
				});
				this.store.add(r);
			}
		}
	}
});