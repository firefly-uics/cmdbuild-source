CMDBuild.Administration.ModIcons = Ext.extend(CMDBuild.ModPanel, {
	modtype:"gis-icons",	
	translation: CMDBuild.Translation.administration.modcartography.icons,
	buttonsTr: CMDBuild.Translation.common.buttons,
	urls: {
		list: "services/json/gis/geticonslist",
		modify: "services/json/gis/updateiconcard",
		add: "services/json/gis/createiconcard",
		remove: "services/json/gis/deleteiconcard"
	},
	
	initComponent : function() {		
		var store =  new Ext.data.JsonStore({
			url : this.urls.list,
			root : "rows",
			fields : ['name', 'description', 'path'],
			autoLoad: true
		});
		
		this.buildUIButtons();
		
		this.iconsGrid = new Ext.grid.GridPanel({
			region: 'center',
			frame: false,
		    border: false,
			store: store,
			tbar: [this.addButton],
			bodyCssClass: CMDBuild.Constants.css.bottom_border_gray,
		    colModel: new Ext.grid.ColumnModel({
		        defaults: {
		            width: 120,
		            sortable: true
		        },
		        columns: [{	
	    			header: '&nbsp', 
	    			width: 50, 
	    			fixed: true, 
	    			sortable: false,
	    			renderer: this.renderIcon, 
	    			align: 'center', 
	    			dataIndex: 'path',
	    			menuDisabled: true,
	    			hideable: false
	    		},{
	    			header : this.translation.description,
	    			hideable: true,
	    			hidden: false,
	    			dataIndex : 'description'    			
	    		}]
		    }),
		    viewConfig: {
		        forceFit: true
		    },		    
		    sm: new Ext.grid.RowSelectionModel({singleSelect:true})
		});
		this.iconsGrid.getSelectionModel().on("rowselect", this.onRowSelect , this);
		
		this.uploadForm = new Ext.form.FormPanel({
			monitorValid: true,
			fileUpload: true,
			plugins: [new CMDBuild.CallbackPlugin()],
			region: 'south',
			split: true,
			frame: false,
			border: false,
			cls: CMDBuild.Constants.css.top_border_gray,
			tbar: [this.modifyButton, this.removeButton],
			items: [{
				xtype: 'panel',
				layout: 'form',
				frame: true,
				border: true,
				cls: CMDBuild.Constants.css.padding5 +" "+ CMDBuild.Constants.css.bg_gray,
				items: [{
					xtype:'hidden',
					name: 'name'
				},{
					xtype: 'textfield',
					inputType : 'file',
					fieldLabel: this.translation.file,
					name: 'file'					
				}, {
					xtype: 'textfield',
					fieldLabel: this.translation.description,
					name: 'description',
					allowBlank: false,
					width: 160
				}]
			}],
			buttonAlign: 'center',
			buttons: [this.saveButton,this.abortButton]
		});
		
		Ext.apply(this, {
			title: this.translation.title,
      		layout: 'border',
      		items: [this.iconsGrid, this.uploadForm]
    	});
		CMDBuild.Administration.ModIcons.superclass.initComponent.apply(this, arguments);
		
		this.on('show', function() {
			if (!this.iconsGrid.getSelectionModel().hasSelection()) {
				this.iconsGrid.getSelectionModel().selectFirstRow();
			}
			this.uploadForm.setFieldsDisabled();
		}, this);
		
		this.iconsGrid.getStore().on('load', function(store, records, options) {
			if (records.length == 0) {
				this.modifyButton.disable();
				this.removeButton.disable();
			}
		}, this);
  	},
  	
  	//private
  	renderIcon: function(value, cell, record) {
  		var path = record.data.path + "?" + Math.floor(Math.random()*100); //to force the reload
  		return "<img src=\"" + path + "\" alt=\"" + record.data.name + "\" class=\"icon-grid-image\"/>";
  	},

  	//private
  	buildUIButtons: function() {
  		this.addButton = new Ext.Button({
	    	text: this.buttonsTr.add,
	    	iconCls: 'add',
	    	scope: this,
	    	handler: this.onAddClick
	    });
  		
  		this.saveButton = new Ext.Button({
			text: this.buttonsTr.save,
			scope: this,
			disabled: true,
			formBind: true,
			handler: this.onSave
		});
		
		this.abortButton = new Ext.Button({
			text: this.buttonsTr.abort,
			scope: this,
			disabled: true,
			handler: this.onAbort
		});
		
		this.modifyButton = new Ext.Button({
	    	text: this.buttonsTr.modify,
	    	iconCls: 'modify',
	    	scope: this,
	    	disabled: true,
	    	handler: this.onModify
	    });
		
		this.removeButton = new Ext.Button({
	    	text: this.buttonsTr.remove,
	    	iconCls: 'delete',
	    	scope: this,
	    	disabled: true,
	    	handler: this.onRemove
	    });
		
  	},
  	
  	//private
  	onRowSelect: function(sm, index, record) {  		
  		this.disableModify();
  		this.modifyButton.enable();
  		this.removeButton.enable();
  		this.uploadForm.getForm().reset();
  		this.uploadForm.getForm().loadRecord(record);
  	},
  	
  	//private
  	onAddClick: function() {
		this.iconsGrid.getSelectionModel().clearSelections();
		this.uploadForm.getForm().reset();
		this.enableModify();
		this.uploadForm.saveStatus = "add";
	},
  	
  	//private	
  	onAbort: function() {
  		this.disableModify();
  		this.iconsGrid.getSelectionModel().clearSelections();
  		this.uploadForm.getForm().reset();
  	},
  	
  	//private  	
  	onModify: function() {
  		this.enableModify();
  		var descriptionField = this.uploadForm.find("name","description");
  		if (descriptionField && descriptionField[0]) {
  			descriptionField[0].disable();
  		}
  		this.uploadForm.saveStatus = "modify";
  	},
  	
  	//private  	
  	disableModify: function() {
  		this.addButton.enable();
  		this.modifyButton.disable();
  		this.removeButton.disable();
  		this.saveButton.disable();
  		this.abortButton.disable();
  		this.uploadForm.setFieldsDisabled();
  	},

  	//private
  	enableModify: function() {
  		this.addButton.disable();
  		this.modifyButton.disable();
  		this.removeButton.disable();
  		this.saveButton.enable();
  		this.abortButton.enable();
  		this.uploadForm.setFieldsEnabled(true);
  	},
  	
  	//private  	
  	onSave: function() {
  		var form = this.uploadForm.getForm();
  		//the save status is set only when click to add
  		//button or modify button. It's used to choose the url of
  		//the save request
  		var url = this.urls[this.uploadForm.saveStatus];
		if (form.isValid()) {
			CMDBuild.LoadMask.get().show();
			form.submit({
				method: 'POST',
				url: url,				
				scope: this,
				success: function(form, action) {
					this.publish("cmdg-icons-reload", {
						publisher: this
					});
					this.iconsGrid.store.load();					
				},
				failure: this.requestFailure,
				callback: this.requestCallback
			});
		}
  	},
  	
  	//private  	
  	onRemove: function() {
  		var title = this.translation.alert.title;
		var msg = this.translation.alert.msg;
  		var doRequest = function(btn) {
  			if (btn != "yes") {
  				return
  			}
	  		var selectedRow = this.iconsGrid.getSelectionModel().getSelected();
	  		if (selectedRow) {
	  			var selectedData = selectedRow.json;
	  			CMDBuild.LoadMask.get().show();
				CMDBuild.Ajax.request({
					scope : this,
					important: true,
					url: this.urls.remove,
					params : {
						"name": selectedData.name
					},
					method : 'POST',
					success: function(form, action) {
						this.publish("cmdg-icons-reload", {
							publisher: this
						});
						this.iconsGrid.store.load();					
					},
					failure: this.requestFailure,
					callback: this.requestCallback
		  	 	});
	  		}
  		};
  		Ext.Msg.confirm(title, msg, doRequest, this);
  	},

  	//private
  	requestFailure: this.onAbort,
  	
  	//private  	
  	requestCallback: function() {
  		CMDBuild.LoadMask.get().hide();
		this.disableModify();
  	}
});