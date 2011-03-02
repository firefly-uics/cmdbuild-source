(function() {

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
    		disabled: false,
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
    	
    	this.classTree.getSelectionModel().on('selectionchange', function(sm, node) {
        	this.onSelectNode(node);
        }, this);
    	
		this.cardList = new CMDBuild.Management.DomainCardList({
			filterType: this.filterType,
			withFilter: true,
			region: 'center',
			split: true,
			subfiltered: false,
			disabled: true,
			sm: new CMDBuild.grid.CMCheckboxSelectionModel({singleSelect:false, grid: this})
		});
		
		this.cardList.pagingBar.style = {'border-bottom': '1px #99BBE8 solid'};
		this.cardList.getStore().baseParams.writeonly = true;
		
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
   	
   	afterBringToFront: function() {
   		if (!this.firstShow) {
   			var tree = this.classTree;
   			var node = CMDBuild.TreeUtility.findFirsSelectableNode(this.classTree.root);
   			
   			if (node) {
	   			(function() {
	   				tree.selectPath(node.getPath());
	   			}).defer(1, tree);
   			}
   			
   			this.firstShow = true;
   		}
   		return true;
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
			if (node.attributes.subtype) {
				eventParams.itemType = node.attributes.subtype;
			}
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
	
	saveCardsChanges: function() {
		if (this.cardList.isFiltered()) {
			
			Ext.Msg.show({
			   title: CMDBuild.Translation.warnings.warning_message,
			   msg: CMDBuild.Translation.warnings.only_filtered,
			   buttons: Ext.Msg.OKCANCEL,
			   fn: doSaveRequest,
			   icon: Ext.MessageBox.WARNING,
			   scope: this
			});
			
		} else {
			doSaveRequest.call(this, confirm="ok");
		}
	},
	
	abortCardsChanges: function() {
		this.clearAll();
	},
	
	clearAll: function() {
		this.cardList.clearFilter();
		this.cardList.getSelectionModel().clearSelections();
		this.cardList.getSelectionModel().clearPersistentSelections();
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

function builSaveParams() {
	var params = this.attributesPanel.getCheckedValues();
	params["FilterCategory"] = this.filterType;
	params['IdClass'] = this.classId;
	params["isInverted"] = this.cardList.getSelectionModel().isInverted();
	var fullTextQuery = this.cardList.getSearchFilterValue();
	if (fullTextQuery != "") {
		params["fullTextQuery"] = this.cardList.getSearchFilterValue();
	}
	params['selections'] = (function formatSelections() {
		var selections = this.cardList.getSelectionModel().getPersistentSelections();
		var out = [];
		for (var key in selections) {
			out.push(selections[key].json.IdClass + "_" + key);
		}
		return out;
	}).call(this);
	
	return params;
}

function doSaveRequest(confirm) {
	if (confirm != "ok") {
		return;
	}
	
	var params = builSaveParams.call(this);
	if (!params["isInverted"] && params["selections"].length == 0) {
		var msg = String.format("<p class=\"{0}\">{1}</p>", CMDBuild.Constants.css.error_msg,
				CMDBuild.Translation.errors.no_selections);
		CMDBuild.Msg.error(null, msg, false);
	} else {
		CMDBuild.Ajax.request({
			url: 'services/json/management/modcard/updatebulkcards',
			params:params, 
			scope: this,
			success: function(response) {
				this.cardList.getStore().reload();
				this.clearAll();
			}
		});
	}	
}
})();