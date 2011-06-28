(function() {

	Ext.define("CMDBuild.controller.management.classes.CMModClassController", {
		extend: "CMDBuild.controller.CMBasePanelController",
		
		constructor: function() {
			this.callParent(arguments);

			this.currentEntryId = null;
			this.currentCard = null;
			this.cardPanel = this.view.cardTabPanel.cardPanel;
			this.notePanel = this.view.cardTabPanel.cardNotesPanel;
			this.cardGrid = this.view.cardGrid;
			this.mdPanel = this.view.mdPanel;

			this.gridSM = this.cardGrid.getSelectionModel();
			this.view.addCardButton.on("cmClick", onAddCardButtonClick, this);

			this.cardGrid.on("itemdblclick", onModifyCardClick, this);
			this.gridSM.on("selectionchange", onCardSelected, this);

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
				this.view.onEntrySelected(selection);
			}
		}
	});

	function onCardSelected(sm, selection) {
		if (selection.length > 0) {
			this.currentCard = selection[0];
			var reloadFields = this.currentEntryId != this.currentCard.get("IdClass");
			this.view.cardTabPanel.onCardSelected(this.currentCard, reloadFields);
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
//		var title = this.translation.delete_card;
//		var msg = this.translation.delete_card_confirm;
		function makeRequest(btn) {
			if (btn != 'yes') {
				return;
			}
			CMDBuild.LoadMask.get().show();
			CMDBuild.Ajax.request({
				scope : this,
				important: true,
				url : 'services/json/management/modcard/deletecard',
				params : {
					"IdClass": this.currentEntryId,
					"Id": this.currentCard.get("Id")
				},
				method : 'POST',
				success : function() {
					this.cardGrid.reload();
					this.cardPanel.reset();
					this.cardPanel.displayMode(enableCMTbar = false);
				},
				callback : function() {
					CMDBuild.LoadMask.get().hide();
		      	}
	  	 	});
		};

		Ext.Msg.confirm("@@ Title", "@@ msg" , makeRequest, this);
	}
	
	function onAbortCardClick() {
		this.classOfCardToAdd = null;
		var enableCMTbar = this.currentCard != null;
		this.cardPanel.displayMode(enableCMTbar);
	}
	
	function onSaveCardClick() {
		var params = {};
		var form = this.cardPanel.getForm();
		var view = this.cardPanel;
		
		var ex = [] //var ex = this.cardExtensionsProvider;
		
		//var invalidAttributes = this.view.form.getInvalidAttributeAsHTML();
		
		if (this.currentCard) {
			params = {
				IdClass: this.currentCard.get("IdClass"),
				Id: this.cloneCard ? -1 : this.currentCard.get("Id")
			}
		} else {
			params = {
				IdClass: this.classOfCardToAdd,
				Id: -1
			}
		}
		
		//if (invalidAttributes == null) {
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
					this.cardGrid.reload();

					//if (action.result.id) {}	
				},

				failure : function() {
					CMDBuild.LoadMask.get().hide();
		      	}
			});
	//	} else {
	//		var msg = String.format("<p class=\"{0}\">{1}</p>", CMDBuild.Constants.css.error_msg, CMDBuild.Translation.errors.invalid_attributes);
	//		CMDBuild.Msg.error(null, msg + invalidAttributes, false);
	//	}

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
	
	function onSaveNoteClick() {
		var form = this.notePanel.actualForm.getForm();
		var params = {
			IdClass: this.currentCard.get("IdClass"),
			Id: this.currentCard.get("Id")
		}

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