/**
 * This is the Grid of Domain that contains a list to make 
 * filter about the relations between table
 * 
 * @class CMDBuild.Management.FilterRelationsDomainGrid
 * @extends CMDBuild.Grid
 */
CMDBuild.Management.FilterRelationsDomainGrid = Ext.extend(CMDBuild.EditorGrid, {
	translation: CMDBuild.Translation.administration.modClass.domainProperties,
	baseUrl : 'services/json/schema/modclass/getdomainlist',
	filtering : false,
	viewConfig: { forceFit:true },
	id: 'relationdomainlist',
	withStore: false,
	withPagingBar: false,
	enableColumnMove: false,
	clicksToEdit: 1,
	
	//custom attributes
	actualIdDomain: undefined,
	
	initComponent:function() {
		this.store =  new CMDBuild.Management.DomainStore({
			keepReadOnly: true,
			classId: this.idClass
		});
		Ext.apply(this, { store : this.store });
		
		this.storeForDestination = new Ext.data.JsonStore({
			url: 'services/json/schema/modclass/getsubclasses',
	        root: "subclasses",
	        fields : ['classId', 'className'],
	        autoLoad: false
		});
			
		this.destination_combo = new Ext.form.ComboBox({
           store: this.storeForDestination,
           displayField: 'className',
           typeAhead: true,
           triggerAction: 'all',
           lazyRender:true,
           listClass: 'x-combo-list-small',
           ownerCt: this //allows to fly and calculate the right z-index 
		});
		
		this.checkNotRel = new Ext.grid.CheckColumn({
	       header: CMDBuild.Translation.management.findfilter.notinrel,
	       dataIndex: 'notInRelation',
	       width: 30,
	       fixed: true,
	       menuDisabled: true,
	       hideable: false
		});
		
		this.checkAll = new Ext.grid.CheckColumn({
	       header: CMDBuild.Translation.management.findfilter.all,
	       dataIndex: 'all',
	       width: 30,
	       fixed: true,
	       menuDisabled: true,
	       hideable: false
		});
		
		this.columns = [{
				id : 'name',
				hideable: false,
				header : this.translation.domain,
				dataIndex : 'DomainDescription'
			},{
				header: CMDBuild.Translation.management.findfilter.direction,
				dataIndex: 'DireDescription'
			},{
				header : this.translation.class_destination,
				dataIndex : 'DestClassName',
				editor: this.destination_combo
			},
			this.checkNotRel,
			this.checkAll
			, {
				header : '&nbsp;',
				width: 30, 
				fixed: true, 
				sortable: false, 
				renderer: function(value) {
					return (value == true)?'<img class="action-attachment-download" src="images/icons/tick.png"/>':'';
				},
				align: 'center', 
				cellCls: 'grid-button', 
				dataIndex: 'setted',
				menuDisabled: true,
				id: 'imagecolumn',
				hideable: false
			}
		];
		Ext.apply(this,{
    		plugins: [this.checkNotRel,this.checkAll]    		
    	});
		
		//when click on a checkbox
		this.checkNotRel.onMouseDown = this.clickNotRel;
		this.checkAll.onMouseDown = this.clickAll;
		
    	CMDBuild.Management.FilterRelationsDomainGrid.superclass.initComponent.apply(this, arguments);
		this.setDataUrl({
		  idClass: this.idClass
		});	
		
		this.getSelectionModel().on('rowselect', this.domainSelected , this);		
	},
  
	//listen the event of a row-selection
	//publish a general event of selection domain
	domainSelected: function(sm, row, rec) {
		//define the domain selected
		//append to the domain a string to defin _D direct or _I inverse domain
		var direction = (rec.data.Direct) ? "_D" : "_I";
		var idDomain = rec.data.DomainId.toString() + direction;
		
		//update the class attributes
		this.actualRecord = rec;
		this.actualIdDomain = idDomain;
		
		var eventParams = {
			idDomain: idDomain,
			cass2Name: rec.data.DestClassName,
			class2Id: rec.data.DestClassId
		};		
		
		//to reload the destinationCombo's store
		var originalDestinationId = rec.originalDestinationId;
		if(typeof originalDestinationId != "undefined"){
			this.storeForDestination.baseParams['ClassId'] = originalDestinationId;
			this.storeForDestination.load();
		}
		this.destination_combo.eventParams = eventParams;
		this.fireEvent('cmdb-domainselected', eventParams);		
  	},
  	
	//this is an override of the onMouseDown method of checkColumn
  	//render the check in the box and call the functions to update the checkedRecords
	clickNotRel: function(e, t){
        if(t.className && t.className.indexOf('x-grid3-cc-'+this.id) != -1){
        	e.stopEvent();
        	var grid = this.grid;
            var index = this.grid.getView().findRowIndex(t);
            var record = this.grid.store.getAt(index);
            //redraw the box
            grid.updateCheck(record, this);
            var directedDomain = record.data.DirectedDomain;   	 
        	var notRel =  record.data.notInRelation;
        	//fix the value of notRel
        	notRel = notRel == "" ? false : true;
        	
        	var params = {
        		checked: notRel,
        		domain: directedDomain,
        		record: record
        	};
        	grid.fireEvent('cmdb-notRelChecked', params);
        }
	},
		
	clickAll: function(e, t){
		if(t.className && t.className.indexOf('x-grid3-cc-'+this.id) != -1){
        	e.stopEvent();
        	var grid = this.grid;
            var index = this.grid.getView().findRowIndex(t);
            var record = this.grid.store.getAt(index);
            var direction = (record.data.Direct) ? "_D" : "_I";
            //redraw the box
            grid.updateCheck(record, this);
            var directedDomain = record.data.DirectedDomain;   	 
        	var all =  record.data.all;
        	//fix the value of notRel
        	all = all == "" ? false : true;
        	
        	var params = {
        		checked: all,
        		domain: domain,
        		record: record
        	};
        	grid.fireEvent('cmdb-allChecked', params);
        }
	},
	
	updateCheck: function(record, column){
		record.set(column.dataIndex, !record.data[column.dataIndex]);
	},
	
	updateDefined: function(checked){
		if (typeof this.actualRecord != "undefined")
			this.actualRecord.set("setted", checked);
	},
	
	getGrid: function() {
		return this;
	}
	
});
Ext.reg('relationsdomain', CMDBuild.Management.FilterRelationsDomainGrid);
