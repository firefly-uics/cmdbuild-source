(function() {
	var STATE = "state",
	STATE_VALUE_OPEN = "open.running",
	STATE_VALUE_SUSPENDED = "open.not_running.suspended",
	STATE_VALUE_COMPLETED = "closed.completed",
	STATE_VALUE_ALL = "all"; // Not existent

	Ext.define("CMDBuild.controller.management.workflow.CMActivityGridController", {
		extend: "CMDBuild.controller.management.common.CMCardGridController",

		constructor: function(view, supercontroller) {
			this.callParent(arguments);

			this.CMEVENTS.processClosed = "processTerminated";

			this.addEvents(this.CMEVENTS.processClosed);
			// from cmmodworkflow
			this.mon(this.view.statusCombo, "select", onStatusComboSelect, this);
			this.mon(this.view.addCardButton, "cmClick", this.onAddCardButtonClick, this);
		},

		onEntryTypeSelected : function(entryType) {
			this.callParent(arguments);
			this.view.addCardButton.updateForEntry(entryType);
		},

		onAddCardButtonClick: function(p) {
			this.gridSM.deselectAll();

			var me = this;
	
			CMDBuild.ServiceProxy.workflow.getstartactivitytemplate(p.classId, {
				scope: this,
				success: success,
				important: true
			});
	
			function success(response) {
				var template =  Ext.JSON.decode(response.responseText);
	
				template.data.ProcessInstanceId = undefined;
				template.data.WorkItemId = undefined;
				template._cmNew = true;

				// to unify the Ext.models with the server response;
				template.raw = template.data; 
				template.get = function(key) {
					return template.raw[key];
				};

				this.onCardSelected(null, [template]);
			}
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
		}
	});

	function onStatusComboSelect() {
		this.view.updateStatusParamInStoreProxyConfiguration();
		this.view.loadPage(1);
	}
})();