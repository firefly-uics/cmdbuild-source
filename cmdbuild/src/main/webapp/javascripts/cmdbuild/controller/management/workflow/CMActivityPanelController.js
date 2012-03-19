(function() {

	var ERROR_TEMPLATE = "<p class=\"{0}\">{1}</p>",
		FLOW_STATUS_CODE = "FlowStatus_code",
		STATE_VALUE_OPEN = "open.running",

		modeConvertionMatrix = {
			VIEW: "read",
			UPDATE: "write",
			REQUIRED: "required"
		};

	Ext.define("CMDBuild.controller.management.workflow.CMActivityPanelController", {
		extend: "CMDBuild.controller.management.classes.CMCardPanelController",

		constructor: function(v, owner, widgetControllerManager) {
			this.callParent(arguments);

			// this flag is used to define if the user has click on the
			// save or advance button. The difference is that on save 
			// the widgets do nothing and the saved activity goes in display mode.
			// On advance, otherwise, the widgets do the react (save their state) and
			// the saved activity lies in edit mode, to continue the data entry.
			this.isAdvance = false;
			this.mon(this.view, this.view.CMEVENTS.advanceCardButtonClick, this.onAdvanceCardButtonClick, this);

			// listen and don't relays
			// this.addEvents("cmeditmode", "cmdisplaymode");
			// this.relayEvents(this.view, ["cmeditmode", "cmdisplaymode"]);
		},

		// override
		onEntryTypeSelected: function(entryType) {
			this.entryType = entryType;

			this.view.displayMode();
			this.view.reset();
		},

		// override
		onCardSelected: function(card) {
			// TODO: copied from CMCardPanelController. Find the way/time to inherits it
			if (!this.view.formIsVisisble()) {
				// defer the calls to update the view
				// because there are several rendering issues
				// when the view will be activate, do the selection
				this.cardToLoadOnActivivate = card;
				return;
			} else {
				this.cardToLoadOnActivivate = null;
			}

			this.card = card;

			if (card == null) {
				return;
			}

			var me = this,
				// could have no raw if is the server template
				data = card.raw || card.data || {CmdbuildExtendedAttributes: []},
				loadRemoteData = this.entryType.get("superclass"),
				updateWidget = isStateOpen(data) || card._cmNew;

			me.view.updateInfo(card);
			me.widgetControllerManager.buildControllers(updateWidget ? card : null);

			if (!this.entryType || !this.card) { return; }

			function loadFieldsCb() {
				if (card._cmNew) {
					me.view.editMode();
				} else {
					me.loadCard(loadRemoteData);
				}
			}

			// reload always the fields because the activity of the
			// same process may be in different stap
			me.loadFields(data.IdClass, loadFieldsCb);
		},

		// override
		onModifyCardClick: function() {
			this.isAdvance = false;
			var data = this.card.raw || this.card.data;

			if (isStateOpen(data)) {
				this.callParent(arguments);
			}
		},

		// override
		onRemoveCardClick: function() {
			var me = this;
			Ext.Msg.confirm(
				CMDBuild.Translation.management.modworkflow.abort_card, // title
				CMDBuild.Translation.management.modworkflow.abort_card_confirm, // message
				confirmCB);

			function confirmCB(btn) {
				if (btn != 'yes') {
					return;
				} else {
					deleteActivity.call(me);
				}
			}
		},

		// override
		onSaveCardClick: function() {
			this.isAdvance = false;
			save.call(this);
		},

		// override
		onAbortCardClick: function() {
			this.isAdvance = false;
			if (this.card && this.card._cmNew) {
				this.clearView();
				this.onCardSelected(null);
			} else {
				this.onCardSelected(this.card);
			}
		},

		onAdvanceCardButtonClick: function() {
			this.isAdvance = true;
			this.widgetControllerManager.waitForBusyWidgets(save, this);
		},

		clearView: function() {
			this.view.clear();
		},

		// override
		loadFields: function(entryTypeId, cb) {
			var me = this,
				data = this.card.raw || this.card.data;

			_CMCache.getAttributeList(entryTypeId, function(attributes) {

				if (isStateOpen(data) || me.card._cmNew) {
					attributes = filterAttributesInStep(attributes, data);
				}

				me.view.fillForm(attributes, editMode = false);

				if (cb) {
					cb();
				}
			});
		},

		loadCardStandardCallBack: function(card) {
			var me = this,
				data = card.raw || card.data,
				editable = (isStateOpen(data) && data.priv_write);

			me.view.loadCard(card);
			if (me.isAdvance) {
				me.isAdvance = false;
				editable ? me.view.editMode() : me.view.displayModeForNotEditableCard();
			} else {
				if (editable) {
					me.view.displayMode(enableTbar = true);
				} else {
					me.view.displayModeForNotEditableCard();
				}

				if (isNotStoppable(data)) {
					me.view.disableStopButton();
				}
			}
		},

		// override
		doFormSubmit: Ext.emptyFn,
		onSaveSuccess: Ext.emptyFn
	});

	function isNotStoppable(data) {
		return !data.stoppable;
	}

	function isStateOpen(data) {
		return data[FLOW_STATUS_CODE] == STATE_VALUE_OPEN;
	}

	function deleteActivity() {
		var me = this;

		me.clearView();

		CMDBuild.LoadMask.get().show();
		CMDBuild.ServiceProxy.workflow.terminateActivity({
			params: {
				WorkItemId: this.card.raw["WorkItemId"],
				ProcessInstanceId: this.card.raw["ProcessInstanceId"]
			},
			success: success,
			failure: failure
		});

		function success(response) {
			CMDBuild.LoadMask.get().hide();
			me.fireEvent(me.CMEVENTS.cardRemoved);
		}

		function failure() {
			CMDBuild.LoadMask.get().hide();
			CMDBuild.Msg.error(
				CMDBuild.Translation.errors.error_message,
				CMDBuild.Translation.errors.generic_error,
				true);
		}
	}

	function filterAttributesInStep(attributes, data) {
		// the attribute to show have a index, so skip the
		// field without index and fill the form
		var cleanedAttrs = []; 

		for (var a in attributes) {
			a = attributes[a];
			var index = data[a.name + "_index"];
			if (index != undefined && index > -1) {
				var mode = data[a.name + "_type"];
				a.fieldmode = modeConvertionMatrix[mode];
				cleanedAttrs[index] = a;
			}
		}

		return cleanedAttrs;
	}

	function onEditMode() {
		_debug("onEditMode");
		this.editMode = true;
		if (this.widgetsController) {
			for (var wc in this.widgetsController) {
				wc = this.widgetsController[wc];
				wc.onEditMode();
			}
		}
	}

	function onDisplayMode() {
		_debug("onDisplayMode");
		this.editMode = false;
	}

	function save() {
		var me = this,
			data = this.card.raw,
			requestParams = {
				Id: data.Id,
				IdClass: data.IdClass,
				ProcessInstanceId: data.ProcessInstanceId,
				WorkItemId: data.WorkItemId,
				advance: this.isAdvance,
				attributes: Ext.JSON.encode(this.view.getValues())
			},
			// Business rule: Someone want the validation
			// only if advance and not if want only save the activity
			valid = requestParams.advance ? validate(me) : true;

		if (valid) {
			CMDBuild.LoadMask.get().show();

			requestParams["ww"] = Ext.JSON.encode(this.widgetControllerManager.getData(me.advance));
			CMDBuild.ServiceProxy.workflow.saveActivity({
				params: requestParams,
				scope : this,
				clientValidation: this.isAdvance, //to force the save request
				callback: function(operation, success, response) {
					CMDBuild.LoadMask.get().hide();
				},
				success: function(response) {
					updateCardData(me, response);
					me.fireEvent(me.CMEVENTS.cardSaved, me.card);
				}
			});
		}
	}

	function validateForm(me) {
		var invalidAttributes = me.view.getInvalidAttributeAsHTML();

		if (invalidAttributes != null) {
			var msg = Ext.String.format("<p class=\"{0}\">{1}</p>", CMDBuild.Constants.css.error_msg, CMDBuild.Translation.errors.invalid_attributes);
			CMDBuild.Msg.error(null, msg + invalidAttributes, false);

			return false;
		} else {
			return true;
		}
	}

	function validate(me) {
		var valid = validateForm(me),
			wrongWidgets = me.widgetControllerManager.getWrongWFAsHTML();

		if (wrongWidgets != null) {
			valid = false;
			var msg = Ext.String.format(ERROR_TEMPLATE
					, CMDBuild.Constants.css.error_msg
					, CMDBuild.Translation.errors.invalid_extended_attributes);
			CMDBuild.Msg.error(null, msg + wrongWidgets, popup = false);
		}

		return valid;
	}

	function updateCardData(me, response) {
		if (me.card) {
			var card = Ext.decode(response.responseText);

			me.card.raw = Ext.apply(me.card.raw, {
				Id: card.Id,
				ProcessInstanceId: card.ProcessInstanceId,
				WorkItemId: card.WorkItemId
			});
		}
	}
})();