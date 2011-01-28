CMDBuild.Management.ModBulkCardUpdate = Ext.extend(CMDBuild.ModPanel, {
	modtype: 'bulkcardupdate',
	title : CMDBuild.Translation.management.modutilities.bulkupdate.title,
	layout: 'fit',
	hideMode: 'offsets',
	frame: false,
	border: true,
	disableEventBus: true,
	filterType: 'modutilities',
	
    initComponent: function() {	
    	this.saveBtn = new Ext.Button({
    		text: CMDBuild.Translation.common.buttons.save,
    		scope: this,
    		disabled: true,
    		handler: this.saveCardsChanges
    	});
    	
    	this.abortButton = new Ext.Button({
    		text: CMDBuild.Translation.common.buttons.abort,
    		handler: this.abortCardsChanges, 
    		scope: this,
    		disabled: true
    	});
    	
    	this.cardSelected = [];
    	
    	var root = CMDBuild.Cache.getTree(CMDBuild.Constants.cachedTableType["class"]);
    	this.classTree = new Ext.tree.TreePanel({    		
    		layout: 'fit',    		
    	 	autoScroll: true,
	        containerScroll: true,
	        enableDD: false,
	        rootVisible: false,
	        disableSort: false,
	    	enableEditing: false,
	    	region: 'west',
	    	split: true,
	    	width: 200,
	    	border: false,
	    	rootName: 'bulkUpdate_classes',
	    	style: {'border-right': '1px #99BBE8 solid'},
			root: root
    	});
    	
    	new Ext.tree.TreeSorter(this.classTree);
    	
    	this.classTree.getSelectionModel().on('selectionchange', function(sm, node){
        	this.onSelectNode(node);
        }, this);
    	
		this.cardList = new CMDBuild.Management.DomainCardList({
			filterType: this.filterType, 
			withFilter: true,
			region: 'center',
			split: true,
			subfiltered: false,
			disabled: true
		});
		
		this.cardList.pagingBar.style = {'border-bottom': '1px #99BBE8 solid'};
		this.cardList.getStore().baseParams.writeonly = true;
		this.cardList.getSelectionModel().on('rowselect', this.cardSelect, this);
		this.cardList.getSelectionModel().on('rowdeselect', this.deselectCard, this);
		this.cardList.pagingBar.on('change', this.onPageChange, this);
		
		this.attributesPanel = new CMDBuild.Management.BulkCardAttributesPanel({
			region: 'south',
    		height: '50%',
    		split: true,
    		autoScroll: true,
    		frame: true,
    		border: false,
    		style: {padding: '1px 5px 5px 5px'}
		});
    	
    	Ext.apply(this, {
    		layout: 'border',
    		items: [this.classTree, {
    			xtype: 'panel',
    			layout: 'border',
    			region: 'center',
    			frame: false,
    			border: false,
    			style: {'border-left': '1px #99BBE8 solid'},
    			items: [this.cardList,this.attributesPanel],
    			buttonAlign: 'center',
    	    	buttons: [this.saveBtn,this.abortButton]
    		}],
    		style: {background: '#DFE8F6'}	    	
    	});
    	CMDBuild.Management.ModBulkCardUpdate.superclass.initComponent.apply(this, arguments);
    	this.subscribe('cmdb-select-'+this.modtype, this.layoutFixForIE, this);
   	},
   	
	layoutFixForIE: function() {
		this.doLayout();
	},

   	onSelectNode: function(node) {
   		if (node) {
			var eventParams = {
				classId: node.attributes.id,
				className: node.attributes.text
			};
			if (node.attributes.subtype)
				eventParams.itemType = node.attributes.subtype;
			this.selectDomain(eventParams);
			this.abortButton.enable();
			this.cardList.enable();
   		}
	},
   	
	selectDomain: function(params){		
		//to reload the card in the abort handler
		this.actualParams = params;
		this.classId = params.classId;
		this.cardSelected = [];
		//this call the fildManager and call the method to load the list of attribute
	 	var callback = this.loadDomainCardList.createDelegate(this, [params.classId, params.className, undefined/*idDomain*/], true);
		CMDBuild.Management.FieldManager.loadAttributes(params.classId,callback);
 	},

	loadDomainCardList: function(attributeList, classId, className, idDomain) {
		var params = {
			classId: classId,
			classAttributes: attributeList,
			idDomain: idDomain,
			className: className
		};
		//initialize the cardlist
		this.cardList.initForClass(params);
		this.attributesPanel.initForClass(params);
	},
	
	cardSelect: function(sm, row, rec){
		var card = rec.json.IdClass + "_" + rec.json.Id;
		this.cardSelected.push(card);
		if (this.saveBtn.disabled){
			this.saveBtn.enable();
		}
	},
	
	deselectCard: function(sm, row, rec){
		var card = rec.json.IdClass + "_" + rec.json.Id;
		var i = 0;
		while ( i < this.cardSelected.length ){
			if(this.cardSelected[i] == card){
				this.cardSelected.splice(i,1);
			} else {
				i++;
			}
		}
		this.disableSaveBtnIfSelectionIsEmpty();
	},
	
	onPageChange: function(p,e){
		var cards = this.cardSelected;
		if (cards.length > 0){
			var records = [];
			var recordsToSelect = [];
			//for all the store elements
			for(var i = 0; i < p.store.data.length; i++){
				var record = p.store.getAt(i);
				var id = record.json.Id;
				//for all the checkedRecords elements
				for(var j = 0 ; j < cards.length; j++){
					if(cards[j] == id){
						recordsToSelect.push(record);
					}
				}
			}
			this.cardList.getSelectionModel().suspendEvents(); 
			this.cardList.getSelectionModel().selectRecords(recordsToSelect);
			this.cardList.getSelectionModel().resumeEvents();
		}
	},
	
	saveCardsChanges: function(){
		var params = this.attributesPanel.getCheckedValues();
		params['IdClass'] = this.classId;
		params['selections'] = this.cardSelected;
		CMDBuild.log.info(params);
		
		CMDBuild.Ajax.request({
			url: 'services/json/management/modcard/updatebulkcards',
			params:params, 
			scope: this,
			success: function(response) {
				this.cardList.getStore().reload();
				this.clearAll();
			}
		});
		
	},
	
	abortCardsChanges: function() {
		this.clearAll();
	},
	
	clearAll: function() {
		this.cardList.clearFilter();
		this.cardList.getSelectionModel().clearSelections();
		this.cardSelected = [];
		this.clearForm();
	},
	
	clearForm: function() {
		this.attributesPanel.resetForm();
	},
	
	disableSaveBtnIfSelectionIsEmpty: function() {
		if (this.cardSelected.length < 1){
			this.saveBtn.disable();
		}
	}
});