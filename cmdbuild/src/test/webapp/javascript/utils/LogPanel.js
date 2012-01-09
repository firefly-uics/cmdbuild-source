Ext.define("LogPanel", {
	extend : "Ext.panel.Panel",
	initComponent : function() {
		this.tbar = [ {
			text : "reset",
			scope : this,
			handler : function() {
				this.reset();
			}
		} ];
		this.callParent(arguments);
	},
	reset : function() {
		this.removeAll();
	},
	log : function(text) {
		this.insert(0, new Ext.panel.Panel({
			html : "** " + ": " + text,
			padding : "5px",
			border : false
		}));
	},
	bindObject: function(prefix, o) {
		var me = this;
		for (var prop in o) {
			if (typeof o[prop] == "function") {
				var cb = function () {
					var argsList = "<ul>";
					for (var i=0, l=arguments.length, arg=null, argString=null; i<l; ++i) {
						arg = arguments[i];
						argString = "<li>" + (typeof arg == "object" ? Ext.encode(arg) : arg) + "</li>"; 
						argsList += argString;
					}
					argsList += "</ul>";
					me.log(prefix + ": Called " + this._name + " with params " + argsList);
				};
				cb._name = prop;
				o[prop] = Ext.Function.createInterceptor(o[prop], cb, cb);
			}
		}
	}
});