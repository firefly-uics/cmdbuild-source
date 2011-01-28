(function() {
	var groupAttributes = function(attributes) {
		var groups = {};
		var fieldsWithoutGroup = []; 
		for ( var i = 0; i < attributes.length; i++) {
			var attribute = attributes[i];
			if (!this.allowNoteFiled && attribute.name == "Notes") {
				continue;
			} else {
				var attrGroup = attribute.group;
				if (attrGroup) {
					if (!groups[attrGroup]) {
						groups[attrGroup] = [];
					}
					groups[attrGroup].push(attribute);
				} else {
					fieldsWithoutGroup.push(attribute);
				}
			}
		}
		
		if (fieldsWithoutGroup.length > 0) {
			groups[CMDBuild.Translation.management.modcard.other_fields] = fieldsWithoutGroup;
		}
		 
		return groups;
	};
	
	var buildItems = function() {
		this.form = new CMDBuild.Management.CardForm();
		this.items = [this.form];
	};
	
	var buildButtons = function() {
		if (this.withButtons) {
			this.saveButton = new Ext.Button({
				text: CMDBuild.Translation.common.buttons.save,
			    name: "saveButton",
			    handler: this.saveButtonHandler || function() {
				    this.controller.saveCard();
			    }, // to switch controller in CreateModifyCard
			    scope: this,
			    disabled: false
			});
			this.cancelButton = new Ext.Button( {
			    text: this.readOnlyForm ? CMDBuild.Translation.common.btns.close : CMDBuild.Translation.common.btns.abort,
			    name: "cancelButton",
			    handler: this.cancelButtonHandler ||  this.disableModify,
			    scope: this
			});
			this.buttonAlign = "center";
			this.buttons = [this.saveButton,this.cancelButton];
			
			this.form.on("clientValidation", function(form, valid) {
				if (this.saveButton) {
					this.saveButton.setDisabled(!valid);
				}
			}, this);
		}
	};
	
	var buildTBar = function() {
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
	
	
	var resetTab = function() {
		this.currentCardId = -1;
		this.disableModify();
	};
	
	var disableButtons = function() {
		CMDBuild.Utils.foreach(this.buttons, function() {
			this.disable();
		});
	};
	
	var enableButtons = function() {
		CMDBuild.Utils.foreach(this.buttons, function() {
			this.enable();
		});
	};
	
	var enableToolbar = function() {
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
	
	var disableToolbar = function() {
		CMDBuild.Utils.foreach(this.toolbarActions, function() {
			if (this.disable) {
				this.disable();
			}
		});
	};
	
	
	var buildRecordForNewCard = function(eventParams) {
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
	
	CMDBuild.Management.CardTabUI = Ext.extend(Ext.Panel, {
		translation: CMDBuild.Translation.management.modcard,
		hideMode: 'offsets',
		layout: "border",
		frame: false,
		border: false,
		// custom
		withButtons: true,
		withToolBar: true,
		allowNoteFiled: false,
		
		style: { 
        	background: CMDBuild.Constants.colors.blue.background
        },
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
			var groupedAttr = groupAttributes.call(this, attributeList);
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
			this.form.startMonitoring();
		},
		
		disableModify: function() {
			if (this.sideTabPanel) {
				this.sideTabPanel.getCentralPanel().callForeachItem("displayMode");
			}
			disableButtons.call(this);
			enableToolbar.call(this);
			this.form.loadCard(this.currentRecord); // to clear dirty fields
			this.form.stopMonitoring();
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
		}
	});

	Ext.reg('cardtab', CMDBuild.Management.CardTabUI);

	CMDBuild.Management.CardForm = Ext.extend(Ext.form.FormPanel, {
		layout: "fit",
		region: "center",
		border: false,
		frame: false,		
		plugins: [new CMDBuild.FieldSetAddPlugin(), new CMDBuild.FormPlugin(), new CMDBuild.CallbackPlugin()],
		bodyStyle: { 
        	"border-bottom": "1px " + CMDBuild.Constants.colors.blue.border+" solid",
        	"background-color": CMDBuild.Constants.colors.blue.background        	
        },
		autoScroll: true,
		
		//customFunctions
		activeSubpanel: function(subpanelId) {
			this.getLayout().setActiveItem(subpanelId);
		},
		loadCard: function(card) { // a card is a Ext.data.Record
			this.clearForm();
			if (!card) {
				return;
			}
			var data = card.data;
			var mapOfFieldValues = this.getForm().getFieldValues();
			if (mapOfFieldValues) {
				for (var fieldName in mapOfFieldValues) {
					var fields = this.find("name", fieldName);
					for (var i=0, l=fields.length; i<l; ++i) {
						var f = fields[i];
						var value;
						if (f.hiddenName) {
							value = data[f.hiddenName];
						} else {
							value = data[fieldName];
						}
						f.setValue(value);
					}
				}
			}
		}
	});

	Ext.reg('cardform', CMDBuild.Management.CardForm);
})();