CMDBuild.Grid = Ext.extend(Ext.grid.GridPanel, {
	columns: [],
	baseUrl: '',
	stripeRows: true,
	remoteSort: false,
	extraFieldsStore: [],
	pagingTools: [], //buttons in the paging bar
	colorsConst: CMDBuild.Constants.colors.blue,
	initComponent: function() {
		var arrColumns = this.extraFieldsStore;
		
		for(var len = arrColumns.length, i = 0; i < this.columns.length; i++) {
			arrColumns[len+i] = this.columns[i].dataIndex;
		}
			
		this.store = new Ext.data.JsonStore({
			url: this.baseUrl,
	        root: "rows",
            totalProperty: 'results',
	        fields: arrColumns,
	        remoteSort: this.remoteSort
		});
		
		this.pagingBar =  new Ext.PagingToolbar({
            pageSize: parseInt(CMDBuild.Config.cmdbuild.rowlimit),
            store: this.store,
            displayInfo: true,
            displayMsg: ' {0} - {1} '+CMDBuild.Translation.common.display_topic_of+' {2}',
	        emptyMsg: CMDBuild.Translation.common.display_topic_none,
            items: this.pagingTools,
            style: { 'border-bottom': '1px '+this.colorsConst.border+' solid' }
        }),
		
		Ext.apply(this, {
	            border : false,
	            autoScroll: true,
	            viewConfig: { forceFit: true },
	            bbar: this.pagingBar,
	            loadMask: true,
	            layout: 'fit'
        });

		if (!this.sm) {
			this.sm = new Ext.grid.RowSelectionModel({ singleSelect: true });
		}

    	CMDBuild.Grid.superclass.initComponent.apply(this, arguments);
	},
	
	reload: function(object){
    	this.store.reload(object);
	},
	
	setDataUrl: function(params) {
		this.store.load({params: params});
	},
	
	setColumns: function(headers){
		var arrColumns = this.extraFieldsStore;
		
		for(var len = arrColumns.length, i = 0; i < headers.length; i++)
			arrColumns[len + i] = headers[i].dataIndex;
        this.pagingBar.unbind(this.store);        
		var store = new Ext.data.JsonStore({
			url: this.baseUrl,
	        root: "rows",
            totalProperty: 'results',
	        fields: arrColumns,
	        remoteSort: this.remoteSort
		});
		store.on('load', function(store, records, options) {
			// TODO see CardListGrid loadCards: dontSelectFirst
			// should be changed with the raw to be selected 
			if (!options.dontSelectFirst && store.getCount() > 0) {
				var sm = this.getSelectionModel();
				if (!sm.getSelected()) {
					this.applySelection(0);
				}
			}
			this.fireEvent("CM_load", arguments);
		}, this);
		this.store = store;
		this.reconfigure(store, new Ext.grid.ColumnModel(headers));
        this.pagingBar.bind(store);
	},

	applySelection: function(row) {
		this.getSelectionModel().selectRow(row);
		this.getView().focusRow(row);
	}
});

Ext.reg('cmdbuildGrid', CMDBuild.Grid);

// CUSTOM RENDERERS

Ext.grid.CheckColumn = function(config){
    Ext.apply(this, config);
    if(!this.id){
        this.id = Ext.id();
    }
    this.renderer = this.renderer.createDelegate(this);
};
Ext.reg('checkcolumn', Ext.grid.CheckColumn);

Ext.grid.CheckColumn.prototype ={
    init : function(grid){
        this.grid = grid;
        this.grid.on('render', function(){
            var view = this.grid.getView();
            view.mainBody.on('mousedown', this.onMouseDown, this);
        }, this);
    },

    onMouseDown : function(e, t){
        if(t.className && t.className.indexOf('x-grid3-cc-'+this.id) != -1){
            e.stopEvent();
            var index = this.grid.getView().findRowIndex(t);
            var record = this.grid.store.getAt(index);
            record.set(this.dataIndex, !record.data[this.dataIndex]);
        }
    },

    renderer : function(v, p, record){
        p.css += ' x-grid3-check-col-td'; 
        v = CMDBuild.Utils.evalBoolean(v);
        return '<div class="x-grid3-check-col'+(v?'-on':'')+' x-grid3-cc-'+this.id+'">&#160;</div>';
    }
};