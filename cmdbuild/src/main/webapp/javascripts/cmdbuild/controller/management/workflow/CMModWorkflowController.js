(function () {

	var ERROR_TEMPLATE = "<p class=\"{0}\">{1}</p>",
		FLOW_STATUS_CODE = "FlowStatus_code",
		STATE_VALUE_OPEN = "open.running";

	Ext.define("CMDBuild.controller.management.workflow.CMModWorkflowController", {

		extend: "CMDBuild.controller.management.common.CMModController",

		constructor: function() {
			this.callParent(arguments);
			this.widgetsController = {};
		},

		buildSubControllers: function() {
			var me = this;

			me.relationsController = new CMDBuild.controller.management.workflow.CMActivityRelationsController(me.view.getRelationsPanel(), me);
			me.historyController = new CMDBuild.controller.management.workflow.CMWorkflowHistoryPanelController(me.view.getHistoryPanel());
			me.attachmentsController = new CMDBuild.controller.management.workflow.CMActivityAttachmentsController(me.view.getAttachmentsPanel(), me);
			me.noteController = new CMDBuild.controller.management.workflow.CMNoteController(me.view.getNotesPanel(), me);

			me.subControllers = [me.relationsController, me.historyController, me.attachmentsController, me.noteController];

			buildActivityPanelController(me);
			buildGridController(me);
		},

		onCardChanged: function(card) {
			var me = this;

			if (card == null) {
				return;
			}

			if (isStateOpen(card) || card._cmNew) {
				me.view.updateDocPanel(card);
			} else {
				me.view.updateDocPanel(null);
			}

			me.callParent(arguments);
		},

		// override
		onEntryTypeChanged: function(entryType) {
			this.view.updateTitleForEntry(entryType);
		}
	});

	function onCardSaved(card) {
		this.gridController.openCard({
			Id: card.raw.Id,
			// use the id class of the grid to use the right filter
			// when look for the position
			IdClass: this.entryType.get("id")
		});
	}

	function isStateOpen(card) {
		var data = card.raw;
		return data[FLOW_STATUS_CODE] == STATE_VALUE_OPEN;
	}

	function buildWidgetControllerManager(me) {
		return new CMDBuild.controller.management.workflow.CMWidgetManager(me.view.getWidgetManager());
	}

	function buildActivityPanelController(me) {
		var widgetControllerManager = buildWidgetControllerManager(me);
		me.activityPanelController = new CMDBuild.controller.management
		.workflow.CMActivityPanelController(me.view.getActivityPanel(), me, widgetControllerManager);

		me.mon(me.activityPanelController, me.activityPanelController.CMEVENTS.cardSaved, onCardSaved, me);
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

		me.subControllers.push(me.gridController);
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