(function() {

	var tr = CMDBuild.Translation.management.modcard;
	var col_tr = CMDBuild.Translation.management.modcard.history_columns;
	    
	Ext.define("CMDBuild.view.management.classes.CMCardHistoryTab", {
		extend: "Ext.grid.Panel",

	    eventtype: 'card',
    	eventmastertype: 'class',

		constructor: function() {
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
					rowBodyTpl : ["@@ TODO"]
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
		},
	
		onClassSelected: function(classId) {
//				this.currentClassPrivileges = Ext.apply({
//						create: false,
//						write: false
//					}, eventParams.privileges);
	
			if (this.currentClassId != classId) {
				this.currentClassId = classId;
//				this.expander.currentAttributes = eventParams.classAttributes;
			}

		},

		onCardSelected: function(card) {
/* TODO 3 to 4
			if (CMDBuild.Utils.isSimpleTable(idClass)) {
				this.disable();
				return;
			}
*/			
			this.currentCardId = card.raw.Id;
			this.currentClassId = card.raw.IdClass;

			this.currentCardPrivileges = {
				create: card.raw.priv_create,
				write: card.raw.priv_write
			};

			this.reloadCard();
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

	function tickRenderer(value) {
		if (value) {
			return '<img style="cursor:pointer" src="images/icons/tick.png"/>&nbsp;';
		} else {
			return '&nbsp;'
		}
	}

})();