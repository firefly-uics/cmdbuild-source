(function() {
	var STATE_VALUE_COMPLETED = "closed.completed";

	Ext.define("CMDBuild.controller.management.workflow.CMActivityGridController", {
		extend: "CMDBuild.controller.management.common.CMCardGridController",

		mixins: {
			wfStateDelegate: "CMDBuild.state.CMWorkflowStateDelegate"
		},

		constructor: function(view, supercontroller) {
			this.callParent(arguments);

			this.CMEVENTS.processClosed = "processTerminated";

			this.addEvents(this.CMEVENTS.processClosed);

			// from cmmodworkflow
			this.mon(this.view.statusCombo, "select", onStatusComboSelect, this);
			this.mon(this.view.addCardButton, "cmClick", this.onAddCardButtonClick, this);

			_CMWFState.addDelegate(this);
		},

		onAddCardButtonClick: function(p) {
			this.gridSM.deselectAll();

			_CMWFState.setProcessInstance(new CMDBuild.model.CMProcessInstance({
				classId: p.classId
			}));

			CMDBuild.LoadMask.get().show();

			CMDBuild.ServiceProxy.workflow.getstartactivitytemplate(p.classId, {
				scope: this,
				success: function success(response, request, decoded) {
					var activity = new CMDBuild.model.CMActivityInstance(decoded.response || {});
					_CMWFState.setActivityInstance(activity);
				},
				callback: function() {
					CMDBuild.LoadMask.get().hide();
				},
				important: true
			});
		},

		// override
		_onGetPositionSuccessForcingTheFilter: function(p, position, resText) {
			this.view.setStatus(resText.FlowStatus);
			this.callParent(arguments);
		},

		// override
		_onGetPositionFailureWithoutForcingTheFilter: function(resText) {
			var flowStatusOfSearchedCard = resText.FlowStatus;
			if (flowStatusOfSearchedCard == STATE_VALUE_COMPLETED) {
				this.view.skipNextSelectFirst();
				this.fireEvent(this.CMEVENTS.processClosed);
			} else {
				this.callParent(arguments);
			}
		},

		// wfStateDelegate
		onProcessClassRefChange : function(entryType) {
			// FIXME: for compatibility, remove when switch to a state obj also on ModCard
			this.onEntryTypeSelected(entryType);
		},

		onEntryTypeSelected: function(entryType, danglingCard) {
			this.callParent(arguments);
			this.view.addCardButton.updateForEntry(entryType);
		}
	});

	function onStatusComboSelect() {
		this.view.updateStatusParamInStoreProxyConfiguration();
		this.view.loadPage(1);
	}
})();