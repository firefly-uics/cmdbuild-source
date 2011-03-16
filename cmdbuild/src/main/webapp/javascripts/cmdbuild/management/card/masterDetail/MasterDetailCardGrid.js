(function() {
	function imageTagBuilderForIcon(iconName) {
		var ICONS_FOLDER = "images/icons/";
		var ICONS_EXTENSION = "png";
		var EVENT_CLASS_PREFIX = "action-masterdetail-";
		
		var TAG_TEMPLATE = '<img style="cursor:pointer" title="{0}" class="{1}{2}" src="{3}{4}.{5}"/>&nbsp;';
		
		var icons = {
			showDetail: {
				title: CMDBuild.Translation.management.moddetail.showdetail,
				event: "show",
				icon: "zoom"
			},
			editDetail: {
				title: CMDBuild.Translation.management.moddetail.editdetail,
				event: "edit",
				icon: "modify"
			},
			deleteDetail: {
				title: CMDBuild.Translation.management.moddetail.deletedetail,
				event: "delete",
				icon: "cross"
			},
			showGraph: {
				title: CMDBuild.Translation.management.moddetail.showgraph,
				event: "graph",
				icon: "chart_organisation"
			},		
			note: {
				title: CMDBuild.Translation.management.moddetail.shownotes,
				event: "note",
				icon: "note"
			},
			attach: {
				title: CMDBuild.Translation.management.moddetail.showattach,
				event: "attach",
				icon: "attach"
			}
		};
		
		function buildTag(iconName) {
			var icon = icons[iconName];
			if (icon) {
				return String.format(TAG_TEMPLATE, icon.title, EVENT_CLASS_PREFIX, icon.event, ICONS_FOLDER, icon.icon, ICONS_EXTENSION);
			} else {
				return String.format("<span>{0}</span>", iconName);
			}
		}
		
		var tag = "";
		if (Ext.isArray(iconName)) {
			for (var i=0, len=iconName.length; i<len; ++i) {
				tag += buildTag(iconName[i]);
			}
		} else {
			tag = buildTag(iconName);
		}
		
		return tag;
	}
	
CMDBuild.Management.MasterDetailCardGrid = Ext.extend(CMDBuild.EditorGrid, {
	enableColumnMove: false,
	viewConfig: { forceFit:true },

	updateEventName: "cmdb-reload-card",

	//custom attributes
	checksmodel: false,
	filterType: undefined,
	translation : CMDBuild.Translation.management.modcard.cardlist,
	detailTranslation: CMDBuild.Translation.management.moddetail,
	
	baseUrl: undefined,	
	detailURL: {
		get: "services/json/management/modcard/getdetaillist",		
		remove: "services/json/management/modcard/deleterelation"
	},
	
	bigTablesURL: {
		get: "services/json/management/modcard/getcardlist",		
		remove: "services/json/management/modcard/deletecard"
	},
	
	withPagingBar: true,
	filtering : false,
	idDomain: undefined,
	currentClassId: undefined,
	remoteSort: true,
	subfiltered: false,
	editable: true,
	
	initComponent : function() { 
		this.pagingTools = [];
		CMDBuild.Management.DomainCardList.superclass.initComponent.apply(this, arguments);
		this.gridsearchfield = new Ext.app.GridSearchField({grid: this});
		this.pagingTools.push(this.gridsearchfield);		
		this.on('cellclick', this.cellclickHandler);
		this.on('rowdblclick', this.doubleclickHandler);
		
		this.subscribe(this.updateEventName, function() {
			if (this.baseUrl) {
				this.store.reload();
			}
		}, this);
	},
	
	loadFKCardList: function(attributes, fkClass, fkAttribute, cardId) {
		this.baseUrl = this.bigTablesURL.get;
		
		this.fkClass = fkClass;
		this.cardId = cardId;
		this.fkAttribute = fkAttribute;
		this.classAttributes = attributes;
		this.setColumnsForClass(attributes);		
		this.setStoreForNewColumn();
		this.setStoreBaseparams();
		this.loadCards();
	},
	
	loadDetailCardList : function(eventParams) {			
		this.baseUrl = this.detailURL.get;
		
		this.directedDomain = eventParams.directedDomain;
		this.cardId = eventParams.cardId;
		this.classId = eventParams.classId;			
		this.classAttributes = eventParams.classAttributes;
		this.className = eventParams.className;
		this.detailClassType = eventParams.classType;
		this.setColumnsForClass(this.classAttributes, eventParams.superclass);
		this.setStoreForNewColumn();
		this.setStoreBaseparams();
		this.loadCards();
	},
	
	setColumnsForClass : function(classAttributes, superClass) {
		this.headers = [];//array with the objectOption for the headers
		if (superClass) {
			var classHeader = {
				header: CMDBuild.Translation.management.modcard.subclass,
				width: 40,
				fixed: false,
				sortable: false,
				dataIndex: 'IdClass_value'
			};
			this.headers.push(classHeader);
		}
		for (var i = 0; i < classAttributes.length; i++) {			
			var attribute = classAttributes[i];
			if (this.notMasterReference(attribute)) {
				var header = CMDBuild.Management.FieldManager.getHeaderForAttr(attribute);
				if (header) {
					this.headers.push(header);
				}
			}
		}
		
		var iconToRender = this.getIconsToRender();
		this.headers.push({
			header: '&nbsp', 
			width: iconToRender.length * 20,
			fixed: true, 
			sortable: false, 
			renderer: function() {
				return imageTagBuilderForIcon(iconToRender);
			},
			align: 'center',
			cellCls: 'grid-button',
			dataIndex: 'Fake',
			menuDisabled: true,
			id: 'imagecolumn',
			hideable: false
		});
	},
	
	setStoreBaseparams: function() {
		this.getStore.baseParams = {};
		if (this.baseUrl == this.detailURL.get) {
			this.getStore().baseParams['DirectedDomain'] = this.directedDomain;
			this.getStore().baseParams['Id'] = this.cardId;
			this.getStore().baseParams['IdClass'] = this.classId;
		} else {
			this.getStore().baseParams['IdClass'] = this.fkClass.id;
			this.getStore().baseParams['CQL'] = "from " + this.fkClass.name + " where " + this.fkAttribute.name + "=" + this.cardId;
		}
		
		if (this.subfiltered) {
			this.getStore().baseParams.FilterCategory = this.filterType;
			this.getStore().baseParams.FilterSubcategory = this.ownerWindow.getId();
		}
	},
	
	setStoreForNewColumn:function(){
		this.arrColumns = [];//array with the name of dataIndex for the store
		for(i = 0; i < this.headers.length; i++) {
			this.arrColumns[i] = this.headers[i].dataIndex;
		}
		this.pagingBar.unbind(this.store);
		this.store = new Ext.data.JsonStore({
			url: this.baseUrl,
	        root: "rows",
            totalProperty: 'results',
	        fields: this.arrColumns,
	        remoteSort: this.remoteSort
		});
			
		this.reconfigure(this.store, new Ext.grid.ColumnModel(this.headers));
        this.pagingBar.bind(this.store);
	},
	
	loadCards : function(baseParams) {
		if (this.storeHasDirectedDomain) {
			this.getStore().load({
				params: {
					start: 0,
					fullcards: true,
					limit: parseInt(CMDBuild.Config.cmdbuild.rowlimit)
				}
			});
		}
	},
		
	storeHasDirectedDomain: function() {
		if (this.baseUrl == this.bigTablesURL) {
			return true;
		} else {
			return (this.getStore().baseParams.DirectedDomain);
		}
	},
	
	getDomain: function(){
		return this.idDomain;
	},
    
    getIconsToRender: function() {
		var icons = [];
    	if (this.editable) {
    		icons = ["editDetail", "deleteDetail", "showGraph", "note"];
    	} else {
    		icons = ["showDetail", "showGraph", "note"];
    	}
    	
    	if (CMDBuild.Config.dms.enabled == "true") {
    		icons.push("attach");
    	}
    	
    	return icons;
    },
    
    cellclickHandler: function(grid, rowIndex, colIndex, event) {
		var className = event.target.className; 
		var jsonRow = grid.getStore().getAt(rowIndex).json;
		var callbackMap = {
			'action-masterdetail-edit': this.showEditDetailWindow,
			'action-masterdetail-show': this.showDetailWindow,
			'action-masterdetail-delete': this.areYouSurePopUp,
			'action-masterdetail-graph': this.openGraphWindow,
			'action-masterdetail-note': this.openNoteWindow,
			'action-masterdetail-attach': this.openAttachWindow
		};
		if (callbackMap[className]) {
			callbackMap[className].createDelegate(grid)(jsonRow);
		}
	},
	
	openNoteWindow: function(jsonRow) {
		new CMDBuild.Management.DetailNoteWindow({
			masterCard: jsonRow
		}).show();
	},
	
	openAttachWindow: function(jsonRow) {
		new CMDBuild.Management.DetailAttachmentsWindow({
			masterCard: jsonRow
		}).show();
	},
	
	showEditDetailWindow: function(jsonRow) {
		var _this = this;
		var record = new Ext.data.Record(jsonRow);
		var editDetailWindow = 	new CMDBuild.Management.EditDetailWindow({
			updateEventName: _this.updateEventName,
			cardData: record.data,
			fkAttribute: this.fkAttribute,
			className: this.className,
			classAttributes: this.classAttributes,
			idDomain:this.directedDomain
		});
		editDetailWindow.show();
	},
	
	showDetailWindow: function(jsonRow) {
		var record = new Ext.data.Record(jsonRow);
		var detailWindow = 	new CMDBuild.Management.EditDetailWindow({
			updateEventName: _this.updateEventName,
			detailClassId: jsonRow.IdClass,
			cardDescription: jsonRow.Description,
			classAttributes: this.classAttributes,
			cardData: record,
			detailName: this.className,
			idDomain:this.directedDomain,
			editable: false
		});
		detailWindow.show();
	},
	
	areYouSurePopUp: function(jsonRow) {
		Ext.Msg.show({
			title: CMDBuild.Translation.management.moddetail.deletedetail,
			msg: CMDBuild.Translation.common.confirmpopup.areyousure,
			scope: this,
			buttons: {
				yes: true,
				no: true
			},
			fn: function(button) {
				if (button == 'yes') {
					if (this.baseUrl == this.detailURL.get) {
						this.deleteDetail(jsonRow);
					} else {
						this.deleteCardDetail(jsonRow.IdClass, jsonRow.Id);
					}
				}
			}
		});
	},
	
	deleteDetail: function(jsonRow) {
		var domain = this.removeDirectionToDomain();
		this.deleteRelation(jsonRow, domain);
	},
	
	deleteRelation: function(jsonRow, domain) {
		var url = this.detailURL.remove;
		CMDBuild.Ajax.request({
			url: url,
			params : {
				"DomainId" : domain,
				"Class1Id" : this.classId,
				"Card1Id" : this.cardId,
				"Class2Id" : jsonRow.IdClass,
				"Card2Id" : jsonRow.Id
			},
			waitTitle : CMDBuild.Translation.common.wait_title,
			waitMsg : CMDBuild.Translation.common.wait_msg,
			method : 'POST',
			scope : this,
			success : function() {
				this.deleteCardDetail(jsonRow.IdClass, jsonRow.Id);
			}
	 	});
	},
	
	deleteCardDetail: function(IdClass, Id) {
		var _this = this;
		var url = this.bigTablesURL.remove;
		CMDBuild.Ajax.request({
			url: url,
			params : {
				"IdClass": IdClass,
				"Id": Id
			},
			waitTitle : CMDBuild.Translation.common.wait_title,
			waitMsg : CMDBuild.Translation.common.wait_msg,
			method : 'POST',
			scope : this,
			success : function() {
				this.publish(_this.updateEventName, {classId: IdClass, cardId: Id});
			}
  	 	});
	},
	
	openGraphWindow: function(jsonRow){
		CMDBuild.Management.showGraphWindow(jsonRow.IdClass, jsonRow.Id);
	},
	
	reset: function(){
		this.setColumnsForClass({});
	},
	
	removeDirectionToDomain: function() {
		var stringLength = this.directedDomain.length;
		return this.directedDomain.substr(0, stringLength - 2);
	},
	
	doubleclickHandler: function(grid, rowIndex, event) {
		var jsonRow = grid.getStore().getAt(rowIndex).json;
		CMDBuild.Management.openCard.createDelegate(grid)({
			ClassId: jsonRow.IdClass,
			CardId: jsonRow.Id,
			Class: jsonRow.IdClass_value,
			ClassType: this.detailClassType
		},CMDBuild.Constants.tabNames.card);				
	},
	
	notMasterReference: function(attribute) {
		if (attribute && attribute.idDomain) {
			var invertedDirection = attribute.domainDirection ? "_I" : "_D";
			var directedDomain = attribute.idDomain + invertedDirection;
			return (directedDomain != this.directedDomain);
		}

		if (this.fkAttribute && attribute.name == this.fkAttribute.name) {
			return false;
		}
		return true;
	}
});
})();