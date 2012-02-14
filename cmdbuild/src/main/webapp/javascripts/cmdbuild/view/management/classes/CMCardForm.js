(function() {
	var tr = CMDBuild.Translation.management.modcard;

	Ext.define("CMDBuild.view.management.classes.CMCardForm", {
		extend: "Ext.form.Panel",

		mixins: {
			cmFormFunctions: "CMDBUild.view.common.CMFormFunctions"
		},

		constructor: function(conf) {
			Ext.apply(this, conf);
			this.CMEVENTS = {
				saveCardButtonClick: "cm-save",
				abortButtonClick: "cm-abort",
				removeCardButtonClick: "cm-remove",
				modifyCardButtonClick: "cm-modify",
				cloneCardButtonClick: "cm-clone",
				printCardButtonClick: "cm-print",
				openGraphButtonClick: "cm-graph",
				formFilled: "cmFormFilled",
				editModeDidAcitvate: "cmeditmode",
				displayModeDidActivate: "cmdisplaymode"
			};

			this.addEvents(this.CMEVENTS.saveCardButtonClick);
			this.addEvents(this.CMEVENTS.abortButtonClick);
			this.addEvents(this.CMEVENTS.removeCardButtonClick);
			this.addEvents(this.CMEVENTS.modifyCardButtonClick);
			this.addEvents(this.CMEVENTS.cloneCardButtonClick);
			this.addEvents(this.CMEVENTS.printCardButtonClick);
			this.addEvents(this.CMEVENTS.openGraphButtonClick);
			this.addEvents(this.CMEVENTS.editModeDidAcitvate);
			this.addEvents(this.CMEVENTS.displayModeDidActivate);

			this.buildTBar();
			this.buildButtons();

			this.callParent(arguments);

			this.mon(this, "activate", onActivate, this);
		},

		initComponent: function() {
			Ext.apply(this, {
				frame: false,
				border: false,
				hideMode: "offsets",
				bodyCls: "x-panel-body-default-framed cmbordertop",
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
			if (this.tabPanel) {
				this.tabPanel.editMode();
			}

			this.disableCMTbar();
			this.enableCMButtons();

			this.fireEvent(this.CMEVENTS.editModeDidAcitvate);
		},

		displayMode: function(enableCmBar) {
			if (this.tabPanel) {
				this.tabPanel.displayMode();
			}

			if (enableCmBar) {
				this.enableCMTbar();
			} else {
				this.disableCMTbar();
			}

			this.disableCMButtons();
			this.fireEvent(this.CMEVENTS.displayModeDidActivate);
		},

		displayModeForNotEditableCard: function() {
			this.displayMode(enableCMBar = false);
			if (this.printCardMenu) {
				this.printCardMenu.enable();
			}
			if (this.graphButton) {
				this.graphButton.enable();
			}
		},

		// fill the form with the data in the card
		loadCard: function(card) {
			this.reset();
			if (!card) { return; }

			if (typeof card == "object") {
				_fillWithData.call(this, card.raw || card.data);
			} else {
				throw "Card must be an object";
			}
		},

		canReconfigureTheForm: function() {
			var out = true;
			try {
				out = this.ownerCt.layout.getActiveItem() == this;
			} catch (e) {
				// if fails, the panel is not in a TabPanel, so don't defer the call
			}

			return out;
		},

		// popolate the form with the right subpanels and fields
		fillForm: fillForm,

		// private, allow simply configuration is subclassing
		buildTBar: buildTBar,

		// private, allow simply configuration is subclassing
		buildButtons: buildButtons,

		getInvalidField: function() {
			var fields = this.getForm().getFields(),
				invalid = [];

			fields.each(function(field) {
				if (!field.isValid()) {
					invalid.push(field);
				}
			});
			
			return invalid;
		},

		hasDomainAttributes: function() {
			var fields = this.getForm().getFields().items;

			for (var i=0, l=fields.length; i<l; ++i) {
				if (fields[i].cmDomainAttribute) {
					return true;
				}
			};

			return false;
		},

		getInvalidAttributeAsHTML: function() {
			var fields = this.getInvalidField();
			var alreadyAdded = {};

			if (fields.length == 0) {
				return null;
			} else {
				var out = "<ul>";
				for (var i=0, l=fields.length; i<l; ++i) {
					var attribute = fields[i].CMAttribute;
					var item="";
					if (attribute) {
						if (alreadyAdded[attribute.description]) {
							continue;
						} else {
							alreadyAdded[attribute.description] = true;
							if (attribute.group) {
								item = attribute.group + " - ";
							}
							out += "<li>" + item + attribute.description + "</li>";
						}
					}
				}

				return out+"</ul>";
			}
		},

		toString: function() {
			return "CMCardForm";
		}
	});

	function _fillWithData(data, referenceAttributes) {
		var fields = this.getForm().getFields();
		addReferenceAttrsToData(data, referenceAttributes);

		if (fields) {
			fields.each(function(f) {
				try {
					f.setValue(data[f.name]);
					if (typeof f.isFiltered == "function" && f.isFiltered()) {
						f.setServerVarsForTemplate(data);
					}
				} catch (e){
					_debug("I can not set the value for " + f.name);
				}
			});
		}
		this.fireEvent(this.CMEVENTS.formFilled);
	}

	// FIXME: probably never reached 'couse the reference's attributes are added
	// in the controller
	function addReferenceAttrsToData(data, referenceAttributes) {
		for (var referenceName in referenceAttributes || {}) {
			var attributes = referenceAttributes[referenceName];
			
			for (var attributeName in attributes) {
				var fullName = "_" + referenceName + "_" + attributeName,
					value = attributes[attributeName];

				data[fullName] = value;
			}
		}
	}

	function onActivate() {
		if (this.paramsForDeferrdFillFormCall) {
			var p = this.paramsForDeferrdFillFormCall;
			fillForm.call(this, p.attributes, p.editMode);
		}
	}

	function loadCard(card) {
		if (this.loadRemoteData || this.hasDomainAttributes()) {
			this.loadCard(card.get("Id"), card.get("IdClass"));
		} else {
			this.loadCard(card);
		}

		this.loadRemoteData = false;
	}

	function fillForm(attributes, editMode) {
		// TODO: Now CMCardPanelController check if it is possible to load the fields.
		// Check if some other subclass of CMCardPanController need it
		// and remove from here

		// If the panel is not active, we defer the population
		// with the field, because this allows a billion of mystical rendering issues
		var deferOperation = !this.canReconfigureTheForm();

		if (deferOperation) {
			this.paramsForDeferrdFillFormCall = {
				attributes: attributes,
				editMode: editMode
			};

			return;
		}

		this.paramsForDeferrdFillFormCall = null;

		var panels = [],
			groupedAttr = CMDBuild.Utils.groupAttributes(attributes, false);

		this.removeAll(autoDestroy = true);

		for (var group in groupedAttr) {
			var attributes = groupedAttr[group];
			var p = CMDBuild.Management.EditablePanel.build({
				attributes: attributes,
				frame: false,
				border: false,
				title: group,
				bodyCls: "x-panel-body-default-framed",
				bodyStyle: {
					padding: "5px"
				}
			});

			if (p) {
				panels.push(p);
			}
		}

		if (this.tabPanel) {
			delete this.tabPanel;
		}

		if (panels.length == 0) {
			// hack to have a framed panel
			// if there are no attributes
			panels = [new CMDBuild.Management.EditablePanel({
				attributes: [],
				frame: false,
				border: false,
				title: "",
				bodyCls: "x-panel-body-default-framed",
				bodyStyle: {
					padding: "5px"
				}
			})];
		}

		this.tabPanel = new CMDBuild.view.management.common.CMTabPanel({
			items: panels,
			frame: false,
			flex: 1
		});

		this.add(this.tabPanel);

		if (this.danglingCard) {
			loadCard.call(this, this.danglingCard);
			this.danglingCard = null;
		}

		if (editMode || this.forceEditMode) {
			this.editMode();
			this.forceEditMode = false;
		}
	};

	function buildTBar() {
		if (this.withToolBar) {
			var me = this;
			this.deleteCardButton = new Ext.button.Button({
				iconCls : "delete",
				text : tr.delete_card,
				handler: function() {
					me.fireEvent(me.CMEVENTS.removeCardButtonClick);
				}
			});

			this.cloneCardButton = new Ext.button.Button({
				iconCls : "clone",
				text : tr.clone_card,
				handler: function() {
					me.fireEvent(me.CMEVENTS.cloneCardButtonClick);
				}
			});

			this.modifyCardButton = new Ext.button.Button({
				iconCls : "modify",
				text : tr.modify_card,
				handler: function() {
					me.fireEvent(me.CMEVENTS.modifyCardButtonClick);
				}
			});

			this.printCardMenu = new CMDBuild.PrintMenuButton({
				text : CMDBuild.Translation.common.buttons.print+" "+CMDBuild.Translation.management.modcard.tabs.card.toLowerCase(),
				callback : function() { this.fireEvent("click");},
				formatList: ["pdf", "odt"]
			});

			this.mon(this.printCardMenu, "click", function(format) {
				me.fireEvent(me.CMEVENTS.printCardButtonClick, format);
			});

			this.cmTBar = [
				this.modifyCardButton,
				this.deleteCardButton,
				this.cloneCardButton
			];

			this.graphButton = new Ext.button.Button({
				iconCls : "graph",
				text : CMDBuild.Translation.management.graph.action,
				handler: function() {
					me.fireEvent(me.CMEVENTS.openGraphButtonClick);
				}
			});

			if (CMDBuild.Config.graph.enabled=="true") {
				this.cmTBar.push(this.graphButton);
			}

			this.cmTBar.push(this.printCardMenu);
		}
	};

	function buildButtons() {
		if (this.withButtons) {
			var me = this;
			this.saveButton = new Ext.button.Button({
				text: CMDBuild.Translation.common.buttons.save,
				handler: function() {
					me.fireEvent(me.CMEVENTS.saveCardButtonClick);
				}
			});

			this.cancelButton = new Ext.button.Button( {
				text: this.readOnlyForm ? CMDBuild.Translation.common.btns.close : CMDBuild.Translation.common.btns.abort,
				handler: function() {
					me.fireEvent(me.CMEVENTS.abortButtonClick);
				}
			});

			this.cmButtons = [this.saveButton,this.cancelButton];
		}
	};
})();
