(function() {

	var tr = CMDBuild.Translation.management.modcard;

	Ext.define("CMDBuild.view.management.classes.CMCardPanel", {
		extend: "CMDBuild.view.management.classes.CMCardForm",

		mixins: {
			cmFormFunction: "CMDBUild.view.common.CMFormFunctions"
		},

		constructor: function(conf) {
			Ext.apply(this, conf);
			this.buildTBar();
			this.buildButtons();

			this.callParent(arguments);
		},

		initComponent: function() {
			Ext.apply(this, {
				frame: false,
				border: false,
				hideMode: "offsets",
				bodyCls: "x-panel-body-default-framed",
				bodyStyle: {
					padding: "5px 5px 0 5px"
				},
				cls: "x-panel-body-default-framed",
				autoScroll: true,
				tbar: this.cmTBar,
				buttonAlign: 'center',
				buttons: this.cmButtons,
				layout: {
					type: 'hbox',
					align:'stretch'
				}
			});

			this.callParent(arguments);
		},

		editMode: function() {
			if (this.sideTabPanel) {
				this.sideTabPanel.editMode();
			}

			this.disableCMTbar();
			this.enableCMButtons();

			this.fireEvent("cmeditmode");
		},

		displayMode: function(enableCmBar) {
			if (this.sideTabPanel) {
				this.sideTabPanel.displayMode();
			}
			
			if (enableCmBar) {
				this.enableCMTbar();
			} else {
				this.disableCMTbar();
			}
			
			this.disableCMButtons();
			this.fireEvent("cmdisplaymode");
		},

		onClassSelected: function(id) {
			_CMCache.getAttributeList(id, Ext.bind(fillForm, this));
			this.displayMode(enableCMTbar = false);
		},

		onCardSelected: function(card, reloadField) {
			this.displayMode(enableCMTbar = true);

			if (reloadField) {
				this.danglingCard = card;
				_CMCache.getAttributeList(card.get("IdClass"), Ext.bind(fillForm, this));
			} else {
				this.loadCard(card);
			}
		},

		onAddCardButtonClick: function(idClass, reloadField) {
			this.reset();
			if (reloadField) {
				var cb = Ext.Function.createSequence(Ext.Function.bind(fillForm, this), this.editMode, this);
				_CMCache.getAttributeList(idClass, cb);
			} else {
				this.editMode();
			}
		},

		fillForm: fillForm,
		// private, could be overridden
		buildTBar: buildTBar,
		buildButtons: buildButtons
	});

	function fillForm(attributes, editMode) {
		var panels = [],
			groupedAttr = CMDBuild.Utils.groupAttributes(attributes, false);

		this.removeAll(autoDestroy = true);

		for (var group in groupedAttr) {
			var attributes = groupedAttr[group];
			var p = CMDBuild.Management.EditablePanel.build({
				attributes: attributes,
				frame: false,
				border: false,
				tabLabel: group,
				bodyCls: "x-panel-body-default-framed",
				bodyStyle: {
					padding: "5px"
				}
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
			border: false,
			flex: 1
		});

		this.add(this.sideTabPanel);

		if (this.danglingCard) {
			this.loadCard(this.danglingCard);
			this.danglingCard = null;
		}
		
		if (editMode) {
			this.editMode();
		}
	};

	function buildTBar() {
		if (this.withToolBar) {
			this.deleteCardButton = new Ext.button.Button({
	      		iconCls : "delete",
	      		text : tr.delete_card
	    	});

	    	this.cloneCardButton = new Ext.button.Button({
	      		iconCls : "clone",
	      		text : tr.clone_card
	    	});

	    	this.modifyCardButton = new Ext.button.Button({
	      		iconCls : "modify",
	      		text : tr.modify_card
	    	});
	    	    	
	    	this.printCardMenu = new CMDBuild.PrintMenuButton({      		
	    		text : CMDBuild.Translation.common.buttons.print+" "+CMDBuild.Translation.management.modcard.tabs.card.toLowerCase(),
	    		callback : function() { this.fireEvent("click")},
	    		formatList: ["pdf", "odt"]
	    	});

//			this.openGraphAction = new CMDBuild.Management.GraphActionHandler().getAction(),
			
			this.cmTBar = [
				this.modifyCardButton,
				this.deleteCardButton,
				this.cloneCardButton,
				this.printCardMenu
			];
        }
	};
	
	function buildButtons() {
		if (this.withButtons) {
			this.saveButton = new Ext.button.Button({
				text: CMDBuild.Translation.common.buttons.save
			});
			
			this.cancelButton = new Ext.button.Button( {
			    text: this.readOnlyForm ? CMDBuild.Translation.common.btns.close : CMDBuild.Translation.common.btns.abort
			});
			
			this.cmButtons = [this.saveButton,this.cancelButton];
		}
	};

/*
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
			this.removeFields();
			
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
*/
})();