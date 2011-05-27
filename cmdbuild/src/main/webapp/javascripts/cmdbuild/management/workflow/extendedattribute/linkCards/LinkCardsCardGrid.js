(function() {
	CMDBuild.Management.LinkCards.LinkCardsCardGrid = Ext.extend(CMDBuild.Management.CardListGrid, {
	    isFirstLoad: true, // flag to identify the first loading to clear the filter
	    onRowSelect: Ext.emptyFn,
		applySelection: Ext.emptyFn,
		
	    constructor: function(config) {
			var outName = this.outputName;
			if (!config.noSelect) {
				this.sm = new Ext.grid.CheckboxSelectionModel({
					header : '&nbsp',
					checkOnly: true,
					singleSelect: config.singleSelect
				});
			}
			CMDBuild.Management.LinkCards.LinkCardsCardGrid.superclass.constructor.call(this, config);
		},
		
		setColumnsForClass : function(classAttributes) {
			var headers = [];
			for (var i = 0; i < classAttributes.length; i++) {
				var attribute = classAttributes[i];
				var header = CMDBuild.Management.FieldManager.getHeaderForAttr(attribute);
				if (header) {
					headers.push(header);
				}
			}
			var graphHeader = {
				header : '&nbsp',
				width : 20,
				fixed : true,
				sortable : false,
				renderer : this.renderGraphIcon,
				align : 'center',
				cellCls : 'grid-button',
				dataIndex : 'Id',
				menuDisabled : true,
				id : 'imagecolumn',
				hideable : false
			};
			headers.push(graphHeader);
			if (!this.noSelect) {
				headers.push(this.getSelectionModel());
			}
			this.setColumns(headers);
		},
		
		loadCards : function(position) {
			this.isFirstLoad = false;
			var pageSize = parseInt(CMDBuild.Config.cmdbuild.rowlimit);
			var page = position ? parseInt((position -1) / pageSize) : 0;
			
			this.getStore().load({
				params: {
					start: page*pageSize,
					limit: pageSize,
					ForceResetFilter: this.isFirstLoad
				},
				callback: function() {
					this.fireEvent("CM_load");
				},
				scope: this
			});
		},
				
		syncSelections: function() {
			var sm = this.getSelectionModel();
			sm.suspendEvents();
			sm.clearSelections();
			sm.resumeEvents();
			
			if (!this.noSelect) {
				var store = this.getStore();
				var selections = this.model.getSelections();
				for (var i = 0, l = selections.length; i<l; ++i) {
					var cardId = selections[i];
					this.selectByCardId(cardId);
				}
			}
		},
		
		selectByCardId: function(cardId) {
			var recIndex = this.getStore().find("Id", cardId);
			if (recIndex >= 0) {
				this.getSelectionModel().selectRow(recIndex, true);
			}
		},
		
		deselectByCardId: function(cardId) {
			var recIndex = this.getStore().find("Id", cardId);
			if (recIndex >= 0) {
				this.getSelectionModel().deselectRow(recIndex, true);
			}
		},
		onRowDoubleClick: Ext.emptyFn
	});	
})();