(function() {

	Ext.define("CMDBuild.state.CMWorkflowStateDelegate", {
		onProcessClassRefChange: Ext.emptyFn,
		onProcessInstanceChange: Ext.emptyFn,
		onActivityInstanceChange: Ext.emptyFn
	});

	Ext.define("CMDBuild.model.CMActivityInstance", {
		constructor: function(data) {
			if (data) {
				this.data = data;
			} else {
				this.data = {};
				this.nullObject = true;
			}
		},

		isNew: function() {
			if (this.nullObject) {
				return false;
			} else {
				return (this.data.id  == null || typeof this.data.id == "undefined");
			}
		},

		getId: function() {
			return this.data.id;
		},

		getVariables: function() {
			return this.data.variables || [];
		},

		getPerformerName: function() {
			return this.data.performerName || "";
		},

		getDescription: function() {
			return this.data.description || "";
		},

		getInstructions: function() {
			return this.data.instructions || "";
		},

		getWidgets: function() {
			return this.data.widgets || [];
		}
	});

	Ext.define("CMDBuild.model.CMProcessInstance", {
		extend: "Ext.data.Model",

		fields: [
			"beginDate",
			"classDescription",
			"className",
			"endDate",
			"flowStatus",
			{name: "id", type: "integer"},
			{name: "classId", type: "integer"},
			{name: "values", type: "auto"},
			{name: "activityInstanceInfoList", type: "auto"}
		],

		STATE: {
			OPEN: "OPEN",
			SUSPENDED: "SUSPENDED",
			COMPLETED: "COMPLETED",
			TERMINATED: "TERMINATED",
			ABORTED: "ABORTED",
			UNSUPPORTED: "UNSUPPORTED"
		},

		getActivityInfoList: function() {
			return this.get("activityInstanceInfoList") || [];
		},

		getId: function() {
			return this.get("id") || null;
		},

		isNew: function() {
			return this.getId() == null;
		},

		getValues: function() {
			return this.get("values") || {};
		},

		getClassId: function() {
			return this.get("classId") || null;
		},

		applyValues: function(values) {
			if (values) {
				this.data.values = Ext.apply(this.getValues(), values);
			}
		},

		getFlowStatus: function() {
			return this.get("flowStatus");
		},

		isStateOpen: function() {
			return this.getFlowStatus() == this.STATE.OPEN;
		}
	});

	Ext.define("CMDBuild.state.CMWorkflowState", {
		constructor: function() {
			var processClassRef = null,
				activityInstance = null,
				processInstance = null,
				delegates = [];

			Ext.apply(this, {
				setProcessClassRef: function(pcr) {
					if (processClassRef !== pcr) {
						processClassRef = pcr;
						this.notifyToDelegates("onProcessClassRefChange", [pcr]);

						this.setProcessInstance(new CMDBuild.model.CMProcessInstance({
							classId: processClassRef.getId()
						}));
					}
				},

				getProcessClassRef: function() {
					return processClassRef;
				},

				setProcessInstance: function(pi) {
					processInstance = pi;

					var processClassRefIsASuperclass = (processClassRef 
							&& processClassRef.isSuperClass());

					var me = this;

					if (!processInstance.isNew() && processClassRefIsASuperclass) {
						CMDBuild.ServiceProxy.card.get({
							params: {
								Id: processInstance.getId(),
								IdClass: processInstance.getClassId()
							},
							success: function(a,b, response) {
								processInstance.applyValues(response.card);
								onProcessInstanceChange();
							}
						});
					} else {
						onProcessInstanceChange();
					}

					function onProcessInstanceChange() {
						// set the current CMActivityInstance to a empty activity
						me.setActivityInstance(new CMDBuild.model.CMActivityInstance());
						me.notifyToDelegates("onProcessInstanceChange", [processInstance]);
					}
				},

				getProcessInstance: function() {
					return processInstance;
				},

				setActivityInstance: function(ai) {
					activityInstance = ai;
					this.notifyToDelegates("onActivityInstanceChange", [ai]);
				},

				getActivityInstance: function() {
					return activityInstance;
				},

				addDelegate: function(delegate) {
					CMDBuild.validateInterface(delegate, "CMDBuild.state.CMWorkflowStateDelegate");
					delegates.push(delegate);
				},

				countDelegates: function() { // for test
					return delegates.length;
				},

				notifyToDelegates: function(method, args) {
					for (var i=0, l=delegates.length, d=null; i<l; ++i) {
						d = delegates[i];
						if (d && typeof d[method] == "function") {
							d[method].apply(d, args);
						}
					}
				}
			});
		}
	});

	// Define a global variable of
	_CMWFState = new CMDBuild.state.CMWorkflowState();
})();