(function() {

	var col_tr = CMDBuild.Translation.management.modcard.history_columns;

	function genHistoryBody(record) {
		var body = '';
		if (record.raw['_RelHist']) {
			body += historyAttribute(col_tr.domain, record.raw['DomainDesc'])
				+ historyAttribute(col_tr.destclass, record.raw['Class'])
				+ historyAttribute(col_tr.code, record.raw['CardCode'])
				+ historyAttribute(col_tr.description, record.raw['CardDescription']);
		}
		for (var a = record.raw['Attr'], i=0, l=a.length; i<l ;++i) {
			var ai = a[i];
			body += historyAttribute(ai.d, ai.v || "", ai.c);
		}
		return body;
	}

	function historyAttribute(label, value, changed) {
		var cls = changed ? " class=\"changed\"" : "";
		return "<p"+cls+"><b>"+label+"</b>: "+(value.dsc || value)+"</p>";
	};

	Ext.define("CMDBuild.view.management.classes.CMCardHistoryTab", {
		extend: "Ext.grid.Panel",

		eventtype: 'card',
		eventmastertype: 'class',

		cls: "history_panel",

		constructor: function() {
			this.currentTemplate = null;
			this.autoScroll = true;

			Ext.apply(this, {
				plugins: [{
					ptype: "rowexpander",
					rowBodyTpl: "ROW EXPANDER REQUIRES THIS TO BE DEFINED",
					getRowBodyFeatureData: function(data, idx, record, orig) {
						var o = Ext.ux.RowExpander.prototype.getRowBodyFeatureData.apply(this, arguments);
						o.rowBody = genHistoryBody(record);
						return o;
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
					fields: [{name:'BeginDate', type:'date', dateFormat:'d/m/y H:i:s'}, {name:'EndDate', type:'date', dateFormat:'d/m/y H:i:s'}, 'User', '_AttrHist', '_RelHist', 'DomainDesc', 'Class', 'CardCode', 'CardDescription', 'Code'],
					baseParams: { IsProcess: (this.eventmastertype == 'processclass')}
				})
			});

			this.callParent(arguments);
			this.view.on("expandbody", function() {
				this.doLayout(); // to refresh the scrollbar status
			}, this);
		},
	
		onClassSelected: function(classId) {
			if (this.currentClassId != classId) {
				this.currentClassId = classId;
				this.disable();
			}

		},

		onCardSelected: function(card) {
			var et = _CMCache.getEntryTypeById(card.get("IdClass"));
			if (et && et.get("tableType") == CMDBuild.Constants.cachedTableType.simpletable) {
				this.disable();
			} else {
				this.enable();
				this.currentCardId = card.raw.Id;
				this.currentClassId = card.raw.IdClass;

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
		},

		onAddCardButtonClick: function() {
			this.disable();
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
