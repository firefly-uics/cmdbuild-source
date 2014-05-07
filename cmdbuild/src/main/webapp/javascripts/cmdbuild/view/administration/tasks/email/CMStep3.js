(function() {

	var tr = CMDBuild.Translation.administration.tasks.taskEmail;

	Ext.define('CMDBuild.view.administration.tasks.email.CMStep3Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		parentDelegate: undefined,
		view: undefined,

		/**
		 * Gatherer function to catch events
		 *
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		// overwrite
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onCheckedAttachmentsFieldset':
					return this.onCheckedAttachmentsFieldset();

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		// GETters functions
			getValueAttachmentsFieldsetCheckbox: function() {
				return this.view.attachmentsFieldset.checkboxCmp.getValue();
			},

// TODO
//			getValueNotificationFieldsetCheckbox: function() {
//				return this.view.notificationFieldset.checkboxCmp.getValue();
//			},

			getValueParsingFieldsetCheckbox: function() {
				return this.view.parsingFieldset.checkboxCmp.getValue();
			},

		/**
		 * Read CMDBuild's alfresco configuration from server and set Combobox store
		 */
		onCheckedAttachmentsFieldset: function() {
			var me = this;

			if (this.view.attachmentsCombo.store.getCount() == 0) {
				CMDBuild.ServiceProxy.configuration.read({
					success: function(response) {
						var decodedJson = Ext.JSON.decode(response.responseText);

						me.view.attachmentsCombo.bindStore(
							CMDBuild.ServiceProxy.lookup.getLookupFieldStore(decodedJson.data['category.lookup'])
						);
					}
				}, name = 'dms');
			}
		},

		// SETters functions
			setValueAttachmentsCombo: function(value) {
				if (!Ext.isEmpty(value)) {
					// HACK to avoid forceSelection timing problem witch don't permits to set combobox value
					this.view.attachmentsCombo.forceSelection = false;
					this.view.attachmentsCombo.setValue(value);
					this.view.attachmentsCombo.forceSelection = true;
				}
			},

			/**
			 * @param (Boolean) value
			 */
			setValueAttachmentsFieldsetCheckbox: function(value) {
				if (value) {
					this.view.attachmentsFieldset.expand();
					this.onCheckedAttachmentsFieldset();
				} else {
					this.view.attachmentsFieldset.collapse();
				}
			},

			/**
			 * Setup all parsing fieldset input values
			 *
			 * @param (String) keyInit
			 * @param (String) keyEnd
			 * @param (String) valueInit
			 * @param (String) valueEnd
			 */
			setValueParsingFields: function(keyInit, keyEnd, valueInit, valueEnd) {
				this.view.parsingKeyInit.setValue(keyInit);
				this.view.parsingKeyEnd.setValue(keyEnd);
				this.view.parsingValueInit.setValue(valueInit);
				this.view.parsingValueEnd.setValue(valueEnd);
			},

			/**
			 * @param (Boolean) value
			 */
			setValueParsingFieldsetCheckbox: function(value) {
				if (value) {
					this.view.parsingFieldset.expand();
				} else {
					this.view.parsingFieldset.collapse();
				}
			}
	});

	Ext.define('CMDBuild.view.administration.tasks.email.CMStep3', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,

		border: false,
		height: '100%',
		overflowY: 'auto',

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.email.CMStep3Delegate', this);

			// Parsing configuration
				this.parsingKeyInit = Ext.create('Ext.form.field.Text', {
					fieldLabel: tr.parsingKeyInit,
					labelWidth: CMDBuild.LABEL_WIDTH,
					name: CMDBuild.ServiceProxy.parameter.PARSING_KEY_INIT,
					width: CMDBuild.ADM_BIG_FIELD_WIDTH
				});

				this.parsingKeyEnd = Ext.create('Ext.form.field.Text', {
					fieldLabel: tr.parsingKeyEnd,
					labelWidth: CMDBuild.LABEL_WIDTH,
					name: CMDBuild.ServiceProxy.parameter.PARSING_KEY_END,
					margin: '0 0 0 20',
					width: CMDBuild.ADM_BIG_FIELD_WIDTH
				});

				this.parsingValueInit = Ext.create('Ext.form.field.Text', {
					fieldLabel: tr.parsingValueInit,
					labelWidth: CMDBuild.LABEL_WIDTH,
					name: CMDBuild.ServiceProxy.parameter.PARSING_VALUE_INIT,
					width: CMDBuild.ADM_BIG_FIELD_WIDTH
				});

				this.parsingValueEnd = Ext.create('Ext.form.field.Text', {
					fieldLabel: tr.parsingValueEnd,
					labelWidth: CMDBuild.LABEL_WIDTH,
					name: CMDBuild.ServiceProxy.parameter.PARSING_VALUE_END,
					margin: '0 0 0 20',
					width: CMDBuild.ADM_BIG_FIELD_WIDTH
				});

				this.parsingFieldset = Ext.create('Ext.form.FieldSet', {
					title: tr.bodyParsing,
					checkboxToggle: true,
					checkboxName: CMDBuild.ServiceProxy.parameter.PARSING_ACTIVE,
					collapsed: true,
					layout: {
						type: 'vbox',
						align: 'stretch'
					},
					items: [
						{
							xtype: 'container',
							layout: 'hbox',
							items: [this.parsingKeyInit, this.parsingKeyEnd]
						},
						{
							xtype: 'container',
							layout: 'hbox',
							margin: '10 0',
							items: [this.parsingValueInit, this.parsingValueEnd]
						}
					]
				});
			// END: BodyParsing configuration

			// Email notification configuration
				this.notificationEmailTemplateCombo = Ext.create('Ext.form.field.ComboBox', {
					name: CMDBuild.ServiceProxy.parameter.NOTIFICATION_EMAIL_TEMPLATE,
					fieldLabel: CMDBuild.Translation.administration.tasks.template,
					labelWidth: CMDBuild.LABEL_WIDTH,
					store: CMDBuild.core.proxy.CMProxyEmailTemplates.getStore(),
					displayField: CMDBuild.ServiceProxy.parameter.NAME,
					valueField: CMDBuild.ServiceProxy.parameter.NAME,
					forceSelection: true,
					editable: false,
					width: CMDBuild.CFG_BIG_FIELD_WIDTH
				});

				this.notificationFieldset = Ext.create('Ext.form.FieldSet', {
					title: CMDBuild.Translation.administration.tasks.sendMail,
					checkboxToggle: true,
					checkboxName: CMDBuild.ServiceProxy.parameter.NOTIFICATION_ACTIVE,
					collapsed: true,
					layout: {
						type: 'vbox'
					},
					items: [this.notificationEmailTemplateCombo]
				});
			// END: Email notification configuration

			// Alfresco configuration
				this.attachmentsCombo = Ext.create('Ext.form.field.ComboBox', {
					name: CMDBuild.ServiceProxy.parameter.ATTACHMENTS_CATEGORY,
					fieldLabel: tr.attachmentsCategory,
					labelWidth: CMDBuild.LABEL_WIDTH,
					displayField: 'Description',
					valueField: 'Id',
					forceSelection: true,
					editable: false,
					width: CMDBuild.CFG_BIG_FIELD_WIDTH
				});

				this.attachmentsFieldset = Ext.create('Ext.form.FieldSet', {
					title: tr.saveToAlfresco,
					checkboxToggle: true,
					checkboxName: CMDBuild.ServiceProxy.parameter.ATTACHMENTS_ACTIVE,
					collapsed: true,
					layout: {
						type: 'vbox'
					},
					items: [this.attachmentsCombo],

					listeners: {
						expand: function(fieldset, eOpts) {
							me.delegate.cmOn('onCheckedAttachmentsFieldset');
						}
					}
				});
			// END: Alfresco configuration

			Ext.apply(this, {
				items: [
					this.parsingFieldset,
					this.notificationFieldset,
					this.attachmentsFieldset
				]
			});

			this.callParent(arguments);
		}
	});

})();