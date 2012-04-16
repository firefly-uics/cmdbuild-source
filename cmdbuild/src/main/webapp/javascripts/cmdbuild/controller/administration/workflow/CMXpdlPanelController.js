(function() {
	
	var tr =  CMDBuild.Translation.administration.modClass.attributeProperties;
	
	Ext.define("CMDBuild.controller.administration.workflow.CMXpdlPanelController", {
		constructor: function(view) {
			this.currentProcessId = null;

			this.view = view;
			this.uploadForm = view.uploadForm;
			this.downloadForm = view.downloadForm;

			this.uploadForm.submitButton.on("click", onUploadSubmitClick, this);
			this.downloadForm.submitButton.on("click", onDownloadSubmitClick, this);
		},
		
		onProcessSelected: function(processId, process) {
			this.currentProcessId = processId;
			if (!process || process.get("superclass")) {
				this.view.disable();
			} else {
				this.view.enable();

				CMDBuild.LoadMask.get().show();
				CMDBuild.Ajax.request({
					url : 'services/json/workflow/xpdlversions',
					method: 'POST',
					params: {idClass : processId},
					scope: this,
					success: function(response, options, json) {
						CMDBuild.LoadMask.get().hide();
						var versions = json.response;
						this.downloadForm.onProcessSelected(versions);
					},
					failure: function() {
						CMDBuild.LoadMask.get().hide();
					}
				});
			}
		},
		
		onAddClassButtonClick: function() {
			this.currentProcessId = null;
			
			this.view.disable();
		}
	});
	
	function onUploadSubmitClick() {
		CMDBuild.LoadMask.get().show();

		this.uploadForm.getForm().submit({
			url: 'services/json/schema/modworkflow/uploadxpdl',
			params: {
				idClass: this.currentProcessId
			},
			scope: this,
			success: function(form, action) {
				CMDBuild.LoadMask.get().hide();
				var result = Ext.decode(action.response.responseText),
					msg = "<ul>";
				
				for (var i=0, len=result.messages.length; i<len; ++i) {
					msg += "<li>" 
						+ CMDBuild.Translation.administration.modWorkflow.xpdlUpload[result.messages[i]]
						+ "</li>";
				}
				msg+="</ul>";
				
				CMDBuild.Msg.info(CMDBuild.Translation.common.success, msg);
			},
			failure: function() {
				CMDBuild.LoadMask.get().hide();
				CMDBuild.Msg.error(CMDBuild.Translation.common.failure,
					CMDBuild.Translation.administration.modWorkflow.xpdlUpload.error, true);
			}
		});
	}
	
	function onDownloadSubmitClick() {
		var version = this.downloadForm.versionCombo.getValue(),
		url = "";

		if (version == 'template' || !version) {
			url = 'services/json/workflow/downloadxpdltemplate';
		} else {
			url = 'services/json/schema/modworkflow/downloadxpdl';
		}

		this.downloadForm.getForm().submit({
			url: url,
			method: "GET",
			params: {
				idClass: this.currentProcessId
			}
		});
	}
})();