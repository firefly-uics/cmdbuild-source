CMDBuild.XRowExpander = function(config){
    Ext.apply(this, config);
	
	if (this.genBodyContent && !this.tpl)
    	this.tpl = 'notused';

    CMDBuild.XRowExpander.superclass.constructor.call(this);
};

Ext.extend(CMDBuild.XRowExpander, Ext.grid.RowExpander, {
    getBodyContent : function(record, index){
    	if (this.genBodyContent)
    		return this.genBodyContent(record, index);
    	else
    		return CMDBuild.XRowExpander.superclass.getBodyContent.call(record, index);
    }
});
