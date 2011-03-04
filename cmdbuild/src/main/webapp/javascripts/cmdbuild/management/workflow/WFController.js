(function () {
	
	CMDBuild.Management.WFController = function(view) {
		setLoadActivityListener(view);
		setEmptyGridListener(view);
		setNewActivityListener(view);
		setTerminateProcessListener(view);
		setStartProcessListener(view);
	};
	
	function setLoadActivityListener(view) {
		view.cardListGrid.on("load_activity", function(activity) {
			view.loadActivity(activity);
		});
	}
	
	function setEmptyGridListener(view) {
		view.cardListGrid.on("empty_grid", function() {
			view.onEmptyActivityGrid();
		});
	}
	
	function setNewActivityListener(view) {
		view.startProcessMenu.on('new_activity', function(params) {
			CMDBuild.ServiceProxy.workflow.getstartactivitytemplate({
				classId: params.classId,
				success: success,
				failure: failure
			});
			
			function success(response) {
				var params = {
					edit:true,
					isnew:true,
					record: Ext.util.JSON.decode(response.responseText)
				};
				view.newActivity(params);
			}
			
			function failure() {
				CMDBuild.Msg.error(CMDBuild.Translation.errors.error_message, CMDBuild.Translation.errors.generic_error, true);
			}			
		});
	}
	
	function setTerminateProcessListener(view) {
		view.activityTabPanel.on("terminate_process", function(params) {
			Ext.Msg.confirm(
					CMDBuild.Translation.management.modworkflow.abort_card, // title
					CMDBuild.Translation.management.modworkflow.abort_card_confirm, // message
					confirmCB);
			
			function confirmCB(btn) {
				if (btn != 'yes') {
                    return;
				} else {
					CMDBuild.LoadMask.get().show();
					CMDBuild.ServiceProxy.workflow.terminateActivity({
						WorkItemId: params.WorkItemId,
		                ProcessInstanceId: params.ProcessInstanceId,
						success: success,
						failure: failure
					});
				}
			}
			
			function success(response) {				
				CMDBuild.LoadMask.get().hide();
				var ret = Ext.util.JSON.decode(response.responseText);
                if (ret.success) {
                    view.cardListGrid.reloadCard();
                }                
			}
			
			function failure() {				
                CMDBuild.LoadMask.get().hide();
                CMDBuild.Msg.error(
                    CMDBuild.Translation.errors.error_message,
                    CMDBuild.Translation.errors.generic_error,
                    true);
			}	
		});
	}

	function setStartProcessListener(view) {
		view.activityTabPanel.on("start_process", function(p) {
			
			CMDBuild.LoadMask.get().show();
			CMDBuild.ServiceProxy.workflow.startProcess({
				idClass: p.activityInfo.idClass,               
				success: success,
				failure: failure
			});
			
			function success(response) {
				CMDBuild.LoadMask.get().hide();
				var process = Ext.util.JSON.decode(response.responseText);
				process.data.isAdvance = p.isAdvance;
				p.callback.call(p.scope, {
					process: process.data,
					isAdvance: p.isAdvance
				});				
			}
			
			function failure(response, options) {
            	CMDBuild.LoadMask.get().hide();
            	CMDBuild.Msg.error(CMDBuild.Translation.errors.error_message, CMDBuild.Translation.errors.generic_error, true);
            }
		});
	}	
})();