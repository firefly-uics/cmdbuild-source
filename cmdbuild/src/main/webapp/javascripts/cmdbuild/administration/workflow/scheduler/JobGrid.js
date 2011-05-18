CMDBuild.Administration.JobGrid = Ext.extend(Ext.grid.GridPanel, {
	translation: CMDBuild.Translation.administration.modWorkflow.scheduler,
	initComponent: function() {
		this.store = new Ext.data.JsonStore({
			url: 'services/json/schema/scheduler/listprocessjobs',
	        root: "rows",
	        fields: [
		           {name: 'description'},
		           {name: 'params'},
		           {name: 'cronExpression'},
		           {name: 'id'}
		        ],
	        remoteSort: this.remoteSort
		});
				    
	    var addJobButton = new Ext.Button({
	    	text: this.translation.addJob,
	    	iconCls: 'add',
	    	scope: this,
	    	handler: function() {
	    		this.getSelectionModel().clearSelections();
	    		this.fireEvent('newJob');
	    	}
	    });
		    
	    Ext.apply(this, {
	        store: this.store,
	        tbar: [addJobButton],
	        viewConfig: { forceFit:true },
	        loadMask: true,
	        columns: [
	            {header: this.translation.description, width: 160, sortable: true, dataIndex: 'description'},
	            {header: this.translation.cronexpression, width: 75, sortable: true, dataIndex: 'cronExpression'}
	        ],
	        stripeRows: true,
	        layout: 'fit'
	    });
	    
	    this.getSelectionModel().on('rowselect', function(sm, index, record){
	    	this.fireEvent('jobSelected', record);
	    }, this);
	    
	    CMDBuild.Administration.JobGrid.superclass.initComponent.apply(this, arguments);
	},
	
	loadProcessJob: function(processType) {
		this.store.load({
			params: {
				idClass: processType
			},
			callback: function(r, option, success) {
				try {
					this.getSelectionModel().clearSelections();
				} catch (e) {
					_debug("JobGrid: I can't clear selections");
				}
				if (r.length == 0) {
					this.fireEvent('cmdb-empty-jobgrid');
				}
			},
			scope: this
		});
	},
	
	isSelected: function() {
		return this.getSelectionModel().isSelected();
	}
});