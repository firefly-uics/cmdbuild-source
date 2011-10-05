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

			this.mon(this, "activate", onActivate, this);
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
			if (!this.writePrivilege) {
				return;
			}

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

			if (enableCmBar && this.writePrivilege) {
				this.enableCMTbar();
			} else {
				this.disableCMTbar();
			}
			
			this.disableCMButtons();
			this.fireEvent("cmdisplaymode");
		},

		onClassSelected: function(id) {
			var privileges = _CMUtils.getClassPrivileges(id);
			this.writePrivilege = privileges.write;

			_CMCache.getAttributeList(id, Ext.bind(fillForm, this));
		},

		onCardSelected: function(card, reloadField, loadRemoteData) {
			var privileges = _CMUtils.getClassPrivileges(card.get("IdClass"));
			this.writePrivilege = privileges.write;

			this.loadRemoteData = loadRemoteData; // used and reset in loadCard;

			if (reloadField) {
				this.danglingCard = card;
				_CMCache.getAttributeList(card.get("IdClass"), Ext.bind(fillForm, this));
			} else {
				this.displayMode(enableCMTbar = true);
				loadCard.call(this, card);
			}
		},

		onAddCardButtonClick: function(idClass, reloadField) {
			this.reset();

			if (this.sideTabPanel) {
				this.sideTabPanel.activateFirst();
			}

			if (reloadField) {
				var cb = Ext.Function.createSequence(Ext.Function.bind(fillForm, this), this.editMode, this);
				_CMCache.getAttributeList(idClass, cb);
			} else {
				this.editMode();
			}
		},

		loadCard: function(card, idClass) {
			// if the panel is set to render the field on activation
			// set the card as dangling
			if (this.paramsForDeferrdFillFormCall) {
				if (typeof card == "object") {
					this.danglingCard = card;
				} else {
					this.danglingCard = {
						get: function(key) {
							var data = {
								Id: card,
								IdClass: idClass
							};

							return data[key];
						}
					};
				}
			} else {
				this.callParent(arguments);
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

		fillForm: fillForm,
		// private, could be overridden
		buildTBar: buildTBar,
		buildButtons: buildButtons
	});

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
		// If the panel is not active, we want defer the population
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
				tabLabel: group,
				bodyCls: "x-panel-body-default-framed",
				cmMaxFieldWidth: this.getWidth() - 50, // See EditablePanel and CMBaseCombo
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

		if (panels.length == 0) {
			// hack to have a framed panel
			// if there are no attributes
			panels = [new CMDBuild.Management.EditablePanel({
				attributes: [],
				frame: false,
				border: false,
				tabLabel: "",
				bodyCls: "x-panel-body-default-framed",
				bodyStyle: {
					padding: "5px"
				}
			})];
		}

		this.sideTabPanel = new CMDBuild.SideTabPanel({
			tabs: panels,
			frame: false,
			border: false,
			flex: 1
		});

		this.add(this.sideTabPanel);

		if (this.danglingCard) {
			loadCard.call(this, this.danglingCard);
			this.danglingCard = null;
		}

		if (editMode || this.forceEditMode) {
			this.editMode();
			this.forceEditMode = false;
		} else {
			this.displayMode(enableCmBar = true);
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

			this.cmTBar = [
				this.modifyCardButton,
				this.deleteCardButton,
				this.cloneCardButton
			];

			this.graphButton = new Ext.button.Button({
				iconCls : "graph",
				text : CMDBuild.Translation.management.graph.action
			});

			if (CMDBuild.Config.graph.enabled=="true") {
				this.cmTBar.push(this.graphButton);
			}

			this.cmTBar.push(this.printCardMenu);
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
})();