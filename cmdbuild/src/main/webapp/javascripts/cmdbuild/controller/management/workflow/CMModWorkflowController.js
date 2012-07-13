(function () {

	var FLOW_STATUS_CODE = "FlowStatus_code",
		STATE_VALUE_OPEN = "open.running";

	Ext.define("CMDBuild.controller.management.workflow.CMModWorkflowController", {

		extend: "CMDBuild.controller.management.common.CMModController",

		mixins: {
			wfStateDelegate: "CMDBuild.state.CMWorkflowStateDelegate"
		},

		constructor: function() {
			this.callParent(arguments);
			this.widgetsController = {};

			_CMWFState.addDelegate(this);
		},

		buildSubControllers: function() {
			var me = this;

			me.relationsController = new CMDBuild.controller.management.workflow.CMActivityRelationsController(me.view.getRelationsPanel(), me);
			me.historyController = new CMDBuild.controller.management.workflow.CMWorkflowHistoryPanelController(me.view.getHistoryPanel());
			me.attachmentsController = new CMDBuild.controller.management.workflow.CMActivityAttachmentsController(me.view.getAttachmentsPanel(), me);

			me.subControllers = [me.relationsController, me.historyController, me.attachmentsController];

			buildActivityPanelController(me);
			buildGridController(me);
			buildNoteController(me);
		},

		// wfStateDelegate
		onActivityInstanceChange: function(activityInstance) {

			this.view.updateDocPanel(activityInstance.getInstructions());

			if (!activityInstance.nullObject 
					&& activityInstance.isNew()) {

				// I could be in a tab different to the first one,
				// but to edit a new card is necessary to have the editing form.
				// So I force the view to go on the ActivityTab

				this.view.showActivityPanel();
			}
		},

		// deprecated
		onCardChanged: function(card) { _deprecated();
			var me = this;

			if (card == null) {
				return;
			}

			if (isStateOpen(card) || card._cmNew) {
				me.view.updateDocPanel(card);
				if (card._cmNew) {
					// I could be in a tab different to the first one,
					// but to edit a new card is necessary to have the editing form.
					// So I force the view to go on the ActivityTab

					me.view.showActivityPanel();
				}
			} else {
				me.view.updateDocPanel(null);
			}

			me.callParent(arguments);
		},

		// override
		// is called when the view is bring to front from the main viewport
		// Set the entry type of the _CMWFState instead to store it inside
		// this controller
		setEntryType: function(entryTypeId) {
			var entryType = _CMCache.getEntryTypeById(entryTypeId);
			_CMWFState.setProcessClassRef(entryType);
			this.view.updateTitleForEntry(entryType);
		},

		// override
		onEntryTypeChanged: function(entryType) { _deprecated();
			
		}
	});

	function isStateOpen(card) {
		var data = card.raw;
		return data[FLOW_STATUS_CODE] == STATE_VALUE_OPEN;
	}

	function buildActivityPanelController(me) {
		var widgetControllerManager = new CMDBuild.controller.management.common.CMWidgetManagerController(me.view.getWidgetManager());
		me.activityPanelController = new CMDBuild.controller.management
		.workflow.CMActivityPanelController(me.view.getActivityPanel(), me, widgetControllerManager);

		me.mon(me.activityPanelController, me.activityPanelController.CMEVENTS.cardRemoved,
			function() {
				me.gridController.onCardDeleted();
			}
		);

		me.subControllers.push(me.activityPanelController);
	}

	function buildGridController(me) {
		me.grid = me.view.cardGrid;

		me.gridController = new CMDBuild.controller.management.workflow.CMActivityGridController(me.view.cardGrid);
		me.mon(me.gridController, me.gridController.CMEVENTS.cardSelected, me.onCardSelected, me);
		me.mon(me.gridController, me.gridController.CMEVENTS.load, onGridLoad, me);
		me.mon(me.gridController, me.gridController.CMEVENTS.processClosed, onProcessTermined, me);

		me.grid.mon(me.gridController, "itemdblclick", function() {
			me.activityPanelController.onModifyCardClick();
		}, me);

		me.activityPanelController.setDelegate(me.gridController);

		me.subControllers.push(me.gridController);
	}

	function buildNoteController(me) {
		me.noteController = new CMDBuild.controller.management.workflow.CMNoteController(me.view.getNotesPanel(), me);
		me.subControllers.push(me.noteController);

		// the history has to know when the notes are changed
		me.mon(me.noteController, me.noteController.CMEVENTS.noteWasSaved, function() {
			if (me.historyController) {
				me.historyController.onProcessInstanceChange(_CMWFState.getProcessInstance());
			}
		}, me);
	}

	function onProcessTermined() {
		this.activityPanelController.clearView();
	}

	function onGridLoad(args) {
		// args[1] is the array with the loaded records
		// so, if there are no records clear the view
		if (args[1] && args[1].length == 0) {
			this.activityPanelController.clearView();
		}
	}
})();