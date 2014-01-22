(function() {

	Ext.define("CMDBuild.view.administration.tasks.mail.CMFilterWindowDelegate", {
		constructor: function(view) {
			this.view = view;
			this.view.delegate = this;
		},
		cmOn: function(name, param, callBack) {
			switch (name) {
				case "onAddFilter" :
					alert(name);
				break;
				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		}
	});
	Ext.define("CMDBuild.view.administration.tasks.mail.CMFilterWindow", {
		extend: "Ext.window.Window",
		modal: true,
		// configuration
		delegate: undefined,
		title: undefined,
		initComponent: function() {
			this.autoScroll = true;
			this.width = 400;
			this.height = 300;
			var me = this;
			this.tbar = [{
				iconCls: 'add',
				type: 'button', 
				text: "@@ Add filter",
				handler : function() {
					me.delegate.cmOn("onAddFilter");
				}
			}];
			this.fbar = [{
				xtype: 'tbspacer',
				flex : 1
			},
			{
				type: 'button', 
				text: CMDBuild.Translation.common.btns.confirm,
				handler : function() {
					me.delegate.cmOn("onFilterWindowOk");
				}
			},
			{
				type: 'button', 
				text: CMDBuild.Translation.common.btns.abort,
				handler : function() {
					me.delegate.cmOn("onFilterWindowCancel");
				}
			},
			{
				xtype: 'tbspacer',
				flex : 1
			}];
			this.items = [{
                xtype: 'container',
                layout: 'hbox',
                items: [{
    				xtype: 'textfield',
                    itemId: 'filter',
                    flex:1
	    		},{
					iconCls: 'delete',
					xtype: 'button',
					width: '22px',
					handler : function() {
						me.delegate.cmOn("onFilterAdd");
					}
	    		}]
			}];
        	this.delegate = new CMDBuild.view.administration.tasks.mail.CMFilterWindowDelegate(this);
			this.callParent(arguments);
		}
	});
})();