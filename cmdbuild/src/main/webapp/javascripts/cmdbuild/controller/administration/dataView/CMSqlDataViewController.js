Ext.define("CMDBuild.controller.administration.dataview.CMSqlDataViewController", {
	extend: "CMDBuild.controller.CMBasePanelController",

	mixins: {
		gridFormPanelDelegate: "CMDBuild.delegate.administration.common.basepanel.CMGridAndFormPanelDelegate"
	},

	constructor: function(view) {
		this.mixins.gridFormPanelDelegate.constructor.call(this, view);
		this.fieldManager = null;
		this.gridConfigurator = null;
		this.record = null;

		this.callParent(arguments);
	},

	onViewOnFront: function(selection) {
		if (this.fieldManager == null) {
			this.fieldManager = new CMDBuild.delegate.administration.common.dataview.CMSqlDataViewFormFieldsManager();
			this.view.buildFields(this.fieldManager);
			this.view.disableModify();
		}

		if (this.gridConfigurator == null) {
			this.gridConfigurator = new CMDBuild.delegate.administration.common.dataview.CMSqlDataViewGridConfigurator();
			this.view.configureGrid(this.gridConfigurator);
		}
	}
});