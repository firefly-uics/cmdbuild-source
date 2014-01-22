(function() {
	var taskTypes = Ext.create('Ext.data.Store', {
	    fields: ['abbr', 'name'],
	    data : [
	        {"abbr":"", "name":""},
	        {"abbr":"mail", "name":"Mail"},
	        {"abbr":"event", "name":"Event"}
	    ]
	});	
	var imaps = Ext.create('Ext.data.Store', {
	    fields: ['abbr', 'name'],
	    data : [
	        {"abbr":"1", "name":"imap.gmail.com"},
	        {"abbr":"2", "name":"imap.googlemail.com"},
	        {"abbr":"3", "name":"imap.secureserver.org"}
	    ]
	});
	Ext.define("CMDBuild.view.administration.tasks.mail.CMMailStep1Delegate", {
		constructor: function(view) {
			this.view = view;
			this.view.delegate = this;
			this.filterWindow = null;
		},
		cmOn: function(name, param, callBack) {
			switch (name) {
				case "onFromAddress" :
					this.filterWindow = new CMDBuild.view.administration.tasks.mail.CMFilterWindow({
						title: "@@ Filter on FromAddress",
						type: "address"
					});
					this.filterWindow.delegate.parentDelegate = this;
					this.filterWindow.show();
					break;
				case "onSubject" :
					this.filterWindow = new CMDBuild.view.administration.tasks.mail.CMFilterWindow({
						title: "@@ Filter on Subject",
						type: "subject"
					});
					this.filterWindow.delegate.parentDelegate = this;
					this.filterWindow.show();
					break;
				case "onFilterWindowOk" :
					this.filterWindow.hide();
					break;
				case "onFilterWindowCancel" :
					this.filterWindow.hide();
					break;
				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		}
	});
	Ext.define("CMDBuild.view.administration.tasks.mail.CMMailStep1", {
		extend: "Ext.panel.Panel",
        title:'Personal Details',
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
                fieldLabel: '@@ IMAP',
                name: 'imap',
                xtype: 'combo',
                store: imaps,
                queryMode: 'local',
                displayField: 'name',
                valueField: 'abbr'
           },{
                fieldLabel: '@@ Polling frequency (minutes)',
                name: 'stepTime',
                width: CMDBuild.ADM_SMALL_FIELD_WIDTH
            }, {
                xtype: 'container',
                layout: 'hbox',
                items: [{
                    fieldLabel: '@@ From address filter',
                    name: 'fromAddressFilter',
                    xtype: 'textareafield',
                    itemId: 'fromAddressFilter',
                    cmImmutable: true
                     	
                }, {
                	xtype: 'button',
                    icon: 'images/icons/table.png',
                    considerAsFieldToDisable: true,
                    border: true,
                    margin: 2,
					handler: function() {
						me.delegate.cmOn("onFromAddress");
					}
                }]
            }, {
                xtype: 'container',
                layout: 'hbox',
                items: [{
                    fieldLabel: '@@ Subject filter',
                    name: 'subjectFilter',
                    xtype: 'textareafield',
                    itemId: 'subjectFilter',
                    cmImmutable: true
                                         	
                }, {
                    xtype: 'button',
                    icon: 'images/icons/table.png',
                    considerAsFieldToDisable: true,
                    border: true,
                    margin: 2,
					handler: function() {
						me.delegate.cmOn("onSubject");
					}
                }]
	        }];
        	this.delegate = new CMDBuild.view.administration.tasks.mail.CMMailStep1Delegate(this);
        	this.callParent(arguments);
        }
	});
})();
