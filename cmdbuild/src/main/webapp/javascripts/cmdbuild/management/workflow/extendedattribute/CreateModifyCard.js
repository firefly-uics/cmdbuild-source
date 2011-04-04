(function() {

CMDBuild.Management.CreateModifyCard = Ext.extend(CMDBuild.Management.BaseExtendedAttribute, {
	attributesLoaded: false,
	classId: -1,
	currentCardId: -1,
	hideMode: "offsets",
	addCardAction: { // null object
		setClassId: Ext.emptyFn 
	},
	getActivityTabForm: function() {
		return Ext.getCmp('activity_tab').actualForm;
	},
	
	/**
     * @param idClass: int
     * @param id: int (if <= 0 a new card will be created)
     * @param outputName:  String
     * @param extAttrDef: Object
     * @return Object
     */
	initialize: function( extAttrDef ) {
		this.outputName = this.getVariable('xa:outputName');
        this.classId = this.getVariable('xa:idClass');
        this.cachedTable = CMDBuild.Cache.getTableById(this.classId);
        
        if (!this.cachedTable) {
        	CMDBuild.Msg.error(CMDBuild.Translation.errors.error_message 
        			, String.format(CMDBuild.Translation.errors.reasons.CLASS_NOTFOUND, this.classId || "")
        			, false);
        }
        
        var extAttr = this;
		this.cardTab = new CMDBuild.Management.CardTabUI({
			readOnlyForm: extAttrDef.ReadOnly,
			withToolBar: false,
			subscribeToEvents: false,
			currentCardPrivileges: { write:true },
			autoScroll: true,
            onCancelButton: function() {
            	this.getForm().reset();
            },
            saveButtonHandler: onControllerSaveCard.createDelegate(extAttr) // TODO: THIS SUPERSUCKS
		});
		
		// save the cardtabui buttons to be returned by
		// buildSpecificButtons and clear them
		this.cardTabButtons = this.cardTab.buttons || []; // because cardTab in ReadOnly mode has not buttons
		this.cardTab.buttons = [];
		
		var config =  {
          layout: 'fit',
          items: [this.cardTab]
        };
		
		return insertAddMenuIfSuperclass.call(this, config);
	},
	
	onExtAttrShow: function(extAttr) {
		if (this.cachedTable) {
	        this.currentCardId = this.getCardId(this.extAttrDef);
	        this.addCardAction.setClassId(this.cachedTable);
	        if (this.isNewCard() && this.cachedTable.superclass) {
				this.clearForm();
				showTopBar.call(this);
			} else {
				hideTopBar.call(this);
				this.loadAttributesAndFillForm(this.classId);
			}
		}
	},

	buildSpecificButtons: function() {
		return this.cardTabButtons;
	},
	
	getCardId: function(extAttrDef) {
		var cardId = this.getVariable('xa:id');
		if (!cardId) {
			return -1;
		}
		if (typeof cardId == "string") {
			cardId = parseInt(cardId);
			if (isNaN(cardId)) {
				cardId = -1;
			}
		}
		return cardId;
	},

	loadAttributesAndFillForm: function(classId) {
		// pass the class id as parameter to allow
		// the load of subclasses attributes
		if (this.attributesLoaded) {
			this.fillForm();
		} else {
    		var callback = this.setupAndFillForm.createDelegate(this);
            CMDBuild.Management.FieldManager.loadAttributes(classId, callback);
            this.attributesLoaded = true;
		}
	},
	
	clearForm: function() {
		this.cardTab.removeFields();
	},
	
	setupAndFillForm: function(attrList) {
		this.attributeList = attrList;
		this.setupForm();
		this.fillForm();
	},

	setupForm: function() {
		var params = {
            classId: this.classId
        };
        this.cardTab.initForClass(params);
        this.cardTab.buildTabbedPanel(this.attributeList);
	},

	isNewCard: function() {
		return (this.currentCardId <= 0);
	},

	fillForm: function() {
        if (this.isNewCard()) {
            this.newCard();
        } else {
            this.loadRemoteCard();
        }
    },

    newCard: function() {
    	this.cardTab.newCard({
    		classId: this.classIdForNewCard || this.classId,
    		silentEnableModify: true
        });
    },

    loadCardData: function(cardData, attributes) {
    	cardData.priv_create = true;
    	cardData.priv_write = true;
    	var eventParams = {
            record : new Ext.data.Record(cardData),
            silentEnableModify: true
        };
        this.cardTab.loadCard(attributes || this.attributeList, eventParams);        
    },

    loadRemoteCard: function() {
		CMDBuild.Ajax.request({
			url : 'services/json/management/modcard/getcard',
			params : {
				IdClass : this.classId,
				Id : this.currentCardId
			},
			method : 'POST',
			scope : this,
			success : function(response, options, jsonResult) {
				this.loadCardData(jsonResult.card, jsonResult.attributes);
			}
		});
	},

	onSave: function() {
		// save card and react with the outputname=id
		if (this.currentCardId >= 0) {
            var reactObj = {};
            reactObj[this.outputName] = this.currentCardId;
            this.react(reactObj);
		}
	}
});
Ext.reg("createModifyCard", CMDBuild.Management.CreateModifyCard);

function onControllerSaveCard() {
	CMDBuild.log.info('custom saveCard on CreateCard extattr called!');
	var form = this.cardTab.getForm();
	var invalidAttributes = this.cardTab.getInvalidAttributeAsHTML();
	
	if (invalidAttributes == null) {
        form.submit({
            method : 'POST',
            url : 'services/json/management/modcard/updatecard',
            waitTitle : CMDBuild.Translation.common.wait_title,
            waitMsg : CMDBuild.Translation.common.wait_msg,
            scope: this,
            success : function(form, action) {
                var newId = action.result.id;
                if (newId) {
                    this.currentCardId = newId;
                }
                this.publish('cmdb-reload-card', { cardId: this.currentCardId, classId: this.classId});

            	if (this.outputName) {
            		var activityForm = this.getActivityTabForm().getForm();
           			var outputField = activityForm.findField(this.outputName);
           			if (outputField && outputField.store) {
           				// TODO: if it is a combo, otherwise the description should be used!
           				outputField.setValue(this.currentCardId);
           			}
            	}
            	this.backToActivityTab();
            }
        });
    } else {
    	var msg = String.format("<p class=\"{0}\">{1}</p>", CMDBuild.Constants.css.error_msg, CMDBuild.Translation.errors.invalid_attributes);
		CMDBuild.Msg.error(null, msg + invalidAttributes, false);
    }
}

function insertAddMenuIfSuperclass(config) {
	if (this.cachedTable) {
		if (this.cachedTable.superclass) {
			this.addCardAction = new CMDBuild.AddCardMenuButton({
				classId: undefined,
				eventName: "cmdb-new-card"
			});
			
			this.addCardAction.on("cmdb-new-card", function(p) {
				this.addCardAction.setTextSuffix(p.className);
				this.classIdForNewCard = p.classId;
				this.attributesLoaded = false; // reload any time
				this.loadAttributesAndFillForm(this.classIdForNewCard);
			}, this);			
			config.tbar = [this.addCardAction];
		}
	}
	return config;
}

function hideTopBar() {
	// if the class is not a superclass
	// there isn't a top bar
	var tbar = this.getTopToolbar();
	if (tbar) {
		tbar.hide();
		this.doLayout();
	}
}

function showTopBar() {
	// if the class is not a superclass
	// there isn't a top bar
	var tbar = this.getTopToolbar();
	if (tbar) {
		tbar.show();
		this.doLayout();
	}
}

})();