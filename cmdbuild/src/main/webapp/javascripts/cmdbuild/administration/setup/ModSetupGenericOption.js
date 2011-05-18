CMDBuild.Administration.ModSetupGenericOption = Ext.extend(CMDBuild.Administration.TemplateModSetup, {
	id : 'modsetupgenericoption',
	configFileName: 'cmdbuild',
	modtype: 'modsetupcmdbuild',
	translation: CMDBuild.Translation.administration.setup.cmdbuild,
	
	initComponent: function() {
		this.instanceNameField = new Ext.form.TextField({
			fieldLabel: this.translation.instancename,
			name: 'instance_name',
			allowBlank: true
		});
	
		Ext.apply(this, {
			title: this.translation.title,
			formItems: [{
				xtype: 'fieldset',
				title: this.translation.fieldsetgeneraltitle,
				autoHeight: true,
				items: [this.instanceNameField,{
				    fieldLabel: this.translation.startingClass,
				    xtype: 'xcombo',
				    name: 'startingclass_value',
				    hiddenName: 'startingclass',
				    valueField: 'name',
				    displayField: 'description',
				    minChars : 0,
				   	grow: true,
			    	triggerAction: 'all',
			    	store: CMDBuild.Cache.getClassesAndProcessAsStoreWithEmptyOption(),
			    	mode: "local"
				},{
					fieldLabel: this.translation.rowlimit,
					xtype: 'numberfield',
					name: 'rowlimit',
					allowBlank: false
				},{
					fieldLabel:  this.translation.referencecombolimit,
					xtype: 'numberfield',
					name: 'referencecombolimit',
					allowBlank: false
				},{
					fieldLabel:  this.translation.relationlimit,
					xtype: 'numberfield',
					name: 'relationlimit',
					allowBlank: false
				},{
					fieldLabel:   this.translation.cardpanelheight,
					xtype: 'numberfield',
					name: 'grid_card_ratio',
					allowBlank: false,
					maxValue: 100,
					minValue: 0
				},{
					fieldLabel:   this.translation.sessiontimeout,
					xtype: 'numberfield',
					name: 'session.timeout',
					allowBlank: true,
					minValue: 0
				}]
			},{
				xtype: 'fieldset',
				title: this.translation.fieldsetlanguageltitle,
				autoHeight: true,
				items: [{
				    fieldLabel: this.translation.language,
				    xtype: 'xcombo',
				    name: 'language_value',
				    hiddenName: 'language',
				    valueField: 'name',
				    displayField: 'value',
				   	grow: true,
			    	triggerAction: 'all',
				    minChars : 0,				  
				    store: {
				        xtype: "jsonstore",
				        autoLoad: true,
				        remoteSort: true,
				        url: 'services/json/utils/listavailabletranslations',
				        root: "translations",
				        fields: ['name', 'value']
				    }
				},{
				    fieldLabel: this.translation.languagePrompt,
				    xtype: 'xcheckbox',
				    name: 'languageprompt'
				}]
			},{
				xtype: 'fieldset',
				title: this.translation.fieldsetpopupwindowtitle,
				autoHeight: true,
				items: [{
					fieldLabel: this.translation.popupheightlabel,
				    xtype: 'numberfield',
				    name: 'popuppercentageheight',
				    maxValue: 100,
				    allowBlank: false
				},{
				  	fieldLabel: this.translation.popupwidthlabel,
				    xtype: 'numberfield',
				    name: 'popuppercentagewidth',
				    maxValue:100,
				    allowBlank: false
				}]
			}]
		});
		CMDBuild.Administration.ModSetupGenericOption.superclass.initComponent.apply(this, arguments);
    },
    afterSubmit: function() {
    	var hdInstanceName = Ext.get('instance_name');
    	hdInstanceName.dom.innerHTML = this.instanceNameField.getValue();
	}
});