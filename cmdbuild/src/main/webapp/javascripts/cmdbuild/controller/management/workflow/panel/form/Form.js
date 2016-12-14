(function () {

	/**
	 * Adapter
	 *
	 * @link CMDBuild.controller.management.common.CMModClassAndWFCommons
	 *
	 * @legacy
	 */
	Ext.define('CMDBuild.controller.management.workflow.panel.form.Form', {
		extend: 'CMDBuild.controller.common.panel.gridAndForm.panel.form.Form',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.controller.management.workflow.panel.form.tabs.activity.Activity'
		],

		mixins: {
			observable: 'Ext.util.Observable',
			wfStateDelegate: 'CMDBuild.state.CMWorkflowStateDelegate'
		},

		/**
		 * @cfg {CMDBuild.controller.management.workflow.Workflow}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onWorkflowFormAbortButtonClick',
			'onWorkflowFormActivitySelect',
			'onWorkflowFormAddButtonClick',
			'onWorkflowFormAdvanceButtonClick',
			'onWorkflowFormInstanceSelect',
			'onWorkflowFormModifyButtonClick = onWorkflowFormActivityItemDoubleClick',
			'onWorkflowFormRemoveButtonClick',
			'onWorkflowFormSaveButtonClick',
			'onWorkflowFormWokflowSelect = onWorkflowWokflowSelect',
			'workflowFormPanelTabActiveSet',
			'workflowFormReset',
			'workflowFormWidgetExists'
		],

		/**
		 * @property {CMDBuild.controller.management.workflow.panel.form.tabs.activity.Activity}
		 */
		controllerTabActivity: undefined,

		/**
		 * @property {CMDBuild.controller.management.workflow.panel.form.tabs.attachments.Attachments}
		 */
		controllerTabAttachments: undefined,

		/**
		 * @property {CMDBuild.controller.management.workflow.panel.form.tabs.Email}
		 */
		controllerTabEmail: undefined,

		/**
		 * @property {CMDBuild.controller.management.workflow.panel.form.tabs.History}
		 */
		controllerTabHistory: undefined,

		/**
		 * @property {CMDBuild.controller.management.workflow.panel.form.tabs.Note}
		 */
		controllerTabNote: undefined,

		/**
		 * @property {CMDBuild.controller.management.workflow.panel.form.tabs.Relations}
		 */
		controllerTabRelations: undefined,

		/**
		 * @property {CMDBuild.view.management.workflow.panel.form.FormPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.management.workflow.Workflow} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.workflow.panel.form.FormPanel', { delegate: this });

			// Shorthands
			this.operativeInstructionsPanel = this.view.operativeInstructionsPanel;
			this.tabPanel = this.view.tabPanel;

			_CMWFState.addDelegate(this);

			// View reset
			this.tabPanel.removeAll();

			// Build sub-controllers
			this.controllerTabActivity = this.buildTabControllerActivity();

			if (!CMDBuild.configuration.userInterface.isDisabledProcessTab(CMDBuild.core.constants.Proxy.PROCESS_ATTACHMENT_TAB))
				this.controllerTabAttachments = Ext.create('CMDBuild.controller.management.workflow.panel.form.tabs.attachments.Attachments', { parentDelegate: this });

			if (!CMDBuild.configuration.userInterface.isDisabledProcessTab(CMDBuild.core.constants.Proxy.PROCESS_EMAIL_TAB))
				this.controllerTabEmail = Ext.create('CMDBuild.controller.management.workflow.panel.form.tabs.Email', { parentDelegate: this });

			if (!CMDBuild.configuration.userInterface.isDisabledProcessTab(CMDBuild.core.constants.Proxy.PROCESS_HISTORY_TAB))
				this.controllerTabHistory = Ext.create('CMDBuild.controller.management.workflow.panel.form.tabs.History', { parentDelegate: this });

			if (!CMDBuild.configuration.userInterface.isDisabledProcessTab(CMDBuild.core.constants.Proxy.PROCESS_NOTE_TAB))
				this.controllerTabNote = Ext.create('CMDBuild.controller.management.workflow.panel.form.tabs.Note', { parentDelegate: this });

			if (!CMDBuild.configuration.userInterface.isDisabledProcessTab(CMDBuild.core.constants.Proxy.PROCESS_RELATION_TAB))
				this.controllerTabRelations = Ext.create('CMDBuild.controller.management.workflow.panel.form.tabs.Relations', { parentDelegate: this });

			// View build (sorted)
			this.tabPanel.add([
				this.controllerTabActivity.getView(),
				Ext.isEmpty(this.controllerTabNote) ? null : this.controllerTabNote.getView(),
				Ext.isEmpty(this.controllerTabRelations) ? null : this.controllerTabRelations.getView(),
				Ext.isEmpty(this.controllerTabHistory) ? null : this.controllerTabHistory.getView(),
				Ext.isEmpty(this.controllerTabEmail) ? null : this.controllerTabEmail.getView(),
				Ext.isEmpty(this.controllerTabAttachments) ? null : this.controllerTabAttachments.getView()
			]);
		},

		/**
		 * @returns {CMDBuild.controller.management.workflow.panel.form.tabs.activity.Activity} activityPanelController
		 */
		buildTabControllerActivity: function () {
			var view = Ext.create('CMDBuild.view.management.workflow.panel.form.tabs.activity.ActivityView');
			this.widgetManager = new CMDBuild.view.management.common.widgets.CMWidgetManager(
				view, // as CMWidgetManagerDelegate
				this.tabPanel // as CMTabbedWidgetDelegate
			);
			this.widgetControllerManager = new CMDBuild.controller.management.common.CMWidgetManagerController(this.widgetManager);
			var activityPanelController = new CMDBuild.controller.management.workflow.panel.form.tabs.activity.Activity(view, this, this.widgetControllerManager);

			return activityPanelController;
		},

		/**
		 * Retrieve the form to use as target for the templateResolver
		 *
		 * @returns {Object or null}
		 */
		getFormForTemplateResolver: function() {
			if (Ext.isObject(this.widgetManager) && !Ext.Object.isEmpty(this.widgetManager) && typeof this.widgetManager.getFormForTemplateResolver == 'function')
				return this.widgetManager.getFormForTemplateResolver() || null;

			return null;
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowFormAbortButtonClick: function () {
			// Forward to sub-controllers
			this.controllerTabActivity.onAbortCardClick();

			if (Ext.isObject(this.controllerTabEmail) && !Ext.Object.isEmpty(this.controllerTabEmail))
				this.controllerTabEmail.onAbortCardClick();

			if (Ext.isObject(this.controllerTabNote) && !Ext.Object.isEmpty(this.controllerTabNote))
				this.controllerTabNote.cmfg('onWorkflowFormTabNoteAbortActivityButtonClick');
		},

		/**
		 * @param {Boolean} isSuperActivity
		 *
		 * @returns {Void}
		 */
		onWorkflowFormActivitySelect: function () {
			_CMWFState.setActivityInstance(
				Ext.create('CMDBuild.model.CMActivityInstance', this.cmfg('workflowSelectedActivityGet', 'rawData'))
			);

			var activityInstance = _CMWFState.getActivityInstance();

			this.operativeInstructionsPanel.update(activityInstance.getInstructions() || '');

			this.workflowFormPanelTabSelectionManage();

			// Forward to sub-controllers
			if (Ext.isObject(this.controllerTabNote) && !Ext.Object.isEmpty(this.controllerTabNote))
				this.controllerTabNote.cmfg('onWorkflowFormTabNoteActivitySelect');
		},

		/**
		 * @param {Number} id
		 *
		 * @returns {Void}
		 */
		onWorkflowFormAddButtonClick: function (id) {
			this.cmfg('workflowFormPanelTabActiveSet');

			// Forward to sub-controllers
			this.controllerTabActivity.onAddCardClick(id);

			if (Ext.isObject(this.controllerTabEmail) && !Ext.Object.isEmpty(this.controllerTabEmail))
				this.controllerTabEmail.onAddCardButtonClick();

			if (Ext.isObject(this.controllerTabHistory) && !Ext.Object.isEmpty(this.controllerTabHistory))
				this.controllerTabHistory.cmfg('onWorkflowFormTabHistoryAddWorkflowButtonClick', id);

			if (Ext.isObject(this.controllerTabNote) && !Ext.Object.isEmpty(this.controllerTabNote))
				this.controllerTabNote.cmfg('onWorkflowFormTabNoteAddButtonClick');

			if (Ext.isObject(this.controllerTabRelations) && !Ext.Object.isEmpty(this.controllerTabRelations))
				this.controllerTabRelations.onAddCardClick(id);
		},

		/**
		 * Forward to sub-controllers
		 *
		 * @returns {Void}
		 */
		onWorkflowFormAdvanceButtonClick: function () {
			this.controllerTabActivity.onAdvanceCardButtonClick();
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowFormInstanceSelect: function () {
			// FIXME: legacy mode to remove on complete Workflow UI and wofkflowState modules refactor
			_CMWFState.setProcessInstanceSynchronous(Ext.create('CMDBuild.model.CMProcessInstance', this.cmfg('workflowSelectedInstanceGet', 'rawData')));

			this.workflowFormPanelTabSelectionManage();

			// Forward to sub-controllers
			if (Ext.isObject(this.controllerTabNote) && !Ext.Object.isEmpty(this.controllerTabNote))
				this.controllerTabNote.cmfg('onWorkflowFormTabNoteInstanceSelect');
		},

		/**
		 * Forward to sub-controllers
		 *
		 * @returns {Void}
		 */
		onWorkflowFormModifyButtonClick: function () {
			// Forward to sub-controllers
			this.controllerTabActivity.onModifyCardClick();

			if (Ext.isObject(this.controllerTabEmail) && !Ext.Object.isEmpty(this.controllerTabEmail))
				this.controllerTabEmail.cmfg('onModifyCardClick');
		},

		/**
		 * Forward to sub-controllers
		 *
		 * @returns {Void}
		 */
		onWorkflowFormRemoveButtonClick: function () {
			this.controllerTabActivity.onRemoveCardClick();
		},

		/**
		 * Forward to sub-controllers
		 *
		 * @returns {Void}
		 */
		onWorkflowFormSaveButtonClick: function () {
			this.controllerTabActivity.onSaveCardClick();

			// Forward to sub-controllers
			if (Ext.isObject(this.controllerTabEmail) && !Ext.Object.isEmpty(this.controllerTabEmail))
				this.controllerTabEmail.onSaveCardClick();
		},

		/**
		 * @param {CMDBuild.model.common.Accordion} node
		 *
		 * @returns {Void}
		 */
		onWorkflowFormWokflowSelect: function (node) {
			this.cmfg('workflowFormReset');

			var danglingCard = CMDBuild.global.controller.MainViewport.cmfg('mainViewportDanglingCardGet');

			_CMWFState.setProcessClassRef(
				Ext.create('CMDBuild.cache.CMEntryTypeModel', this.cmfg('workflowSelectedWorkflowGet', 'rawData')),
				danglingCard,
				false,
				Ext.isEmpty(node) ? null : node.get(CMDBuild.core.constants.Proxy.FILTER)
			);

			this.workflowFormPanelTabSelectionManage(true);
		},

		// Tab panel manage methods
			/**
			 * @param {Boolean} enableDagglingCard
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			workflowFormPanelTabSelectionManage: function (enableDagglingCard) {
				enableDagglingCard = Ext.isBoolean(enableDagglingCard) ? enableDagglingCard : false;

				var danglingCard = CMDBuild.global.controller.MainViewport.cmfg('mainViewportDanglingCardGet');

				if (Ext.isEmpty(this.tabPanel.getActiveTab()))
					if (
						enableDagglingCard
						&& Ext.isObject(danglingCard) && !Ext.Object.isEmpty(danglingCard)
						&& !Ext.isBoolean(danglingCard.activateFirstTab)
					) {
						this.cmfg('workflowFormPanelTabActiveSet', danglingCard.activateFirstTab)
					} else {
						this.cmfg('workflowFormPanelTabActiveSet');
					}

				this.tabPanel.getActiveTab().fireEvent('show'); // Manual show event fire because was already selected
			},

			/**
			 * @param {Object} panelToDisplay
			 *
			 * @returns {Void}
			 */
			workflowFormPanelTabActiveSet: function (panelToDisplay) {
				if (
					(Ext.isObject(panelToDisplay) && !Ext.Object.isEmpty(panelToDisplay))
					|| (Ext.isNumber(panelToDisplay) && !Ext.isEmpty(panelToDisplay))
				) {
					return this.tabPanel.setActiveTab(panelToDisplay);
				}

				this.tabPanel.setActiveTab(0);
			},

		/**
		 * @returns {Void}
		 */
		workflowFormReset: function () {
			// Forward to sub-controllers
			this.controllerTabActivity.reset();

			if (Ext.isObject(this.controllerTabAttachments) && !Ext.Object.isEmpty(this.controllerTabAttachments))
				this.controllerTabAttachments.reset();

			if (Ext.isObject(this.controllerTabEmail) && !Ext.Object.isEmpty(this.controllerTabEmail))
				this.controllerTabEmail.reset();

			if (Ext.isObject(this.controllerTabHistory) && !Ext.Object.isEmpty(this.controllerTabHistory))
				this.controllerTabHistory.reset();

			if (Ext.isObject(this.controllerTabNote) && !Ext.Object.isEmpty(this.controllerTabNote))
				this.controllerTabNote.cmfg('workflowFormTabNoteReset');

			if (Ext.isObject(this.controllerTabRelations) && !Ext.Object.isEmpty(this.controllerTabRelations))
				this.controllerTabRelations.reset();
		},

		/**
		 * @param {String} type
		 *
		 * @returns {Boolean} exists
		 */
		workflowFormWidgetExists: function (type) {
			var exists = false;

			if (
				Ext.isObject(this.widgetControllerManager) && !Ext.Object.isEmpty(this.widgetControllerManager)
				&& Ext.isObject(this.widgetControllerManager.controllers) && !Ext.Object.isEmpty(this.widgetControllerManager.controllers)
				&& Ext.isString(type) && !Ext.isEmpty(type)
			) {
				Ext.Object.each(this.widgetControllerManager.controllers, function (id, controller, myself) {
					if (
						Ext.isObject(controller) && !Ext.Object.isEmpty(controller)
						&& Ext.isFunction(controller.cmfg) && controller.cmfg('widgetConfigurationGet', CMDBuild.core.constants.Proxy.TYPE) == type
					) {
						exists = true;
					}
				}, this);
			}

			return exists;
		}
	});

})();
