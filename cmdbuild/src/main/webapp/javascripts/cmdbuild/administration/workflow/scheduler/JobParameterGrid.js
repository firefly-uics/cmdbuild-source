Ext.define("CMDBuild.Administration.JobParameterGrid", {
	extend: "Ext.grid.GridPanel", //TODO Extjs 3 to 4 migration @@ editorgrid
	translation: CMDBuild.Translation.administration.modWorkflow.scheduler,
	initComponent: function() {
		this.recordTemplate = Ext.data.Record.create ([
   		    {name: 'key', mapping: 'key'},
   		    {name: 'value', mapping: 'value'}
   		]);
		
	 	this.store = new Ext.data.SimpleStore({
	        fields: [
	           {name: 'key'},
	           {name: 'value'}
	        ]
	    });
	    
	    var addParameterButton = new Ext.Button({
	    	text: this.translation.addParameter,
	    	iconCls: 'add',
	    	scope: this,
	    	handler:  function() {
				var r = new this.recordTemplate({
					key: this.translation.key,
					value: this.translation.value
				});
				this.stopEditing();
				this.store.insert(0, r);
				this.startEditing(0, 0);
			}
	    });
	    
	    var removeParameter = this.translation.removeParameter;
	    this.renderDeleteActions = function() {
			return '<img style="cursor:pointer" title="' +
			removeParameter + 
			'" class="action-parameter-delete" src="images/icons/cross.png"/>&nbsp;'
		};
		
		this.cm = new Ext.grid.ColumnModel([{
			header: this.translation.key,
			dataIndex: 'key',
			editor: new Ext.form.TextField({
				allowBlank: false
			})
		},{
			header: this.translation.value,
			dataIndex: 'value',
			editor:new Ext.form.TextField({
				allowBlank: false
			})
		},{
			header: '&nbsp', 
			width: 40, 
			fixed: true, 
			sortable: false, 
			renderer: this.renderDeleteActions, 
			align: 'center', 
			cellCls: 'grid-button', 
			dataIndex: 'delete',
			menuDisabled: true,
			id: 'imagecolumn',
			hideable: false
		}]);
	    
	    Ext.apply(this, {
	    	layout: 'fit',
	    	stripeRows: true,
	        frame: false,
	        clicksToEdit: 1,
	        cm: this.cm,
	        store: this.store,
	        viewConfig: { forceFit:true },
	        tbar: [addParameterButton]
	    });
	    this.on('cellclick', this.onCellClick, this);
	    CMDBuild.Administration.JobParameterGrid.superclass.initComponent.apply(this, arguments);
	},
	
	onCellClick: function(grid, rowIndex, colIndex, event) {
		var className = event.target.className;
		if (className == "action-parameter-delete") {
			var record = this.store.getAt(rowIndex);
			this.store.remove(record);
		}
	},
	
	getParametersAsMap: function() {
		var records = this.store.data.items
		var parameters = {}
		for (var i=0, l=records.length; i<l; i++) {
			var rec = records[i].data;
			parameters[rec.key] = rec.value;
		}
		return parameters;
	},
	
	loadJobParameter: function(parameters) {		
		this.store.removeAll();
		for (var p in parameters) {
			var r = new this.recordTemplate({
				key: p,
				value: parameters[p]
			});
			this.stopEditing();
			this.store.insert(0, r);
		}
		
	},
	
	removeAll: function() {
		this.store.removeAll();
	}
});