/**
 * This is the Relations Panel that contains the part of the filter that allow you
 * to make a filter for the relation between class
 * 
 * @class CMDBuild.Management.Relations
 * @extends Ext.Panel
 */
CMDBuild.Management.Relations = Ext.extend(Ext.Panel, {
	title: CMDBuild.Translation.management.findfilter.relations,
	layout: 'fit',
	collapsed: true,
	
	//custom attributes
	filterType: 'domaincardlistfilter',	
	idClass: undefined,
	currentDomain: -1,
	
	initComponent:function() {
		this.cardStates = {},
		this.domaincardlistfilter = new CMDBuild.Management.DomainCardListFilter({
			filterType: this.filterType,
			ownerWindow: this
		});
		
		this.domainGrid = new CMDBuild.Management.FilterRelationsDomainGrid({
			idClass: this.idClass
		});
		
		this.domaincardlist = new CMDBuild.Management.DomainCardList({
			filterType: this.filterType,
			ownerWindow: this
		});
		
		this.tabPanel = new Ext.TabPanel({
			activeTab: 0,
			id: 'filterDomainTabPanel',
			items: [{
				xtype: 'panel',
				layout: 'fit',
				height: 150,
				title: CMDBuild.Translation.management.findfilter.list,				
				items: [this.domaincardlist]
			},{
				xtype: 'panel',
				layout: 'fit',
				title: CMDBuild.Translation.management.findfilter.filter,
				items: [this.domaincardlistfilter]
			}]
		});
		
		this.tabPanel.on('tabchange', function(tabPanel, tab){
			tab.doLayout();
		});
		
		Ext.apply(this, {
			layout: 'border',
			items: [{
				region: 'center',
				xtype: 'panel',
				layout: 'fit',
				split: true,
				style: {border: '1px #8DB2E3 solid'},
				items: [this.domainGrid]
			},{
				region: 'south',
				heihgt: '50%',
				layout: 'fit',
				split: true,
				items: [this.tabPanel]
			}],
			frame: true
		});
    	CMDBuild.Management.Relations.superclass.initComponent.call(this);
    	//initialize the cardStates		
    	this.domainGrid.getStore().on('load', this.initCardStates, this);
    	
		this.domainGrid.on('cmdb-domainselected', this.selectDomain, this);
    	this.domainGrid.on('cmdb-notRelChecked', this.notRelChecked, this);
    	this.domainGrid.on('cmdb-allChecked', this.allChecked, this);
    	
    	this.domainGrid.destination_combo.on('select', this.sectDestinationCombo, this);
    	this.domainGrid.on('render', function(comp){
    		var view = comp.getView();
            view.mainBody.on('mousedown', this.clickOnCombo, this);
    	}, this);
    	this.domaincardlist.getSelectionModel().on('rowselect', this.cardSelect, this);
    	this.domaincardlist.getSelectionModel().on('rowdeselect', this.cardDeselect, this);
    	this.domaincardlist.pagingBar.on('change', this.onPageChange, this);
    	this.domaincardlistfilter.on(CMDBuild.Management.Relations.FILTER_SUCCESS_EVENT_NAME, this.filtersuccess, this)
     },
    
	 initCardStates: function(store, records) {
	 	for (var i = 0 ; i < records.length ; i++) {
	 		var state = new DomainCardState(records[i].data.DestClassId);
	 		var direction = (records[i].data.Direct) ? "_D" : "_I";
	 		var domain = records[i].data.DomainId + direction;
	 		this.addCardState(domain, state);
	 		
	 		//set foreach record the original destination to reload the subclass in the destination combo
	 		records[i]['originalDestinationId'] = records[i].data.DestClassId
	 		records[i]['originalDestinationName'] = records[i].data.DestClassName
	 	}
	 	this.domainGrid.getSelectionModel().selectFirstRow();
	 },
     
	getCardStates: function() {
		return this.cardStates;
	},
	
	addCardState: function(domain, cardState) {
		this.cardStates[domain] = (cardState);
	},
	
	getCardState: function(domain) {
		return this.cardStates[domain];
	},
	
	
     /*
      * the listener of the selectDomain event
      * 	
      * 	oldIdDomain: oldIdDomain,
			idDomain: idDomain,
			cass2Name: rec.data.DestClassName,
			class2Id: rec.data.DestClassId
      * 
      */
     
     selectDomain: function(params) {
	 	this.currentDomain = params.idDomain;
	 	//this call the fildManager and publish the event with the list of attribute
	 	var callback = this.initTabPanel.createDelegate(this, [params.class2Id, params.cass2Name, params.idDomain], true);
		CMDBuild.Management.FieldManager.loadAttributes(params.class2Id,callback);
     },

     initTabPanel: function(attributeList, classId, className, idDomain) {
		this.initCardList(attributeList, classId, className, idDomain);
    	this.initCardListFilter(attributeList, classId, className, idDomain);
    	var type = this.getCardState(this.currentDomain).getType();
		this.tabPanel.setDisabled(type == "notRel" || type == "all");
	 },
	 
	 //private
	 initCardList: function(attributeList, classId, className, idDomain) {
		 this.domaincardlist.initForClass({
			 classId: classId,
			 classAttributes: attributeList,
			 idDomain: idDomain,
			 className: className
		 });
	 },
	 
	 //private
	 initCardListFilter: function(attributeList, classId, className, idDomain) {
		 this.domaincardlistfilter.selectClass({
			 classId: classId,
			 classAttributes: attributeList,
			 idDomain: idDomain
		 });		 
	 },
	 
	 /*
	  *params
	  *
	  * checked: boolean
	  * domain: domainid_direction
	  * 
	 */
	 notRelChecked: function(params){
 	 	if (params.checked){
	 		this.getCardState(params.domain).setType('notRel');
	 	} else {
	 		this.getCardState(params.domain).setType('cards');
	 	}
	 	if(params.domain == this.currentDomain) {
	 		this.tabPanel.setDisabled(params.checked);
	 	}
	 	params.record.set('all', false);
	 },
	 
	 allChecked: function(params){
	 	if (params.checked){
	 		this.getCardState(params.domain).setType('all');
	 	} else {
	 		this.getCardState(params.domain).setType('cards');
	 	}
	 	if(params.domain == this.currentDomain) {
	 		this.tabPanel.setDisabled(params.checked);
	 	}
	 	params.record.set('notInRelation', false);
	 },
	 
	cardSelect: function(sm, row, rec){
		var card = rec.json.IdClass + "_" + rec.json.Id;
		this.getCardState(this.currentDomain).addCard(card);
		if (this.getCardState(this.currentDomain).getCards().length == 1){
			this.domainGrid.updateDefined(true)
		};
	},
	
	cardDeselect: function(sm, row, rec){
		var card = rec.json.IdClass + "_" + rec.json.Id;
		this.getCardState(this.currentDomain).removeCard(card);
		if (!this.getCardState(this.currentDomain).hasCards()){
			this.domainGrid.updateDefined(false)
		};
	},
	
	onPageChange: function(p,e){
		var cardState = this.getCardState(this.currentDomain);
		if (cardState) {
			var cards = cardState.getCards();
			if (cards.length > 0){
				var records = [];
				var recordsToSelect = [];
				//for all the store elements
				for(var i = 0; i < p.store.data.length; i++){
					var record = p.store.getAt(i);
					var id = record.json.IdClass + "_" + record.json.Id;
					//for all the checkedRecords elements
					for(var j = 0 ; j < cards.length; j++){
						if(cards[j] == id){
							recordsToSelect.push(record);
						}
					}
				}
				this.domaincardlist.getSelectionModel().suspendEvents(); 
				this.domaincardlist.getSelectionModel().selectRecords(recordsToSelect);
				this.domaincardlist.getSelectionModel().resumeEvents();
			}
		}
	},
	
	
	sectDestinationCombo: function(combo, record, index){
		//when change the destination combo all the information of the cardStete are deleted 
		//then change the class2id and class2name
		var gridRecord = this.domainGrid.comboRecord;
    	var direction = gridRecord.data.Direct ? "_D":"_I";
    	var domain = gridRecord.data.DomainId + direction;
    	this.getCardState(domain).clearCardState(record.data.classId);
    	this.domainGrid.updateDefined(false);
    	gridRecord.set(this.domainGrid.checkNotRel.dataIndex, false);
    	gridRecord.set(this.domainGrid.checkAll.dataIndex, false);
    	combo.eventParams['class2Id'] = record.data.classId;
    	combo.eventParams['class2Name'] = record.data.className;
		gridRecord.data.DestClassId = record.data.classId;
    	this.getCardState(domain)
        var seleMod = this.domainGrid.getSelectionModel();
    	seleMod.selectRecords([gridRecord]);
    },
	
	getCardStatesToSend: function() {
		var cardStatesToSend = {};
		for (var domain in this.cardStates){
			var state = this.getCardState(domain);
			if (state.getType() != "cards"){
				var tmpState = {
						type: state.getType(),
						destinationClass: state.getDestinationClass()
				}
				cardStatesToSend[domain] = (tmpState)
			} else {
				if (state.getCards().length > 0){
					cardStatesToSend[domain] = (state.getCardState());
				}
			}
		}
		return cardStatesToSend;
	},
    
    clickOnCombo: function(e,t){
		if(t.className == 'x-grid3-cell-inner x-grid3-col-2'){
            e.stopEvent();
			var index = this.domainGrid.getView().findRowIndex(t);
            var record = this.domainGrid.store.getAt(index);
            this.domainGrid.storeForDestination.baseParams['ClassId'] = record.originalDestinationId;
    		this.domainGrid.storeForDestination.load();
    		this.domainGrid.comboRecord = record;
    		/*
    		if the store of the combo has only one item, select directly the gridRow
    		var records = this.domainGrid.storeForDestination.getRange();
    		if (records.length == 1){
    			this.domainGrid.getSelectionModel().selectRecords([record]);
    		}
    		*/
		}
	},
	
	filtersuccess: function() {
		this.tabPanel.setActiveTab(0);
		this.domaincardlist.reload();
	}
});

DomainCardState = function(destination){
	this.cardState = {
		type: "cards",
		cards:[],
		destinationClass: destination
	},
	
	this.setCardState = function(state){
		this.cardState = state;
	},
	
	this.clearCardState = function(destination){
		this.cardState = {
				type: "cards",
				cards:[],
				destinationClass: destination
			}
	},
	
	this.setType = function(type){
		this.cardState["type"] = type
	},
	
	this.setCards = function(cards){
		this.cardState["cards"] = cards
	},
	
	this.addCard = function(card){
		this.cardState["cards"].push(card);		
	},
	
	this.removeCard = function(card){
		var i = 0;
		var cards = this.cardState["cards"];
		while ( i < cards.length ){
			if(cards[i] == card){
				cards.splice(i,1);
			} else {
				i++
			}
		} 
	},
	
	this.getCardState = function() {
		return this.cardState;
	},
	
	this.getType = function() {
		return this.cardState["type"];
	},
	
	this.getCards = function() {
		return this.cardState["cards"];
	},
	
	this.getDestinationClass = function() {
		return this.cardState["destinationClass"];
	},
	
	this.hasCards = function() {
		return (this.cardState.cards.length > 0);
	}
};

Ext.reg('cardfilterrelations', CMDBuild.Management.Relations);

CMDBuild.Management.Relations.FILTER_SUCCESS_EVENT_NAME = "cmdbuild-domaincardlistfiltersuccess";
