CMDBuild.Management.CardTabController = function(view, subscribeToEvents) {
	this.view = view;
	this.idClassOfCurrentRecord = undefined;
	this.cardExtensionsProvider = [];
	if (subscribeToEvents) {
		this.subscribe('cmdb-init-class', this.initForClass, this);
		this.subscribe('cmdb-enablemodify-card', this.onEnableModifyCard, this);
		this.subscribe('cmdb-reload-class', this.resetTab, this);
	    this.subscribe('cmdb-load-card', this.loadCard, this);
	    this.subscribe('cmdb-new-card', this.newCard, this);	    
	    this.subscribe('cmdb-empty-card', this.onEmptyCard, this);
	}
};
	
Ext.extend(CMDBuild.Management.CardTabController, Ext.util.Observable, {
	initForClass: function(eventParams) {
		eventParams.cachedTable = CMDBuild.Cache.getTableById(eventParams.classId);
		this.view.initForClass(eventParams);
	},
	
	setView: function(view) {
		this.view = view;
	},
	
	resetTab: function(eventParams) {		
		this.view.resetTab(eventParams);
	},
	
	onEmptyCard: function() {
		this.idClassOfCurrentRecord = undefined;
		this.view.removeFields();
	},
	
	loadCard: function(eventParams) {
		var eventIdClass = eventParams.record.data.IdClass;
		var isPublishedByTheMap = function() {
			return eventParams.publisher && eventParams.publisher.map;
		};
		
		if (this.idClassOfCurrentRecord != eventIdClass || isPublishedByTheMap()) {
			var callback = this.view.loadCard.createDelegate(this.view, [eventParams], true);
			this.idClassOfCurrentRecord = eventIdClass;
			CMDBuild.Management.FieldManager.loadAttributes(this.idClassOfCurrentRecord, callback, true);
		} else {
			this.view.loadCard(undefined, eventParams);
		}
	},
	
	onEnableModifyCard: function(params) {
		if (params && params.publisher.id == this.view.id) {
			this.view.silentEnableModify(params);
		} else {
			this.view.enableModify(params);
		}
	},
	
	newCard: function(eventParams) {
		if (this.idClassOfCurrentRecord != eventParams.classId && !eventParams.clone) {
			var v = this.view;
			var callback = function(attributeList) {
				v.buildTabbedPanel(attributeList);
				v.newCard(eventParams);
			};
			CMDBuild.Management.FieldManager.loadAttributes(eventParams.classId, callback, true);
		} else {
			this.view.newCard(eventParams);
		}
	},	
	
	printCard: function(format) {		
		CMDBuild.LoadMask.get().show();
		CMDBuild.Ajax.request({
			url : 'services/json/management/modreport/printcarddetails',
			params : {
				IdClass: this.currentClassId,
				Id: this.currentCardId,
				format: format
			},
			method : 'POST',
			scope : this,
			success: function(response) {
				CMDBuild.LoadMask.get().hide();
				var popup = window.open("services/json/management/modreport/printreportfactory", "Report", "height=400,width=550,status=no,toolbar=no,scrollbars=yes,menubar=no,location=no,resizable");
				if (!popup) {
					CMDBuild.Msg.warn(CMDBuild.Translation.warnings.warning_message,CMDBuild.Translation.warnings.popup_block);
				}
			},
			callback : function() {
				CMDBuild.LoadMask.get().hide();
	      	}
		});
	},
	
	deleteCard: function() {
		var title = this.translation.delete_card;
		var msg = this.translation.delete_card_confirm;
		var makeRequest = function(btn) {
			if (btn != 'yes') {
				return;
			}
			CMDBuild.LoadMask.get().show();
			var scope = scope;
			CMDBuild.Ajax.request({
				scope : this,
				important: true,
				url : 'services/json/management/modcard/deletecard',
				params : {
					"IdClass": this.currentClassId,
					"Id": this.currentCardId
				},
				method : 'POST',
				success : function() {
					this.publish('cmdb-init-class', { classId: this.currentClassId }); // to reload the grid
					this.publish('cmdb-delete-card', { classId: this.currentClassId });
				},
				callback : function() {
					CMDBuild.LoadMask.get().hide();
		      	}
	  	 	});
		};
		Ext.Msg.confirm(title, msg,makeRequest, this);
	},
	
	addCardExtensionProvider: function(ex) {
		this.cardExtensionsProvider.push(ex);
	},
	
	saveCard: function() {
		var form = this.view.form.getForm();
		var view = this.view;
		var ex = this.cardExtensionsProvider;
		
		var invalidAttributes = this.view.form.getInvalidAttributeAsHTML();
		
		if (invalidAttributes == null) {
			CMDBuild.LoadMask.get().show();
			form.submit({
				method : 'POST',
				url : 'services/json/management/modcard/updatecard',				
				scope: this.view,
				params: (function(ex) {
					var params = {};
					for (var i=0, l=ex.length; i<l; ++i) {
						params[ex[i].getExtensionName()] =  Ext.encode(ex[i].getValues());
					}
					return params;
				})(ex),
				success : function(form, action) {
					this.disableModify();
					if (action.result.id) {
						var newId = action.result.id;
						this.publish('cmdb-reload-card', { cardId: newId, classId: this.currentClassId });
					} else {
						this.publish('cmdb-reload-card', { cardId: this.currentCardId, classId: this.currentClassId});
					}
					view.fireEvent("cmdb-close-window");
				},
				callback : function() {
					CMDBuild.LoadMask.get().hide();
		      	}
			});
		} else {
			var msg = String.format("<p class=\"{0}\">{1}</p>", CMDBuild.Constants.css.error_msg, CMDBuild.Translation.errors.invalid_attributes);
			CMDBuild.Msg.error(null, msg + invalidAttributes, false);
		}
    }
});