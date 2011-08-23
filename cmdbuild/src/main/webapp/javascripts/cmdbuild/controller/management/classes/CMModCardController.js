(function() {

	Ext.define("CMDBuild.controller.management.classes.CMModClassController", {
		extend: "CMDBuild.controller.CMBasePanelController",
		
		constructor: function() {
			this.callParent(arguments);

			this.currentEntryId = null;
			this.currentEntry = null;
			this.currentCard = null;

			this.cardPanel = this.view.cardTabPanel.cardPanel;
			this.notePanel = this.view.cardTabPanel.cardNotesPanel;
			this.cardGrid = this.view.cardGrid;

			this.mdPanel = this.view.cardTabPanel.mdPanel;
			this.mdController = new CMDBuild.controller.management.classes.masterDetails.CMMasterDetailsController(this.mdPanel, this);

			this.attachmentsPanel = this.view.cardTabPanel.attachmentPanel;
			this.attachmentsController = new CMDBuild.controller.management.classes.attacchments.CMCardAttacchmentsController(this.attachmentsPanel);

			this.relationsPanel = this.view.cardTabPanel.relationsPanel;
			this.relationsController = new CMDBuild.controller.management.classes.CMCardRelationsController(this.relationsPanel, this);

			this.gridSM = this.cardGrid.getSelectionModel();
			this.view.addCardButton.on("cmClick", onAddCardButtonClick, this);

			this.cardGrid.on("itemdblclick", onModifyCardClick, this);
			this.cardGrid.printGridMenu.on("click", onPrintGridMenuClick, this);
			this.gridSM.on("selectionchange", onCardSelected, this);

			// TODO build a separate controller for the cardtab
			this.cardPanel.deleteCardButton.on("click", onDeleteCardClick, this);
			this.cardPanel.cloneCardButton.on("click", onCloneCardClick, this);
			this.cardPanel.modifyCardButton.on("click", onModifyCardClick, this);
			this.cardPanel.printCardMenu.on("click", onPrintCardMenuClick, this);
			this.cardPanel.cancelButton.on("click", onAbortCardClick, this);
			this.cardPanel.saveButton.on("click", onSaveCardClick, this);

			this.notePanel.saveButton.on("click", onSaveNoteClick, this);
		},

		onViewOnFront: function(selection) {
			if (selection) {
				this.currentEntryId = selection.get("id");
				this.currentEntry = _CMCache.getEntryTypeById(this.currentEntryId);

				if (this.danglingCardToOpen) {
					this.view.openCard(this.danglingCardToOpen);
					this.danglingCardToOpen = null;
				} else {
					this.view.onEntrySelected(selection);
				}

				// sub-controllers
				this.attachmentsController.onEntrySelect(selection);
				this.relationsController.onEntrySelect(selection);
				this.mdController.onEntrySelect(selection);
			}
		},

		/*
		 * p = {
				Id: the id of the card
				IdClass: the id of the class which the card belongs,
				activeFirstTab: true to force the tab panel to return to the first tab 
			}
		 */
		openCard: function(p) {
			var entryType = _CMCache.getEntryTypeById(p.IdClass),
				accordion = _CMMainViewportController.getFirstAccordionWithANodeWithGivenId(p.IdClass),
				modPanel = _CMMainViewportController.findModuleByCMName(entryType.get("type"));

			if (p.activeFirstTab) {
				this.view.cardTabPanel.activeFirstTab();
			}

			this.danglingCardToOpen = p;

			accordion.expand();

			Ext.Function.createDelayed(function() {
			// TODO try to substitute this with the listener "afterlayout"
				accordion.deselect();
				accordion.selectNodeById(p.IdClass);
			}, 100)();
		}
	});

	function onCardSelected(sm, selection) {
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
	}
	
	function onAddCardButtonClick(p) {
		this.cloneCard = false;
		this.currentCard = null;
		this.classOfCardToAdd = p.classId;
		var reloadFields = this.currentEntryId != this.classOfCardToAdd;
		
		this.view.cardTabPanel.onAddCardButtonClick(this.classOfCardToAdd,reloadFields);
		this.gridSM.deselectAll();
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
					this.cardPanel.reset();
					this.cardPanel.displayMode(enableCMTbar = false);

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
		var enableCMTbar = this.currentCard != null;
		this.cardPanel.displayMode(enableCMTbar);
	}
	
	function onSaveCardClick() {
		var params = {},
			form = this.cardPanel.getForm(),
			view = this.cardPanel,
			ex = []; //var ex = this.cardExtensionsProvider;
		
		var invalidAttributes = this.cardPanel.getInvalidAttributeAsHTML();
		
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
		
		if (invalidAttributes == null) {
			CMDBuild.LoadMask.get().show();
			form.submit({
				method : 'POST',
				url : 'services/json/management/modcard/updatecard',
				scope: this,
				params: params,
/*				
				params: (function(ex) {
					var params = {};
					for (var i=0, l=ex.length; i<l; ++i) {
						params[ex[i].getExtensionName()] =  Ext.encode(ex[i].getValues());
					}
					return params;
				})(ex),
*/
				success : function(form, action) {
					CMDBuild.LoadMask.get().hide();
					this.cardPanel.displayMode();

					var c = {
						Id: action.result.id || params.Id,// if is a new card, the id is given by the request
						IdClass: this.currentEntryId
					};

					this.cardGrid.openCard(c);
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
					this.cardGrid.reload();
				}
			});
		}
	}
})();