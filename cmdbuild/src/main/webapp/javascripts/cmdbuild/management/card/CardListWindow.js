Ext.define("CMDBuild.Management.CardListWindow", {
	extend: "CMDBuild.PopupWindow",

	idClass: undefined, // passed at instantiation
	filterType: undefined, // passed at instantiation

	initComponent: function() {
		if (typeof this.idClass == "undefined") {
			return;
		}

 		this.filterButton = new Ext.Button({
			text: CMDBuild.Translation.management.findfilter.go_filter,
			iconCls: 'ok',
			handler: this.onFilterButtonClick,
			scope: this
		});

		this.grid = new CMDBuild.view.management.common.CMCardGrid({
			filterCategory: this.filterType || this.id,
			filterSubcategory: this.id,
			cmAdvancedFilter: false,
			columns: [],
			title: CMDBuild.Translation.management.findfilter.list,
			frame: false,
			border: false
		});

		this.filter = new CMDBuild.Management.Attributes({
			attributeList: {}, 
			IdClass: this.idClass, 
			windowSize: this.windowSize,
			filterButton: this.filterButton,
			title: CMDBuild.Translation.management.findfilter.filter,
			frame: false,
			border: false
		});

		this.filter.resetFilterButton.on("click", this.onResetFilterButtonClick, this);

		this.tabPanel = new Ext.tab.Panel({
			frame: false,
			border: false,
			items: [this.grid, this.filter]
		});

		Ext.apply(this, {
			items: [this.tabPanel]
		});

		this.callParent(arguments);
		this.grid.getSelectionModel().on("selectionchange", this.onSelectionChange, this);
		this.grid.on("itemdblclick", this.onGridDoubleClick, this);
	},
	
	show: function() {
		this.callParent(arguments);
		this.grid.updateStoreForClassId(this.idClass);
		this.filter.updateMenuForClassId(this.idClass);
		return this;
	},
	
	// private, to override in subclass
	onSelectionChange: Ext.emptyFn,
	onFilterButtonClick: Ext.emptyFn,
	onResetFilterButtonClick: Ext.emptyFn,
	onGridDoubleClick: Ext.emptyFn,
	buildAddButton: Ext.emptyFn
});
