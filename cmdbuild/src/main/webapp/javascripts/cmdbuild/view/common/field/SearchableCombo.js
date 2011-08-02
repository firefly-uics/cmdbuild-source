Ext.define("CMDBuild.Management.SearchableCombo", {
	extend: "Ext.form.field.ComboBox",

	trigger1Cls: Ext.baseCSSPrefix + 'form-arrow-trigger',
    trigger2Cls: Ext.baseCSSPrefix + 'form-clear-trigger',    
    trigger3Cls: Ext.baseCSSPrefix + 'form-search-trigger',


	initComponent : function(){
		this.labelAlign = "right",
		this.callParent(arguments);
/*        
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
*/
    },
    
    onTrigger1Click: function() {
    	//business rule: if the store has more record than the configuration limit
    	//we want open the search window

    	if (this.store.isLoading()) {
    		this.store.on('load', manageTrigger, this, {single: true});
    	} else {
    		manageTrigger.call(this);
    	}
    	
    	function manageTrigger() {
    		if (this.storeIsLargerThenLimit()) {
        		this.onTrigger3Click();
        	} else {
        		CMDBuild.Management.SearchableCombo.superclass.onTriggerClick.call(this);
        	}
    	};
    },
    
	onTrigger2Click: function() {
		if (!this.disabled) {
			this.setValue([""]); // if use clearValue the form does not send the value, so it is not possible delete the value on server side
			this.fireEvent("clear");
			this.fireEvent('change', this, this.getValue(), this.startValue);
		}
	},
	
	onTrigger3Click: function(){
		this.createSearchWindow();
	},

    
    growSizeFix: function() {
/* TODO 3 to 4
		if (this.storeIsLargerThenLimit()) {
    		// the dropdown list is never opened. it needs to grow
    		// only to fit the record selected on the popup window
    		return;
    	} else {
    		CMDBuild.Management.SearchableCombo.superclass.growSizeFix.call(this);
    	}     	
*/
    },
    
    storeIsLargerThenLimit: function() {
    	if (this.store !== null) {
    		return this.store.getTotalCount() > CMDBuild.Config.cmdbuild.referencecombolimit;
    	}
    	return false;
    },
    	
	createSearchWindow: function() {
		if (!this.disabled && !this.searchWin) {
			var callback = Ext.Function.bind(this.buildSearchWindow,this, [this.store.baseParams], true);

			CMDBuild.Management.FieldManager.loadAttributes(this.store.baseParams.IdClass, callback);	
		}
	},
	
	buildSearchWindow: function(attributeList, baseParams) {
		this.searchWin = new CMDBuild.Management.ReferenceSearchWindow({
			idClass: this.store.baseParams.IdClass,
			filterType: 'reference'
		}).show().on('cmdbuild-referencewindow-selected', function(record) {
			this.addToStoreIfNotInIt(record);
			this.focus(); // to allow the "change" event that occurs on blur
			this.setValue(record.get("Id"));
			this.fireEvent('cmdbuild-reference-selected', record, this);
		}, this);

//		if ( !this.filtered || (this.filtered && this.callParams) ) {
			//if the CQL params are all resolved
//			this.searchWin = new CMDBuild.Management.ReferenceSearchWindow({
//				filterType: 'reference',
//				combo: this,
//				params: this.callParams,
//				className: this.hiddenName,
//				idClass: this.store.baseParams.IdClass,
//				attributes: attributeList
//			});
//			

//			
//			//to destroy the handler to
//			this.searchWin.on('destroy',function() {
//				delete(this.searchWin);
//			}, this);
//			
//	    	this.searchWin.show();
//		}
		
		
	},
	
	addToStoreIfNotInIt: function(record) {
		var _store = this.store;
		if (_store.find('Id', record.Id) == -1 ) {
			_store.add({
				Id : record.Id, 
				Description: this.recordDescriptionFixedForCarriageReturnBugOnComboBoxes(record)
			});
		}
	},
	
	recordDescriptionFixedForCarriageReturnBugOnComboBoxes: function(record) {
		return record.get("Description").replace(/\n/g," ");
	},
	
	hideTrigger1 :false,
	hideTrigger2 :false,
	hideTrigger3 :false
});