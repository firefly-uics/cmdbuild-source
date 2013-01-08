Ext.define("CMDBuild.Management.CardListWindow", {
	extend: "CMDBuild.PopupWindow",

	ClassName: undefined, // passed at instantiation
	idClass: undefined, // passed at instantiation
	filterType: undefined, // passed at instantiation
	readOnly: undefined, // passed at instantiation
	selModel: undefined, // if undefined is used the default selType
	selType: 'rowmodel', // to allow the opportunity to pass a selection model to the grid
	multiSelect: false,
	extraParams: {},
	gridConfig: {}, // passed at instantiation

	initComponent: function() {
		if (typeof this.idClass == "undefined" && typeof this.ClassName == "undefined") {
			throw "There are no Class Id or Class Name to load";
		}

		this.filterButton = new Ext.Button({
			text: CMDBuild.Translation.management.findfilter.go_filter,
			iconCls: 'ok',
			handler: this.onFilterButtonClick,
			scope: this
		});

		var gridConfig = Ext.apply(this.gridConfig, {
			filterCategory: this.filterType || this.id,
			filterSubcategory: this.id,
			cmAdvancedFilter: false,
			columns: [],
			title: CMDBuild.Translation.management.findfilter.list,
			frame: false,
			border: "0 0 1 0",
			selType: this.selType,
			multiSelect: this.multiSelect,
			CQL: this.extraParams
		});

		if (typeof this.selModel == "undefined") {
			gridConfig["selType"] = this.selType;
		} else {
			gridConfig["selModel"] = this.selModel;
		}

		this.grid = new CMDBuild.view.management.common.CMCardGrid(gridConfig);

		this.filter = new CMDBuild.view.management.common.filter.CMFilterAttributes({
			attributeList: {}, 
			IdClass: this.getIdClass(),
			windowSize: this.windowSize,
			filterButton: this.filterButton,
			title: CMDBuild.Translation.management.findfilter.filter,
			frame: false,
			border: false,
			disabled: this.extraParams.CQL
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
		var id = this.getIdClass();
		this.grid.updateStoreForClassId(id);
		this.filter.updateMenuForClassId(id);

		return this;
	},

	// protected
	setItems: function() {
		this.items = [this.tabPanel]

		if (!this.readOnly) {
			this.addCardButton = this.buildAddButton();
			this.tbar = [this.addCardButton];
		}
	},

	onFilterButtonClick: function() {
		var params = this.filter.getForm().getValues();
		
		params['IdClass'] =  this.getIdClass();
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
		var entry = _CMCache.getEntryTypeById(this.getIdClass());

		addCardButton.updateForEntry(entry);
		addCardButton.on("cmClick", function buildTheAddWindow(p) {
			var w = new CMDBuild.view.management.common.CMCardWindow({
				withButtons: true,
				title: p.className
			});

			new CMDBuild.controller.management.common.CMCardWindowController(w, {
				cmEditMode: true,
				card: null,
				entryType: p.classId
			});
			w.show();

			w.on("destroy", function() {
				this.grid.reload();
			}, this);

		}, this);

		return addCardButton;
	},

	getIdClass: function() {
		if (this.idClass) {
			return this.idClass;
		} else {
			var et = _CMCache.getEntryTypeByName(this.ClassName);
			if (et) {
				return et.getId();
			}
		}

		throw "No class info for " + Ext.getClassName(this);
	},

	onSelectionChange: Ext.emptyFn,
	onGridDoubleClick: Ext.emptyFn
});
