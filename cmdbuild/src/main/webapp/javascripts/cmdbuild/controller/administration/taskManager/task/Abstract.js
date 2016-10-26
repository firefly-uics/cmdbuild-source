(function () {

	/**
	 * Create/Update discrimination is base on presence of selectedTask value, form has an hidden field for task ID that will be sent to server while updating a task
	 *
	 * @abstract
	 */
	Ext.define('CMDBuild.controller.administration.taskManager.task.Abstract', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.Form}
		 */
		parentDelegate: undefined,

		/**
		 * @returns {Void}
		 */
		onTaskManagerFormTaskAbortButtonClick: function () {
			if (this.cmfg('taskManagerSelectedTaskIsEmpty'))
				return this.cmfg('taskManagerClearSelection');

			return this.cmfg('onTaskManagerFormTaskRowSelected');
		},

		/**
		 * @returns {Void}
		 */
		onTaskManagerFormTaskAddButtonClick: function () {
			this.cmfg('taskManagerFormViewGet').panelFunctionModifyStateSet({ state: true });
			this.cmfg('onTaskManagerFormNavigationButtonClick', 'first');
		},

		/**
		 * @returns {Void}
		 */
		onTaskManagerFormTaskCloneButtonClick: function () {
			this.cmfg('taskManagerFormViewGet').panelFunctionModifyStateSet({ state: true });
			this.cmfg('onTaskManagerFormNavigationButtonClick', 'first');
		},

		/**
		 * @returns {Void}
		 */
		onTaskManagerFormTaskModifyButtonClick: function () {
			this.cmfg('taskManagerFormViewGet').panelFunctionModifyStateSet({ state: true });
			this.cmfg('onTaskManagerFormNavigationButtonClick', 'first');
		},

		/**
		 * @returns {Void}
		 */
		onTaskManagerFormTaskRemoveButtonClick: function () {
			Ext.Msg.show({
				title: CMDBuild.Translation.common.confirmpopup.title,
				msg: CMDBuild.Translation.common.confirmpopup.areyousure,
				buttons: Ext.Msg.YESNO,
				scope: this,

				fn: function (buttonId, text, opt) {
					if (buttonId == 'yes')
						this.removeItem();
				}
			});
		},

		/**
		 * @returns {Void}
		 *
		 * @abstract
		 */
		onTaskManagerFormTaskRowSelected: function () {
			this.cmfg('taskManagerFormModifyButtonStateManage');
			this.cmfg('taskManagerExternalServicesFormStateManager'); // External services endpoint to correctly set form state
		},

		/**
		 * @returns {Void}
		 *
		 * @abstract
		 */
		onTaskManagerFormTaskSaveButtonClick: Ext.emptyFn,

		/**
		 * @returns {Void}
		 *
		 * @abstract
		 */
		removeItem: Ext.emptyFn,

		/**
		 * @param {Object} response
		 * @param {Object} options
		 * @param {Object} decodedResponse
		 *
		 * @returns {Void}
		 */
		success: function (response, options, decodedResponse) {
			decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

			this.cmfg('onTaskManagerFormNavigationButtonClick', 'first');
			this.cmfg('taskManagerStoreLoad', {
				scope: this,
				callback: function (records, operation, success) {
					if (Ext.isEmpty(records)) {
						this.cmfg('taskManagerClearSelection');
					} else {
						this.cmfg('taskManagerRecordSelect', Ext.isEmpty(decodedResponse) ? this.cmfg('taskManagerFormViewGet').panelFunctionValueGet({
							includeDisabled: true,
							propertyName: CMDBuild.core.constants.Proxy.ID
						}) : decodedResponse);

						if (this.cmfg('taskManagerSelectedTaskIsEmpty'))
							this.cmfg('taskManagerFormViewGet').panelFunctionModifyStateSet({
								forceToolbarTopState: false,
								state: false
							});
					}
				}
			});
		},

		/**
		 * @return {Boolean}
		 *
		 * @abstract
		 * @override
		 * @private
		 */
		validate: function () {
			return this.callParent([this.cmfg('taskManagerFormViewGet')]);
		}
	});

})();
