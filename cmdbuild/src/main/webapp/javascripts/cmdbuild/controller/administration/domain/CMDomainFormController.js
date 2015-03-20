(function() {

	Ext.define("CMDBuild.controller.administration.domain.CMDomainFormController", {
		constructor: function(view) {
			this.view = view;
			this.view.delegate = this;
			this.currentDomain = null;

			this.view.saveButton.on("click", onSaveButtonClick, this);
			this.view.deleteButton.on("click", onDeleteButtonClick, this);
			this.view.abortButton.on("click", onAbortButtonClick, this);
		},

		/**
		 * Gatherer function to catch events
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		onDomainSelected: function(cmDomain) {
			this.currentDomain = cmDomain;
			this.view.onDomainSelected(cmDomain);
		},
		onAddButtonClick: function() {
			this.currentDomain = null;
			this.view.onAddButtonClick();
		},
		onDomainDeleted: Ext.emptyFn
	});

	function onSaveButtonClick() {
		var originDisabledClasses = [];
		var destinationDisabledClasses = [];

		// Get origin disabled classes
		this.parentDelegate.controllerEnabledClasses.view.originTree.getStore().getRootNode().eachChild(function(childNode) {
			if (!childNode.get(CMDBuild.core.proxy.CMProxyConstants.ENABLED))
				originDisabledClasses.push(childNode.get(CMDBuild.core.proxy.CMProxyConstants.NAME));
		}, this);

		// Get destination disabled classes
		this.parentDelegate.controllerEnabledClasses.view.destinationTree.getStore().getRootNode().eachChild(function(childNode) {
			if (!childNode.get(CMDBuild.core.proxy.CMProxyConstants.ENABLED))
				destinationDisabledClasses.push(childNode.get(CMDBuild.core.proxy.CMProxyConstants.NAME));
		}, this);

		var invalidFields = this.view.getNonValidFields();

		if (invalidFields.length == 0) {
			CMDBuild.LoadMask.get().show();
			var withDisabled = true;
			var data = this.view.getData(withDisabled);
			if (this.currentDomain == null) {
				data.id = -1;
			} else {
				data.id = this.currentDomain.get(CMDBuild.core.proxy.CMProxyConstants.ID);

				data.disabled1 = Ext.encode(originDisabledClasses); // TODO proxy constants
				data.disabled2 = Ext.encode(destinationDisabledClasses); // TODO proxy constants
			}

			CMDBuild.ServiceProxy.administration.domain.save({
				params: data,
				scope: this,
				success: function(req, res, decoded) {
					this.view.disableModify();
					_CMCache.onDomainSaved(decoded.domain);
					_CMCache.flushTranslationsToSave(decoded.domain.name);
				},
				callback: function() {
					CMDBuild.LoadMask.get().hide();
				}
			});

		} else {
			CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);
		}
	}

	function onAbortButtonClick() {
		if (this.currentDomain != null) {
			this.onDomainSelected(this.currentDomain);
		} else {
			this.view.reset();
			this.view.disableModify();
		}
	}

	function onDeleteButtonClick() {
		Ext.Msg.show({
			title: this.view.translation.delete_domain,
			msg: CMDBuild.Translation.common.confirmpopup.areyousure,
			scope: this,
			buttons: Ext.Msg.YESNO,
			fn: function(button) {
				if (button == "yes") {
					deleteDomain.call(this);
				}
			}
		});
	}

	function deleteDomain() {
		if (this.currentDomain == null) {
			// nothing to delete
			return;
		}

		var me = this;
		var params = {};
		params[_CMProxy.parameter.DOMAIN_NAME] = this.currentDomain.get("name");

		CMDBuild.LoadMask.get().show();
		CMDBuild.ServiceProxy.administration.domain.remove({
			params: params,
			success : function(form, action) {
				me.view.reset();
				_CMCache.onDomainDeleted(me.currentDomain.get("id"));
			},
			callback : function() {
				CMDBuild.LoadMask.get().hide();
			}
		});
	}
})();