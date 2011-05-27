// Se attivo al load-card o quando diventa attivo e l'idcard è diverso, carica la history
// All'init-class azzera l'idCard e prepara il template per il RowExpander
CMDBuild.Management.CardHistoryTab = Ext.extend(Ext.grid.GridPanel, {
	translation : CMDBuild.Translation.management.modcard,
    eventtype: 'card',
    eventmastertype: 'class',

	initComponent: function() {
	    var col_tr = CMDBuild.Translation.management.modcard.history_columns;

		function tickRenderer(value) {
			if (value) {
				return '<img style="cursor:pointer" src="images/icons/tick.png"/>&nbsp;';
			} else {
				return '&nbsp;'
			}
		}

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

	    var headers = [
	            this.expander,
	            {header: col_tr.begin_date,  width: 180, fixed: true, sortable: true, dataIndex: 'BeginDate', renderer: Ext.util.Format.dateRenderer('d/m/y H:i:s')},
	            {header: col_tr.end_date,  width: 180, fixed: true, sortable: true, dataIndex: 'EndDate', renderer: Ext.util.Format.dateRenderer('d/m/y H:i:s')},
	            {header: col_tr.user, width: 20, sortable: true, dataIndex: 'User'},
	            {header: col_tr.attributes, width: 60, fixed: true, sortable: true, renderer: tickRenderer, dataIndex: '_AttrHist', align: 'center', cellCls: 'grid-button'},
	            {header: col_tr.relation, width: 60, fixed: true, sortable: true, renderer: tickRenderer, dataIndex: '_RelHist', align: 'center', cellCls: 'grid-button'},
	            {header: col_tr.domain, width: 20, sortable: true, dataIndex: 'DomainDesc'},
	            {header: col_tr.description, width: 40, sortable: true, dataIndex: 'CardDescription'}
	        ];
		if (this.eventmastertype == 'processclass')
			headers.push({header: col_tr.activity_name, width: 40, sortable: true, dataIndex: 'Code'});
		Ext.apply(this, {
			loadMask: true,
			plugins: this.expander,
	        collapsible: false,
	        border: false,
			store: new Ext.data.JsonStore({
				url: 'services/json/management/modcard/getcardhistory',
		        root: "rows",
		        fields: [{name:'BeginDate', type:'date', dateFormat:'d/m/y H:i:s'}, {name:'EndDate', type:'date', dateFormat:'d/m/y H:i:s'}, 'User', '_AttrHist', '_RelHist', 'DomainDesc', 'CardDescription', 'Code'],
		        sortInfo:  {field: "BeginDate", direction: "DESC"},
		        baseParams: { IsProcess: (this.eventmastertype == 'processclass')}
			}),
	        cm: new Ext.grid.ColumnModel(headers),
	        viewConfig: {
	            forceFit:true
	        },	        
	        tbar: [
				new CMDBuild.Management.GraphActionHandler().getAction()
			]
		});
		
		CMDBuild.Management.CardHistoryTab.superclass.initComponent.apply(this, arguments);

		this.subscribe('cmdb-init-' + this.eventmastertype, this.initForClass, this);
		this.subscribe('cmdb-new-' + this.eventtype, this.newCard, this);
		this.subscribe('cmdb-load-' + this.eventtype, this.loadCard, this);
		this.subscribe('cmdb-reload-' + this.eventtype, this.reloadCard, this);

		this.on('activate', this.loadCardHistory, this)
	},
	
	initForClass: function(eventParams) {
		this.disable();
		if (eventParams) {
			this.currentClassPrivileges = Ext.apply({
					create: false,
					write: false
				}, eventParams.privileges);
			if (this.currentClassId != eventParams.classId) {
				this.currentClassId = eventParams.classId;
				this.expander.currentAttributes = eventParams.classAttributes;
			}
		}
	},
	
	newCard: function(eventParams) {
		this.disable();
	},

	loadCard: function(eventParams) {
		var idClass = eventParams.record.data.IdClass;
		if (CMDBuild.Utils.isSimpleTable(idClass)) {
			this.disable();
			return;
		}
		
		this.currentCardId = eventParams.record.data.Id;
		this.currentClassId = eventParams.record.data.IdClass;
		this.currentCardPrivileges = {
			create: eventParams.record.priv_create,
			write: eventParams.record.priv_write
		};
		this.reloadCard();
	},

	reloadCard: function(eventParams) {
		this.enable();
		this.loaded = false;
		if (this.ownerCt.getActiveTab() === this)
			this.loadCardHistory();
	},

	loadCardHistory: function() {
		if (this.loaded)
			return;
		this.getStore().load({
			params : {
				IdClass: this.currentClassId,
				Id: this.currentCardId
			}
		});
		this.loaded = true;
	}
});
Ext.reg('cardhistoytab', CMDBuild.Management.CardHistoryTab);
