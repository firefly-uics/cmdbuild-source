(function() {
	CMDBuild.Management.CardTabUI = Ext.extend(Ext.Panel, {
		translation: CMDBuild.Translation.management.modcard,
		hideMode: 'offsets',
		layout: "border",
		frame: false,
		border: false,
		baseCls: CMDBuild.Constants.css.bg_blue,
		
		// custom
		withButtons: true,
		withToolBar: true,
		allowNoteFiled: false,
		
		initComponent: function() {
			buildTBar.call(this);
			buildItems.call(this);
			buildButtons.call(this);
            CMDBuild.Management.CardTabUI.superclass.initComponent.apply(this, arguments);
		},
		
		initForClass: function(eventParams) {
			var classId = eventParams.classId;
			if (this.currentClassId != classId) {
				this.currentClassId = classId;
				var table = CMDBuild.Cache.getTableById(classId);
				this.currentClassPrivileges = {
						priv_create: table.priv_create,
						priv_write: table.priv_write
				};
				this.isSuperclass = table.superclass;
				this.isSimpleTable = table.tableType == "simpletable";
				resetTab.call(this);				
				this.doLayout();
			}
		},
		
		buildTabbedPanel: function(attributeList) {
			this.form.removeAll(true);
			var panels = [];
			var groupedAttr = CMDBuild.Utils.groupAttributes(attributeList, this.allowNoteFiled);
			for (var group in groupedAttr) {
				var attributes = groupedAttr[group];
				var p = CMDBuild.Management.EditablePanel.build({
					attributes: attributes,
					frame: false,
					border: false,
					tabLabel: group
				});
				if (p) {
					panels.push(p);
				}
			}
			if (this.sideTabPanel) {
				delete this.sideTabPanel;
			}
			this.sideTabPanel = new CMDBuild.SideTabPanel({
				tabs: panels,
				frame: false,
				border: false
			});
			this.form.add(this.sideTabPanel);
			this.form.add(new Ext.form.Hidden({
				name: 'IdClass',
				value: this.currentClassId
			}));	
			this.form.add(new Ext.form.Hidden({
				name: 'Id'
			}));
			this.doLayout();
		},
		
		loadCard: function(attributeList, eventParams) {
			if (attributeList) {
				this.buildTabbedPanel(attributeList);
			}
			this.currentCardId = eventParams.record.data.Id;
			this.currentRecord = eventParams.record;
			this.currentCardPrivileges = {
				create: eventParams.record.data.priv_create,
				write: eventParams.record.data.priv_write
			};
			this.form.loadCard(this.currentRecord);
			
			if (eventParams.enableModify) {
				this.enableModify();
			} else if (eventParams.silentEnableModify) {
				this.silentEnableModify();
			} else {
				this.disableModify();
			}
		},
		
		enableModify: function() {
			this.silentEnableModify();
			
			this.publish("cmdb-enablemodify-card", {
				publisher: this,
				newCard: this.currentCardId == -1
			});
		},
		
		silentEnableModify: function() {
			if (!this.currentCardPrivileges.write ||
					this.readOnlyForm) {
				this.disableModify();
				return;
			}
			if (this.sideTabPanel) { // show the form
				this.sideTabPanel.getCentralPanel().callForeachItem("editMode");
			}
			enableButtons.call(this);
			disableToolbar.call(this);
		},
		
		disableModify: function() {
			if (this.sideTabPanel) {
				this.sideTabPanel.getCentralPanel().callForeachItem("displayMode");
			}
			disableButtons.call(this);
			enableToolbar.call(this);
			this.form.loadCard(this.currentRecord); // to clear dirty fields			
			this.publish('cmdbuild-card-disablemodify', {publisher: this});
		},
		
		newCard: function(eventParams) {
			if (eventParams.clone) {
				this.currentRecord = Ext.ux.clone(this.currentRecord);
				this.currentRecord.data.Id = -1;
			} else {
				var priv = CMDBuild.Utils.getClassPrivileges(eventParams.classId);
				this.currentCardPrivileges = Ext.apply({}, priv);
				this.currentRecord = buildRecordForNewCard.call(this, eventParams);
			}
			this.currentCardId = -1;
			this.form.loadCard(this.currentRecord);
			if (eventParams.silentEnableModify) {
				// used in create modify card
				// remove it with the migration to 
				// MVC architecture
				this.silentEnableModify();
			} else {
				this.enableModify();
			}
		},
		
		removeFields: function() {
			this.form.removeAll();
			disableToolbar();
		},
		
		getForm: function() {
			return this.form.getForm();
		},
		
		getInvalidField: function() {
			return this.form.getInvalidField();
		},
		
		getInvalidAttributeAsHTML: function() {
			return this.form.getInvalidAttributeAsHTML();
		}
	});
	
	function buildItems() {
		this.form = new CMDBuild.Management.CardForm({
			layout: 'fit',
			region: "center",
			baseCls: CMDBuild.Constants.css.bottom_border_blue
		});
		this.items = [this.form];
	};
	
	function buildButtons() {
		if (this.withButtons) {
			this.saveButton = new Ext.Button({
				text: CMDBuild.Translation.common.buttons.save,
			    name: "saveButton",
			    handler: this.saveButtonHandler || function() {
				    this.controller.saveCard();
			    }, // to switch controller in CreateModifyCard
			    scope: this
			});
			this.cancelButton = new Ext.Button( {
			    text: this.readOnlyForm ? CMDBuild.Translation.common.btns.close : CMDBuild.Translation.common.btns.abort,
			    name: "cancelButton",
			    handler: this.cancelButtonHandler ||  this.disableModify,
			    scope: this
			});
			this.buttonAlign = "center";
			this.buttons = [this.saveButton,this.cancelButton];
		}
	};
	
	function buildTBar() {
		if (this.withToolBar) {
			this.deleteCardAction = new Ext.Action({
	      		iconCls : "delete",
	      		text : this.translation.delete_card,
	      		handler : this.controller.deleteCard,
	      		scope : this,
	      		disabled: true
	    	});

	    	this.cloneCardAction = new Ext.Action({
	      		iconCls : "clone",
	      		text : this.translation.clone_card,
	      		handler : function() {
	      			this.publish('cmdb-new-card', {clone: true});
	      		},
	      		scope : this,
	      		disabled: true
	    	});

	    	this.modifyCardAction = new Ext.Action({
	      		iconCls : "modify",
	      		text : this.translation.modify_card,
				handler : this.enableModify,
	      		scope: this,
	      		disabled: true
	    	});
	    	    	
	    	this.printCardMenu = new CMDBuild.PrintMenuButton({      		
	    		text : CMDBuild.Translation.common.buttons.print+" "+CMDBuild.Translation.management.modcard.tabs.card.toLowerCase(),
	    		callback : this.controller.printCard,
	    		formatList: ["pdf", "odt"],
	    		scope: this,
	    		disabled: true
	    	});

			this.openGraphAction = new CMDBuild.Management.GraphActionHandler().getAction(),
			this.openGraphAction.disable();
			
			this.toolbarActions = [
				this.modifyCardAction,
				this.deleteCardAction,
				this.cloneCardAction,
				this.openGraphAction, 
				this.printCardMenu				
			];
			this.tbar = this.toolbarActions;
        }
	};
	
	
	function resetTab() {
		this.currentCardId = -1;
		this.disableModify();
	};
	
	function disableButtons() {
		CMDBuild.Utils.foreach(this.buttons, function() {
			this.disable();
		});
	};
	
	function enableButtons() {
		CMDBuild.Utils.foreach(this.buttons, function() {
			this.enable();
		});
	};
	
	function enableToolbar() {
		CMDBuild.Utils.foreach(this.toolbarActions, function() {
			if (this.enable) {
				this.enable();
			}
		});
		
		if (this.isSimpleTable) {
			this.printCardMenu.disable();
			this.openGraphAction.disable();
		}
		
		if (this.toolbarActions && (this.currentCardId == -1 || !this.currentCardPrivileges.write)) {
			this.modifyCardAction.disable();
			this.cloneCardAction.disable();
			this.deleteCardAction.disable();
		}
	};
	
	function disableToolbar() {
		CMDBuild.Utils.foreach(this.toolbarActions, function() {
			if (this.disable) {
				this.disable();
			}
		});
	};
	
	
	function buildRecordForNewCard(eventParams) {
		var recordForNewCard = Ext.data.Record.create( [ {
		    name: 'Id',
		    mapping: 'Id'
		}, {
		    name: 'IdClass',
		    mapping: 'IdClass'
		} ]);

		return new recordForNewCard( {
		    IdClass: eventParams.classId,
		    Id: -1
		});
	};
	
	Ext.reg('cardtab', CMDBuild.Management.CardTabUI);
})();