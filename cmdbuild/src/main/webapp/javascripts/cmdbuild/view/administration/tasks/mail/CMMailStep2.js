(function() {
	var templates = Ext.create('Ext.data.Store', {
	    fields: ['abbr', 'name'],
	    data : [
	        {"abbr":"1", "name":"template 1"},
	        {"abbr":"2", "name":"template 2"},
	        {"abbr":"3", "name":"template 3"}
	    ]
	});

	Ext.define("CMDBuild.view.administration.tasks.mail.CMMailStep2Delegate", {
		constructor: function(view) {
			this.view = view;
			this.view.delegate = this;
		},
		cmOn: function(name, param, callBack) {
			switch (name) {
				case "onBodyParsingChecked" :
					showComponent(this.view, 'keyValues', param.checked);
					return showComponent(this.view, 'valueValues', param.checked);
				case "onMailChecked" :
					return showComponent(this.view, 'template', param.checked);
				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		}
	});
	Ext.define("CMDBuild.view.administration.tasks.mail.CMMailStep2", {
		extend: "Ext.panel.Panel",
        title:'Phone Numbers',
        defaultType: 'textfield',
        border: false,
		bodyCls: 'cmgraypanel',
        height: "100%",
        defaults: {
            anchor: '100%'
        },
        initComponent: function() {
        	var me = this;
            this.items = [{
                fieldLabel: '@@ Body parsing',
                name: 'bodyParsing',
                xtype: 'checkbox',
                listeners: {
                	change: function(that, newValue, oldValue, eOpts) {
                		me.delegate.cmOn("onBodyParsingChecked", {'checked': newValue});
                	}
                }
            },{
                xtype: 'container',
                layout: 'hbox',
                itemId : 'keyValues',
                hidden: true,
                items: [{
                    fieldLabel: '@@ Key init',
                    name: 'keyInit',
                    xtype: 'textfield'
                }, {
                    fieldLabel: '@@ Key end',
                    name: 'keyEnd',
                    margin: '0 0 0 20',
                    xtype: 'textfield'
                }]
            	
            },{
                xtype: 'container',
                layout: 'hbox',
                itemId : 'valueValues',
                margin: '10 0 10 0',
                hidden: true,
                items: [{
                    fieldLabel: '@@ Value init',
                    name: 'valueInit',
                    xtype: 'textfield'
                }, {
                    fieldLabel: '@@ Value end',
                    name: 'valueEnd',
                    margin: '0 0 0 20',
                    xtype: 'textfield'
                }]
            	
            },{
                fieldLabel: '@@ Send mail',
                name: 'sendMail',
                xtype: 'checkbox',
                listeners: {
                	change: function(that, newValue, oldValue, eOpts) {
                		me.delegate.cmOn("onMailChecked", {'checked': newValue});
                	}
                }
            },{
                fieldLabel: '@@ Template',
                itemId: 'template',
                name: 'template',
                xtype: 'combo',
                store: templates,
                queryMode: 'local',
                displayField: 'name',
                valueField: 'abbr',
                hidden: true
            },{
                fieldLabel: '@@ Save attachments to Alfresco',
                name: 'saveToAlfresco',
                xtype: 'checkbox'
            }];
        	this.delegate = new CMDBuild.view.administration.tasks.mail.CMMailStep2Delegate(this);
        	this.callParent(arguments);
        }
	});
	function showComponent(view, fieldName, showing) {
		var component = view.query("#" + fieldName);
		if (showing) {
			component[0].show();
		}
		else {
			component[0].hide();
		}
	}
})();
