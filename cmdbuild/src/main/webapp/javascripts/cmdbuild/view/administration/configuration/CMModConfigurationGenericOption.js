(function() {
	
var tr = CMDBuild.Translation.administration.setup.cmdbuild;

Ext.define("CMDBuild.view.administration.configuration.CMModConfigurationGenericOption", {
	extend: "CMDBuild.view.administration.configuration.CMBaseModConfiguration",
	
	alias: "widget.configuregenericoptions",

	configFileName: 'cmdbuild',
	
	constructor: function() {
		this.title = tr.title;
		this.instanceNameField = new Ext.form.CMTranslatableText({
			fieldLabel: tr.instancename,
			name: 'instance_name',
			allowBlank: true,
			// this configuration is on the parent but for this special field
			// is repeated here
			labelAlign: 'left',
			labelWidth: CMDBuild.CFG_LABEL_WIDTH,
			width: CMDBuild.CFG_MEDIUM_FIELD_WIDTH,
			// end of duplicate configuration
			translationsKeyType: "InstanceName"
		});
		
		var startingClass = new CMDBuild.field.ErasableCombo({
			fieldLabel : tr.startingClass,
			name : 'startingclass',
			valueField : 'id',
			displayField : 'description',
			editable : false,
			store : _CMCache.getClassesAndProcessesAndDahboardsStore(),
			queryMode : 'local'
		});
		this.enabledLanguages = Ext.create("CMDBuild.view.administration.configuration.CMModConfigurationTranslations", {
			
		});
		this.items = [
			{
			xtype: 'fieldset',
			title: tr.fieldsetgeneraltitle,
			items: [
				this.instanceNameField
				,startingClass
			,{
				fieldLabel: tr.rowlimit,
				xtype: 'numberfield',
				name: 'rowlimit',
				allowBlank: false
			},{
				fieldLabel: tr.referencecombolimit,
				xtype: 'numberfield',
				name: 'referencecombolimit',
				allowBlank: false
			},{
				fieldLabel: tr.relationlimit,
				xtype: 'numberfield',
				name: 'relationlimit',
				allowBlank: false
			},{
				fieldLabel: tr.cardpanelheight,
				xtype: 'numberfield',
				name: 'grid_card_ratio',
				allowBlank: false,
				maxValue: 100,
				minValue: 0
			},{
				fieldLabel: tr.tabs_position.label,
				xtype: 'combobox',
				name: 'card_tab_position',
				allowBlank: false,
				displayField: "description",
				valueField: "value",
				store: new Ext.data.Store({
					fields: ["value", "description"],
					data: [{value: "top", description: tr.tabs_position.top}, {value: "bottom", description: tr.tabs_position.bottom}]
				})
			},{
				fieldLabel: tr.sessiontimeout,
				xtype: 'numberfield',
				name: 'session.timeout',
				allowBlank: true,
				minValue: 0
			}]
		},{
			xtype: 'fieldset',
			title: tr.fieldsetpopupwindowtitle,
			items: [{
				fieldLabel: tr.popupheightlabel,
			    xtype: 'numberfield',
			    name: 'popuppercentageheight',
			    maxValue: 100,
			    allowBlank: false
			},{
			  	fieldLabel: tr.popupwidthlabel,
			    xtype: 'numberfield',
			    name: 'popuppercentagewidth',
			    maxValue:100,
			    allowBlank: false
			}]
		},{
			xtype: 'fieldset',
			title : tr.fieldsetlanguageltitle,
			items : [ {
				fieldLabel : tr.language,
				xtype : 'xcombo',
				name : 'language',
				hiddenName : 'language',
				valueField : 'name',
				displayField : 'value',
				grow : true,
				triggerAction : 'all',
				minChars : 0,
				store : new Ext.data.Store( {
					model : 'TranslationModel',
					proxy : {
						type : 'ajax',
						url : 'services/json/utils/listavailabletranslations',
						reader : {
							type : 'json',
							root : 'translations'
						}
					},
					autoLoad : true
				})
			}, {
				fieldLabel : tr.languagePrompt,
				xtype : 'xcheckbox',
				name : 'languageprompt'
			}]
		},{
			xtype: 'fieldset',
			title : CMDBuild.Translation.translations_enabled,
			items : [this.enabledLanguages]
		},{
			xtype: 'fieldset',
			title : CMDBuild.Translation.lock_cards_in_edit,
			items: [{
				fieldLabel: CMDBuild.Translation.enabled,
				xtype: 'xcheckbox',
				name: 'lockcardenabled'
			}, {
				fieldLabel: CMDBuild.Translation.show_name_of_locker_user,
				xtype: 'xcheckbox',
				name: 'lockcarduservisible'
			}, {
				fieldLabel: CMDBuild.Translation.lock_timeout,
				xtype: 'numberfield',
				name: "lockcardtimeout"
			}]
		}];

		this.callParent(arguments);
	},
	
	//override
	getValues: function() {
		var values = this.callParent(arguments);
		var languages = this.enabledLanguages.getValues();
		values.enabled_languages = languages;
		return values;
	},
	
	//override
	populateForm: function(configurationOptions) {
		this.callParent(arguments);
	},

	afterSubmit: function() {
		var hdInstanceName = Ext.get('instance_name');
		hdInstanceName.dom.innerHTML = this.instanceNameField.getValue();
	}

});
Ext.define("CMDBuild.view.administration.configuration.CMTranslatableCheck", {
	extend: "Ext.container.Container",
	layout: "hbox",
	padding: "0 0 0 5",
	width: 200,
	name : 'no name',
	allowBlank : false,
	vtype : '',
	setValue: function(value) {
		this.check.setValue(value);
	},
	getValue: function() {
		return this.check.getValue();
	},
	initComponent : function() {
		var me = this;
		this.check = new Ext.form.field.Checkbox( {
			fieldLabel : me.language,
			labelWidth: CMDBuild.LABEL_WIDTH,
			name : me.name,
			submitValue: false
		});
		this.width += 22;
		this.translationsButton = new Ext.form.field.Display( {
			iconCls: me.image,
			renderer : function(){
			    return '<div style="background-repeat:no-repeat;background-position:center;" class="' + me.image + '">&#160;</div>';
			},
			width: 22
		});
		this.items = [this.translationsButton, this.check];
		this.callParent(arguments);
	}
});
Ext.define("CMDBuild.view.administration.configuration.CMRowConfigurationTranslations", {
	extend: "Ext.container.Container",
	layout: "hbox",
	padding: "0 0 0 5",
	field1: undefined,
	field2: undefined,
	field3: undefined,
	initComponent: function() {
		this.items = [];
		if (this.field1) {
			this.items.push(this.field1);
		}
		if (this.field2) {
			this.items.push(this.field2);
		}
		if (this.field3) {
			this.items.push(this.field3);
		}
		this.callParent(arguments);
	}
});
Ext.define("CMDBuild.view.administration.configuration.CMModConfigurationTranslations", {
	extend: "Ext.form.Panel",
	configFileName: 'translations',
//	cls: "x-panel-body-default-framed",
	border: 0,
	bodyCls: 'cmgraypanel',
	languages: [],
	constructor: function() {
		var me = this;
		me.callParent(arguments);
		this.languageItems = [];
		_CMCache.registerOnTranslations(this);
		CMDBuild.ServiceProxy.translations.readAvailableTranslations({
			success : function(response, options, decoded) {
				var column = 0;
				var arColumns = [];
				for (key in decoded.translations) {
					column++;
					var item = Ext.create("CMDBuild.view.administration.configuration.CMTranslatableCheck", {
							name: decoded.translations[key].name,
							image: "ux-flag-" + decoded.translations[key].name,
							language: decoded.translations[key].value,
							submitValue: false
						});
					me.languageItems.push(item);
					
					arColumns.push(item);
					if (column == 3) {
						me.add(getLanguagesRow(arColumns));
						arColumns = [];
						column = 0;
					}
				}
				if (column > 0) {
					me.add(getLanguagesRow(arColumns));
				}
			}
		});
	},
	getValues: function() {
		var languages = "";
		var first = true;
		for (key in this.languageItems) {
			var l = this.languageItems[key];
			if (l.getValue()) {
				languages += ((first) ? "" : ", ") + l.name;
				first = false;
			}
		}
		return languages;
	},

	setValues: function(activeLanguages) {
		for (key in this.languageItems) {
			var l = this.languageItems[key];
			l.setValue(inActiveLanguages(l, activeLanguages));
		}
	},

	resetLanguages: function() {
		var activeLanguages = _CMCache.getActiveTranslations();
		this.setValues(activeLanguages);
	}
});
function getLanguagesRow(arColumns) {
	var row = Ext.create("CMDBuild.view.administration.configuration.CMRowConfigurationTranslations", {
		field1: arColumns[0],
		field2: arColumns[1],
		field3: arColumns[2],
	});
	return row;
}
function inActiveLanguages(language, activeLanguages) {
	for (var i = 0; i < activeLanguages.length; i++) {
		if (language.name == activeLanguages[i].name) {
			return true;
		}
	}
	return false;
}
})();