(function() {
	Ext.define("CMDBuild.view.common.CMTranslationsWindow.CMTranslatableText", {
			extend: "Ext.container.Container",
		layout: "hbox",
		padding: "0 0 5 5",
		width: "100%",
		name : 'no name',
		allowBlank : false,
		setValue: function(value) {
			this.text.setValue(value);
		},
		getValue: function() {
			return this.text.getValue();
		},
		initComponent : function() {
			var me = this;
			this.text = this.buildTextField();
			this.width += 22;
			this.translationsButton = new Ext.form.field.Display( {
				iconCls: me.image,
				renderer : function(){
				    return '<div style="background-repeat:no-repeat;background-position:center;" class="' + me.image + '">&#160;</div>';
				},
				width: 22
			});
			this.items = [this.translationsButton, this.text];
			this.callParent(arguments);
		},
		buildTextField: function() {
			var text = new Ext.form.field.Text( {
				padding: "0 0 0 5",
				fieldLabel : this.language,
				labelWidth: CMDBuild.LABEL_WIDTH,
				flex: 1,
				name : this.name,
				allowBlank : false,
			});
			return text;
		}
	});
	Ext.define("CMDBuild.view.common.CMTranslationsWindow.CMTranslatableTextArea", {
		extend: "CMDBuild.view.common.CMTranslationsWindow.CMTranslatableText",
		buildTextField: function() {
			var text = new Ext.form.field.TextArea( {
				padding: "0 0 0 5",
				fieldLabel : this.language,
				labelWidth: CMDBuild.LABEL_WIDTH,
				flex: 1,
				name : this.name,
				allowBlank : false,
			});
			return text;
		}
	});
	Ext.define('CMDBuild.view.common.CMTranslationsWindowDelegate', {

		parentDelegate: undefined,
		view: undefined,

		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onFilterWindowConfirm':
					var values = this.view.form.getValues();
					var oldValues = this.view.form.getOldValues();//control for create, delete or update values
					var translationsKeyType = this.view.translationsKeyType;
					var translationsKeyName = this.view.translationsKeyName;
					var translationsKeySubName = this.view.translationsKeySubName;
					var translationsKeyField = this.view.translationsKeyField;
					_CMCache.createTranslations(translationsKeyType, translationsKeyName, translationsKeySubName, translationsKeyField, values, oldValues);
					this.view.destroy();
					break;
				case 'onFilterWindowAbort':
					this.view.destroy();
					break;

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

	});
	Ext.define("CMDBuild.view.common.CMTranslationsWindow.CMTranslatableForm", {
		extend: "Ext.form.Panel",
		border: 0,
		bodyCls: 'cmgraypanel',
		height: "100%",
		textArea: false,
		oldValues: {},
		buildWindowItem: function(translations) {
			var activeTranslations = _CMCache.getActiveTranslations();
			var componentType = (! this.textArea) ?
					"CMDBuild.view.common.CMTranslationsWindow.CMTranslatableText" :
					"CMDBuild.view.common.CMTranslationsWindow.CMTranslatableTextArea";
			for (var i = 0; i < activeTranslations.length; i++) {
				var at = activeTranslations[i];
				var item = Ext.create(componentType, {
					name: at.name,
					image: at.image,
					language: at.language
				});
				this.add(item);
				item.setValue(translations[at.name]);
			}
		},
		getOldValues: function() {
			return this.oldValues;
		},
		initComponent : function() {
			this.callParent(arguments);
			var me = this;
			_CMCache.readTranslations(this.translationsKeyType, this.translationsKeyName, this.translationsKeySubName, 
					this.translationsKeyField, function(a, b, response) {
				me.oldValues = response.response;
				me.buildWindowItem(response.response);
			});
		}
	});
	Ext.define('CMDBuild.view.common.CMTranslationsWindow', {
		extend: 'Ext.window.Window',

		bodyCls: 'cmgraypanel',
		autoScroll: true,
		content: undefined,
		delegate: undefined,
		height: 300,
		modal: true,
		title: undefined,
		type: undefined,
		width: 480,
		configFileName: "",
		translationsKeyType: "",
		translationsKeyName: "",
		translationsKeySubName: "",
		translationsKeyField: "",
		textArea: false,

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.common.CMTranslationsWindowDelegate');
			this.delegate.view = this;
			this.form = Ext.create('CMDBuild.view.common.CMTranslationsWindow.CMTranslatableForm', {
				delegate: me.delegate,
				configFileName: me.configFileName,
				textArea: this.textArea,
				translationsKeyType: this.translationsKeyType,
				translationsKeyName: this.translationsKeyName,
				translationsKeySubName: this.translationsKeySubName,
				translationsKeyField: this.translationsKeyField
			});
			this.contentComponent = Ext.create('Ext.panel.Panel', {
				layout: {
					anchor: '100%'
				},
				items: [this.form]
			});
			this.fbar = [
				{
					xtype: 'tbspacer',
					flex: 1
				},
				{
					type: 'button',
					text: CMDBuild.Translation.common.buttons.save,
					handler: function() {
						me.delegate.cmOn('onFilterWindowConfirm');
					}
				},
				{
					type: 'button',
					text: CMDBuild.Translation.common.buttons.abort,
					handler: function() {
						me.delegate.cmOn('onFilterWindowAbort');
					}
				},
				{
					xtype: 'tbspacer',
					flex: 1
				}
			];

			this.items = [this.contentComponent];

			this.callParent(arguments);
		}
	});
})();