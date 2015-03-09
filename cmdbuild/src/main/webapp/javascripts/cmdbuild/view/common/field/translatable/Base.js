(function() {

	Ext.define('CMDBuild.view.common.field.translatable.Base', {
		extend: 'Ext.form.FieldContainer',

		/**
		 * @cfg {Boolean}
		 */
		considerAsFieldToDisable: true,

		/**
		 * @property {CMDBuild.core.buttons.FieldTranslation}
		 */
		translationButton: undefined,

		translationsKeyField: undefined,
		translationsKeyType: undefined,

		layout: 'hbox',

		initComponent: function() {
			this.field = createField();

			if (_CMCache.isMultiLanguages())
				this.translationButton = Ext.create('CMDBuild.core.buttons.FieldTranslation', {
					scope: this,

					handler: function(button, e) {
						Ext.create('CMDBuild.view.common.CMTranslationsWindow', { // TODO controllare se c'è controller in modo da implementarlo con la solita metodologia autonoma
							title: CMDBuild.Translation.translations,
							translationsKeyType: this.translationsKeyType,
							translationsKeyName: this.translationsKeyName,
							translationsKeySubName: this.translationsKeySubName,
							translationsKeyField: this.translationsKeyField
						}).show();
					}
				});

			Ext.apply(this, {
				items: [this.field, this.translationButton]
			});

			_CMCache.registerOnTranslations(this);

			this.callParent(arguments);
		},

		/**
		 * @abstract
		 */
		createField: function() {},

		/**
		 * @return {String}
		 */
		getValue: function() {
			return this.field.getValue();
		},

		/**
		 * @return {Boolean}
		 */
		isValid: function() {
			return this.field.isValid();
		},

		/**
		 * @param {String} value
		 */
		setValue: function(value) {
			this.field.setValue(value);
		},

		reset: function() {
			this.field.reset();
		},
	});










//	Ext.define("Ext.form.CMTranslatableText", {
//		extend: "Ext.container.Container",
//		layout: "hbox",
//		fieldLabel : "no data",
//		labelWidth: 0,
//		padding: "0 0 5px 0",
//		width: 0,
//		name : 'no name',
//		translationsKeyType: "",
//		translationsKeyName: "",
//		translationsKeySubName: "",
//		allowBlank: true,
//		considerAsFieldToDisable: true,
//		translationsKeyField: "",
//		textArea : false,
//		vtype : '',
//		setValue: function(value) {
//			this.text.setValue(value);
//		},
//		getValue: function() {
//			return this.text.getValue();
//		},
//		isValid: function() {
//			return this.text.isValid();
//		},
//		reset: function() {
//			this.text.reset();
//		},
//		enable: function() {
//			this.text.enable();
//			this.translationsButton.enable();
//		},
//		disable: function() {
//			this.text.disable();
//			this.translationsButton.disable();
//		},
//		resetLanguages: function() {
//			if (_CMCache.isMultiLanguages()) {
//				this.translationsButton.show();
//			}
//			else {
//				this.translationsButton.hide();
//			}
//		},
//		createTextItem: function() {
//			return new Ext.form.field.Text( {
//				fieldLabel : this.fieldLabel,
//				labelWidth: this.labelWidth,
//				width: this.width,
//				name : this.name,
//				allowBlank : this.allowBlank,
//				vtype : this.vtype,
//			});
//		},
//		createHiddenValue: function(valueField) {
//			return new Ext.form.field.Hidden( {
//				width: 0,
//				name : this.original_name,
//				submitValue: true,
//				valueField: valueField,
//				//override
//				getValue : function() {
//					return this.valueField.getValue();
//				},
//				//override
//				getRawValue : function() {
//					return this.valueField.getRawValue();
//				}
//			});
//		},
//		setButtonMargin: Ext.emptyFn,
//		initComponent : function() {
//			var me = this;
//			this.original_name = this.name;
//			this.name += "_default";
//			this.text = this.createTextItem();
//			this.hiddenValue = this.createHiddenValue(this.text);
//			this.width += 22;
//			this.translationsButton = new Ext.Button( {
//				iconCls: 'translate',
//				width: 22,
//				tooltip: CMDBuild.Translation.translations,
//				considerAsFieldToDisable: true,
//				handler: function() {
//					var translationsWindow = new CMDBuild.view.common.CMTranslationsWindow({
//						text		title: CMDBuild.Translation.translations,
//						translationsKeyType: me.translationsKeyType,
//						translationsKeyName: me.translationsKeyName,
//						translationsKeySubName: me.translationsKeySubName,
//						translationsKeyField: me.translationsKeyField,
//						textArea: me.textArea
//					});
//					translationsWindow.show();
//				}
//			});
//			this.setButtonMargin();
//			this.items = [this.text, this.translationsButton, this.hiddenValue];
//			_CMCache.registerOnTranslations(this);
//			this.callParent(arguments);
//			this.resetLanguages();
//		}
//	});

})();