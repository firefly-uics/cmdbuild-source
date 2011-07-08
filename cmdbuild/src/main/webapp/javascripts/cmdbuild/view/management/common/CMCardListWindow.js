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
			border: "0 0 1 0"
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

		this.setItems();

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
	setItems: function() {
		this.addCardButton = this.buildAddButton();
		Ext.apply(this, {
			tbar: [this.addCardButton],
			items: [this.tabPanel]
		});
	},

	onFilterButtonClick: function() {
		var params = this.filter.getForm().getValues();

		params['IdClass'] =  this.idClass;
		params['FilterCategory'] = this.filterType;
		params['FilterSubcategory'] = this.id;

		CMDBuild.Ajax.request({
			url: 'services/json/management/modcard/setcardfilter',
			params: params,
			method: 'POST',
			scope: this,
			success: function(response) {
				this.tabPanel.setActiveTab(this.grid);
				this.grid.reload();
			}
		});
	},

	onResetFilterButtonClick: function() {
		this.filter.removeAllFieldsets();
		this.grid.clearFilter();
		this.tabPanel.setActiveTab(this.grid);
	},

	buildAddButton: function() {
		var addCardButton = new CMDBuild.AddCardMenuButton();
		var entry = _CMCache.getClassById(this.idClass) || _CMCache.getProcessById(this.idClass);
		addCardButton.updateForEntry(entry);
		
		addCardButton.on("cmClick", function buildTheAddWindow(p) {
			var w = new CMDBuild.view.management.common.CMCardWindow({
				classId: this.idClass,
				cmEditMode: true,
				withButtons: true,
				title: p.className
			}).show();

			w.on("close", function() {
				this.grid.reload();
			}, this);

			new CMDBuild.controller.management.common.CMCardWindowController(w);

		}, this);

		return addCardButton;
	},
	
	onSelectionChange: Ext.emptyFn,
	onGridDoubleClick: Ext.emptyFn
});
