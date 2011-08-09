CMDBuild.Management.FixedCardGrid = Ext.extend( Ext.grid.GridPanel, {
	attributes: [],
	baseParams: undefined,
	region: 'center',
	
	initComponent: function() {
		var headers = this.buildHeaders(this.attributes);
		var storeFields = this.getStoreFields(headers);
		
		var params = Ext.apply({        	
        	limit: CMDBuild.Config.cmdbuild.rowlimit
        },this.baseParams);
		
		if (this.params) {
			Ext.apply(params, this.params)
		}
		
		var store = new Ext.data.JsonStore({
			url : 'services/json/management/modcard/getcardlist',			
	        root: "rows",
	        totalProperty: 'results',
	        fields: storeFields,
	        baseParams: params
		});
		
		this.pagingBar = new Ext.PagingToolbar({
	        pageSize: parseInt(CMDBuild.Config.cmdbuild.rowlimit),
	        store: store,
	        displayInfo: true,
	        displayMsg: ' {0} - {1} '+CMDBuild.Translation.common.display_topic_of+' {2}',
	        emptyMsg: CMDBuild.Translation.common.display_topic_none,
	        items: [new Ext.app.GridSearchField({grid: this})]
		});
		
		Ext.apply(this, {
			store: store,
			columns: headers,
			viewConfig: {forceFit:true},
			bbar: this.pagingBar
		});
		CMDBuild.Management.FixedCardGrid.superclass.initComponent.apply(this);
		store.load();
	},
	
	//private
	buildHeaders : function(classAttributes) {
		var headers = [];//array with the objectOption for the headers
		for (var i = 0; i < classAttributes.length; i++) {
			var attribute = classAttributes[i];
			var header = CMDBuild.Management.FieldManager.getHeaderForAttr(attribute);
			if (header) {
	    	   headers.push(header);
			}
		}
		return headers
	},
	
	//private
	getStoreFields: function(headers) {
		var storeFields = [];
		for(i = 0; i < headers.length; i++) {
			storeFields[i] = headers[i].dataIndex;
		}
		return storeFields;
	},
	
	reload: function() {
		this.store.reload();
	},
	
	clearFilter: function(){
		CMDBuild.Ajax.request({
			url: 'services/json/management/modcard/resetcardfilter'
		});
	}
});