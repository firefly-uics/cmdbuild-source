(function () {

	Ext.define('CMDBuild.controller.administration.taskManager.Form', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.TaskManager}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onTaskManagerFormAbortButtonClick = taskManagerModifyCancel',
			'onTaskManagerFormAddButtonClick',
			'onTaskManagerFormCloneButtonClick',
			'onTaskManagerFormModifyButtonClick',
			'onTaskManagerFormNavigationButtonClick',
			'onTaskManagerFormRemoveButtonClick',
			'onTaskManagerFormRowSelected',
			'onTaskManagerFormSaveButtonClick',
			'taskManagerFormClearSelection',
			'taskManagerFormNavigationSetDisableNextButton',
			'taskManagerFormPanelForwarder',
			'taskManagerFormPanelsAdd',
			'taskManagerFormPanelsRemoveAll',
			'taskManagerFormViewDataGet',
			'taskManagerFormViewGet',
			'taskManagerFormViewReset'
		],

		/**
		 * @property {Object}
		 */
		controllerTask: undefined,

		/**
		 * @property {CMDBuild.view.administration.taskManager.FormPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.taskManager.TaskManager} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.taskManager.FormPanel', { delegate: this });
		},

		/**
		 * @param {Array} type
		 *
		 * @returns {Object}
		 *
		 * @private
		 */
		buildControllerTask: function (type) {
			switch (type[0]) {
				case 'connector':
					return Ext.create('CMDBuild.controller.administration.taskManager.task.connector.Connector', { parentDelegate: this });

				case 'email':
					return Ext.create('CMDBuild.controller.administration.taskManager.task.email.Email', { parentDelegate: this });

				case 'event':
					return Ext.create('CMDBuild.controller.administration.taskManager.task.event.Event', {
						parentDelegate: this,
						subType: type[1]
					});

				case 'generic':
					return Ext.create('CMDBuild.controller.administration.taskManager.task.generic.Generic', { parentDelegate: this });

				case 'workflow':
					return Ext.create('CMDBuild.controller.administration.taskManager.task.workflow.Workflow', { parentDelegate: this });

				default:
					return _error('buildControllerTask(): unmanaged type property', this, type);
			}
		},

		/**
		 * @returns {Boolean}
		 *
		 * @private
		 */
		hasNext: function () {
			return (
				this.view.getLayout().getNext()
				&& !Ext.isEmpty(this.view.getLayout().getNext().isDisabled()) && !this.view.getLayout().getNext().isDisabled()
			);
		},

		/**
		 * @returns {Boolean}
		 *
		 * @private
		 */
		hasPrevious: function () {
			return (
				this.view.getLayout().getPrev()
				&& !Ext.isEmpty(this.view.getLayout().getPrev().isDisabled()) && !this.view.getLayout().getPrev().isDisabled()
			);
		},

		/**
		 * @returns {Void}
		 *
		 * @private
		 */
		manageButtonsDisabledState: function () {
			this.view.previousButton.setDisabled(!this.hasPrevious());
			this.view.nextButton.setDisabled(!this.hasNext());
		},

		/**
		 * Forwarder method
		 *
		 * @returns {Void}
		 */
		onTaskManagerFormAbortButtonClick: function () {
			this.controllerTask.cmfg('onTaskManagerFormTaskAbortButtonClick');
		},

		/**
		 * @param {Array} type
		 *
		 * @returns {Void}
		 */
		onTaskManagerFormAddButtonClick: function (type) {
			this.cmfg('taskManagerClearSelection');

			if (Ext.isArray(type) && !Ext.isEmpty(type)) {
				this.controllerTask = this.buildControllerTask(type);

				if (Ext.isObject(this.controllerTask) && !Ext.Object.isEmpty(this.controllerTask))
					this.controllerTask.cmfg('onTaskManagerFormTaskAddButtonClick');
			} else {
				_error('onTaskManagerFormAddButtonClick(): empty or wrong type parameter', this, type);
			}
		},

		/**
		 * Forwarder method
		 *
		 * @returns {Void}
		 */
		onTaskManagerFormCloneButtonClick: function () {
			this.cmfg('taskManagerClearSelection', { enableClearForm: false });

			this.controllerTask.cmfg('onTaskManagerFormTaskCloneButtonClick');
		},

		/**
		 * Forwarder method
		 *
		 * @returns {Void}
		 */
		onTaskManagerFormModifyButtonClick: function () {
			this.controllerTask.cmfg('onTaskManagerFormTaskModifyButtonClick');
		},

		/**
		 * @param {String} action
		 *
		 * @returns {Void}
		 */
		onTaskManagerFormNavigationButtonClick: function (action) {
			if (Ext.isString(action) && !Ext.isEmpty(action)) {
				switch (action) {
					case 'first': {
						if (!Ext.isEmpty(this.view.getLayout().getLayoutItems()))
							this.view.getLayout().setActiveItem(0);
					} break;

					case 'next': {
						if (this.hasNext())
							this.view.getLayout().next();
					} break;

					case 'previous': {
						if (this.hasPrevious())
							this.view.getLayout().prev();
					} break;

					default: {
						_error('onTaskManagerFormNavigationButtonClick(): action parameter invalid value', this, action);
					}
				}

				this.manageButtonsDisabledState();

				// Fires show event on first item
				if (!Ext.isEmpty(this.view.getLayout().getActiveItem()) && !this.view.getLayout().getPrev())
					this.view.getLayout().getActiveItem().fireEvent('show');
			} else {
				_error('onTaskManagerFormNavigationButtonClick(): empty or unmanaged action parameter', this, action);
			}
		},

		/**
		 * Forwarder method
		 *
		 * @returns {Void}
		 */
		onTaskManagerFormRemoveButtonClick: function () {
			this.controllerTask.cmfg('onTaskManagerFormTaskRemoveButtonClick');
		},

		/**
		 * @returns {Void}
		 */
		onTaskManagerFormRowSelected: function () {
			if (!this.cmfg('taskManagerSelectedTaskIsEmpty')) {
				this.controllerTask = this.buildControllerTask(this.cmfg('taskManagerSelectedTaskGet', CMDBuild.core.constants.Proxy.TYPE));

				if (Ext.isObject(this.controllerTask) && !Ext.Object.isEmpty(this.controllerTask))
					this.controllerTask.cmfg('onTaskManagerFormTaskRowSelected');
			} else {
				_error('onTaskManagerFormRowSelected(): empty or wrong selected grid task', this, this.cmfg('taskManagerSelectedTaskGet'));
			}
		},

		/**
		 * Forwarder method
		 *
		 * @returns {Void}
		 */
		onTaskManagerFormSaveButtonClick: function () {
			this.controllerTask.cmfg('onTaskManagerFormTaskSaveButtonClick');
		},

		/**
		 * Clear form panel
		 *
		 * @returns {Void}
		 *
		 * FIXME: waiting for refactor (CMDBuild.view.common.PanelFunctions)
		 */
		taskManagerFormClearSelection: function () {
			this.cmfg('taskManagerFormPanelsRemoveAll');
			this.cmfg('taskManagerFormPanelForwarder', {
				functionName: 'disableModify',
				params: true
			});
			this.cmfg('taskManagerFormPanelForwarder', { functionName: 'disableCMTbar' });
		},

		/**
		 * @param {Boolean} state
		 *
		 * @returns {Void}
		 */
		taskManagerFormNavigationSetDisableNextButton: function (state) {
			state = Ext.isBoolean(state) ? state : false;

			this.view.nextButton.setDisabled(state);
		},

		/**
		 * Adapter method
		 *
		 * @param {Object} parameters
		 * @param {Object} parameters.functionName
		 * @param {Object} parameters.params
		 *
		 * @returns {Void}
		 *
		 * FIXME: waiting for refactor (CMDBuild.view.common.PanelFunctions)
		 */
		taskManagerFormPanelForwarder: function (parameters) {
			this.view.cmTBar = this.view.getDockedComponent(CMDBuild.core.constants.Proxy.TOOLBAR_TOP).items.items;
			this.view.cmButtons = this.view.getDockedComponent(CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM).items.items;

			switch (parameters.functionName) {
				case 'disableCMTbar':
					return this.view.disableCMTbar();

				case 'disableModify':
					return this.view.disableModify(parameters.params);

				case 'enableCMButtons':
					return this.view.enableCMButtons();

				case 'enableTabbedModify':
					return this.view.enableTabbedModify(parameters.params);
			}
		},

		/**
		 * @param {Array} panels
		 *
		 * @returns {Void}
		 */
		taskManagerFormPanelsAdd: function (panels) {
			this.view.removeAll();

			if (Ext.isArray(panels) && !Ext.isEmpty(panels)) {
				this.view.add(panels);
			} else {
				_error('taskManagerFormPanelsAdd(): unmanaged panels parameter', this, panels);
			}
		},

		/**
		 * @returns {Object}
		 */
		taskManagerFormPanelsRemoveAll: function () {
			this.view.removeAll();
		},

		/**
		 * @param {Boolean} withDisabled
		 *
		 * @returns {Object}
		 */
		taskManagerFormViewDataGet: function (withDisabled) {
			withDisabled = Ext.isBoolean(withDisabled) ? withDisabled : false;

			return this.view.getData(withDisabled);
		},

		/**
		 * @returns {CMDBuild.view.administration.taskManager.FormPanel}
		 */
		taskManagerFormViewGet: function () {
			return this.view;
		},

		/**
		 * @returns {Void}
		 */
		taskManagerFormViewReset: function () {
			this.view.reset();
		}
	});

})();
