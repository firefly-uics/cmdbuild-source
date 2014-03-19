(function() {

	CMDBuild.ServiceProxy.workflow = {
		getstartactivitytemplate: function(classId, p) {
			CMDBuild.ServiceProxy.core.doRequest(Ext.apply({
				url: CMDBuild.ServiceProxy.url.workflow.getStartActivity,
				method: 'GET',
				params: {
					classId : classId
				}
			}, p));
		},

		getActivityInstance: function(params, conf) {
			conf.url = CMDBuild.ServiceProxy.url.workflow.getActivityInstance,
			conf.method = 'GET';
			conf.params = params;
			conf.important = true;

			if (typeof conf.callback == "undefined") {
				conf.callback = function() {
					CMDBuild.LoadMask.get().hide();
				};
			}

			CMDBuild.ServiceProxy.core.doRequest(conf);
		},

		isPorcessUpdated: function(p) {
			p.url = CMDBuild.ServiceProxy.url.workflow.isProcessUpdated,
			p.method = 'GET';

			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		terminateActivity: function(p) {
			p.url = CMDBuild.ServiceProxy.url.workflow.abortProcess,
			p.method = POST;

			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		saveActivity: function(p) {
			p.url = CMDBuild.ServiceProxy.url.workflow.saveActivity,
			p.method = POST;

			CMDBuild.ServiceProxy.core.doRequest(p);
		}
	};

	function adaptWidgets(inputWidgets) {
		var outputWidgets = [];
		Ext.Array.forEach(inputWidgets, function(w) {
			outputWidgets.push(adaptWidget(w));
		});
		return outputWidgets;
	}

	function adaptWidget(inputWidget) {
		return Ext.apply({
			identifier : inputWidget.id,
			ButtonLabel : inputWidget.label,
			btnLabel : inputWidget.label
		}, {
			".OpenNote" : function() {
				return {
					extattrtype : "openNote"
				};
			},
			".OpenAttachment" : function() {
				return {
					extattrtype : "openAttachment"
				};
			}
		}[inputWidget.type]());
	}

	function adaptVariables(inputVars) {
		var outputVars = {};
		for (i = 0, len = inputVars.length; i < len; ++i) {
			var v = inputVars[i];
			outputVars[v.name] = "";
			outputVars[v.name+"_index"] = i;
			outputVars[v.name+"_type"] = {
				READ_ONLY: "VIEW",
				READ_WRITE: "UPDATE",
				READ_WRITE_REQUIRED: "UPDATEREQUIRED"
			}[v.type];
		}
		return outputVars;
	}

})();