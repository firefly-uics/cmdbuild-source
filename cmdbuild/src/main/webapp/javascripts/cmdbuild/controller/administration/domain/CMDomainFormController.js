(function() {

	Ext.define("CMDBuild.controller.administration.domain.CMDomainFormController", {
		constructor: function(view) {
			this.view = view;
			this.view.delegate = this;
			this.currentDomain = null;

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

		onAbortButtonClick: onAbortButtonClick,

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