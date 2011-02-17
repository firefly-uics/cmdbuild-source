LoggerPanel = Ext.extend(Ext.grid.GridPanel, {
	initComponent: function() {		
		this.title = "Logger Panel";
		this.autoScroll = true;
		
		this.expander = new CMDBuild.XRowExpander({
			genBodyContent : function(record, index){
				var body = "<h1>Parametri:</h1>";
				for (var parameter in this.params) {
					body += '<p><b>'+parameter+'</b>: '+this.params[parameter]+'</p>';
				}
				return body;
			}
	    });
		
		this.plugins = this.expander;
		
		this.columns = [
		    this.expander,
		  {
	    	header : "Time",
		    dataIndex : "time",
		    width: 50
	      },{
	        header : "Event",
	        dataIndex : "event",
	        width: 50
	      }
		];
		this.store = new Ext.data.SimpleStore({
	        fields: ["event", "time"]
		});
		this.viewConfig = {forceFit:true};		
		LoggerPanel.superclass.initComponent.apply(this, arguments);
	},
	log: function(event, params) {
		var now = new Date();
		var r = new this.store.recordType({
			"event": event,			
			"time": now.getHours()+":"+now.getMinutes()+":"+now.getSeconds()
		});
		this.store.insert(0,r);
		this.expander.params = params;
	}
});