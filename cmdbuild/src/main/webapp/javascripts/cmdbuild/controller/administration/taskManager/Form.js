(function () {

	Ext.define('CMDBuild.controller.administration.taskManager.Form', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

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
			'taskManagerFormModifyButtonStateManage',
			'taskManagerFormNavigationSetDisableNextButton',
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
					return _error('buildControllerTask(): unmanaged type parameter', this, type);
			}
		},

		/**
		 * Buttons disabling could be also dived by disabling next step
		 *
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
		 * Buttons disabling could be also dived by disabling previous step
		 *
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

			// Error handling
				if (!Ext.isArray(type) || Ext.isEmpty(type))
					return _error('onTaskManagerFormAddButtonClick(): unmanaged type parameter', this, type);
			// END: Error handling

			this.controllerTask = this.buildControllerTask(type);

			if (Ext.isObject(this.controllerTask) && !Ext.Object.isEmpty(this.controllerTask))
				this.controllerTask.cmfg('onTaskManagerFormTaskAddButtonClick');
		},

		/**
		 * @returns {Void}
		 */
		onTaskManagerFormCloneButtonClick: function () {
			this.cmfg('taskManagerClearSelection', { disableClearForm: true });

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
			switch (action) {
				case 'first': {
					if (!Ext.isEmpty(this.view.getLayout().getLayoutItems()))
						this.view.getLayout().setActiveItem(0);
				} break;

				case 'next': {
					if (this.hasNext()) { // Optimize show event fire
						this.view.getLayout().getNext().suspendEvent('show');
						this.view.getLayout().next();
						this.view.getLayout().getActiveItem().resumeEvent('show');
					}
				} break;

				case 'previous': {
					if (this.hasPrevious()) { // Optimize show event fire
						this.view.getLayout().getPrev().suspendEvent('show');
						this.view.getLayout().prev();
						this.view.getLayout().getActiveItem().resumeEvent('show');
					}
				} break;

				default: {
					_error('onTaskManagerFormNavigationButtonClick(): unmanaged action parameter value', this, action);
				}
			}

			this.manageButtonsDisabledState();

			// Fires show event before activeItem set action (cons: fire show event fired twice)
			if (!Ext.isEmpty(this.view.getLayout().getActiveItem()))
				this.view.getLayout().getActiveItem().fireEvent('show');
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
			// Error handling
				if (this.cmfg('taskManagerSelectedTaskIsEmpty'))
					return _error('onTaskManagerFormRowSelected(): empty selected task property', this, this.cmfg('taskManagerSelectedTaskGet'));
			// END: Error handling

			this.controllerTask = this.buildControllerTask(this.cmfg('taskManagerSelectedTaskGet', CMDBuild.core.constants.Proxy.TYPE));

			if (Ext.isObject(this.controllerTask) && !Ext.Object.isEmpty(this.controllerTask))
				this.controllerTask.cmfg('onTaskManagerFormTaskRowSelected');
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
		 */
		taskManagerFormClearSelection: function () {
			this.cmfg('taskManagerFormPanelsRemoveAll');

			this.view.panelFunctionModifyStateSet({
				forceToolbarBottomState: true,
				forceToolbarTopState: true
			});
		},

		/**
		 * @returns {Void}
		 */
		taskManagerFormModifyButtonStateManage: function () {
			this.view.modifyButton.setDisabled(this.cmfg('taskManagerSelectedTaskGet', CMDBuild.core.constants.Proxy.ACTIVE));
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
		 * @param {Array} panels
		 *
		 * @returns {Void}
		 */
		taskManagerFormPanelsAdd: function (panels) {
			this.view.removeAll(false);

			// Error handling
				if (!Ext.isArray(panels) || Ext.isEmpty(panels))
					return _error('taskManagerFormPanelsAdd(): unmanaged panels parameter', this, panels);
			// END: Error handling

			this.view.add(panels);
		},

		/**
		 * @returns {Object}
		 */
		taskManagerFormPanelsRemoveAll: function () {
			this.view.removeAll(false);
		},

		/**
		 * @returns {Object}
		 */
		taskManagerFormViewDataGet: function () {
			return this.view.panelFunctionDataGet({ includeDisabled: true });
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
			this.view.panelFunctionReset();
		}
	});

})();
