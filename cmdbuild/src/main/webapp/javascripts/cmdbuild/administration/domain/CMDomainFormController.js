(function() {

	Ext.ns("CMDBuild.administration.domain");
	var STRUCTURE = CMDBuild.core.model.CMDomainModel.STRUCTURE;
	
	CMDBuild.administration.domain.CMDomainFormController = function(domainForm) {
		this.domainForm = domainForm;
		this.domainForm.saveButton.on("click", onSaveButtonClick, this.domainForm);
		this.domainForm.deleteButton.on("click", onDeleteButtonClick, this.domainForm);
	}
	
	CMDBuild.administration.domain.CMDomainFormController.prototype = {
		onDomainSelected: function(cmDomain) {
			this.domainForm.onDomainSelected(cmDomain);
		},
		onAddButtonClick: function() {
			this.domainForm.prepareToAdd();
		}
	}
	
	function onSaveButtonClick() {
		var invalidFields = this.getInvalidFieldsAsHTML();
		if (invalidFields == null) {
			CMDBuild.LoadMask.get().show();
			CMDBuild.ServiceProxy.administration.domain.save({
				form: this.getForm(),
				scope: this,
				success: function(req,res) {
					this.disableModify();
					var newDomain = new CMDBuild.core.model.CMDomainModel.buildFromJSON(res.result.domain);
					var oldDomain = CMDomainModelLibrary.get(newDomain.getid());
					if (oldDomain == null) {
						CMDomainModelLibrary.add(newDomain);
					} else {
						oldDomain.update(newDomain);
					}
				},
				callback: function() {
					CMDBuild.LoadMask.get().hide();
				}
			});
		} else {
			var msg = String.format("<p class=\"{0}\">{1}</p>", CMDBuild.Constants.css.error_msg, CMDBuild.Translation.errors.invalid_attributes);
			CMDBuild.Msg.error(null, msg + invalidFields, false);
		}
	}
	
	function onDeleteButtonClick() {
		Ext.Msg.show({
			title: this.translation.delete_domain,
			msg: CMDBuild.Translation.common.confirmpopup.areyousure,
			scope: this,
			buttons: {
				yes: true,
				no: true
			},
			fn: function(button) {
				if (button == "yes") {
					deleteDomain.call(this);
				}
			}
		});
	}
	
	function deleteDomain() {
		CMDBuild.LoadMask.get().show();
		CMDBuild.ServiceProxy.administration.domain.remove({
			params: {
				id: this.model.getid()
			},
			scope : this,
			success : function(form, action) {
				this.destroyModel();
			},
			callback : function() {
				CMDBuild.LoadMask.get().hide();
			}
		});
	}
})();