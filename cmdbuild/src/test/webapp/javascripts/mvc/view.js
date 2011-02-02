MVCView = function(model) {
	this.model = model;
	MVCView.superclass.constructor.call(this);
};

Ext.extend(MVCView, Ext.Viewport, {
	initComponent : function() {
		var TITLE_TEMPLATE = "Main Panel ({0})";	
		var changeButton = new Ext.Button({
			text: "Change"
		});
		var currentSelectionButton = new Ext.Button({
			text: "CurrentSelection"
		});
		
		var mainPanel = new Ext.grid.GridPanel({
			title: "Main Panel",
			region: "center",
			viewConfig: {
		        forceFit: true
	        },
			columns:[{
				header: "Number",
				sortable: false,
				dataIndex: "number" // TODO get it from the model?
			}],
			store: this.model.getNumberStore(),
			tbar: [changeButton, currentSelectionButton]
		});
		
		var logPanel = new Ext.Panel({
			title: "Logger Panel",
			region: "south",
			height: 200,
			split: true
		});
		
		this.layout = "border";
		this.items = [mainPanel, logPanel];
		
		MVCView.superclass.initComponent.call(this);
	}
});