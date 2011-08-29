Ext.define("CMDBuild.view.administration.workflow.cron.CMJobParameterGrid", {

	extend: "Ext.grid.GridPanel",
	translation: CMDBuild.Translation.administration.modWorkflow.scheduler,

	initComponent: function() {

		var cellEditing = Ext.create('Ext.grid.plugin.CellEditing', {
			clicksToEdit : 1
		});

		this.store = new Ext.data.Store({
			data: [],
			fields: ['key','value']
		});

		this.addParameterButton = new Ext.Button({
			text: this.translation.addParameter,
			iconCls: 'add',
			scope: this,
			handler:  function() {
				this.store.insert(0, {key:"", value:""});
				cellEditing.startEditByPosition({row: 0, column: 0});
			}
		});

		this.renderDeleteActions = function() {
			return '<img style="cursor:pointer" title="' +
			this.translation.removeParameter + 
			'" class="action-parameter-delete" src="images/icons/cross.png"/>&nbsp;'
		};

		this.columns = [{
			header: this.translation.key,
			dataIndex: 'key',
			flex: 1,
			field: {
				allowBlank: false
			}
		},{
			header: this.translation.value,
			dataIndex: 'value',
			flex: 1,
			field: {
				allowBlank: false
			}
		},{
			header: '&nbsp', 
			width: 40, 
			fixed: true, 
			sortable: false, 
			renderer: this.renderDeleteActions, 
			align: 'center', 
			tdCls: 'grid-button', 
			dataIndex: 'delete',
			menuDisabled: true,
			hideable: false
		}];

		Ext.apply(this, {
			layout : 'fit',
			stripeRows : true,
			frame : false,
			clicksToEdit : 1,
			store : this.store,
			plugins: [cellEditing],
			tbar : [ this.addParameterButton ]
		});

		this.on('beforeitemclick', this.onCellClick, this);
		this.callParent(arguments);
	},

	onCellClick: function(grid, model, htmlelement, rowIndex, event, opt) {
		if (event.target.className == "action-parameter-delete") {
			this.store.remove(this.store.getAt(rowIndex));
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

	onJobSelected: function(parameters) {
		this.store.removeAll();
		if (parameters && parameters != {}) {
			for (var p in parameters) {
				this.store.add({
					key: p,
					value: parameters[p]
				});
			}
		}
		this.addParameterButton.disable();
	},
	
	onAddJobButtonClick: function() {
		this.store.removeAll();
		this.addParameterButton.enable();
	},

	removeAll: function() {
		this.store.removeAll();
	}
});