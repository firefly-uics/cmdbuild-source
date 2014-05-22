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
			/**
			 * @return (Object) delegate
			 */
			getNotificationDelegate: function() {
				return this.view.notificationForm.delegate;
			},

			/**
			 * @return (Boolean)
			 */
			getValueAttachmentsFieldsetCheckbox: function() {
				return this.view.attachmentsFieldset.checkboxCmp.getValue();
			},

			/**
			 * @return (Boolean)
			 */
			getValueNotificationFieldsetCheckbox: function() {
				return this.view.notificationFieldset.checkboxCmp.getValue();
			},

			/**
			 * @return (Boolean)
			 */
			getValueParsingFieldsetCheckbox: function() {
				return this.view.parsingFieldset.checkboxCmp.getValue();
			},

		/**
		 * Read CMDBuild's alfresco configuration from server and set Combobox store
		 */
		onCheckedAttachmentsFieldset: function() {
			var me = this;

			if (this.view.attachmentsCombo.store.getCount() == 0)
				CMDBuild.ServiceProxy.configuration.read({
					success: function(response) {
						var decodedJson = Ext.JSON.decode(response.responseText);

						me.view.attachmentsCombo.bindStore(
							CMDBuild.ServiceProxy.lookup.getLookupFieldStore(decodedJson.data['category.lookup'])
						);
					}
				}, name = 'dms');
		},

		// SETters functions
			/**
			 * Set attachments field as required/unrequired
			 *
			 * @param (Boolean) state
			 */
			setAllowBlankAttachmentsField: function(state) {
				this.view.attachmentsCombo.allowBlank = state;
			},


			/**
			 * Set parsing fields as required/unrequired
			 *
			 * @param (Boolean) state
			 */
			setAllowBlankParsingFields: function(state) {
				this.view.parsingKeyInit.allowBlank = state;
				this.view.parsingKeyEnd.allowBlank = state;
				this.view.parsingValueInit.allowBlank = state;
				this.view.parsingValueEnd.allowBlank = state;
				this.view.parsingFieldset.allowBlank = state;
			},

			/**
			 * @param (String) value
			 */
			setValueAttachmentsCombo: function(value) {
				this.view.attachmentsCombo.setValue(value);
			},

			/**
			 * @param (Boolean) state
			 */
			setValueAttachmentsFieldsetCheckbox: function(state) {
				if (state) {
					this.view.attachmentsFieldset.expand();
					this.onCheckedAttachmentsFieldset();
				} else {
					this.view.attachmentsFieldset.collapse();
				}
			},

			/**
			 * @param (Boolean) state
			 */
			setValueNotificationFieldsetCheckbox: function(state) {
				if (state) {
					this.view.notificationFieldset.expand();
				} else {
					this.view.notificationFieldset.collapse();
				}
			},

			/**
			 * @param (String) value
			 */
			setValueNotificationTemplate: function(value) {
				this.getNotificationDelegate().setValue('template', value);
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
			 * @param (Boolean) state
			 */
			setValueParsingFieldsetCheckbox: function(state) {
				if (state) {
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
					name: CMDBuild.core.proxy.CMProxyConstants.PARSING_KEY_INIT,
					width: CMDBuild.ADM_BIG_FIELD_WIDTH
				});

				this.parsingKeyEnd = Ext.create('Ext.form.field.Text', {
					fieldLabel: tr.parsingKeyEnd,
					labelWidth: CMDBuild.LABEL_WIDTH,
					name: CMDBuild.core.proxy.CMProxyConstants.PARSING_KEY_END,
					margin: '0 0 0 20',
					width: CMDBuild.ADM_BIG_FIELD_WIDTH
				});

				this.parsingValueInit = Ext.create('Ext.form.field.Text', {
					fieldLabel: tr.parsingValueInit,
					labelWidth: CMDBuild.LABEL_WIDTH,
					name: CMDBuild.core.proxy.CMProxyConstants.PARSING_VALUE_INIT,
					width: CMDBuild.ADM_BIG_FIELD_WIDTH
				});

				this.parsingValueEnd = Ext.create('Ext.form.field.Text', {
					fieldLabel: tr.parsingValueEnd,
					labelWidth: CMDBuild.LABEL_WIDTH,
					name: CMDBuild.core.proxy.CMProxyConstants.PARSING_VALUE_END,
					margin: '0 0 0 20',
					width: CMDBuild.ADM_BIG_FIELD_WIDTH
				});

				this.parsingFieldset = Ext.create('Ext.form.FieldSet', {
					title: tr.bodyParsing,
					checkboxName: CMDBuild.core.proxy.CMProxyConstants.PARSING_ACTIVE,
					checkboxToggle: true,
					collapsed: true,
					collapsible: true,
					toggleOnTitleClick: true,

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
				this.notificationForm = Ext.create('CMDBuild.view.administration.tasks.common.notificationForm.CMNotificationForm', {
					template: {
						type: 'template',
						disabled: false
					}
				});

				this.notificationFieldset = Ext.create('Ext.form.FieldSet', {
					title: CMDBuild.Translation.administration.tasks.notificationForm.title,
					checkboxName: CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_ACTIVE,
					checkboxToggle: true,
					collapsed: true,
					collapsible: true,
					toggleOnTitleClick: true,

					layout: {
						type: 'vbox'
					},

					items: [this.notificationForm]
				});
			// END: Email notification configuration

			// Attachments configuration
				this.attachmentsCombo = Ext.create('Ext.form.field.ComboBox', {
					name: CMDBuild.core.proxy.CMProxyConstants.ATTACHMENTS_CATEGORY,
					fieldLabel: tr.attachmentsCategory,
					labelWidth: CMDBuild.LABEL_WIDTH,
					displayField: 'Description',
					valueField: 'Id',
					forceSelection: true,
					editable: false,
					width: CMDBuild.ADM_BIG_FIELD_WIDTH
				});

				this.attachmentsFieldset = Ext.create('Ext.form.FieldSet', {
					title: tr.saveToAlfresco,
					checkboxName: CMDBuild.core.proxy.CMProxyConstants.ATTACHMENTS_ACTIVE,
					checkboxToggle: true,
					collapsed: true,
					collapsible: true,
					toggleOnTitleClick: true,

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
			// END: Attachments configuration

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