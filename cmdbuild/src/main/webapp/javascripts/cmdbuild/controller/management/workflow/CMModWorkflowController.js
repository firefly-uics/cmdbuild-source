(function () {

	var FLOW_STATUS_CODE = 'FlowStatus_code';
	var STATE_VALUE_OPEN = 'open.running';

	Ext.define('CMDBuild.controller.management.workflow.CMModWorkflowController', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		mixins: {
			commonFunctions: 'CMDBuild.controller.management.common.CMModClassAndWFCommons',
			observable: 'Ext.util.Observable',
			wfStateDelegate: 'CMDBuild.state.CMWorkflowStateDelegate'
		},

		/**
		 * @property {CMDBuild.controller.management.workflow.CMActivityPanelController}
		 */
		activityPanelController: undefined,

		/**
		 * @property {CMDBuild.controller.management.workflow.CMActivityAttachmentsController}
		 */
		attachmentsController: undefined,

		/**
		 * @property {Ext.data.Model}
		 */
		card: undefined,

		/**
		 * @property {CMDBuild.controller.management.workflow.tabs.Email}
		 */
		controllerTabEmail: undefined,

		/**
		 * @property {CMDBuild.controller.management.workflow.CMWorkflowHistoryPanelController}
		 */
		controllerTabHistory: undefined,

		/**
		 * @property {CMDBuild.controller.management.workflow.CMNoteController}
		 */
		noteController: undefined,

		/**
		 * @property {CMDBuild.controller.management.workflow.CMActivityRelationsController}
		 */
		relationsController: undefined,

		/**
		 * @property {Array}
		 */
		subControllers: [],

		/**
		 * @property {CMDBuild.view.management.workflow.CMModProcess}
		 */
		view: undefined,

		/**
		 * @property {Object}
		 */
		widgetsController: {},

		constructor: function() {
			this.callParent(arguments);

			this.view.delegate = this;

			this.buildSubControllers();

			_CMWFState.addDelegate(this);

			this.mon(this.view, this.view.CMEVENTS.addButtonClick, this.onAddCardButtonClick, this);
		},

		/**
		 * Build all controllers and adds view in tab panel with controller declaration order
		 *
		 * @override
		 */
		buildSubControllers: function() {
			// Tabs controllers
			this.buildTabControllerActivity();
			this.buildTabControllerNote();
			this.buildTabControllerRelations();
			this.buildTabControllerHistory();
			this.buildTabControllerEmail();
			this.buildTabControllerAttachments();

			// Generic controllers
			buildGridController(this);
		},

		buildTabControllerActivity: function() {
			var view = this.view.getActivityPanel();
			var widgetControllerManager = new CMDBuild.controller.management.common.CMWidgetManagerController(this.view.getWidgetManager());

			this.activityPanelController = new CMDBuild.controller.management.workflow.CMActivityPanelController(view, this, widgetControllerManager);

			this.mon(this.activityPanelController, this.activityPanelController.CMEVENTS.cardRemoved, function() {
				this.gridController.onCardDeleted();
			}, this);

			this.subControllers.push(this.activityPanelController);

			this.view.cardTabPanel.acutalPanel.add(view); // Add panel to view
		},

		buildTabControllerAttachments: function() {
			var view = this.view.getAttachmentsPanel();

			if (!Ext.isEmpty(view)) {
				this.attachmentsController = new CMDBuild.controller.management.workflow.CMActivityAttachmentsController(view, this);

				this.subControllers.push(this.attachmentsController);

				this.view.cardTabPanel.acutalPanel.add(view);
			}
		},

		buildTabControllerEmail: function() {
			if (!CMDBuild.configuration.userInterface.isDisabledProcessTab(CMDBuild.core.constants.Proxy.PROCESS_EMAIL_TAB)) {
				this.controllerTabEmail = Ext.create('CMDBuild.controller.management.workflow.tabs.Email', { parentDelegate: this });

				this.subControllers.push(this.controllerTabEmail);

				this.view.cardTabPanel.emailPanel = this.controllerTabEmail.getView(); // Creates tabPanel object

				this.view.cardTabPanel.acutalPanel.add(this.controllerTabEmail.getView());
			}
		},

		buildTabControllerHistory: function() {
			if (!CMDBuild.configuration.userInterface.isDisabledProcessTab(CMDBuild.core.constants.Proxy.PROCESS_HISTORY_TAB)) {
				this.controllerTabHistory = Ext.create('CMDBuild.controller.management.workflow.tabs.History', { parentDelegate: this });

				this.subControllers.push(this.controllerTabHistory);

				this.view.cardTabPanel.cardHistoryPanel = this.controllerTabHistory.getView(); // Creates tabPanel object

				this.view.cardTabPanel.acutalPanel.add(this.controllerTabHistory.getView());
			}
		},

		buildTabControllerNote: function() {
			var view = this.view.getNotesPanel();

			if (!Ext.isEmpty(view)) {
				this.noteController = new CMDBuild.controller.management.workflow.CMNoteController(view, this);

				this.subControllers.push(this.noteController);

				this.view.cardTabPanel.acutalPanel.add(view);
			}
		},

		buildTabControllerRelations: function() {
			var view = this.view.getRelationsPanel();

			if (!Ext.isEmpty(view)) {
				this.relationsController = new CMDBuild.controller.management.workflow.CMActivityRelationsController(view, this);

				this.subControllers.push(this.relationsController);

				this.view.cardTabPanel.acutalPanel.add(view);
			}
		},

		// wfStateDelegate
		onActivityInstanceChange: function(activityInstance) {
			this.view.updateDocPanel(activityInstance.getInstructions());

			if (!activityInstance.nullObject
					&& activityInstance.isNew()) {
				_CMUIState.onlyFormIfFullScreen();
			}
		},

		/**
		 * Forward onAbortCardClick event to email tab controller
		 */
		onAbortCardClick: function() {
			if (!Ext.isEmpty(this.controllerTabEmail) && Ext.isFunction(this.controllerTabEmail.onAbortCardClick))
				this.controllerTabEmail.onAbortCardClick();
		},

		/**
		 * Forward onAddCardButtonClick event to email tab controller
		 */
		onAddCardButtonClick: function() {
			if (!Ext.isEmpty(this.controllerTabEmail) && Ext.isFunction(this.controllerTabEmail.onAddCardButtonClick))
				this.controllerTabEmail.onAddCardButtonClick();
		},

		/**
		 * @deprecated
		 */
		onCardChanged: function(card) {
			_deprecated('onCardChanged', this);

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

			this.callForSubControllers('onCardSelected', this.card);
		},

		// private, call a given function for all the subcontrolles, and
		// pass the arguments to them.
		callForSubControllers: function(fnName, params) {
			for (var i=0, l = this.subControllers.length, ct=null; i<l; ++i) {
				ct = this.subControllers[i];
				if (typeof fnName == 'string'
					&& typeof ct[fnName] == 'function') {

					params = Ext.isArray(params) ? params : [params];
					ct[fnName].apply(ct, params);
				}
			}
		},

		onCardSelected: function onCardSelected(card) {
			this.setCard(card);
		},

		setCard: function(card) {
			this.card = card;
			this.onCardChanged(card);
		},

		getCard: function() {
			return this.card;
		},

		/**
		 * Forward onModifyCardClick event to email tab controller
		 */
		onModifyCardClick: function() {
			if (!Ext.isEmpty(this.controllerTabEmail) && Ext.isFunction(this.controllerTabEmail.onModifyCardClick))
				this.controllerTabEmail.onModifyCardClick();
		},

		/**
		 * Forward onSaveCardClick event to email tab controller
		 */
		onSaveCardClick: function() {
			if (!Ext.isEmpty(this.controllerTabEmail) && Ext.isFunction(this.controllerTabEmail.onSaveCardClick))
				this.controllerTabEmail.onSaveCardClick();
		},

		/**
		 * Is called when the view is bring to front from the main viewport.
		 * Set the entry type of the _CMWFState instead to store it inside this controller
		 *
		 * @param {Number} entryTypeId
		 * @param {Object} danglingCard
		 * @param {String} filter
		 *
		 * @override
		 */
		setEntryType: function(entryTypeId, danglingCard, filter) {
			var entryType = _CMCache.getEntryTypeById(entryTypeId);

			if (!Ext.isEmpty(danglingCard)) {
				if (!Ext.isEmpty(danglingCard.flowStatus))
					this.gridController.view.setStatus(danglingCard.flowStatus);

				if (!Ext.isEmpty(danglingCard.activateFirstTab))
					this.view.cardTabPanel.activeTabSet(danglingCard.activateFirstTab);
			}

			_CMWFState.setProcessClassRef(entryType, danglingCard, false, filter);

			this.view.updateTitleForEntry(entryType);

			_CMUIState.onlyGridIfFullScreen();
		},

		getEntryType: function() {
			return this.entryType || null;
		},

		getEntryTypeId: function() {
			var id = null;
			if (this.entryType) {
				id = this.entryType.get('id');
			}

			return id;
		},

		/**
		 * @param {Object} entryType
		 */
		onViewOnFront: function (entryType) {
			if (!Ext.isEmpty(entryType)) {
				var idPropertyName = Ext.isEmpty(entryType.get(CMDBuild.core.constants.Proxy.ENTITY_ID)) ? CMDBuild.core.constants.Proxy.ID : CMDBuild.core.constants.Proxy.ENTITY_ID;
				var dc = CMDBuild.global.controller.MainViewport.cmfg('mainViewportDanglingCardGet');
				var filter = entryType.get(CMDBuild.core.constants.Proxy.FILTER);
				var newEntryId = entryType.get(idPropertyName);

				// If we haven't a filter try to get default one from server
				if (Ext.isEmpty(filter)) {
					var params = {};
					params[CMDBuild.core.constants.Proxy.CLASS_NAME] = entryType.get(CMDBuild.core.constants.Proxy.NAME);
					params[CMDBuild.core.constants.Proxy.GROUP] = CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.DEFAULT_GROUP_NAME);

					CMDBuild.proxy.userAndGroup.group.tabs.DefaultFilters.read({
						params: params,
						scope: this,
						success: function (response, options, decodedResponse) {
							decodedResponse = decodedResponse.response.elements[0];

							if (!Ext.isEmpty(decodedResponse)) {
								if (
									Ext.isString(decodedResponse[CMDBuild.core.constants.Proxy.CONFIGURATION])
									&& CMDBuild.core.Utils.isJsonString(decodedResponse[CMDBuild.core.constants.Proxy.CONFIGURATION])
								) {
									decodedResponse[CMDBuild.core.constants.Proxy.CONFIGURATION] = Ext.decode(decodedResponse[CMDBuild.core.constants.Proxy.CONFIGURATION]);
								}

								filter = Ext.create('CMDBuild.model.CMFilterModel', decodedResponse);
							}

							this.setEntryType(newEntryId, dc, filter);
						}
					});
				} else {
					this.setEntryType(newEntryId, dc, filter);
				}
			}
		}
	});

	function isStateOpen(card) {
		var data = card.raw;
		return data[FLOW_STATUS_CODE] == STATE_VALUE_OPEN;
	}

	function buildGridController(me) {
		me.grid = me.view.cardGrid;

		me.gridController = new CMDBuild.controller.management.workflow.CMActivityGridController(me.view.cardGrid);
		me.mon(me.gridController, me.gridController.CMEVENTS.cardSelected, me.onCardSelected, me);
		me.mon(me.gridController, me.gridController.CMEVENTS.load, onGridLoad, me);

		me.grid.mon(me.gridController, 'itemdblclick', function() {
			me.activityPanelController.onModifyCardClick();
			_CMUIState.onlyFormIfFullScreen();
		}, me);

		me.activityPanelController.setDelegate(me.gridController);

		me.subControllers.push(me.gridController);
	}

	function onGridLoad(args) {
		// args[1] is the array with the loaded records
		// so, if there are no records clear the view
		if (args[1] && args[1].length == 0) {
			this.activityPanelController.clearView();
		}
	}

})();