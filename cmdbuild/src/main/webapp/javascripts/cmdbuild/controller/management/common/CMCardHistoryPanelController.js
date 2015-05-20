(function() {

//	Ext.define('CMDBuild.controller.management.common.CMCardHistoryPanelController', {
//		extend: 'CMDBuild.controller.management.common.tabs.History'
//	});

	Ext.define('CMDBuild.controller.management.workflow.CMWorkflowHistoryPanelController', {
		extend: 'CMDBuild.controller.management.common.tabs.History',

		mixins: {
			observable: 'Ext.util.Observable',
			wfStateDelegate: 'CMDBuild.state.CMWorkflowStateDelegate'
		},

		constructor: function() {
			this.callParent(arguments);
			_CMWFState.addDelegate(this);

			this.mon(this.view, 'activate', function() {
				if (!this._loaded) {
					this.load();
				}
			}, this);
		},

		// wfStateDelegate
		onProcessInstanceChange: function(processInstance) {
			this._loaded = false;

			if (processInstance.isNew()) {
				this.view.disable();
			} else {
				this.view.enable();
				if (this.view.isVisible()) {
					this.load();
				}
			}

		},

		// override
		load: function() {
			this._loaded = true;
			var processInstance = _CMWFState.getProcessInstance();
			if (processInstance) {
				var params = {};
				params[_CMProxy.parameter.CARD_ID] = processInstance.get('id');
				params[_CMProxy.parameter.CLASS_NAME] = _CMCache.getEntryTypeNameById(processInstance.get('classId'));

				this.view.getStore().load({
					params: params
				});
			}
		},

		// override
		buildCardModuleStateDelegate: Ext.emptyFn,
		onEntryTypeSelected: Ext.emptyFn,
		onCardSelected: Ext.emptyFn
	});

})();