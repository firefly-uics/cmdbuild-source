CMDBuild.Management.AddRelationWindow = Ext.extend(CMDBuild.Management.DomainCardListWindow, {
	translation: CMDBuild.Translation.management.modcard.add_relations_window,
	currentDomain: undefined,
	filterType: 'addRelationcardlistfilter',

	initComponent: function() {
		this.relations = undefined;

		this.store = new CMDBuild.Management.DomainStore({
			classId: this.classId,
			autoLoad: true
		});
	
		this.saveButton = new Ext.Button({
			text : CMDBuild.Translation.common.buttons.save,
			name: 'saveButton',
			formBind : true,
			handler : this.onSave,
			scope : this
		});
	
		this.cancelButton = new Ext.Button({
			text : CMDBuild.Translation.common.buttons.abort,
			name: 'cancelButton',
			handler : this.onCancel,
			scope : this
		});
	
		this.cardListFilter = new CMDBuild.Management.DomainCardListFilter({
			IdClass: this.idClass,
			filterType: this.filterType,
			ownerWindow: this
		});
		
		var addCardbutton = this._buildAddButton({
			eventName: "cmdbuild-new-relationcard",
			disabled: true
		});

		var domainComboBox = new Ext.form.ComboBox({
			id: 'comboDomain',
			fieldLabel: this.translation.domain,
			name: 'FullDescription',
			hiddenName: 'DomainId',
			store: this.store,
			valueField: 'DirectedDomain',
			displayField: 'FullDescription',
			triggerAction: 'all',
			allowBlank: false,
			forceSelection: true,
			mode: 'local',
			emptyText: this.translation.select_domain,
			width: 380
		});
	
		domainComboBox.on('select',  function(combo, rec, index) {
			var destClassId = rec.data.DestClassId;
			var destClassName = rec.data.DestClassName;
			var directedDomain = rec.data.DirectedDomain;

			var eventParams = {
				class2Id: destClassId,
				cass2Name: destClassName,
				idDomain: directedDomain
			};
			Ext.getCmp('filterdomainlistpanel').enable();
			this.selectDomain(eventParams);

			var destClass = CMDBuild.Cache.getClassById(destClassId);
			if (destClass) {
				addCardbutton.setClassId(destClass);
			} else {
				// probably it's a process
				addCardbutton.disable();
			}
		},this);

		this.cardList = new CMDBuild.Management.DomainCardList({
			ownerWindow: this,
			filterType: this.filterType,
			tbar: [domainComboBox, addCardbutton]
		});
	
		this.cardListFilter.on(CMDBuild.Management.Relations.FILTER_SUCCESS_EVENT_NAME, this.filtersuccess, this);
		this.cardList.getSelectionModel().on('rowselect', this.cardSelect, this);
		this.cardList.getSelectionModel().on('rowdeselect', this.cardDeselect, this);
		this.cardList.pagingBar.on('change', this.onPageChange, this);
	
		this.tabPanel = new Ext.TabPanel({
			activeTab: 0,
			id: 'filterDomainTabPanel',
			region: 'center',
			items: [{
				xtype: 'panel',
				layout: 'fit',
				title: CMDBuild.Translation.management.findfilter.list,
				items: [this.cardList]				
			},{
				xtype: 'panel',
				id: 'filterdomainlistpanel',
				layout: 'fit',
				disabled: true,
				title: CMDBuild.Translation.management.findfilter.filter,
				items: [this.cardListFilter]
			}]
		});
	
		this.tabPanel.on('tabchange', function(tabPanel, tab) {
			tab.doLayout();
		});

		Ext.apply(this, {
			title: this.translation.window_title,
			items: {
				xtype: 'panel',
				frame: true,
				layout: 'border',
				labelAlign: 'right',
				monitorValid: true,
				items: [
					this.tabPanel
				],
				buttonAlign: 'center',
				buttons: [this.saveButton, this.cancelButton]
			}
		});
	
		if (this.domainId) {
			if (typeof this.domainId == "string") {
				this.domainId = this.domainId.replace('_D','').replace('_I','');
			}
			var domId = this.domainId;
			this.store.on('load', function() {
				var record;
				var index = -1;
				if (this.store.getCount() > 0) {
					this.store.each(function(r,idx) {
						if (r.data['DomainId'] == domId) {
							record = r;
							index = idx;
						}
					});
				}
				domainComboBox.onSelect(record,index);
			},this);
			domainComboBox.disable();
		}
		CMDBuild.Management.AddRelationWindow.superclass.initComponent.apply(this);
	},
	
	onSave: function() {
		var relations = {};
		relations[this.currentDomain] = this.selections;
		CMDBuild.Ajax.request({
			url : 'services/json/management/modcard/createrelations',
			params : {
				"IdClass": this.classId,
				"Id": this.cardId,
				"Relations": Ext.util.JSON.encode(relations)
			},
			waitTitle : CMDBuild.Translation.common.wait_title,
			waitMsg : CMDBuild.Translation.common.wait_msg,
			method : 'POST',
			scope : this,
			success : function() {
				this.close();
				this.publish('cmdb-reload-card', {cardId: this.cardId});
			}
		});
	},
	
	cardSelect: function(sm, row, rec) {
		var card = rec.json.IdClass + "_" + rec.json.Id;
		this.selections.push(card);
	},
	
	cardDeselect: function(sm, row, rec) {
		var card = rec.json.IdClass + "_" + rec.json.Id;
		var i = 0;
		while ( i < this.selections.length ) {
			if (this.selections[i] == card) {
				this.selections.splice(i,1);
			} else {
				i++;
			}
		} 
	},
	
	onPageChange: function(p,e) {
		var cards = this.selections;	
		if (cards && cards.length > 0) {
			var records = [];
			var recordsToSelect = [];
			//for all the store elements
			for (var i = 0; i < p.store.data.length; i++) {
				var record = p.store.getAt(i);
				var id = record.json.IdClass + "_" + record.json.Id;
				//for all the checkedRecords elements
				for (var j = 0 ; j < cards.length; j++) {
					if (cards[j] == id) {
						recordsToSelect.push(record);
					}
				}
			}
			this.cardList.getSelectionModel().suspendEvents(); 
			this.cardList.getSelectionModel().selectRecords(recordsToSelect);
			this.cardList.getSelectionModel().resumeEvents();
		}
	},
	
	selectDomain: function(params) {
		this.currentDomain = params.idDomain;
		//this call the fildManager and publish the event with the list of attribute
		var callback = this.loadDomainCardList.createDelegate(this, [params.class2Id, params.cass2Name, params.idDomain], true);
		CMDBuild.Management.FieldManager.loadAttributes(params.class2Id,callback);
		//reset the selections 
		this.selections = [];
	},
	
	loadDomainCardList: function(attributeList, classId, className, idDomain) {
		//initialize the cardlist
		this.cardList.initForClass({
			classId: classId,
			classAttributes: attributeList,
			idDomain: idDomain,
			className: className
		});
		//initialize the cardlist filter
		this.cardListFilter.selectClass({
			classId: classId,
			classAttributes: attributeList
		});
	},
	
	filtersuccess: function() {
		this.tabPanel.setActiveTab(0);
		this.cardList.reload();
	}
});

Ext.reg('addrelationwindow', CMDBuild.Management.AddRelationWindow);