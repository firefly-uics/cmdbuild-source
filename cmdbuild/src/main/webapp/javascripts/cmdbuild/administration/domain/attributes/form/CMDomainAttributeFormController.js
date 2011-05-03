(function() {
	
	Ext.ns("CMDBuild.administration.domain");
	var ns = CMDBuild.administration.domain;
	var translation = CMDBuild.Translation.administration.modClass.attributeProperties;
	
	ns.CMDomainAttributeFormController = function(conf) {
		ns.CMDomainAttributeFormController.superclass.constructor.call(this, conf);
		this.view.saveButton.on("click", onSaveButtonClick, this);
		this.view.deleteButton.on("click", onDeleteButtonClick, this);
	}

	Ext.extend(ns.CMDomainAttributeFormController, CMDBuild.core.CMBaseController, {
		onAttributeSaved: Ext.emptyFn,
		onAddAttributeClick: function() {
			this.model = null;
			this.view.prepareToAdd();
		},
		onDomainSelected: function(domain) {
			this.domain = domain;
			this.view.clearForm();
			this.view.disableModify();
		},
		onRowSelected: function(record) {
			this.model = record.ownerModel
			this.view.disableModify();
			this.view.fillWithModel(this.model);
		}
	});
	
	function onSaveButtonClick() {
		var invalidFields = this.view.getInvalidFieldsAsHTML();
		if (invalidFields == null) {
			CMDBuild.LoadMask.get().show();
			var params = this.view.getValues();
			params.tableId = this.domain.getid();
			CMDBuild.ServiceProxy.administration.domain.attribute.save({
				params: params,
				scope: this,
				success: function(response, request, decoded) {
					this.view.disableModify();
					this.onAttributeSaved(decoded.attribute);
				},
				callback: function() {
					CMDBuild.LoadMask.get().hide();
				}
			});
		} else {
			var msg = String.format("<p class=\"{0}\">{1}</p>",
					CMDBuild.Constants.css.error_msg,
					CMDBuild.Translation.errors.invalid_attributes);
			CMDBuild.Msg.error(null, msg + invalidFields, false);
		}
	}
	
	function onDeleteButtonClick() {
		Ext.Msg.show({
			title: translation.delete_attribute,
			msg: CMDBuild.Translation.common.confirmpopup.areyousure,
			scope: this,
			buttons: {
				yes: true,
				no: true
			},
			fn: function(button) {
				if (button == "yes") {
					deleteAttribute.call(this);
				}
			}
		});
	}
	
	function deleteAttribute() {
		CMDBuild.LoadMask.get().show();
		CMDBuild.ServiceProxy.administration.domain.attribute.remove({
			params: {
				tableId: this.domain.getid(),
				name: this.model.getname()
			},
			scope : this,
			success : function(form, action) {
				this.view.destroyModel();
				this.view.disableToolBar();
			},
			callback : function() {
				CMDBuild.LoadMask.get().hide();
			}
		});
	}
})();