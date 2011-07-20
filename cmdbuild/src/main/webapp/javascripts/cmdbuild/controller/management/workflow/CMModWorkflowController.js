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

			// instantiate sub-controllers
			this.activityPanelController = new CMDBuild.controller.management.workflow.CMActivityPanelController(
				this.tabPanel.activityTab, this);

			// grid events
			this.grid.statusCombo.on("select", onStatusComboSelect, this);
			this.grid.addCardButton.on("cmClick", onAddCardButtonClick, this);
			this.gridSM.on("selectionchange", onActivitySelect, this);
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
			// TODO 3 to 4 check the extended attr

			if (this.currentActivity.data.ProcessInstanceId == "tostart") {
				startProcess.call(this);
			} else {
				updateActivity.call(this);
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
		}
	});

	function onActivitySelect(sm, selection) {
		if (selection.length > 0) {
			var reloadFields = false,
				editMode = this.activityPanelController.isAdvance && this.currentActivity.data.Id == selection[0].data.Id;

			this.currentActivity = selection[0];
			if (this.idClassOfLastAttributesLoaded != this.currentActivity.data.IdClass) {
				this.idClassOfLastAttributesLoaded = this.currentActivity.data.IdClass;
				reloadFields = true;
			}

			this.activityPanelController.onActivitySelect(this.currentActivity, reloadFields, editMode);
		}
	}

	function onStatusComboSelect() {
		this.grid.updateStatusParamInStoreProxyConfiguration();
		this.grid.loadPage(1);
	}

	function onAddCardButtonClick(p) {
		this.currentActivity = null;
		this.gridSM.deselectAll();

		CMDBuild.ServiceProxy.workflow.getstartactivitytemplate(p.classId, {
			scope: this,
			success: success,
			failure: failure
		});

		function success(response) {
			this.currentActivity = Ext.JSON.decode(response.responseText);
			var p = {
				edit:true,
				isnew:true,
				activity: this.currentActivity
			}
			this.tabPanel.onAddCardButtonClick(p);

			this.activityPanelController.onAddButtonClick(p);
		}

		function failure() {
			CMDBuild.Msg.error(CMDBuild.Translation.errors.error_message, CMDBuild.Translation.errors.generic_error, true);
		}
	}

//	function setLoadActivityListener(view) {
//		view.cardListGrid.on("load_activity", function(activity) {
//			view.loadActivity(activity);
//		});
//	}

	function delteActivity() {
		var me = this;

		CMDBuild.LoadMask.get().show();
		CMDBuild.ServiceProxy.workflow.terminateActivity({
			WorkItemId: this.currentActivity.raw["WorkItemId"],
			ProcessInstanceId: this.currentActivity.raw["ProcessInstanceId"],
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

	function updateActivity() {
		// if the record is new it has not raw data, so use the normal data
		var data = this.currentActivity.raw || this.currentActivity.data,
			requestParams = {
				Id: data.Id,
				IdClass: data.IdClass,
				ProcessInstanceId: data.ProcessInstanceId,
				WorkItemId: data.WorkItemId,
				advance: this.activityPanelController.isAdvance
			};

		CMDBuild.LoadMask.get().show();
		this.activityPanelController.view.getForm().submit({
			method : 'POST',
			url : "services/json/management/modworkflow/updateactivity",
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

	function startProcess() {
		var me = this;

		CMDBuild.LoadMask.get().show();
		CMDBuild.ServiceProxy.workflow.startProcess({
			idClass: this.currentActivity.data.IdClass,
			success: Ext.bind(success, this),
			failure: failure
		});

		function success(response) {
			CMDBuild.LoadMask.get().hide();
			me.currentActivity = Ext.JSON.decode(response.responseText);
			updateActivity.call(me);
		}

		function failure(response, options) {
			CMDBuild.LoadMask.get().hide();
			CMDBuild.Msg.error(CMDBuild.Translation.errors.error_message, CMDBuild.Translation.errors.generic_error, true);
		}
	}
})();