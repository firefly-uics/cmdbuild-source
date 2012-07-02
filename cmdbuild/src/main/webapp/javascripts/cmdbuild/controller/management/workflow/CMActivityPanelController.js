(function() {

	var ERROR_TEMPLATE = "<p class=\"{0}\">{1}</p>",
		FLOW_STATUS_CODE = "FlowStatus_code",
		STATE_VALUE_OPEN = "open.running";

	Ext.define("CMDBuild.controller.management.workflow.CMActivityPanelController", {
		extend: "CMDBuild.controller.management.classes.CMCardPanelController",

		mixins: {
			wfStateDelegate: "CMDBuild.state.CMWorkflowStateDelegate"
		},

		constructor: function(v, owner, widgetControllerManager) {
			this.callParent(arguments);

			// this flag is used to define if the user has click on the
			// save or advance button. The difference is that on save 
			// the widgets do nothing and the saved activity goes in display mode.
			// On advance, otherwise, the widgets do the react (save their state) and
			// the saved activity lies in edit mode, to continue the data entry.
			this.isAdvance = false;

			this.mon(this.view, this.view.CMEVENTS.advanceCardButtonClick, this.onAdvanceCardButtonClick, this);
			this.mon(this.view, this.view.CMEVENTS.editModeDidAcitvate, onEditMode, this);
			this.mon(this.view, this.view.CMEVENTS.displayModeDidActivate, onDisplayMode, this);

			_CMWFState.addDelegate(this);
		},

		// wfStateDelegate
		onProcessInstanceChange: function(processInstance) {
			this.clearView();
		},

		// wfStateDelegate
		onActivityInstanceChange: function(activityInstance) {
			_debug("** ** ** ** ** ** ** activity instance ", activityInstance);

			var me = this;
			var processInstance = _CMWFState.getProcessInstance();

			me.view.displayMode();


//				// could have no raw if is the server template
//				data = card.raw || card.data || {CmdbuildExtendedAttributes: []},
//				
//	//			updateWidget = isStateOpen(data) || card._cmNew;
//	
			// at first update the widget because they could depends
			// to the form. The Template resolver starts when the form goes
			// in edit mode, so the widgets must be already ready to done them works

			// TODO update passing null if the process is closed
			me.widgetControllerManager.buildControllers(activityInstance);

			me.view.updateInfo(activityInstance.getPerformerName(), activityInstance.getDescription());

			if (processInstance) {
				// Load always the fields
				me.loadFields(processInstance.getClassId(), function loadFieldsCb() {
					// TODO: manage the advance
					if (activityInstance.isNew()) {
						me.view.editMode();
					}
//					else {
//						me.loadCard(loadRemoteData);
//					}
				});
			}
		},

		// override // deprecated
		onEntryTypeSelected: function(entryType) { _deprecated(); },

		// override // deprecated
		onCardSelected: function(card) {_deprecated(); },

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
			var activityInstance = _CMWFState.getActivityInstance();

			if (activityInstance && activityInstance.isNew()) {
				this.onProcessInstanceChange();
			} else {
				this.onActivityInstanceChange(activityInstance);
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
			var me = this;
			var activityInstance = _CMWFState.getActivityInstance();
			var variables = [];

			if (activityInstance) {
				variables = activityInstance.getVariables();
			}

			_CMCache.getAttributeList(entryTypeId, function(attributes) {

				if (activityInstance.isNew()
//						|| isStateOpen(me.card.data)
					) {
					attributes = filterAttributesInStep(attributes, variables);
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

	// iterate over all the attributes of the Process and
	// take only the ones defined as variables for this step
	function filterAttributesInStep(attributes, variables) {
		var modeConvertionMatrix = {
			READ_ONLY: "read",
			READ_WRITE: "write",
			READ_WRITE_REQUIRED: "required"
		},
		out = [];

		for (var i=0, attr=null; i<attributes.length; ++i) {
			attr = attributes[i];
			for (var j=0, variable=null; j<variables.length; ++j) {
				variable = variables[j];
				if (attr.name == variable.name) {
					attr.fieldmode = modeConvertionMatrix[variable.type];
					out.push(attr);
					break;
				}
			}
		}

		return out;
	}

	function onEditMode() {
		this.editMode = true;
		if (this.widgetControllerManager) {
			this.widgetControllerManager.onCardGoesInEdit();
		}
	}

	function onDisplayMode() {
		this.editMode = false;
	}

	function save() {
		var me = this,
			requestParams = {},
			pi = _CMWFState.getProcessInstance(),
			valid;

		if (pi) {
			 requestParams = {
				Id: pi.getId(),
				IdClass: pi.getClassId(),
				advance: me.isAdvance,
				attributes: Ext.JSON.encode(this.view.getValues())
			};	

			// Business rule: Someone want the validation
			// only if advance and not if want only save the activity
			valid = requestParams.advance ? validate(me) : true;

			if (valid) {

				requestParams["ww"] = Ext.JSON.encode(this.widgetControllerManager.getData(me.advance));
				_debug("save the process with params", requestParams);

				// FIXME: do the request below

//				CMDBuild.LoadMask.get().show();
//				
//				CMDBuild.ServiceProxy.workflow.saveActivity({
//					params: requestParams,
//					scope : this,
//					clientValidation: this.isAdvance, //to force the save request
//					callback: function(operation, success, response) {
//						CMDBuild.LoadMask.get().hide();
//					},
//					success: function(response) {
//						updateCardData(me, response);
//						me.fireEvent(me.CMEVENTS.cardSaved, me.card);
//					}
//				});
			}

		} else {
			throw "There are no processInstance to save";
		}
	}

// Code version management ;)

//	function save() {
//		var me = this,
//			data = this.card.raw,
//			requestParams = {
//				Id: data.Id,
//				IdClass: data.IdClass,
//				ProcessInstanceId: data.ProcessInstanceId,
//				WorkItemId: data.WorkItemId,
//				advance: this.isAdvance,
//				attributes: Ext.JSON.encode(this.view.getValues())
//			},
//
//			// Business rule: Someone want the validation
//			// only if advance and not if want only save the activity
//			valid = requestParams.advance ? validate(me) : true;
//
//		if (valid) {
//			CMDBuild.LoadMask.get().show();
//
//			requestParams["ww"] = Ext.JSON.encode(this.widgetControllerManager.getData(me.advance));
//			CMDBuild.ServiceProxy.workflow.saveActivity({
//				params: requestParams,
//				scope : this,
//				clientValidation: this.isAdvance, //to force the save request
//				callback: function(operation, success, response) {
//					CMDBuild.LoadMask.get().hide();
//				},
//				success: function(response) {
//					updateCardData(me, response);
//					me.fireEvent(me.CMEVENTS.cardSaved, me.card);
//				}
//			});
//		}
//	}

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
			var card = Ext.decode(response.responseText).response;

			me.card.raw = Ext.apply(me.card.raw, {
				Id: card.Id,
				ProcessInstanceId: card.ProcessInstanceId,
				WorkItemId: card.WorkItemId
			});
		}
	}
})();