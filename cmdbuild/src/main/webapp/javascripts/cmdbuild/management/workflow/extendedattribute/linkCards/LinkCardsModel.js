CMDBuild.Management.LinkCardsModel = Ext.extend(Ext.util.Observable, {
	constructor: function(config) {
		config = config || {};
        this.selections = {};
        this.singleSelect = config.singleSelect;
        
        this.addEvents({
            "select" : true,
            "deselect" : true
        });
        
        CMDBuild.Management.LinkCardsModel.superclass.constructor.call(this, config);
    },
    
    select: function(selection) {
    	_debug("LinkCardsModel - select", selection);
    	if (this.isSelected(selection)) {
    		return;
    	} else {
	    	if (this.singleSelect) {
				this.reset();
	    	}
	    	this.selections[selection] = true;
	    	this.fireEvent("select", selection);
    	}
    },
    
    deselect: function(selection) {    	
    	_debug("LinkCardsModel - deselect", selection);
    	if (this.isSelected(selection)) {
    		delete this.selections[selection];
    		this.fireEvent("deselect", selection);
    	}
    },
    
    getSelections: function() {
    	var selections = [];
    	for (var selection in this.selections) {
    		selections.push(selection);
		}
    	return selections;
    },
    
    isSelected: function(selection) {
		return this.selections[selection];
	},
	
	reset: function() {
		for (var selection in this.selections) {
			this.deselect(selection);
		}
	},
	
	hasSelection: function() {
		return this.getSelections().length > 0;
	},
	
	length: function() {
		return this.getSelections().length;
	}
});