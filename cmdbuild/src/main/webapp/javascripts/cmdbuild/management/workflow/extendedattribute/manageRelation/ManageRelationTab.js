CMDBuild.Management.ManageRelationTab = Ext.extend(CMDBuild.Management.CardRelationsTab, {
	extAttrDef: undefined,
	classId: undefined,
	domainId: undefined,
	extAttrInst: undefined,
	currentCardId: undefined,
	
	subscribeToEvents: false,
	updateEventName: 'cmdb-wf-extattr-reloadrelations',

	initComponent: function() {
		CMDBuild.Management.ManageRelationTab.superclass.initComponent.apply(this, arguments);
		this.updateEventName += this.extAttrDef.identifier;
		Ext.apply(this, {
			 style: {'border-bottom': '1px ' +  CMDBuild.Constants.colors.blue.border +' solid'}
		});
		this.subscribe(this.updateEventName, this.loadCardRelations, this);
		// my goodness! the events that are unsubscribed are only those in this.events!
		this.on('destroy', function() {
			this.unsubscribe(this.updateEventName, this.loadCardRelations, this);
		}, this);
	},
	
	loadCardRelations: function() {
    	this.getStore().load({
            params : {
            	IdClass: this.classId,
            	Id: this.currentCardId,
            	DirectedDomain: this.domainId
            }
        });
    },
	
	renderRelationActions: function(value,meta,record,rowIndex,colIndex,store) {
		var enabledFunctions = this.extAttrDef.enabledFunctions;
		var out = '';
		
		var extAttrDef = this.extAttrDef;
		
	  	var isSel = (function(record) {
			var id = parseInt(record.get('CardId'));
			if(undefined === extAttrDef.currentValue){return false;}
			return extAttrDef.currentValue.indexOf(id) >= 0;
		})(record);

        if (enabledFunctions['single'] || enabledFunctions['multi']) {
            var type = 'checkbox';
            if (enabledFunctions['single']) {
                type = 'radio';
            }
            out += '<input type="' + type + '" name="' + this.extAttrDef.outputName + '" value="' + record.get('CardId') + '"';
            if(isSel) { out += ' checked="true"'; }
            out += '/>';
        }
        
        if (enabledFunctions['allowModify']) {
            out += '<img style="cursor:pointer" class="action-relation-edit" src="images/icons/link_edit.png"/>&nbsp;';
        }
        if (enabledFunctions['allowUnlink']) {
            out += '<img style="cursor:pointer" class="action-relation-delete" src="images/icons/link_delete.png"/>&nbsp;';
        }
        if (enabledFunctions['allowModifyCard']) {
        	out += '<img style="cursor:pointer" class="action-card-modify" src="images/icons/modify.png"/>&nbsp;';
        }
        if (enabledFunctions['allowDelete']) {
            out += '<img style="cursor:pointer" class="action-card-delete" src="images/icons/delete.png"/>&nbsp;';
        }
        
        return out;
	},

	modifyCard: function(jsonRow) {
      	var _this = this;
      	CMDBuild.LoadMask.get().show();
      	Ext.Ajax.request({
      		url : 'services/json/management/modcard/getcard',
            params: {
            	Id: jsonRow.CardId,
            	IdClass: jsonRow.ClassId
            },
            method : 'POST',
            scope : _this,
            success : function(response) {
            	var resp  = new Ext.util.JSON.decode(response.responseText);
                var attrs = resp['attributes'];
            	var card  = new Ext.data.Record(resp.card);
            	
                var editDetailWindow = new CMDBuild.Management.EditDetailWindow({
                	updateEventName: _this.updateEventName,
                	classId: jsonRow.ClassId,
                    id: jsonRow.CardId,
                    cardDescription: jsonRow.CardDescription,
                    classAttributes: attrs,
                    cardData: card.data,
                    className: jsonRow.Class,
                    idDomain: jsonRow.Domain
                });
                editDetailWindow.show();
            },
            failure : function(response, options) {
            	CMDBuild.Msg.error(CMDBuild.Translation.errors.error_message, CMDBuild.Translation.errors.error_message, true);
            },
            callback: function() {
            	CMDBuild.LoadMask.get().hide();
            }
      	});
	},
	
	deleteCard: function(jsonRow) {				
      	Ext.Msg.show({
            title: CMDBuild.Translation.management.moddetail.deletedetail,
            msg: CMDBuild.Translation.common.confirmpopup.areyousure,
            scope: this,
            buttons: {
                yes: true,
                no: true
            },
            fn: function(button){
                if (button == 'yes'){
                	this.doDelete(jsonRow);
                }
            }
        });
	},
	
	doDelete: function(jsonRow) {
		CMDBuild.LoadMask.get().show();
        CMDBuild.Ajax.request({
            url : 'services/json/management/modcard/deletedetailcard',
            params : jsonRow,                            
            method : 'POST',
            scope : this,
            success : function() {
                this.reloadCard();
            },
            callback: function() {
            	CMDBuild.LoadMask.get().hide();
            }
        });
	},

	// override
	onDeleteRelationSuccess: function() {
		// don't reload the process, only the relation grid
		this.loadCardRelations();
	},

	takeDetailAttributes: function(detail) {
  	    var callback = this.showAddDetailWindow.createDelegate(this, [detail], true);
        CMDBuild.Management.FieldManager.loadAttributes(detail.classId, callback);
    },
    
    showAddDetailWindow: function(attributes, detail) {
        new CMDBuild.Management.AddDetailWindow({
        	updateEventName: this.updateEventName,
            detail: detail,
            classAttributes: attributes,
            masterData: {Id: this.extAttrInst.getCardId(),IdClass: this.classId},
            idDomain: this.domainId,
            classId: detail.classId,
            className: detail.className    		
        }).show();
    },
    
    addRelations: function() {
	  	var _this = this;
	  	var addRelationsWin = new CMDBuild.Management.AddRelationWindow({
	  		classId: this.classId,
	  		cardId: this.extAttrInst.getCardId(),
	  		domainId: this.domainId,
	  		addRelations: function() {
	            var relations = {};
	            relations[this.currentDomain] = this.selections;
	            CMDBuild.LoadMask.get().show();
	            CMDBuild.Ajax.request({
	                url : 'services/json/management/modcard/createrelations',
	                params : {
	                    "IdClass": this.classId,
	                    "Id": this.cardId,
	                    "Relations": Ext.util.JSON.encode(relations)
	                },                       
	                method : 'POST',
	                scope : this,
	                success : function() {
	                    this.close();
	                    _this.loadCardRelations();
	                },
	                callback: function() {
	                	CMDBuild.LoadMask.get().hide();
	                }
	            });
	        }
	  	});
	  	addRelationsWin.findById('comboDomain').disable();
	  	addRelationsWin.show();
    }
});