CMDBuild.Management.SearchableCombo = Ext.extend(CMDBuild.CMDBuildCombo, {
	initComponent : function(){
		CMDBuild.Management.SearchableCombo.superclass.initComponent.call(this);
		
        this.triggerConfig = {
            tag:'span', cls:'x-form-twin-triggers', cn:[
            {tag: "img", src: Ext.BLANK_IMAGE_URL, cls: "x-form-trigger " + this.trigger1Class},
            {tag: "img", src: Ext.BLANK_IMAGE_URL, cls: "x-form-trigger " + this.trigger2Class},
            {tag: "img", src: Ext.BLANK_IMAGE_URL, cls: "x-form-trigger " + this.trigger3Class}
        ]};
        
        //the keypress event call the onTriggetClick function
        this.onTriggerClick = this.onTrigger1Click ;
        
        this.on("beforequery", function(e) {
        	if (this.storeIsLargerThenLimit()) {
        		// cancel button on the ReferenceSearchWindow triggers the field onBlur so
        		// assertValue, not finding a matching record, calls setValue with the rawValue
        		this.setRawValue("");
        		this.onTrigger1Click();
        		return false;
        	}
        	return true;
        }, this);
    },
    
    growSizeFix: function() {
    	if (this.storeIsLargerThenLimit()) {
    		// the dropdown list is never opened. it needs to grow
    		// only to fit the record selected on the popup window
    		return;
    	} else {
    		CMDBuild.Management.SearchableCombo.superclass.growSizeFix.call(this);
    	}   	
    },
    
    storeIsLargerThenLimit: function() {
    	if (this.store !== null) {
    		return this.store.getTotalCount() > CMDBuild.Config.cmdbuild.referencecombolimit;
    	}
    	return false;
    },
    
	getTrigger: Ext.form.TwinTriggerField.prototype.getTrigger,
	initTrigger: Ext.form.TwinTriggerField.prototype.initTrigger,
	trigger1Class: Ext.ux.form.XComboBox.prototype.triggerClass,
	trigger2Class: 'x-form-clear-trigger',
	trigger3Class: 'x-form-search-trigger',
	
	onTrigger1Click: function() {
    	//business rule: if the store has more record than the configuration limit
    	//we want open the search window
    	var _this = this; 
    	var manageTrigger = function() {
    		if (_this.storeIsLargerThenLimit()) {
        		_this.onTrigger3Click();
        	} else {
        		CMDBuild.Management.SearchableCombo.superclass.onTriggerClick.call(_this);
        	}
    	};
    	
    	if (this.store.isLoading) {
    		this.store.on('load', manageTrigger, this, {single: true});
    	} else {
    		manageTrigger();
    	}
    },
    
	onTrigger2Click: function(){
		if (!this.disabled) {
			this.clearValue();
			this.fireEvent('change', this, this.getValue(), this.startValue);
		}
	},
	
	onTrigger3Click: function(){
		this.createSearchWindow();
	},
	
	createSearchWindow: function(){
		if (!this.disabled && !this.searchWin) {
			var callback = this.buildSearchWindow.createDelegate(this, [this.store.baseParams], true);
			CMDBuild.Management.FieldManager.loadAttributes(this.store.baseParams.IdClass, callback);		
		}
	},
	
	buildSearchWindow: function(attributeList, baseParams) {
		if ( !this.filtered || (this.filtered && this.callParams) ) {
			//if the CQL params are all resolved
			this.searchWin = new CMDBuild.Management.ReferenceSearchWindow({
				filterType: 'reference',
				combo: this,
				params: this.callParams,
				className: this.hiddenName,
				idClass: this.store.baseParams.IdClass,
				attributes: attributeList
			});
			
			this.searchWin.on('cmdbuild-referencewindow-selected', function(record){
				this.addToStoreIfNotInIt(record);
				this.focus(); // to allow the "change" event that occurs on blur
				this.setValue(record.Id);
				this.fireEvent('cmdbuild-reference-selected', record, this);
			}, this);
			
			//to destroy the handler to
			this.searchWin.on('destroy',function() {
				delete(this.searchWin);
			}, this);
			
	    	this.searchWin.show();
		}
	},
	
	addToStoreIfNotInIt: function(record) {
		var _store = this.store;
		if (_store.find('Id', record.Id) == -1 ) {	
			var data = {};
			data[_store.root] = [{ 
					Id : record.Id, 
					Description: record.Description 
				}];
			data[_store.totalProperty] = _store.getTotalCount();
			_store.loadData(data, true);
		}
	},
	
	hideTrigger1 :false,
	hideTrigger2 :false,
	hideTrigger3 :false
});