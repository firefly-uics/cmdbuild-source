/**
 * This is the Grid of Domain that contains a list to make 
 * filter about the relations between table
 * 
 * @class CMDBuild.Management.FilterRelationsDomainList
 * @extends CMDBuild.Grid
 */
CMDBuild.Management.DomainCardList = Ext.extend(CMDBuild.EditorGrid, {
	enableColumnMove: false,
	viewConfig: { forceFit:true },
	//custom attributes
	checksmodel: true,
	filterType: undefined,
	translation : CMDBuild.Translation.management.modcard.cardlist,
	baseUrl : 'services/json/management/modcard/getcardlist',
	withPagingBar: true,
	filtering : false,
	idDomain: undefined,
	currentClassId: undefined,
	remoteSort: true,
	withFilter: false,
	subfiltered: true,
	
	initComponent : function() { 
		this.pagingTools =  [];
		this.gridsearchfield = new Ext.app.GridSearchField({grid: this});
		
		if (this.withFilter) {
			var openFilterBtn = new Ext.Button({
				scope: this,
				xtype: 'button',
				iconCls: 'find',
				text: CMDBuild.Translation.management.findfilter.set_filter,
				handler: this.openFilter
			});
			this.clearFilterBtn = new Ext.Button({
				scope: this,
				id: 'clear_card_filter',
				xtype: 'button',
				iconCls: 'clear_find',
				disabled: true,	
				text: CMDBuild.Translation.management.findfilter.clear_filter,
				handler: this.clearFilter
			});			
			this.pagingTools =  [
				openFilterBtn,
				this.clearFilterBtn,
				'-',
				this.gridsearchfield
			];
		} else {
			this.pagingTools = [this.gridsearchfield];
		}
		CMDBuild.Management.DomainCardList.superclass.initComponent.apply(this, arguments);		
	},
	
	/*
	 * eventparams:
	 * 
	 * classId: classId,
	 * classAttributes: attributeList,
	 * idDomain: idDomain,
	 * className: className,
	 * cardState: this.states[idDomain]
	 * 
	 */
	initForClass : function(eventParams) {
		this.currentClassId = eventParams.classId;
		this.classAttributes = eventParams.classAttributes;
		this.className = eventParams.className;
		this.setColumnsForClass(eventParams.classAttributes);
		this.store.baseParams.CQL = eventParams.classFilter;
		this.reconfigureStore();
		this.getStore().baseParams.IdClass = this.currentClassId;
		if(this.subfiltered){
			this.getStore().baseParams.FilterCategory = this.filterType;
			this.getStore().baseParams.FilterSubcategory = this.ownerWindow.getId();
		}
		this.clearFilter();
	},
	
	setColumnsForClass : function(classAttributes) {
		this.headers = [];//array with the objectOption for the headers
		
		for (var i = 0; i < classAttributes.length; i++) {
			var attribute = classAttributes[i];
			var header = CMDBuild.Management.FieldManager.getHeaderForAttr(attribute);
			if (header) {
				this.headers.push(header);
			}
		}
	},
	
	reconfigureStore:function(){
		this.arrColumns = [];//array with the name of dataIndex for the store
		for(i = 0; i < this.headers.length; i++)
			this.arrColumns[i] = this.headers[i].dataIndex;

		if(this.withPagingBar)
        	this.pagingBar.unbind(this.store);
		
        var oldBaseParams = this.store.baseParams;
		this.store = new Ext.data.JsonStore({
			url: this.baseUrl,
	        root: "rows",
            totalProperty: 'results',
	        fields: this.arrColumns,
	        baseParams: oldBaseParams
		});

		if(this.checksmodel)
			this.headers.push(this.smodel);
		
		this.reconfigure(this.store, new Ext.grid.ColumnModel(this.headers));
        if (this.withPagingBar)
        	this.pagingBar.bind(this.store);
	},
	
	loadCards : function() {
		this.getStore().load({
			params: {
				start: 0,
				limit: parseInt(CMDBuild.Config.cmdbuild.rowlimit)
			}
		});
	},
	
	getDomain: function(){
		return this.idDomain;
	},
	
	openFilter: function() {
		searchWin = new CMDBuild.Management.SearchFilterWindow({
			attributeList:this.classAttributes,
			className: this.className,
			IdClass: this.currentClassId,
			grid: this
		});
		searchWin.show();
	},

	clearFilter: function(){
		CMDBuild.Ajax.request({
			url: 'services/json/management/modcard/resetcardfilter',
			scope: this,
			success: function(response) {
				if (this.clearFilterBtn) {
					this.clearFilterBtn.setDisabled(true);
				}
				
				if (this.pagingBar) {
					this.pagingBar.changePage(1);
				} else {
					this.reload();
				}
			}
		});
	}
});
