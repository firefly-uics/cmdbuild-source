Ext.define("CMDBuild.controller.administration.filter.CMBIMPanelController", {
	extend: "CMDBuild.controller.CMBasePanelController",

	mixins: {
		gridFormPanelDelegate: "CMDBuild.delegate.administration.common.basepanel.CMGridAndFormPanelDelegate"
	},

	constructor: function(view) {
		this.callParent(arguments);
		this.mixins.gridFormPanelDelegate.constructor.call(this, view);
		this.fieldManager = null;
		this.gridConfigurator = null;
		this.record = null;
	},

	onViewOnFront: function(group) {
		if (this.fieldManager == null) {
			this.fieldManager = new CMDBuild.delegate.administration.bim.CMBIMFormFieldsManager();
			this.view.buildFields(this.fieldManager);
		}

		this.view.disableModify();

		if (this.gridConfigurator == null) {
			this.gridConfigurator = new CMDBuild.delegate.administration.bim.CMBIMGridConfigurator();
			this.view.configureGrid(this.gridConfigurator);
		}

		this.gridConfigurator.getStore().load();
	},

	// as gridFormPanelDelegate

	/**
	 * called after the save button click
	 * @param {CMDBuild.view.administration.common.basepanel.CMForm} form
	 */
	// override
	onGridAndFormPanelSaveButtonClick: function(form) {
		var me = this;
		var params = me.fieldManager.getValues() || {};
		var url = _CMProxy.url.bim.create;

		if (this.record != null) {
			url = _CMProxy.url.bim.update;
			params["id"] = this.record.getId();
		}

		if (form != null) {
			CMDBuild.LoadMask.get().show();
			this.view.enableModify();
			form.submit({
				url: url,
				params: params,
				fileUpload: true,
				success: function() {
					me.fieldManager.enableFileField();
					CMDBuild.LoadMask.instance.hide();
					me.gridConfigurator.getStore().load();
				},
				failure: function() {
					me.view.disableModify();
					CMDBuild.LoadMask.instance.hide();
				}
			});
		}
	},

	/**
	 * 
	 * @param {CMDBuild.view.administration.common.basepanel.CMForm} form
	 * the form that call the function
	 * 
	 * @param {String} action
	 * a string that say if the button is clicked when configured
	 * to activate or deactivate something ["disable" | "enable"]
	 */
	// override
	onEnableDisableButtonClick: function(form, action) {
		var me = this;
		if (!me.record) {
			return;
		}

		var proxyFunction = _CMProxy.bim.disable;
		if (action == "enable") {
			proxyFunction = _CMProxy.bim.enable;
		}

		CMDBuild.LoadMask.instance.show();
		proxyFunction({
			params: {
				id: me.record.getId()
			},
			callback: function() {
				CMDBuild.LoadMask.instance.hide();
				me.gridConfigurator.getStore().load();
			}
		});
	},

	// as grid delegate

	// override
	onCMGridSelect: function(grid, record) {
		this.mixins.gridFormPanelDelegate.onCMGridSelect.apply(this, arguments);

		if (record) {
			this.view.updateEnableDisableButton(!record.get("active"));
		}
	}
});