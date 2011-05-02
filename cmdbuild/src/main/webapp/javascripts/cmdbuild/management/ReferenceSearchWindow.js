CMDBuild.Management.ReferenceSearchWindow = Ext.extend(CMDBuild.Management.CardListWindow, {
	layout: 'border',
	//custom attributes
	translation: CMDBuild.Translation.management.modcard.modify_relation_window,
	currentDomain: -1,
	attributeList: {},
	notInRelation: [],
	checkedCards: {},
	idClass: undefined,
	className: undefined,
	attributes: [],

	initComponent: function() {
		this.baseParams = this.combo.store.baseParams;
		
		this.saveButton = new Ext.Button({
			text : CMDBuild.Translation.common.buttons.save,
			name: 'saveButton',
			disabled: true,
			handler : this.onSave,
			scope : this
		});
		
		this.cancelButton = new Ext.Button({
			text : CMDBuild.Translation.common.buttons.abort,
			name: 'cancelButton',
			handler : function() {
				this.cardList.destroy();
				this.destroy();
			},
			scope : this
		});
	
		var cardListFilter = this.buildCardListFilter();
		this.filterPanel = this.buildFilterPanel(cardListFilter);
		this.cardList = this.buildCardList();
		this.addCardButton = this._buildAddButton({
			classId: this.baseParams.IdClass,
			eventName: "cmdbuild-new-referencecard"
		});
		this.tabPanel = this.buildTabPanel();

		var destClassDescription = CMDBuild.Cache.getTableById(this.idClass).text;
		Ext.apply(this, {
			border: false,
			title: CMDBuild.Translation.management.modcard.search_reference_window.window_title + destClassDescription,
			items: this.tabPanel
		});
		CMDBuild.Management.ReferenceSearchWindow.superclass.initComponent.apply(this);
		
		this.on('show', function() {
			this.addCardButton.manageDropDownArrowVisibility(CMDBuild.Cache.getTableById(this.idClass));
		}, this);
	},
	
	//private
	buildCardListFilter: function() {
		var cardListFilter = new CMDBuild.Management.DomainCardListFilter({
			IdClass: this.baseParams.IdClass,
			filterType: this.filterType,
			ownerWindow: this
		});
		
		cardListFilter.selectClass({
			classId: this.baseParams.IdClass,
			classAttributes: this.attributes
		});
		cardListFilter.on(CMDBuild.Management.Relations.FILTER_SUCCESS_EVENT_NAME, this.filtersuccess, this);
		return cardListFilter;
	},
	
	//private
	buildFilterPanel: function(cardListFilter) {		
		var filterPanel = {
			xtype: 'panel',
			layout: 'fit',
			disabled: 'true',
			title: CMDBuild.Translation.management.findfilter.filter,
			border:false
		};
		if (!this.baseParams.CQL) {
			filterPanel.items= [cardListFilter];
			filterPanel.disabled = false;
		}
		return filterPanel;
	},
	
	//private
	buildCardList: function() {
		var tmpParams = Ext.apply({},this.combo.store.baseParams);
		delete (tmpParams.limit);
		var baseParams = Ext.apply({
			FilterCategory: this.filterType,
			FilterSubcategory: this.id
		}, tmpParams);
		delete(baseParams.NoFilter);
		
		var cardList = new CMDBuild.Management.FixedCardGrid({
			attributes: this.attributes,
			baseParams: baseParams,
			params: this.params,
			singleSelect: true,
			checksmodel: false,
			filterType: this.filterType,
			ownerWindow: this,
			border: false,
			isValid: function(preventMark) {
				return this.validate();
			},
			validate: function() {
				return this.getSelectionModel().hasSelection();
			}
		});
		cardList.getSelectionModel().on('rowselect', function(sm, row, rec){
			this.saveButton.enable();
		}, this);
		cardList.getSelectionModel().on('rowdeselect', function(sm, row, rec){
			this.saveButton.disable();
		}, this);
		cardList.on('rowdblclick',this.onSave , this);
		return cardList;
	},

	//private
	buildTabPanel: function() {
		var tabPanel = new Ext.TabPanel({
			activeTab: 0,
			region: 'center',
			items: [{
				xtype: 'panel',
				layout: 'fit',
				border: false,
				title: CMDBuild.Translation.management.findfilter.list,
				items: [this.cardList],
				tbar: [this.addCardButton]
			}, this.filterPanel],
			buttonAlign: 'center',
			buttons: [
				this.saveButton,
				this.cancelButton
			]
		});
		tabPanel.on('tabchange', function(tabPanel, tab){
			tab.doLayout();
		});
		return tabPanel;
	},
	
	//private
	onAddClick: function(p) {
		var cardWin = new CMDBuild.Management.CardWindow({
			classId: p.classId,
			classAttributes: decoded.attributes,
			className: jsonRow.Class
		});
		cardWin.show();
	},
	
	//private
	onSave: function() {
		var sel = this.cardList.getSelectionModel().getSelected();
		if (typeof sel != "undefined") {
			this.fireEvent('cmdbuild-referencewindow-selected', sel.json);
		}
		this.cardList.clearFilter();
		this.destroy();
	},
	
	//private
	filtersuccess: function() {
		this.tabPanel.setActiveTab(0);
		this.cardList.reload();
	}
});