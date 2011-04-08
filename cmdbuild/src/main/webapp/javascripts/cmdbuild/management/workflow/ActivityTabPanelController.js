(function() {
	
	var ERROR_TEMPLATE = "<p class=\"{0}\">{1}</p>";
	
	CMDBuild.Management.ActivityTabPanelController = function(view) {
		
		view.optionsTab.on("deactivate", function(p) {
			p.disable();
		});
		
		view.optionsTab.on("wrong_wf_widgets", function showTheWrongWidgets(wrongWidgets) {
			var out = "<ul>";
			for (var i=0, l=wrongWidgets.length; i<l; ++i) {
				out += "<li>" + wrongWidgets[i] + "</li>";
			}
			out+"</ul>";
			var msg = String.format(ERROR_TEMPLATE
					, CMDBuild.Constants.css.error_msg
					, CMDBuild.Translation.errors.wrong_wf_widgets);
			CMDBuild.Msg.error(null, msg + out, true);
		}, view);
		
		view.activityTab.on("terminate_process", function(params) {
			this.fireEvent("terminate_process", params);
		}, view);
		
		view.activityTab.on("start_edit", function() {
			this.optionsTab.onActivityStartEdit();
		}, view);
		
		view.activityTab.on("save", function(params) {
			var isAdvance = params.isAdvance;
			
			this.optionsTab.waitForBusyWidgets(callback=save, scope=view);
			
			function save() {
				if (!(params.skipValidation || activityFormAndWFWidgetsAreBothValid.call(view))) {
					return;
				}
				if (params.toStart) {
					fireStartProcessEvent();
				} else {
					view.optionsTab.saveWFWidgets(params, function() {
						view.activityTab.updateActivity(isAdvance);
					}, view);
				}
			}
			
			function fireStartProcessEvent() {
				view.fireEvent("start_process", {
					activityInfo: params,
					callback: function(p) {
						var process = p.process;
						process.isAdvance = isAdvance;
						view.activityTab.processStarted(process);
						view.optionsTab.saveWFWidgets(process, function() {
							view.activityTab.updateActivity(isAdvance);
						}, view);
					},
					scope: view
				});
			}
			
		}, view);
	};
	
	function activityFormAndWFWidgetsAreBothValid() {		
		return (wFWidgetsAreValid.call(this) & activityFormIsValid.call(this)); //I always want to call both
	}

	function wFWidgetsAreValid() {
		var widget = this.optionsTab.getInvalidWidgetsAsHTML();
		if (widget != null) {
			var msg = String.format(ERROR_TEMPLATE
					, CMDBuild.Constants.css.error_msg
					, CMDBuild.Translation.errors.invalid_extended_attributes);
			CMDBuild.Msg.error(null, msg + widget, false);
			
			return false;
		} else {
			return true;
		}		
	}
	
	function activityFormIsValid() {
		var invalidFields = this.activityTab.getInvalidAttributeAsHTML();
		if (invalidFields != null) {
			var msg = String.format(ERROR_TEMPLATE
					, CMDBuild.Constants.css.error_msg
					, CMDBuild.Translation.errors.invalid_attributes);
			CMDBuild.Msg.error(null, msg + invalidFields, false);
			
			return false;
		} else {
			return true;
		}
	}
})();