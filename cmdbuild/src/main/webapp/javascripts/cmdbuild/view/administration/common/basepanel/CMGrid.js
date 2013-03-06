Ext.define("CMDBuild.view.administration.common.basepanel.CMGrid", {
	extend: "Ext.grid.Panel",

	// configuration
	gridConfigurator: null,
	// configuration

	mixins: {
		delegable: "CMDBuild.core.CMDelegable"
	},

	constructor: function() {
		this.mixins.delegable.constructor.call(this,
				"CMDBuild.delegate.administration.common.basepanel.CMGridDelegate");

		this.callParent(arguments);
	},

	initComponent: function() {
		var me = this;

		this.columns = [];
		this.store =  new Ext.data.SimpleStore({
			fields: [],
			data: []
		});


		this.callParent(arguments);

		this.on("select", function(grid, record, options) {
			me.callDelegates("onCMGridSelect", [grid, record]);
		});
	},

	/**
	 * 
	 * @param {CMDBuild.delegate.administration.common.basepanel.CMGridConfigurator} gridConfigurator
	 * configure the store and the columns of this grid asking for them to the
	 * given configurator
	 */
	configureGrid: function(gridConfigurator) {
		if (gridConfigurator) {
			this.store = gridConfigurator.getStore();
			this.columns = gridConfigurator.getColumns();
			this.reconfigure(this.store, this.columns);
		}
	}
});