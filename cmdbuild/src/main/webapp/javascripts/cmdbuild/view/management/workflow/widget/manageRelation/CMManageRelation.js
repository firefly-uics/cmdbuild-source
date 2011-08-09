(function() {
	Ext.define("CMDBuild.view.management.workflow.widgets.CMManageRelation", {
		extend: "CMDBuild.view.management.classes.CMCardRelationsPanel",

		constructor: function(c) {
			this.extattrtype = "manageRelation";
			this.widgetConf = c.widget;
			this.activity = c.activity.raw || c.activity.data;
			this.clientForm = c.clientForm;

			this.callParent(arguments);
		},

		initComponent: function() {
			var createAndLink = this.widgetConf.enabledFunctions.createAndLinkElement || false,
				linkElement = this.widgetConf.enabledFunctions.linkElement || false;

			this.cmWithAddButton = createAndLink || linkElement;
			this.border = false;
			this.frame = false;
			
			this.callParent(arguments);
		},

		cmActivate: function() {
			this.ownerCt.cmActivate();
		},

		renderRelationActions: function(value, metadata, record) {
			if (record.get("depth") == 1) { // the domains node has no icons to render
				return "";
			}

			var tr = CMDBuild.Translation.management.modcard,
				actionsHtml = '',
				enabledFunctions = this.widgetConf.enabledFunctions,
				extAttrDef = this.widgetConf,
				isSel = (function(record) {
						var id = parseInt(record.get('CardId'));
						if(undefined === extAttrDef.currentValue){return false;}
						return extAttrDef.currentValue.indexOf(id) >= 0;
					})(record);

			if (enabledFunctions['single'] || enabledFunctions['multi']) {
				var type = 'checkbox';
				if (enabledFunctions['single']) {
					type = 'radio';
				}

				actionsHtml += '<input type="' + type + '" name="'
						+ this.widgetConf.outputName + '" value="'
						+ record.get('dst_id') + '"';

				if (isSel) {
					actionsHtml += ' checked="true"';
				}

				actionsHtml += '/>';
			}

			if (enabledFunctions['allowModify']) {
				actionsHtml += getImgTag("edit", "link_edit.png");
			}
			if (enabledFunctions['allowUnlink']) {
				actionsHtml += getImgTag("delete", "link_delete.png");
			}
			if (enabledFunctions['allowModifyCard']) {
				actionsHtml += getImgTag("editcard", "modify.png");
			}
			if (enabledFunctions['allowDelete']) {
				actionsHtml += getImgTag("deletecard", "delete.png");
			}

			return actionsHtml;
		}
	});

	function getImgTag(action, icon) {
		return '<img style="cursor:pointer" class="action-relation-'+ action +'" src="images/icons/' + icon + '"/>';
	}

})();

///**
// * 
// * @class CMDBuild.Management.ManageRelation
// * @extends CMDBuild.Management.BaseExtendedAttribute
// */
//CMDBuild.Management.ManageRelation = Ext.extend(CMDBuild.Management.BaseExtendedAttribute, {
//
//	getCardId: function() {
//		return this.getVariable('xa:id');
//	},
//	
//	/**
//	 * parameters:
//	 * String domainName
//	 * int id
//	 * int idClass
//	 * String outputName
//	 * int[] currentValue
//	 * @param {} extAttrDef
//	 * @return {}
//	 */
//	initialize: function( extAttrDef ) {
//		var theExtAttrInst = this;
//		var theClassId = this.getVariable('xa:idClass');
//		var plainId = this.getVariable('xa:domainIdNoDir');
//		
//		this.outputName = extAttrDef.outputName;
//        
//		this.relationsTab = new CMDBuild.Management.ManageRelationTab({
//        	extAttrDef: extAttrDef,
//        	classId: theClassId,
//        	domainId: this.getVariable('xa:domainId'),
//        	extAttrInst: theExtAttrInst,
//        	currentCardId: this.getCardId(),     	
//        	subscribeToEvents: false
//        });
//		
//		if (!extAttrDef.enabledFunctions['linkElement']) {
//            this.relationsTab.getAddRelationButton().disable();
//        }
//        var priv = {create:true,write:true};
//        if (!extAttrDef.enabledFunctions['createAndLinkElement']) {
//        	//disable createAndLink Action
//        	priv.create=false;
//        	priv.write=false;
//        } else {
//        	var relTab = this.relationsTab;
//        	var detailMenu = new CMDBuild.AddCardMenuButton({
//    			classId: this.getVariable('xa:TargetClassId'),
//    			eventName: "cmdb-new-card"
//    		});
//        	detailMenu.on("cmdb-new-card", function(p) {
//        		relTab.takeDetailAttributes(p);
//    		}, this);
//        	relTab.getTopToolbar().add(detailMenu);
//        }
//        this.relationsTab.currentClassPrivileges = priv;
//
//		return {
//		  items: [this.relationsTab]
//		};
//	},
//	
//	onExtAttrShow: function(extAttr) {
//		this.relationsTab.currentCardId = this.getCardId();
//		this.relationsTab.loadCardRelations();
//	},
//	
//	onSave: function() {
//		if (this.outputName) {
//			var out = {};
//			out[this.outputName] = this.getData();
//			this.react(out);
//		}
//	},
//
//	getData: function() {
//		var dataArr = [];
//		var nodes = Ext.query('input[name='+this.outputName+']');
//		Ext.each(nodes, function(item) {
//			if(item.checked) {
//                dataArr.push(item.value);
//            }
//        });
//		return dataArr;
//	}
//});
//
//Ext.reg("manageRelation", CMDBuild.Management.ManageRelation);