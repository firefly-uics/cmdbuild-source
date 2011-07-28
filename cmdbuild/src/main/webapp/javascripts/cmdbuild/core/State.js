CMDBuild.State = (function() {
	var lastClassSelected = undefined;
	var lastCardSelected = undefined;
	
	var newCard = function() {
		lastCardSelected = undefined;
	};
	
	var State = function() {
		this.subscribe('cmdb-init-class', this.onSelectClass, this);
	    this.subscribe('cmdb-load-card', this.onLoadCard, this);
	    this.subscribe('cmdb-new-card', newCard, this);
	};
	
    Ext.extend(State, Ext.util.Observable, {
    	onSelectClass: function(p) {
    		lastClassSelected = p;
    		lastCardSelected = undefined;
    	},
    	onLoadCard: function(p) {
    		lastCardSelected = p;
    		lastClassSelected.cardId = p.record.data.Id;
    	},
    	getLastClassSelected: function() {
    		return lastClassSelected;
    	},
    	getLastCardSelected: function() {
    		return lastCardSelected;
    	},
    	getLastClassSelectedId: function() {
    		if (lastClassSelected) {
    			return lastClassSelected.classId;
    		}
    		return undefined;
    	},
    	getLastCardSelectedId: function() {
    		if (lastCardSelected) {
    			return lastCardSelected.record.data.Id;
    		}
    		return undefined;
    	}
    });
    
    return new State();
})();


