(function() {
	Ext.define("CMDBuild.view.management.common.CMTabPanelWithCardGridAndFilter", {
		extend: "Ext.tab.Panel",

		selType: "rowmodel", // can be passed in configuration
		multiSelect: false, // can be passed in configuration
		filterButton: undefined, // can be passed in configuration
		filterType: undefined, // can be passed in configuration
		idClass: undefined, // can be passed in configuration

		initComponent: function() {
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
				border: "0 0 1 0",
				selType: this.selType,
				multiSelect: this.multiSelect,
				cmAddGraphColumn: false
	 		});

			this.filter = new CMDBuild.Management.Attributes({
				attributeList: {},
				IdClass: this.idClass,
				filterButton: this.filterButton,
				title: CMDBuild.Translation.management.findfilter.filter,
				frame: false,
				border: false
			});

			this.filter.resetFilterButton.on("click", this.onResetFilterButtonClick, this);

			Ext.apply(this, {
				items: [this.grid, this.filter]
			});

			this.callParent(arguments);
		},

		updateForClassId: function(classId) {
			this.idClass = classId;
			this.grid.updateStoreForClassId(classId);
			this.filter.updateMenuForClassId(classId);
			this.filter.removeAllFieldsets();
		},
		
		onResetFilterButtonClick: function() {
			this.filter.removeAllFieldsets();
			this.grid.clearFilter();
			this.setActiveTab(this.grid);
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
					this.setActiveTab(this.grid);
					this.grid.reload();
				}
			});
		}
	})
})();