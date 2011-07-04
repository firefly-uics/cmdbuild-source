(function() {
	var TARGET_CLASS_ID = "ClassId";
	var tr = CMDBuild.Translation.management.modcard;
	var col_tr = CMDBuild.Translation.management.modcard.relation_columns;

	Ext.define("CMRelationPanelModel", {
		extend: "Ext.data.Model",
		fields: [
			'dom_id', 'dom_desc', 'label',
			'dst_code', 'dst_id', 'dst_desc', 'dst_cid',
			'rel_attr', 'rel_date', 'rel_id',
			'relations_size', 'src' // used only for the folder nodes
		]
	});

	Ext.define("CMDBuild.view.management.classes.CMCardRelationsPanel", {
		extend: "Ext.tree.Panel",
		initComponent: function() {

			this.addRelationButton = new Ext.button.Button({
				iconCls : 'add',
				text : tr.add_relations
			});

			var me = this;

			Ext.apply(this, {
				loadMask: false,
				hideMode: "offsets",
				store: new Ext.data.TreeStore({
					model: "CMRelationPanelModel",
					root : {
						expanded : true,
						children : []
					},
					autoLoad: false
				}),
				rootVisible: false,
				columns: [
					{header: col_tr.domain, sortable: false, dataIndex: 'dom_id', hidden: true},
					{header: col_tr.destclass, flex: 2, sortable: false, dataIndex: 'label', xtype: 'treecolumn'},
					{header: col_tr.begin_date, flex: 1, sortable: false, dataIndex: 'rel_date'},
					{header: col_tr.code, flex: 1, sortable: false, dataIndex: 'dst_code'},
					{header: col_tr.description, flex: 2, sortable: false, dataIndex: 'dst_desc'},
					{header: CMDBuild.Translation.administration.modClass.tabs.attributes, flex: 4, sortable: false, dataIndex: 'rel_attr'},
					{
						header: '&nbsp', 
						width: 90,
						fixed: true, 
						sortable: false, 
						renderer: renderRelationActions, 
						align: 'center', 
						cellCls: 'grid-button', 
						dataIndex: 'Fake',
						menuDisabled: true,
						hideable: false
					}
				],
				tbar: [this.addRelationButton]
			});

			this.callParent(arguments);
		},

		clearStore: function() {
			this.store.getRootNode().removeAll();
		},

		fillWithData: function(domains) {
			domains = domains || [];
			var nodes = [],
				r = this.store.getRootNode();

			for (var i=0, l=domains.length; i<l; ++i) {
				var domainRensonseObj = domains[i],
					domainCachedData = _CMCache.getDomainById(domainRensonseObj.id);

				nodes.push( buildNodeFor(domainRensonseObj, domainCachedData));
			}

			r.removeAll();
			r.appendChild(nodes);
		},

		convertRelationInNodes: convertRelationInNodes
	});
	
	function buildNodeFor(domainRensonseObj, domainCachedData) {
		var children = [],
			attributes = domainCachedData.data.attributes,
			attributesToString = "",
			oversize = domainRensonseObj.relations_size > CMDBuild.Config.cmdbuild.relationlimit,
			node = {
				dom_id: domainCachedData.get("id"),
				label: buildDescriptionForDomainNode(domainRensonseObj, domainCachedData),

				src: domainRensonseObj.src,
				relations_size: domainRensonseObj.relations_size,

				expanded: !oversize,
				leaf: false,
				children: []
			};

			if (oversize) {
				// it is asynchronous, add an empty obj to get the possibility to expand the tree widget
				node.children.push({});
			} else {
				node.children = convertRelationInNodes(domainRensonseObj.relations);
			}

		if (attributes.length > 0) {
			for (var i=0, l=attributes.length; i<l; i++) {
				attributesToString += i==0 ? "" : " - ";
				attributesToString += attributes[i].description;
			}

			node.rel_attr = attributesToString;
		}
		return node;
	}

	function convertRelationInNodes(relations) {
		relations = relations || [];
		var r,c,i=0,
			l=relations.length,
			nodes = [];

		for (; i<l; ++i) {
			r = relations[i];
			c = _CMCache.getEntryTypeById(r.dst_cid);

			r.leaf = true;
			r.label = c.get("text");

			nodes.push(r);
		}

		return nodes;
	}

	function renderRelationActions(value, metadata, record) {
		if (record.get("depth") == 1) {
			return "";
		}
		var tr = CMDBuild.Translation.management.modcard,
			actionsHtml = '<img style="cursor:pointer" title="'+tr.open_relation+'" class="action-relation-go" src="images/icons/bullet_go.png"/>&nbsp;',
			tableId = record.get(TARGET_CLASS_ID),
			table = _CMCache.getClassById(tableId);

		if (!this.readOnly) {
			actionsHtml += '<img style="cursor:pointer" title="'+tr.edit_relation+'" class="action-relation-edit" src="images/icons/link_edit.png"/>&nbsp;'
			+ '<img style="cursor:pointer" title="'+tr.delete_relation+'" class="action-relation-delete" src="images/icons/link_delete.png"/>&nbsp;';
		}

		if (table && table.priv_write) {
			actionsHtml += '<img style="cursor:pointer" class="action-relation-editcard" src="images/icons/modify.png"/>';
		} else {
			actionsHtml += '<img style="cursor:pointer" title="'+tr.view_relation+'" class="action-relation-viewcard" src="images/icons/zoom.png"/>';
		}

		return actionsHtml;
	}
	
	function buildDescriptionForDomainNode(domainRensonseObj, domainCachedData) {
		var prefix = domainCachedData.get("descr"+domainRensonseObj.src),
			s = domainRensonseObj.relations_size,
			postfix = s  > 1 ? CMDBuild.Translation.management.modcard.relation_columns.items : CMDBuild.Translation.management.modcard.relation_columns.item;
		
		return prefix + " ("+ s + " " + postfix + ")" ;
	}
	/*

Ext.define("CMDBuild.view.management.classes.CMCardRelationsPanel", {
	extend: "Ext.grid.Panel",
	alias: "cardrelationstab",

	translation : CMDBuild.Translation.management.modcard,

	subscribeToEvents : true,
    eventtype: 'card',
    eventmastertype: 'class',
    readOnly: false,
    modifyCard: Ext.emptyFn,
    deleteCard: Ext.emptyFn,
    updateEventName: 'cmdb-reload-card',
	initComponent: function() {
	    var col_tr = CMDBuild.Translation.management.modcard.relation_columns;

    	this.addRelationsAction = new Ext.Action({
      		iconCls : 'add',
      		text : this.translation.add_relations,
			handler : this.addRelations,
      		scope: this
    	});

		function cellclickHandler(grid, rowIndex, colIndex, event) {
			var className = event.target.className; 
			var jsonRow = grid.getStore().getAt(rowIndex).json;
			var functionArray = {
				'action-relation-go': CMDBuild.Management.openCard,
				'action-relation-edit': this.editRelation,
				'action-relation-delete': this.deleteRelation,
				'action-relation-editcard': this.editCard,
				'action-relation-viewcard': this.viewCard,
				//TODO: refactor this, as the next 2 things are useful only for the workflow (or port modify/card also here)
				'action-card-modify': this.modifyCard,
                'action-card-delete': this.deleteCard
			};
			if (functionArray[className]) {
				functionArray[className].createDelegate(grid)(jsonRow);
			}
		}

		function doubleclickHandler(grid, rowIndex, event) {
			var jsonRow = grid.getStore().getAt(rowIndex).json;
			CMDBuild.Management.openCard.createDelegate(grid)(jsonRow, CMDBuild.Constants.tabNames.relations);
		}

    	var reader = new Ext.data.JsonReader({
			root: "rows",
			fields: ['Domain','DomainCount','DomainDesc','DomainId','Card1Id','Class1Id','Card2Id','Class2Id','DomainDirection','DomainDestClassId', 'Class',TARGET_CLASS_ID,'BeginDate','CardId','CardCode','CardDescription']
		});

		var proxy = new Ext.data.HttpProxy({
			url: 'services/json/management/modcard/getrelationlist'
		});

		var me = this;
		
		Ext.apply(this, {
			loadMask: true,
			collapsible: false,
	        border: false,
	        hideMode: "offsets",
			store: new CMDBuild.data.XGroupingStore({
				reader: reader,
				proxy: proxy,
				groupField: 'Domain',
				remoteGroup: true,
				groupTotalField: 'DomainCount',
				groupParameter: 'DirectedDomain',
				autoLoad: false
	        }),
	        cm: new Ext.grid.ColumnModel([
	        	{header: col_tr.domain, width: 20, fixed: true, sortable: false, dataIndex: 'Domain', hidden: true},
	        	{header: col_tr.destclass, width: 20, sortable: false, dataIndex: 'Class'},
	        	{header: col_tr.begin_date, width: 20, sortable: false, dataIndex: 'BeginDate'},
	            {header: col_tr.code, width: 20, sortable: false, dataIndex: 'CardCode'},
	            {header: col_tr.description, width: 40, sortable: false, dataIndex: 'CardDescription'},
	            {
	            	header: '&nbsp', 
	            	width: 90,
	            	fixed: true, 
	            	sortable: false, 
	            	renderer: me.renderRelationActions, 
	            	align: 'center', 
	            	cellCls: 'grid-button', 
	            	dataIndex: 'Fake',
	            	menuDisabled: true,
	    			id: 'imagecolumn',
	    			hideable: false
	            }
	        ]),
	        view: new CMDBuild.grid.XGroupingView({
				forceFit: true,
				groupTextTpl: '{[values.rs[0].data.DomainDesc]} ({[values.rs[0].data.DomainCount]} {[values.rs[0].data.DomainCount > 1 ? CMDBuild.Translation.management.modcard.relation_columns.items : CMDBuild.Translation.management.modcard.relation_columns.item]})',
	        	enableGroupingMenu: false,
	        	enableNoGroups: false
			}),	        
	        tbar: [
				this.addRelationsAction,
				new CMDBuild.Management.GraphActionHandler().getAction()
			]
		});

		this.callParent(arguments);

		this.on('rowdblclick', doubleclickHandler);
		this.on('cellclick', cellclickHandler);
		
		if (this.subscribeToEvents === true) {
            this.subscribe('cmdb-init-' + this.eventmastertype, this.initForClass, this);
            this.subscribe('cmdb-new-' + this.eventtype, this.newCard, this);
            this.subscribe('cmdb-load-' + this.eventtype, this.loadCard, this);
            this.subscribe('cmdb-reload-' + this.eventtype, this.reloadCard, this);
		}

		this.on('activate', this.loadCardRelations, this);
	},

	getAddRelationButton : function() {
		return this.addRelationsAction;
	},

	renderRelationActions : function(value, metadata, record) {
		var tr = CMDBuild.Translation.management.modcard;
		var actionsHtml = '<img style="cursor:pointer" title="'+tr.open_relation+'" class="action-relation-go" src="images/icons/bullet_go.png"/>&nbsp;';
		if (!this.readOnly) {
			actionsHtml += '<img style="cursor:pointer" title="'+tr.edit_relation+'" class="action-relation-edit" src="images/icons/link_edit.png"/>&nbsp;'
		     + '<img style="cursor:pointer" title="'+tr.delete_relation+'" class="action-relation-delete" src="images/icons/link_delete.png"/>&nbsp;';
		}
		var tableId = record.get(TARGET_CLASS_ID);
		var table = CMDBuild.Cache.getClassById(tableId);
		if (table && table.priv_write) {
			actionsHtml += '<img style="cursor:pointer" class="action-relation-editcard" src="images/icons/modify.png"/>';
		} else {
			actionsHtml += '<img style="cursor:pointer" title="'+tr.view_relation+'" class="action-relation-viewcard" src="images/icons/zoom.png"/>';
		}
		return actionsHtml;
    },

	initForClass: function(eventParams) {
		this.disable();
		this.getStore().removeAll();
		if (eventParams) {
			this.currentClassPrivileges = Ext.apply({
					create: false,
					write: false
				}, eventParams.privileges);
		}
	},

	newCard: function(eventParams) {
		this.disable();
		this.getStore().removeAll();
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
			create: eventParams.record.data.priv_create,
			write: eventParams.record.data.priv_write
		};
		this.addRelationsAction.setDisabled(!this.currentCardPrivileges.write || this.readOnly);
		this.reloadCard();
	},

	reloadCard: function(eventParams) {
		this.enable();
		this.loaded = false;
		if (this.relationTabIsActive()) {
			this.loadCardRelations();
		}
	},

	relationTabIsActive: function() {
		// getActiveTab is not present in manageRelationTab so load it always
		return (!this.ownerCt.getActiveTab || this.ownerCt.getActiveTab() === this);
	},

	loadCardRelations: function() {
		if (this.loaded || 
				!(this.currentClassId && this.currentCardId)) {
			return;
		}
		var store = this.getStore();
		store.baseParams = {
				IdClass: this.currentClassId,
				Id: this.currentCardId
			};
		store.load({
			params : {
				domainlimit: CMDBuild.Config.cmdbuild.relationlimit
			}
		});
		this.loaded = true;
	},

	addRelations: function() {
		addRelationsWin = new CMDBuild.Management.AddRelationWindow({
			classId: this.currentClassId,
			cardId: this.currentCardId,
			filterType: 'addrelation'
		});
		addRelationsWin.show();
	},

	deleteRelation: function(jsonRow) {
		Ext.Msg.confirm(
			this.translation.delete_relation,
			this.translation.delete_relation_confirm,
			function(btn) {
				if (btn != 'yes') {
					return;
				}
				CMDBuild.LoadMask.get().show();
				CMDBuild.Ajax.request({
					url : 'services/json/management/modcard/deleterelation',
					params : {
						"DomainId" : jsonRow.DomainId,
						"Class1Id" : jsonRow.Class1Id,
						"Card1Id" : jsonRow.Card1Id,
						"Class2Id" : jsonRow.Class2Id,
						"Card2Id" : jsonRow.Card2Id
					},					
					method : 'POST',
					scope : this,
					success : this.onDeleteRelationSuccess,
					callback: function() {
		            	CMDBuild.LoadMask.get().hide();
		            }
			 	});
			}, this);
	},

	onDeleteRelationSuccess: function() {
		this.publish('cmdb-reload-' + this.eventtype, {cardId: this.currentCardId});
	},
	
	editRelation: function(jsonRowP) {
		var cardIdP = this.currentCardId;
		var _this = this;
		var showRelationWindow = function(attributes) {
			var jsonRow = jsonRowP;
			var cardId = cardIdP; //visibility issues
			editRelationsWin = new CMDBuild.Management.EditRelationWindow({
				cardId: cardId,
				domainId : jsonRow.DomainId,
				class1Id : jsonRow.Class1Id,
				card1Id : jsonRow.Card1Id,
				class2Id : jsonRow.Class2Id,
				card2Id : jsonRow.Card2Id,
				domainDirection : jsonRow.DomainDir,
				domainDestClassId : jsonRow.DomainDestClassId,
				attributes: attributes,
				filterType: 'editrelation',
				updateEventName: _this.updateEventName
			});
			editRelationsWin.show();
		};
		CMDBuild.Management.FieldManager.loadAttributes(jsonRowP.DomainDestClassId, showRelationWindow);
	},

	viewCard: function(jsonRow) {
		var _this = this;
		CMDBuild.Ajax.request({
			url : 'services/json/management/modcard/getcard',
			params : {
				"IdClass" : jsonRow.ClassId,
				"Id" : jsonRow.CardId
			},
			method : 'POST',
			scope : this,
			success : function(response, options, decoded) {
				new CMDBuild.Management.DetailWindow({
					cardData: decoded.card,
					className: jsonRow.Class,
					classAttributes: decoded.attributes,
					idDomain: jsonRow.Domain
				}).show();
			}
	 	});
	},

	editCard: function(jsonRow) {
		var _this = this;
		CMDBuild.Ajax.request({
			url : 'services/json/management/modcard/getcard',
			params : {
				"IdClass" : jsonRow.ClassId,
				"Id" : jsonRow.CardId
			},
			method : 'POST',
			scope : this,
			success : function(response, options, decoded) {
				new CMDBuild.Management.EditDetailWindow({
					updateEventName: _this.updateEventName,
					cardData: decoded.card,
					className: jsonRow.Class,
					classAttributes: decoded.attributes,
					idDomain: jsonRow.Domain
				}).show();
			}
	 	});
	}
});
	 */
})();