MVCModel = function() {
	var store = new Ext.data.SimpleStore({
		fields: ["number"],
		data: [[1], [2]]
	});
	
	this.getNumberStore = function() {
		return store;
	};
	
	MVCModel.superclass.constructor.call(this);
}
Ext.extend(MVCModel, Ext.util.Observable);