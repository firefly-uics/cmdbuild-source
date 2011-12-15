(function () {

	var ERROR_TEMPLATE = "<p class=\"{0}\">{1}</p>",
		FLOW_STATUS_CODE = "FlowStatus_code",
		STATE_VALUE_OPEN = "open.running";

	Ext.define("CMDBuild.controller.management.workflow.CMModWorkflowController", {
		extend: "CMDBuild.controller.CMBasePanelController",
		mixins: {
			commonFunctions: "CMDBuild.controller.management.common.CMModClassAndWFCommons"
		},
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
			this.relationsController = new CMDBuild.controller.management.classes.CMCardRelationsController(
				this.tabPanel.relationsPanel, this);

			// grid events
			this.grid.mon(this.grid.statusCombo, "select", onStatusComboSelect, this);
			this.grid.mon(this.grid.addCardButton, "cmClick", onAddCardButtonClick, this);
			this.grid.mon(this.grid.printGridMenu, "click", onPrintGridMenuClick, this);
			this.grid.mon(this.grid, "load", onGridLoad, this);
			this.grid.mon(this.grid, "processTerminated", onProcessTermined, this);

			this.grid.mon(this.grid, "itemdblclick", function() {
				this.activityPanelController.onModifyButtonClick();
			}, this);

			this.gridSM.on("selectionchange", onActivitySelect, this);

			this.tabPanel.on("cmeditmode", onEditMode, this);
			this.tabPanel.on("cmdisplaymode", onDisplayMode, this);

			// graph "code reuse"
			this.tabPanel.activityTab.activityForm.graphButton.on("click", onShowGraphClick, this);
			this.tabPanel.relationsPanel.graphButton.on("click", onShowGraphClick, this);
		},

		// FIXME: Wonderful "code reuse"
		onViewOnFront: function(selection) {
			if (selection) {
				var newEntryId = selection.get("id"),
					dc = _CMMainViewportController.getDanglingCard(),
					entryIdChanged = this.currentEntryId != newEntryId;

				if (entryIdChanged) {
					this.currentEntryId = newEntryId;
					this.currentEntry = _CMCache.getEntryTypeById(this.currentEntryId);
					this.currentCard = null;

					// notify sub-controllers
					this.activityPanelController.onEntrySelected(selection);
					this.relationsController.onEntrySelect(selection); // FIXME naming
				}

				if (dc != null) {
					if (dc.activateFirstTab) {
						this.view.cardTabPanel.activateFirstTab();
					} else {
						this.view.cardTabPanel.activateRelationTab();
					}
					this.view.openCard(dc, retryWithoutFilter = true);
				} else if (entryIdChanged) {
					this.view.onEntrySelected(selection);
				}
			}
		},

		onSaveButtonClick: function() {
			// if is advance, also the widgets must be saved, so
			// wait for the template resolver before call the save;
			var me = this;
			if (me.activityPanelController.isAdvance) {
				waitForBusyWidgets(me, 
					function() {
						saveActivity(me)
					}
				);
			} else {
				saveActivity(me);
			}
		},

		onAbortButtonClick: function() {
			// if the currentActivity is a Ext model.
			// the card is loaded from the grid, so we can reload it
			// to abort the modifications. Else clear the panel
			if (typeof this.currentActivity.get == "function") {
				onActivitySelect.call(this, null, [this.currentActivity]);
			} else {
				this.activityPanelController.clearViewForNoActivity();
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
					deleteActivity.call(me);
				}
			}
		},

		onWFWidgetButtonClick :function(w) {
			var wc = this.widgetsController[w.identifier];
			if (wc) {
				wc.activeView();
			}
		},

		showActivityPanel: function() {
			this.tabPanel.showActivityPanel();
		},

		isStateOpen: function(activity) {
			var data = activity.raw;
			return data[FLOW_STATUS_CODE] == STATE_VALUE_OPEN;
		}
	});

	function onProcessTermined() {
		this.grid.skipNextSelectFirst();
		this.activityPanelController.clearViewForNoActivity();
	}

	function waitForBusyWidgets(me, cb) {
		new _CMUtils.PollingFunction({
			success: cb,
			failure: function failure() {
				CMDBuild.Msg.error(null,CMDBuild.Translation.errors.busy_wf_widgets, false);
			},
			checkFn: function() {
				// I want exit if there are no busy wc
				return !areThereBusyWidget(me);
			},
			cbScope: me,
			checkFnScope: me
		}).run();
	}

	function areThereBusyWidget(me) {
		for (var wc in me.widgetsController) {
			wc = me.widgetsController[wc];
			if (wc.isBusy && wc.isBusy()) {
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
			var firstSelection = selection[0];

			if (this.isStateOpen(firstSelection)) {
				var editMode = this.activityPanelController.isAdvance 
					&& this.currentActivity.data.Id == firstSelection.data.Id;

				updateForActivity.call(this, firstSelection, editMode);
			} else {
				loadClosedActivity.call(this, firstSelection);
			}

			this.relationsController.onCardSelected(firstSelection);
		}
	}

	function updateForActivity(a, editMode) {
		this.currentActivity = a;

		var reloadFields = false,
			remotely = this.currentActivity.data.IdClass != this.currentEntryId;

		if (this.idClassOfLastAttributesLoaded != this.currentActivity.data.IdClass) {
			this.idClassOfLastAttributesLoaded = this.currentActivity.data.IdClass;
			reloadFields = true;
		}

		// load the right fields and build the WFwidgets
		this.widgetsController = clearWidgetControllers(this.widgetsController);
		var me = this;

		function buildTheWidgetsControllers() {
			var wwWidgets = me.view.getWFWidgets();

			for (var identifier in wwWidgets) {
				var wc = buildWidgetController.call(me, wwWidgets[identifier]);
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
			remotely: remotely,
			cb: buildTheWidgetsControllers
		});
	}

	function buildWidgetController(w) {
		var me = this,
			builders = {
				createModifyCard: function(w) {
					return new CMDBuild.controller.management.workflow.widgets.CMCreateModifyCard(w, me);
				},
				createReport: function(w) {
					return new CMDBuild.controller.management.workflow.widgets.CMCreateReportController(w, me);
				},
				linkCards: function(w) {
					return new CMDBuild.controller.management.workflow.widgets.CMLinkCardsController(w, me);
				},
				manageEmail: function(w) {
					return new CMDBuild.controller.management.workflow.widgets.CMManageEmailController(w, me);
				},
				manageRelation: function(w) {
					return new CMDBuild.controller.management.workflow.widgets.CMManageRelationController(w, me);
				},
				openNote: function(w) {
					return new CMDBuild.controller.management.workflow.widgets.CMOpenNoteController(w, me);
				},
				openAttachment: function(w) {
					return new CMDBuild.controller.management.workflow.widgets.CMAttachmentController(w, me);
				},
				calendar: function(w) {
					return new CMDBuild.controller.management.workflow.widgets.CMCalendarController(w, me);
				}
			};

		return builders[w.extattrtype](w);
	}

	function loadClosedActivity(a) {
		this.currentActivity = a;
		this.widgetsController = clearWidgetControllers(this.widgetsController);
		this.view.updateForClosedActivity(a);
	}

	function onStatusComboSelect() {
		this.grid.updateStatusParamInStoreProxyConfiguration();
		this.grid.loadPage(1);
	}

	function onPrintGridMenuClick(format) {
		if (typeof format != "string") {
			return
		}

		var columns = this.grid.getVisibleColumns();
		CMDBuild.LoadMask.get().show();

		CMDBuild.Ajax.request({
			url: 'services/json/management/modreport/printcurrentview',
			scope: this,
			params: {
				FilterCategory: this.grid.filterCategory,
				IdClass: this.currentEntry.get("id"),
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

	function onAddCardButtonClick(p) {
		this.view.onAddButtonClick();
		var me = this;

		CMDBuild.ServiceProxy.workflow.getstartactivitytemplate(p.classId, {
			scope: this,
			success: success,
			important: true
		});

		function success(response) {
			me.currentActivity = null;
			me.gridSM.deselectAll();

			var template =  Ext.JSON.decode(response.responseText);

			template.data.ProcessInstanceId = undefined;
			template.data.WorkItemId = undefined;

			// to unify the Ext.models with the server response;
			template.raw = template.data; 
			template.get = function(key) {
				return template.raw[key];
			}

			updateForActivity.call(me, template, editMode = true);
		}
	}

	function deleteActivity() {
		var me = this;

		me.activityPanelController.clearViewForNoActivity();

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
			me.view.reset(me.currentEntry.get("id"));
		}

		function failure() {
			CMDBuild.LoadMask.get().hide();
			CMDBuild.Msg.error(
				CMDBuild.Translation.errors.error_message,
				CMDBuild.Translation.errors.generic_error,
				true);
		}
	}

	function saveActivity(me) {
		var data = me.currentActivity.raw,
			requestParams = {
				Id: data.Id,
				IdClass: data.IdClass,
				ProcessInstanceId: data.ProcessInstanceId,
				WorkItemId: data.WorkItemId,
				advance: me.activityPanelController.isAdvance,
				attributes: Ext.JSON.encode(me.activityPanelController.view.getValues())
			},
			valid = false;
		
		if (requestParams.advance) {
			valid = validate.call(me);
		} else {
			// Business rule: Someone want the validation
			// only if advance and not if want only save the activity
			valid = true;
		}

		if (valid) {
			CMDBuild.LoadMask.get().show();

			var ww = {};
			for (var wc in me.widgetsController) {
				wc = me.widgetsController[wc];
				var wcData = wc.getData(advance = me.activityPanelController.isAdvance);
				if (wcData != null) {
					ww[wc.wiewIdenrifier] = wcData;
				}
			}
	
			if (ww) {
				requestParams["ww"] = Ext.JSON.encode(ww);
			}
	
			CMDBuild.ServiceProxy.workflow.saveActivity({
				timeout: 90,
				params: requestParams,
				scope : me,
				clientValidation: me.activityPanelController.isAdvance, //to force the save request
				callback: function(operation, success, response) {
					CMDBuild.LoadMask.get().hide();
				},
	
				success: function(response) {
					me.view.reset(requestParams.IdClass);
					updateActivityData.call(me, response);
	
					me.activityPanelController.view.reset();
					me.activityPanelController.view.displayMode();
	
					me.grid.openCard({
						Id: me.currentActivity.raw.Id,
						// use the id class of the grid to use the right filter
						// when look for the position
						IdClass: me.currentEntryId
					});
				},
	
				failure : function(response) {
					updateActivityData.call(me, response);
				}
			});
		}
	}
	
	function validate() {
		var valid = this.activityPanelController.isValid(),
			wrongWidgets = getWrongWFAsHTML.call(this);

		if (wrongWidgets != null) {
			valid = false;
			var msg = Ext.String.format(ERROR_TEMPLATE
					, CMDBuild.Constants.css.error_msg
					, CMDBuild.Translation.errors.invalid_extended_attributes);
			CMDBuild.Msg.error(null, msg + wrongWidgets, popup = false);
		}


		return valid;
	}

	function getWrongWFAsHTML() {
		var out = "<ul>",
			valid = true;

		for (var wc in this.widgetsController) {
			wc = this.widgetsController[wc];
			if (!wc.isValid()) {
				valid = false;
				out += "<li>" + wc.widgetConf.ButtonLabel + "</li>";
			}
		}
		out + "</ul>";

		if (valid) {
			return null
		} else {
			return out;
		}
	}
	
	function updateActivityData(response) {
		if (this.currentActivity) {
			var activity = Ext.decode(response.responseText);

			this.currentActivity.raw = Ext.apply(this.currentActivity.raw, {
				Id: activity.Id,
				ProcessInstanceId: activity.ProcessInstanceId,
				WorkItemId: activity.WorkItemId
			});
		}
	}

	function onGridLoad(args) {
		// args[1] is the array with the loaded records
		// so, if there are no records clear the view
		if (args[1] && args[1].length == 0) {
			this.activityPanelController.clearViewForNoActivity();
		}
	}

	function clearWidgetControllers(wcs) {
		if (wcs) {
			for (var wc in wcs) {
				wc = wcs[wc];
				wc.destroy();
				delete wc;
			}
		}
		return {};
	}

	// ancient code reuse technique
	function onShowGraphClick() {
		var classId = this.currentActivity.get("IdClass"),
			cardId = this.currentActivity.get("Id");
		CMDBuild.Management.showGraphWindow(classId, cardId);
	}
})();