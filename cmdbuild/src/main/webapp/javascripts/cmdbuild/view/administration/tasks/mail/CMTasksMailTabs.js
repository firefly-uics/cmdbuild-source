(function() {

		Ext.define("CMDBuild.view.administration.tasks.CMTasksPanelFormStep1", {
			extend: "Ext.panel.Panel",
                title:'Personal Details',
                defaultType: 'textfield',
				border: false,
				bodyCls: 'cmgraypanel',
		        height: "100%",
		        defaults: {
                    anchor: '100%'
                },
                items: [{
                    fieldLabel: '@@ Id',
                    name: 'id',
                    hidden:true,
                },{
                    fieldLabel: '@@ Task type',
                    name: 'type',
                    allowBlank:false,
                    enable: false
                },{
                    fieldLabel: '@@ IMAP',
                    name: 'imap',
                },{
                    fieldLabel: '@@ Polling frequency (minutes)',
                    name: 'stepTime',
                }, {
                    fieldLabel: '@@ From address filter',
                    name: 'fromAddressFilter',
                }, {
                    fieldLabel: '@@ Subject filter',
                    name: 'subjectFilter',
                }]
		});
		Ext.define("CMDBuild.view.administration.tasks.CMTasksPanelFormStep2", {
			extend: "Ext.panel.Panel",
            title:'Phone Numbers',
            defaultType: 'textfield',
            border: false,
			bodyCls: 'cmgraypanel',
	        height: "100%",
            defaults: {
                anchor: '100%'
            },
            items: [{
                fieldLabel: '@@ Body parsing',
                name: 'bodyParsing',
            },{
                fieldLabel: '@@ Send mail',
                name: 'sendMail'
            },{
                fieldLabel: '@@ Save attachments to Alfresco',
                name: 'saveToAlfresco'
            }]
		});
		Ext.define("CMDBuild.view.administration.tasks.CMTasksPanelFormStep3", {
			extend: "Ext.panel.Panel",
            title:'Phone Numbers',
            defaultType: 'textfield',
            border: false,
			bodyCls: 'cmgraypanel',
	        height: "100%",
            defaults: {
                anchor: '100%'
            },
            items: [{
                fieldLabel: '@@ Start workflow',
                name: 'workflow',
            }]
		});
		Ext.define("CMDBuild.view.administration.tasks.mail.CMTasksMailTabs", {
			constructor: function() {
				this.step1 = new CMDBuild.view.administration.tasks.CMTasksPanelFormStep1();
				this.step2 = new CMDBuild.view.administration.tasks.CMTasksPanelFormStep2();
				this.step3 = new CMDBuild.view.administration.tasks.CMTasksPanelFormStep3();
			},
			getTabs: function() {
				return [step1, step2, step3];
			}
		});
})();