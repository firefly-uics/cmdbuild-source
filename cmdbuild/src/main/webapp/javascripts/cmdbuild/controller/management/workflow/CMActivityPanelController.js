(function() {

	var ERROR_TEMPLATE = "<p class=\"{0}\">{1}</p>",
		FLOW_STATUS_CODE = "FlowStatus_code",
		STATE_VALUE_OPEN = "open.running";

	Ext.define("CMDBuild.controller.management.workflow.CMActivityPanelControllerDelegate", {
		onCardSaved: Ext.emptyFn
	});

	Ext.define("CMDBuild.controller.management.workflow.CMActivityPanelController", {
		extend: "CMDBuild.controller.management.classes.CMCardPanelController",

		mixins: {
			wfStateDelegate: "CMDBuild.state.CMWorkflowStateDelegate"
		},

		constructor: function(v, owner, widgetControllerManager, delegate) {
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
			this.setDelegate(delegate || new CMDBuild.controller.management.workflow.CMActivityPanelControllerDelegate());
		},

		setDelegate: function(d) {
			CMDBuild.validateInterface(d, "CMDBuild.controller.management.workflow.CMActivityPanelControllerDelegate");
			this.delegate = d;
		},

		// wfStateDelegate
		onProcessInstanceChange: function(processInstance) {
			this.clearView();
		},

		// wfStateDelegate
		onActivityInstanceChange: function(activityInstance) {
			var me = this;
			var processInstance = _CMWFState.getProcessInstance();

			// reduce the layouts work while
			// fill the panel and build the widgets.
			// Resume it at the end
			// and force a layout update
			Ext.suspendLayouts();

			me.view.displayMode(enableToolbar = true);

			// at first update the widget because they could depends
			// to the form. The Template resolver starts when the form goes
			// in edit mode, so the widgets must be already ready to done them works

			var updateWidget = processInstance.isStateOpen() || activityInstance.isNew();
			if (updateWidget) {
				me.widgetControllerManager.buildControllers(activityInstance);
			} else {
				me.widgetControllerManager.buildControllers(null);
			}

			me.view.updateInfo(activityInstance.getPerformerName(), activityInstance.getDescription());

			// Load always the fields
			me.loadFields(processInstance.getClassId(), function loadFieldsCb() {
				// TODO: manage the advance
				if (activityInstance.isNew()) {
					me.view.editMode();
				} else {
					me.fillFormWidthProcessInstanceData(processInstance);
				}
			});

			Ext.resumeLayouts();
			this.view.doLayout();
		},

		// override // deprecated
		onEntryTypeSelected: function(entryType) { _deprecated(); },

		// override // deprecated
		onCardSelected: function(card) {_deprecated(); },

		// override
		onModifyCardClick: function() {
			this.isAdvance = false;
			var pi = _CMWFState.getProcessInstance();

			if (pi && pi.isStateOpen()) {
				// FIXME: check privileges
				this.view.editMode();
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
			var processInstance = _CMWFState.getProcessInstance();
			var variables = [];

			if (activityInstance) {
				variables = activityInstance.getVariables();
			}

			_CMCache.getAttributeList(entryTypeId, function(attributes) {

				if (activityInstance.isNew()
						|| processInstance.isStateOpen()) {

					attributes = filterAttributesInStep(attributes, variables);
				} else {
					// if here, we have a closed process, so show
					// all the attributes
				}

				me.view.fillForm(attributes, editMode = false);

				if (cb) {
					cb();
				}
			});
		},

		fillFormWidthProcessInstanceData: function(processInstance) {

			if (processInstance == null) {return;}

			var me = this,
				editable = true; //FIXME: manage privileges (isStateOpen(data) && data.priv_write);

			me.view.loadCard(new CMDBuild.DummyModel(processInstance.getValues()));

			if (me.isAdvance) {
				me.isAdvance = false;
				editable ? me.view.editMode() : me.view.displayModeForNotEditableCard();
			} else {
				if (editable) {
					me.view.displayMode(enableTbar = true);
				} else {
					me.view.displayModeForNotEditableCard();
				}

				// FIXME: manage stoppable

//				if (isNotStoppable(data)) {
//					me.view.disableStopButton();
//				}
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

				CMDBuild.LoadMask.get().show();

				CMDBuild.ServiceProxy.workflow.saveActivity({
					params: requestParams,
					scope : this,
					clientValidation: this.isAdvance, //to force the save request
					callback: function(operation, success, response) {
						CMDBuild.LoadMask.get().hide();
					},
					success: function(operation, requestConfiguration, decodedResponse) {
						_debug(arguments);
						this.delegate.onCardSaved(decodedResponse.response.Id);
					}
				});
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
})();