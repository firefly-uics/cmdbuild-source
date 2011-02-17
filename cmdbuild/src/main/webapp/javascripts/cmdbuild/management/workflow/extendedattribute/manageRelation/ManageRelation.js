/**
 * 
 * @class CMDBuild.Management.ManageRelation
 * @extends CMDBuild.Management.BaseExtendedAttribute
 */
CMDBuild.Management.ManageRelation = Ext.extend(CMDBuild.Management.BaseExtendedAttribute, {

	getCardId: function() {
		return this.getVariable('xa:id');
	},
	
	/**
	 * parameters:
	 * String domainName
	 * int id
	 * int idClass
	 * String outputName
	 * int[] currentValue
	 * @param {} extAttrDef
	 * @return {}
	 */
	initialize: function( extAttrDef ) {
		var theExtAttrInst = this;
		var theClassId = this.getVariable('xa:idClass');
		var plainId = this.getVariable('xa:domainIdNoDir');
		
		this.outputName = extAttrDef.outputName;
        
		this.relationsTab = new CMDBuild.Management.ManageRelationTab({
        	extAttrDef: extAttrDef,
        	classId: theClassId,
        	domainId: this.getVariable('xa:domainId'),
        	extAttrInst: theExtAttrInst,
        	currentCardId: this.getCardId(),     	
        	subscribeToEvents: false
        });
		
		if (!extAttrDef.enabledFunctions['linkElement']) {
            this.relationsTab.getAddRelationButton().disable();
        }
        var priv = {create:true,write:true};
        if (!extAttrDef.enabledFunctions['createAndLinkElement']) {
        	//disable createAndLink Action
        	priv.create=false;
        	priv.write=false;
        } else {
        	var relTab = this.relationsTab;
        	var detailMenu = new CMDBuild.AddCardMenuButton({
    			classId: this.getVariable('xa:TargetClassId'),
    			eventName: "cmdb-new-card"
    		});
        	detailMenu.on("cmdb-new-card", function(p) {
        		relTab.takeDetailAttributes(p);
    		}, this);
        	relTab.getTopToolbar().add(detailMenu);
        }
        this.relationsTab.currentClassPrivileges = priv;

		return {
		  items: [this.relationsTab]
		};
	},
	
	onExtAttrShow: function(extAttr) {
		this.relationsTab.currentCardId = this.getCardId();
		this.relationsTab.loadCardRelations();
	},
	
	onSave: function(evtParams,fn) {
		if (this.outputName) {
			var out = {};
			out[this.outputName] = this.getData();
			this.react(out,fn);
		}
	},

	getData: function() {
		var dataArr = [];
		var nodes = Ext.query('input[name='+this.outputName+']');
		Ext.each(nodes, function(item) {
			if(item.checked) {
                dataArr.push(item.value);
            }
        });
		return dataArr;
	}
});

Ext.reg("manageRelation", CMDBuild.Management.ManageRelation);