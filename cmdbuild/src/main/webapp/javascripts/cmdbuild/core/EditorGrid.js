CMDBuild.EditorGrid = Ext.extend(Ext.grid.EditorGridPanel, {
	columns: [],
	baseUrl: '',
	stripeRows: true,
	clickstoEdit: 1,
	withCheckColumn: false, //set to true if you want a column with checkbox
	checkColumnHeader: "&nbsp;", //ovveride to change the header of the checkcolum
	withPagingBar: false, //set to true if you want the paging bar
	withStore: true,
	checksmodel: false, //set true to checkboxSelectoinModel 
	remoteSort: false,

	initComponent: function() {
		var arrColumns = [];
		
		//dummy initialization of the paging bar
		this.pagingBar = {
			bind: function(){},
			unbind: function(){}
		};
		
		
		if (this.withCheckColumn) {
			//the checkBox
		    this.checkColumn = new Ext.grid.CheckColumn({
		       header: this.checkColumnHeader,
		       dataIndex: 'checked',
		       width: 30,
		       fixed: true
		    });
		}
		
		if(this.checksmodel){
			this.smodel = this.sm || new CMDBuild.grid.XCheckboxSelectionModel({singleSelect:false, grid: this});
		} else {
			this.smodel = new Ext.grid.RowSelectionModel();
		}
		
		for(i = 0; i < this.columns.length; i++) {
			arrColumns[i] = this.columns[i].dataIndex;
		}
		
		if (this.withStore) {
			this.store = new Ext.data.JsonStore({
				url: this.baseUrl,
		        root: "rows",
	            totalProperty: 'results',
		        fields: arrColumns,
		        remoteSort: this.remoteSort
			});
		}
		
		if (this.withPagingBar) {
		    this.pagingBar = new Ext.PagingToolbar({
		        pageSize: parseInt(CMDBuild.Config.cmdbuild.rowlimit),
		        store: this.store,
		        displayInfo: true,
		        displayMsg: ' {0} - {1} '+CMDBuild.Translation.common.display_topic_of+' {2}',
		        emptyMsg: CMDBuild.Translation.common.display_topic_none,
		        items: this.pagingTools
			});
		}
		
		Ext.apply(this, {
            border : false,
            autoScroll: true,
            viewConfig: { forceFit:true },
            loadMask: true,
            layout: 'fit',
            sm: this.smodel
        });

        if(this.withPagingBar){
        	Ext.apply(this, {
				bbar: this.pagingBar
        	});
        };
        
        if(this.withCheckColumn){
        	Ext.apply(this,{
        		plugins: this.checkColumn
        	});
        };

    	CMDBuild.EditorGrid.superclass.initComponent.apply(this, arguments);
	},
	
	reload: function(object){
    	this.store.reload(object);
	},
	
	setDataUrl: function(params) {
		this.store.load({params: params});
	},
	
	setColumns: function(headers){
		var arrColumns = [];
		
		if (this.withCheckColumn) {
	    	headers.push(this.checkColumn);
		}
		
		for (i = 0; i < headers.length; i++) {
			arrColumns[i] = headers[i].dataIndex;
		}
			
        this.pagingBar.unbind(this.store);
        if (this.withStore) {
			this.store = new Ext.data.JsonStore({
				url: this.baseUrl,
		        root: "rows",
	            totalProperty: 'results',
		        fields: arrColumns,
		        remoteSort: this.remoteSort
			});
        }
		this.reconfigure(this.store, new Ext.grid.ColumnModel(headers));
        this.pagingBar.bind(this.store);
	}
});

Ext.reg('cmdbuildEditorGrid', CMDBuild.EditorGrid);