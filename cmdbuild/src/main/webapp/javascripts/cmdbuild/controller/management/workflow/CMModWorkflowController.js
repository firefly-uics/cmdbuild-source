(function () {

	Ext.define("CMDBuild.controller.management.workflow.CMModWorkflowController", {
		extend: "CMDBuild.controller.CMBasePanelController",
		constructor: function() {
			this.callParent(arguments);
			this.grid = this.view.cardGrid;
			this.gridSM = this.grid.getSelectionModel();
			this.tabPanel = this.view.cardTabPanel;

			// initialize state variables
			this.idClassOfLastAttributesLoaded = null;
			this.currentEntry = null;
			this.currentActivity = null;
			this.widgetsController = {};

			// instantiate sub-controllers
			this.activityPanelController = new CMDBuild.controller.management.workflow.CMActivityPanelController(
				this.tabPanel.activityTab, this);

			// grid events
			this.grid.statusCombo.on("select", onStatusComboSelect, this);
			this.grid.addCardButton.on("cmClick", onAddCardButtonClick, this);
			this.gridSM.on("selectionchange", onActivitySelect, this);

			this.tabPanel.on("cmeditmode", onEditMode, this);
			this.tabPanel.on("cmdisplaymode", onDisplayMode, this);
		},

		onViewOnFront: function(selection) {
			if (selection) {
				this.currentEntry = selection;
				this.view.cardGrid.onEntrySelected(selection);

				// notify sub-controllers
				this.activityPanelController.onEntrySelected(selection);
			}
		},

		onSaveButtonClick: function() {
			// if is advance, also the widgets must be saved, so
			// wait for the template resolver before call the save;

			if (this.view.isAdvance) {
				waitForBusyWidgets.call(this, save, this);
			} else {
				saveActivity.call(this);
			}
		},

		onAbortButtonClick: function() {
			if (this.currentActivity) {
				onActivitySelect.call(this, null, [this.currentActivity]);
			} else {
				this.activityPanelController.onEntrySelected(this.currentEntry);
			}
		},

		onDeleteButtonClick: function() {
			var me = this;
			Ext.Msg.confirm(
				CMDBuild.Translation.management.modworkflow.abort_card, // title
				CMDBuild.Translation.management.modworkflow.abort_card_confirm, // message
				confirmCB);
	
			function confirmCB(btn) {
				if (btn != 'yes') {
					return;
				} else {
					delteActivity.call(me);
				}
			}
		},

		onWFWidgetButtonClick :function(w) {
			var wc = this.widgetsController[w.identifier];
			if (wc) {
				wc.activeView();
			}
		}
	});

	function waitForBusyWidgets(cb, cbScope) {
		new _CMUtils.PollingFunction({
			success: cb,
			failure: function failure() {
				CMDBuild.Msg.error(null,CMDBuild.Translation.errors.busy_wf_widgets, false);
			},
			checkFn: function() {
				// I want exit if there are no busy wc
				return !areThereBusyWidget()
			},
			cbScope: cbScope,
			checkFnScope: this
		}).run();
	}

	function areThereBusyWidget() {
		for (var wc in this.widgetsController) {
			wc = this.widgetsController[wc];
			if (wc.isBusy()) {
				return true;
			} else {
				continue;
			}
		}

		return false;
	}

	function onEditMode() {
		this.editMode = true;
		if (this.widgetsController) {
			_debug(this.widgetsController);
			for (var wc in this.widgetsController) {
				wc = this.widgetsController[wc];
				wc.onEditMode();
			}
		}
	}

	function onDisplayMode() {
		this.editMode = false;
	}

	function onActivitySelect(sm, selection) {
		if (selection.length > 0) {
			var editMode = this.activityPanelController.isAdvance && this.currentActivity.data.Id == selection[0].data.Id;
			updateForActivity.call(this, selection[0], editMode);
		}
	}

	function updateForActivity(a, editMode) {
		var reloadFields = false;

		this.currentActivity = a;
		if (this.idClassOfLastAttributesLoaded != this.currentActivity.data.IdClass) {
			this.idClassOfLastAttributesLoaded = this.currentActivity.data.IdClass;
			reloadFields = true;
		}

		// load the right fields and build the WFwidgets
		this.widgetsController = {};
		var me = this;

		function buildTheWidgetsControllers() {
			var wwWidgets = me.view.getWFWidgets();

			for (var identifier in wwWidgets) {
				var wc = buildWidgetController(wwWidgets[identifier]);
				me.widgetsController[identifier] = wc;

				// on add, the widget controllers are build after that
				// the form go in editing, so call the onEditMode to notify the editing status
				if (me.editMode) {
					wc.onEditMode();
				}
			}
		}

		this.view.updateForActivity(this.currentActivity, {
			reloadFields: reloadFields,
			editMode: editMode,
			cb: buildTheWidgetsControllers
		});
	}

	function buildWidgetController(w) {
		var builders = {
				linkCards: function(w) {
					return new CMDBuild.controller.management.workflow.widgets.CMLinkCardsController(w);
				}
			}

		return builders[w.extattrtype](w);
	}

	function onStatusComboSelect() {
		this.grid.updateStatusParamInStoreProxyConfiguration();
		this.grid.loadPage(1);
	}

	function onAddCardButtonClick(p) {
		this.view.onAddButtonClick();
		this.currentActivity = null;
		
		this.gridSM.deselectAll();

		CMDBuild.ServiceProxy.workflow.getstartactivitytemplate(p.classId, {
			scope: this,
			success: success,
			failure: failure
		});

		function success(response) {
			var template =  Ext.JSON.decode(response.responseText); 
			template.data.ProcessInstanceId = undefined;
			template.data.WorkItemId = undefined;

			updateForActivity.call(this, template, editMode = true);
		}

		function failure() {
			CMDBuild.Msg.error(CMDBuild.Translation.errors.error_message, CMDBuild.Translation.errors.generic_error, true);
		}
	}

	function delteActivity() {
		var me = this;

		CMDBuild.LoadMask.get().show();
		CMDBuild.ServiceProxy.workflow.terminateActivity({
			params: {
				WorkItemId: this.currentActivity.raw["WorkItemId"],
				ProcessInstanceId: this.currentActivity.raw["ProcessInstanceId"]
			},
			success: success,
			failure: failure
		});

		function success(response) {
			CMDBuild.LoadMask.get().hide();
			me.grid.reload();
		}

		function failure() {
			CMDBuild.LoadMask.get().hide();
			CMDBuild.Msg.error(
				CMDBuild.Translation.errors.error_message,
				CMDBuild.Translation.errors.generic_error,
				true);
		}
	}

	function saveActivity() {
		// if the record is new it has not raw data, so use the normal data
		var data = this.currentActivity.raw || this.currentActivity.data,
			requestParams = {
				Id: data.Id,
				IdClass: data.IdClass,
				ProcessInstanceId: data.ProcessInstanceId,
				WorkItemId: data.WorkItemId,
				advance: this.activityPanelController.isAdvance,
				attributes: Ext.JSON.encode(this.activityPanelController.view.getValues())
			};

		var ww = undefined;
		for (var wc in this.widgetsController) {
			wc = this.widgetsController[wc];
			var wcData = wc.getData();
			if (wcData != {} && wcData != null) {
				ww = ww || {};
				ww[wc.wiewIdenrifier] = wcData;
			}
		}

		if (ww) {
			requestParams["ww"] = Ext.JSON.encode(ww);
		}

//		CMDBuild.LoadMask.get().show();
		
		CMDBuild.ServiceProxy.workflow.saveActivity({
			timeout: 90,
			params: requestParams,
			scope : this,
			clientValidation: this.activityPanelController.isAdvance, //to force the save request
			success : function() {
				CMDBuild.LoadMask.get().hide();
				this.activityPanelController.view.reset();
				this.activityPanelController.view.displayMode();

				this.grid.openCard({
					Id: data.Id,
					IdClass: data.IdClass
				});
			},
			failure : function(response, options) {
				CMDBuild.LoadMask.get().hide();
			}
		});
	}
})();