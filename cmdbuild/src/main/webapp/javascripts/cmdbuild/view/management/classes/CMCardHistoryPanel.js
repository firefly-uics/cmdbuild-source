(function() {

	var tr = CMDBuild.Translation.management.modcard;
	var col_tr = CMDBuild.Translation.management.modcard.history_columns;
	    
	Ext.define("CMDBuild.view.management.classes.CMCardHistoryTab", {
		extend: "Ext.grid.Panel",

		eventtype: 'card',
		eventmastertype: 'class',

		constructor: function() {
			this.currentTemplate = null;
			this.autoScroll = true;
			/*
			this.expander = new CMDBuild.XRowExpander({
				genBodyContent : function(record, index){
					var body = '';
					if (record.json['_RelHist']) {
						body += '<p><b>'+col_tr.domain+'</b>: '+record.json['DomainDesc']+'</p>'
						body += '<p><b>'+col_tr.destclass+'</b>: '+record.json['Class']+'</p>'
						body += '<p><b>'+col_tr.code+'</b>: '+record.json['CardCode']+'</p>'
						body += '<p><b>'+col_tr.description+'</b>: '+record.json['CardDescription']+'</p>'
					} else {
						for (var i=0; i<this.currentAttributes.length; i++) {
							var attribute = this.currentAttributes[i]
							var displayField = CMDBuild.Management.FieldManager.getDisplayNameForAttr(attribute);
							var displayValue = record.json[displayField];
							if (!displayValue)
								displayValue = "";
							body += '<p><b>'+attribute.description+'</b>: '+displayValue+'</p>'
						}
					}
					return body
				}
			});
			*/

			Ext.apply(this, {
				plugins: [{
					ptype: 'rowexpander',
					rowBodyTpl: " ",
					getRowBodyFeatureData: function(data, idx, record, orig) {
						if (this.view.currentTemplate) {
							var o = this.callParent(arguments);

							o.rowBody = this.view.currentTemplate.applyTemplate(data);
							o.rowCls = this.rowCollapsedCls;
							o.rowBodyCls = this.rowBodyHiddenCls;
	
							return o;
						}
					}
				}],
				columns: [
					{header: col_tr.begin_date,  width: 180, fixed: true, sortable: true, dataIndex: 'BeginDate', renderer: Ext.util.Format.dateRenderer('d/m/y H:i:s'), flex:1},
					{header: col_tr.end_date,  width: 180, fixed: true, sortable: true, dataIndex: 'EndDate', renderer: Ext.util.Format.dateRenderer('d/m/y H:i:s'), flex:1},
					{header: col_tr.user, width: 20, sortable: true, dataIndex: 'User', flex:1},
					{header: col_tr.attributes, width: 60, fixed: true, sortable: true, renderer: tickRenderer, dataIndex: '_AttrHist', align: 'center', cellCls: 'grid-button', flex:1},
					{header: col_tr.relation, width: 60, fixed: true, sortable: true, renderer: tickRenderer, dataIndex: '_RelHist', align: 'center', cellCls: 'grid-button', flex:1},
					{header: col_tr.domain, width: 20, sortable: true, dataIndex: 'DomainDesc', flex:1},
					{header: col_tr.description, width: 40, sortable: true, dataIndex: 'CardDescription', flex:1}
				],
				store: new Ext.data.JsonStore({
					proxy : {
						type : 'ajax',
						url: 'services/json/management/modcard/getcardhistory',
						reader : {
							type : 'json',
							root : 'rows'
						}
					},
					sorters : [ {
						property : 'BeginDate',
						direction : "DESC"
					}],			
					fields: [{name:'BeginDate', type:'date', dateFormat:'d/m/y H:i:s'}, {name:'EndDate', type:'date', dateFormat:'d/m/y H:i:s'}, 'User', '_AttrHist', '_RelHist', 'DomainDesc', 'CardDescription', 'Code'],
					baseParams: { IsProcess: (this.eventmastertype == 'processclass')}
				})
			});

			this.callParent(arguments);
			this.view.on("expandbody", function() {
				this.doLayout() // to refresh the scrollbar status
			}, this);
		},
	
		onClassSelected: function(classId) {
			if (this.currentClassId != classId) {
				this.currentClassId = classId;
			}

		},

		onCardSelected: function(card) {
			var et = _CMCache.getEntryTypeById(card.get("IdClass"));
			if (et && et.get("tableType") == CMDBuild.Constants.cachedTableType.simpletable) {
				this.disable();
			} else {
				this.currentCardId = card.raw.Id;
				this.currentClassId = card.raw.IdClass;

				_CMCache.getAttributeList(this.currentClassId, 
						Ext.Function.bind(function buildTemplate(attributes) {
							var body = '';
							
							if (card.raw['_RelHist']) {
								body += '<p class="historyItem"><b>'+col_tr.domain+'</b>: '+ card.raw['DomainDesc']+'</p>'
								body += '<p class="historyItem"><b>'+col_tr.destclass+'</b>: '+ card.raw['Class']+'</p>'
								body += '<p class="historyItem"><b>'+col_tr.code+'</b>: ' + card.raw['CardCode']+'</p>'
								body += '<p class="historyItem"><b>'+col_tr.description+'</b>: ' + card.raw['CardDescription']+'</p>'
							} else {
								for (var i=0; i<attributes.length; i++) {
									var attribute = attributes[i],
										displayField = CMDBuild.Management.FieldManager.getDisplayNameForAttr(attribute),
										displayValue = card.raw[displayField];
	
									displayValue = displayValue || "";
									body += '<p class="historyItem"><b>' + attribute.description + ': </b>' + displayValue +'</p>';
								}
							}
							this.view.currentTemplate = Ext.create('Ext.XTemplate', body);
						}, this)
				);

				this.currentCardPrivileges = {
					create: card.raw.priv_create,
					write: card.raw.priv_write
				};

				if (tabIsActive(this)) {
					this.reloadCard();
				} else {
					this.on("activate", this.reloadCard, this);
				}
			}
		},

		reloadCard: function() {
			this.enable();
			this.loaded = false;
			this.loadCardHistory();
		},
	
		loadCardHistory: function() {
			if (this.loaded) {
				return;
			}

			this.getStore().load({
				params : {
					IdClass: this.currentClassId,
					Id: this.currentCardId
				}
			});

			this.loaded = true;
		}
	});

	function tabIsActive(t) {
		return t.ownerCt.layout.getActiveItem().id == t.id;
	}

	function tickRenderer(value) {
		if (value) {
			return '<img style="cursor:pointer" src="images/icons/tick.png"/>&nbsp;';
		} else {
			return '&nbsp;'
		}
	}

})();