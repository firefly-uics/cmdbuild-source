(function () {
	
	Ext.define("CMDBuild.controller.management.workflow.CMModWorkflowController", {
		extend: "CMDBuild.controller.CMBasePanelController",
		constructor: function() {
			this.callParent(arguments);
			this.grid = this.view.cardGrid;
			this.gridSM = this.grid.getSelectionModel();
			this.cardTabPanel = this.view.cardTabPanel;

			// initialize state variables
			this.idClassOfLastAttributesLoaded = null;
			this.currentEntryId = null;
			this.currentActivity = null;

			// instantiate sub-controllers
			this.activityPanelController = new CMDBuild.controller.management.workflow.CMActivityPanelController(
				this.cardTabPanel.activityTab, this);

			// grid events
			this.grid.statusCombo.on("select", onStatusComboSelect, this);
			this.grid.addCardButton.on("cmClick", onAddCardButtonClick, this);
			this.gridSM.on("selectionchange", onActivitySelect, this);
		},

		onViewOnFront: function(selection) {
			if (selection) {
				this.currentEntryId = selection.get("id");

//				if (this.danglingCardToOpen) {
//					this.view.openCard(this.danglingCardToOpen);
//					this.danglingCardToOpen = null;
//				} else {
					this.view.onEntrySelected(selection);
//				}

				// sub-controllers
//				this.attachmentsController.onEntrySelect(selection);
//				this.relationsController.onEntrySelect(selection);
//				this.mdController.onEntrySelect(selection);
			}
		},

		onSaveButtonClick: function() {
			// TODO 3 to 4 check the extended attr

			if (this.currentActivity.data.ProcessInstanceId == "tostart") {
				startProcess.call(this);
			} else {
				updateActivity.call(this);
			}
		}
	});

	function onActivitySelect(sm, selection) {
		if (selection.length > 0) {
			var reloadFields = false;
			this.currentActivity = selection[0];
			if (this.idClassOfLastAttributesLoaded != this.currentActivity.data.IdClass) {
				this.idClassOfLastAttributesLoaded = this.currentActivity.data.IdClass;
				reloadFields = true;
			}

			this.activityPanelController.onActivitySelect(this.currentActivity, reloadFields);
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

			this.cardTabPanel.onAddCardButtonClick({
				edit:true,
				isnew:true,
				activity: this.currentActivity
			});
		}

		function failure() {
			CMDBuild.Msg.error(CMDBuild.Translation.errors.error_message, CMDBuild.Translation.errors.generic_error, true);
		}

//		this.cardTabPanel.onAddCardButtonClick(p);
	}

//	function setLoadActivityListener(view) {
//		view.cardListGrid.on("load_activity", function(activity) {
//			view.loadActivity(activity);
//		});
//	}

//
//	function setTerminateProcessListener(view) {
//		view.activityTabPanel.on("terminate_process", function(params) {
//			Ext.Msg.confirm(
//					CMDBuild.Translation.management.modworkflow.abort_card, // title
//					CMDBuild.Translation.management.modworkflow.abort_card_confirm, // message
//					confirmCB);
//
//			function confirmCB(btn) {
//				if (btn != 'yes') {
//					return;
//				} else {
//					CMDBuild.LoadMask.get().show();
//					CMDBuild.ServiceProxy.workflow.terminateActivity({
//						WorkItemId: params.WorkItemId,
//						ProcessInstanceId: params.ProcessInstanceId,
//						success: success,
//						failure: failure
//					});
//				}
//			}
//
//			function success(response) {
//				CMDBuild.LoadMask.get().hide();
//				var ret = Ext.util.JSON.decode(response.responseText);
//				if (ret.success) {
//					view.cardListGrid.reloadCard();
//				}
//			}
//
//			function failure() {
//				CMDBuild.LoadMask.get().hide();
//				CMDBuild.Msg.error(
//					CMDBuild.Translation.errors.error_message,
//					CMDBuild.Translation.errors.generic_error,
//					true);
//			}	
//		});
//	}
	function updateActivity() {
		CMDBuild.LoadMask.get().show();
		this.activityPanelController.view.getForm().submit({
			method : 'POST',
			url : "services/json/management/modworkflow/updateactivity",
			timeout: 90,
			params: {
				Id: this.currentActivity.raw.Id,
				IdClass: this.currentActivity.raw.IdClass,
				ProcessInstanceId: this.currentActivity.raw.ProcessInstanceId,
				WorkItemId: this.currentActivity.raw.WorkItemId,
				advance: this.activityPanelController.isAdvance
			},
			scope : this,
			clientValidation: this.activityPanelController.isAdvance, //to force the save request
			success : function() {
				CMDBuild.LoadMask.get().hide();
				alert("@@ well done")
			},
			failure : function(response, options) {
				CMDBuild.LoadMask.get().hide();
				alert("@@ something goes wrong")
			}
		});
	}

	function startProcess() {
		CMDBuild.LoadMask.get().show();
		CMDBuild.ServiceProxy.workflow.startProcess({
			idClass: this.currentActivity.data.IdClass,
			success: Ext.bind(success, this),
			failure: failure
		});

		function success(response) {
			CMDBuild.LoadMask.get().hide();
			var process = Ext.JSON.decode(response.responseText);
			updateActivity.call(this);
		}

		function failure(response, options) {
			CMDBuild.LoadMask.get().hide();
			CMDBuild.Msg.error(CMDBuild.Translation.errors.error_message, CMDBuild.Translation.errors.generic_error, true);
		}
	}
})();