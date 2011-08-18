Ext.define("CMDBuild.Management.SearchableCombo", {
	extend: "CMDBuild.field.CMBaseCombo",

	trigger1Cls: Ext.baseCSSPrefix + 'form-arrow-trigger',
    trigger2Cls: Ext.baseCSSPrefix + 'form-clear-trigger',    
    trigger3Cls: Ext.baseCSSPrefix + 'form-search-trigger',


	initComponent : function(){
		this.labelAlign = "right",
		this.callParent(arguments);
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
    		return this.store.getTotalCount() > parseInt(CMDBuild.Config.cmdbuild.referencecombolimit);
    	}
    	return false;
    },
    	
	createSearchWindow: function() {
		if (!this.disabled) {
			var callback = Ext.Function.bind(this.buildSearchWindow,this, [this.store.baseParams], true);

			CMDBuild.Management.FieldManager.loadAttributes(this.store.baseParams.IdClass, callback);	
		}
	},
	
	buildSearchWindow: function(attributeList, extraParams) {
		new CMDBuild.Management.ReferenceSearchWindow({
			idClass: this.store.baseParams.IdClass,
			filterType: 'reference',
			extraParams: extraParams
		}).show().on('cmdbuild-referencewindow-selected', function(record) {
			this.addToStoreIfNotInIt(record);
			this.focus(); // to allow the "change" event that occurs on blur
			this.setValue(record.get("Id"));
			this.fireEvent('cmdbuild-reference-selected', record, this);
		}, this);
	},
	
	addToStoreIfNotInIt: function(record) {
		var _store = this.store,
			id = record.get("Id");

		if (_store.find('Id', id) == -1 ) {
			_store.add({
				Id : id,
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