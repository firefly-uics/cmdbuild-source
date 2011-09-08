(function() {

	Ext.define("CMDBuild.controller.management.classes.CMModClassController", {
		extend: "CMDBuild.controller.CMBasePanelController",
		mixins: {
			commonFunctions: "CMDBuild.controller.management.common.CMModClasseAndWFCommons"
		},
		constructor: function() {
			this.callParent(arguments);

			this.currentEntryId = null;
			this.currentEntry = null;
			this.currentCard = null;
			this.cardExtensionsProviders = [];

			this.cardPanel = this.view.cardTabPanel.cardPanel;
			this.notePanel = this.view.cardTabPanel.cardNotesPanel;
			this.cardGrid = this.view.cardGrid;

			this.mdPanel = this.view.cardTabPanel.mdPanel;
			this.mdController = new CMDBuild.controller.management.classes.masterDetails.CMMasterDetailsController(this.mdPanel, this);

			this.attachmentsPanel = this.view.cardTabPanel.attachmentPanel;
			this.attachmentsController = new CMDBuild.controller.management.classes.attachments.CMCardAttachmentsController(this.attachmentsPanel);

			this.relationsPanel = this.view.cardTabPanel.relationsPanel;
			this.relationsController = new CMDBuild.controller.management.classes.CMCardRelationsController(this.relationsPanel, this);

			if (typeof this.view.getMapPanel == "function") {
				this.mapController = new CMDBuild.controller.management.classes.CMMapController(this.view.getMapPanel(), this.cardPanel, this);
			} else {
				this.mapController = {
					onEntryTypeSelect: Ext.emptyFn,
					getValues: function() {return false;},
					refresh: Ext.emptyFn
				};
			}

			this.gridSM = this.cardGrid.getSelectionModel();
			this.view.addCardButton.on("cmClick", onAddCardButtonClick, this);

			this.cardGrid.on("itemdblclick", onModifyCardClick, this);
			this.cardGrid.on("cmWrongSelection", onSelectionWentWrong, this);

			this.cardGrid.on("cmVisible", function onCardGridVisible(visible) {
				if (visible 
						&& this.currentEntryId 
						&& this.currentCard) {
					var currentGridSelection = this.gridSM.getSelection();
					if (currentGridSelection 
						&& currentGridSelection[0] && currentGridSelection[0].get("Id") != this.currentCard.get("Id")) {
							this.cardGrid.openCard({
								IdClass: this.currentEntryId,
								Id: this.currentCard.get("Id")
							}, retryWithoutFilter = true);
					}
				}
			}, this);

			this.cardGrid.on("load", function(args) {
				// args[1] is the array with the loaded records
				// so, if there are no records clear the view
				if (args[1] && args[1].length == 0) {
					this.cardPanel.displayMode();
				}
			}, this);

			this.cardGrid.printGridMenu.on("click", onPrintGridMenuClick, this);
			this.gridSM.on("selectionchange", onCardSelected, this);

			// TODO build a separate controller for the cardtab
			this.cardPanel.deleteCardButton.on("click", onDeleteCardClick, this);
			this.cardPanel.modifyCardButton.on("click", onModifyCardClick, this);
			this.cardPanel.cancelButton.on("click", onAbortCardClick, this);
			this.cardPanel.saveButton.on("click", onSaveCardClick, this);

			this.cardPanel.cloneCardButton.on("click", onCloneCardClick, this);
			this.cardPanel.printCardMenu.on("click", onPrintCardMenuClick, this);

			this.cardPanel.graphButton.on("click", onShowGraphClick, this);
			this.relationsPanel.graphButton.on("click", onShowGraphClick, this);

			this.notePanel.saveButton.on("click", onSaveNoteClick, this);
		},

		onViewOnFront: function(selection) {
			if (selection) {
				var newEntryId = selection.get("id"),
					dc = _CMMainViewportController.getDanglingCard(),
					entryIdChanged = this.currentEntryId != newEntryId;

				if (entryIdChanged) {
					this.currentEntryId = newEntryId;
					this.currentEntry = _CMCache.getEntryTypeById(this.currentEntryId);
					this.currentCard = null;

					// sub-controllers
					this.attachmentsController.onEntrySelect(selection);
					this.relationsController.onEntrySelect(selection);
					this.mdController.onEntrySelect(selection);
					this.mapController.onEntryTypeSelect(this.currentEntry);
				}

				if (dc != null) {
					this.view.openCard(dc, retryWithoutFilter = true);
				} else if (entryIdChanged) {
					this.view.onEntrySelected(selection);
				}

			}
		},

		onCardSelected: onCardSelected
	});

	function onCardSelected(sm, selection) {
		if (Ext.isArray(selection)) {
			if (selection.length > 0) {
				this.currentCard = selection[0];
	
				// If the current entryType is a superclass the record has only the value defined
				// in the super class. So, we say to the form to load the remote data.
				var loadRemoteData = this.currentEntry.get("superclass"),
					reloadFields = this.currentEntryId != this.currentCard.get("IdClass");
	
				this.view.cardTabPanel.onCardSelected(this.currentCard, reloadFields, loadRemoteData);
	
				// sub-controllers
				this.attachmentsController.onCardSelected(this.currentCard);
				this.relationsController.onCardSelected(this.currentCard);
				this.mdController.onCardSelected(this.currentCard);
			}
		} else {
			// it was not selected by the grid, so retrive remotely the
			// card info and do the regular selection.

			CMDBuild.ServiceProxy.card.get({
				params: selection,
				scope: this,
				success: function(a,b, response) {
					var raw = response.card;
					if (raw) {
						var c = new CMDBuild.DummyModel(response.card);
						c.raw = raw;
						this.onCardSelected(sm = null, [c]);
					}
				}
			});

		}
	}

	function onSelectionWentWrong() {
		this.view.cardTabPanel.reset(this.currentEntryId);
	}

	function onAddCardButtonClick(p) {
		this.cloneCard = false;
		this.currentCard = null;
		this.classOfCardToAdd = p.classId;
		var reloadFields = this.currentEntryId != this.classOfCardToAdd;
		
		this.view.cardTabPanel.onAddCardButtonClick(this.classOfCardToAdd,reloadFields);
		this.gridSM.deselectAll();
		this.mapController.onAddCardButtonClick();
	}
	
	function onModifyCardClick() {
		this.cloneCard = false;
		this.cardPanel.editMode();
	}
	
	function onCloneCardClick() {
		onModifyCardClick.call(this);
		this.cloneCard = true;
	}
	
	function onDeleteCardClick() {
		function makeRequest(btn) {
			if (btn != 'yes') {
				return;
			}
			CMDBuild.LoadMask.get().show();
			CMDBuild.ServiceProxy.card.remove({
				scope : this,
				important: true,
				params : {
					"IdClass": this.currentEntryId,
					"Id": this.currentCard.get("Id")
				},
				success : function() {
					this.cardGrid.reload();
					this.view.reset(this.currentEntryId);

					_CMCache.onClassContentChanged(this.currentEntryId);
				},
				callback : function() {
					CMDBuild.LoadMask.get().hide();
				}
			});
		};

		Ext.Msg.confirm(CMDBuild.Translation.management.findfilter.msg.attention, CMDBuild.Translation.management.modcard.delete_card_confirm , makeRequest, this);
	}

	function onAbortCardClick() {
		this.classOfCardToAdd = null;
		if (this.currentCard) {
			onCardSelected.call(this, null, [this.currentCard]);
		} else {
			this.cardPanel.reset();
			this.cardPanel.displayMode(enableCMTbar = false);
		}
	}

	function onSaveCardClick() {
		var params = {},
			form = this.cardPanel.getForm(),
			view = this.cardPanel,
			invalidAttributes = this.cardPanel.getInvalidAttributeAsHTML(),
			mapValuesToSend = this.mapController.getValues();

		if (this.currentCard) {
			params = {
				IdClass: this.currentCard.get("IdClass"),
				Id: this.cloneCard ? -1 : this.currentCard.get("Id")
			};
		} else {
			params = {
				IdClass: this.classOfCardToAdd,
				Id: -1
			};
		}

		if (mapValuesToSend) {
			params["geoAttributes"] = mapValuesToSend;
		}

		if (invalidAttributes == null) {
			CMDBuild.LoadMask.get().show();
			form.submit({
				method : 'POST',
				url : 'services/json/management/modcard/updatecard',
				scope: this,
				params: params,
				success : function(form, action) {
					CMDBuild.LoadMask.get().hide();
					this.cardPanel.displayMode();

					var c = {
						Id: action.result.id || params.Id,// if is a new card, the id is given by the request
						IdClass: this.currentEntryId
					};

					this.cardGrid.openCard(c);

					if (!this.cardGrid.cmVisible) {
						// to load the card in the tab-panels
						this.onCardSelected(selectionModel = null, card = c);
					}

					this.mapController.onCardSaved(c);

					_CMCache.onClassContentChanged(this.currentEntryId);
				},

				failure : function() {
					CMDBuild.LoadMask.get().hide();
				}
			});
		} else {
			var msg = Ext.String.format("<p class=\"{0}\">{1}</p>", CMDBuild.Constants.css.error_msg, CMDBuild.Translation.errors.invalid_attributes);
			CMDBuild.Msg.error(null, msg + invalidAttributes, false);
		}

	}
	
	function onPrintCardMenuClick(format) {
		if (typeof format != "string") {
			return
		}
		CMDBuild.LoadMask.get().show();
		CMDBuild.Ajax.request({
			url : 'services/json/management/modreport/printcarddetails',
			params : {
				IdClass: this.currentEntryId,
				Id: this.currentCard.get("Id"),
				format: format
			},
			method : 'POST',
			scope : this,
			success: function(response) {
				CMDBuild.LoadMask.get().hide();
				var popup = window.open("services/json/management/modreport/printreportfactory", "Report", "height=400,width=550,status=no,toolbar=no,scrollbars=yes,menubar=no,location=no,resizable");
				if (!popup) {
					CMDBuild.Msg.warn(CMDBuild.Translation.warnings.warning_message,CMDBuild.Translation.warnings.popup_block);
				}
			},
			callback : function() {
				CMDBuild.LoadMask.get().hide();
	      	}
		});
	}

	function onPrintGridMenuClick(format) {
		if (typeof format != "string") {
			return
		}

		var columns = this.cardGrid.getVisibleColumns();
		CMDBuild.LoadMask.get().show();

		CMDBuild.Ajax.request({
			url: 'services/json/management/modreport/printcurrentview',
			scope: this,
			params: {
				FilterCategory: this.cardGrid.filterCategory,
				IdClass: this.currentEntryId,
				type: format,
				columns: Ext.JSON.encode(columns)
			},
			success: function(response) {
				var popup = window.open("services/json/management/modreport/printreportfactory", "Report", "height=400,width=550,status=no,toolbar=no,scrollbars=yes,menubar=no,location=no,resizable");
				if (!popup) {
					CMDBuild.Msg.warn(CMDBuild.Translation.warnings.warning_message,CMDBuild.Translation.warnings.popup_block);
				}
			},
			callback : function() {
				CMDBuild.LoadMask.get().hide();
			}
		});
	}

	function onShowGraphClick() {
		var classId = this.currentCard.get("IdClass"),
			cardId = this.currentCard.get("Id");

		CMDBuild.Management.showGraphWindow(classId, cardId);
	}

	function onSaveNoteClick() {
		var form = this.notePanel.actualForm.getForm(),
			params = {
				IdClass: this.currentCard.get("IdClass"),
				Id: this.currentCard.get("Id")
			};

		if (form.isValid()) {
			form.submit({
				method : 'POST',
				url : 'services/json/management/modcard/updatecard',
				waitTitle : CMDBuild.Translation.common.wait_title,
				waitMsg : CMDBuild.Translation.common.wait_msg,
				scope: this,
				params: params,
				success : function() {
					this.notePanel.disableModify();
					var val = this.notePanel.actualForm.getValue();
					this.notePanel.displayPanel.setValue(val);
					syncSavedNoteWithModel(this.currentCard, val);
					// TODO: reload history
				}
			});
		}
	}

	// "code reuse" from CMOpenNoteController
	function syncSavedNoteWithModel(card, val) {
		card.set("Notes", val);
		card.commit();
		if (card.raw) {
			card.raw["Notes"] = val;
		}
	}
})();