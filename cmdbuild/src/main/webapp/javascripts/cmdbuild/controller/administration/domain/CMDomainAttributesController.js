(function() {
	
	var translation = CMDBuild.Translation.administration.modClass.attributeProperties;
	
	Ext.define("CMDBuild.controller.administration.domain.CMDomainAttributesController", {
		constructor: function(view) {
			this.view = view;
			this.currentDomain = null;
			this.currentAttribute = null;

			this.gridSM = this.view.grid.getSelectionModel();
			this.gridSM.on('selectionchange', onSelectionChanged , this);

			this.view.form.abortButton.on("click", onAbortButtonClick, this);
			this.view.form.saveButton.on("click", onSaveButtonClick, this);
			this.view.form.deleteButton.on("click", onDeleteButtonClick, this);
			this.view.grid.addAttributeButton.on("click", onAddAttributeClick, this);
		},

		onDomainSelected: function(domain) {
			this.currentDomain = domain;
			this.view.onDomainSelected(domain);
		}
	});

	function onSelectionChanged(selection) {
		if (selection.selected.length > 0) {
			_debug(selection.selected.items[0])
			this.currentAttribute = selection.selected.items[0];
			this.view.form.onAttributeSelected(this.currentAttribute);
		}
	}

	function onAddAttributeClick() {
		this.currentAttribute = null;
		this.view.onAddAttributeClick();
	}
	
	function onAbortButtonClick() {
		if (this.currentAttribute == null) {
			this.view.form.disableModify();
			this.view.form.reset();
		} else {
			this.view.form.onAttributeSelected(this.currentAttribute);
		}
	}
	
	function onSaveButtonClick() {
		var nonValid = this.view.form.getNonValidFields();
		if (nonValid.length > 0) {
			CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);
			return;
		}
		var data = this.view.form.getData(withDisabled = true);
		data.tableId = this.currentDomain.get("id");
		CMDBuild.LoadMask.get().show();
		CMDBuild.ServiceProxy.administration.domain.attribute.save({
			params: data,
			scope: this,
			success: function(response, request, decoded) {
				this.currentAttribute = null;
				this.view.form.disableModify();
				_CMCache.onDomainAttributeSaved(this.currentDomain.get("id"), decoded.attribute)
				this.view.grid.selectAttributeByName(decoded.attribute.name);
			},
			callback: function() {
				CMDBuild.LoadMask.get().hide();
			}
		});
	}
	
	function onDeleteButtonClick() {
		Ext.Msg.show({
			title: translation.delete_attribute,
			msg: CMDBuild.Translation.common.confirmpopup.areyousure,
			scope: this,
			buttons: Ext.Msg.YESNO,
			fn: function(button) {
				if (button == "yes") {
					deleteAttribute.call(this);
				}
			}
		});
	}
	
	function deleteAttribute() {
		if (!this.currentDomain || !this.currentAttribute) {
			return;
		}

		CMDBuild.LoadMask.get().show();
		CMDBuild.ServiceProxy.administration.domain.attribute.remove({
			params: {
				tableId: this.currentDomain.get("id"),
				name: this.currentAttribute.get("name")
			},
			scope : this,
			success : function(form, action) {
				_CMCache.onDomainAttributeDelete(this.currentDomain.get("id"), this.currentAttribute.data);
				this.currentAttribute = null;
			},
			callback : function() {
				CMDBuild.LoadMask.get().hide();
			}
		});
	}
})();