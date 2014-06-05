(function() {

	var tr = CMDBuild.Translation.administration.tasks;

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep1Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		parentDelegate: undefined,

		view: undefined,
		taskType: 'connector',

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
			 * @return (String)
			 */
			getValueId: function() {
				return this.view.idField.getValue();
			},

			/**
			 * @return (Boolean)
			 */
			getValueNotificationFieldsetCheckbox: function() {
				return this.view.notificationFieldset.checkboxCmp.getValue();
			},

		// GETters functions
			/**
			 * @param (Boolean) state
			 */
			setDisabledTypeField: function(state) {
				this.view.typeField.setDisabled(state);
			},

			/**
			 * @param (Object) value
			 */
			setValueActive: function(value) {
				this.view.activeField.setValue(value);
			},

			/**
			 * @param (Object) value
			 */
			setValueDescription: function(value) {
				this.view.descriptionField.setValue(value);
			},

			/**
			 * @param (Object) value
			 */
			setValueId: function(value) {
				this.view.idField.setValue(value);
			},

			/**
			 * @param (Object) value
			 */
			setValueNotificationAccount: function(value) {
				this.getNotificationDelegate().setValue('sender', value);
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
			setValueNotificationTemplateError: function(value) {
				this.getNotificationDelegate().setValue('templateError', value);
			}
	});

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep1', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,

		bodyCls: 'cmgraypanel',
		border: false,
		autoScroll: true,

		initComponent: function() {
			this.delegate = Ext.create('CMDBuild.view.administration.tasks.connector.CMStep1Delegate', this);

			this.typeField = Ext.create('Ext.form.field.Text', {
				fieldLabel: tr.type,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name: CMDBuild.core.proxy.CMProxyConstants.TYPE,
				value: tr.tasksTypes.connector,
				width: CMDBuild.CFG_BIG_FIELD_WIDTH,
				disabled: true,
				cmImmutable: true,
				readOnly: true,
				submitValue: false
			});

			this.idField = Ext.create('Ext.form.field.Hidden', {
				name: CMDBuild.core.proxy.CMProxyConstants.ID
			});

			this.descriptionField = Ext.create('Ext.form.field.Text', {
				name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
				fieldLabel: CMDBuild.Translation.description_,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.CFG_BIG_FIELD_WIDTH,
				allowBlank: false
			});

			this.activeField = Ext.create('Ext.form.field.Checkbox', {
				name: CMDBuild.core.proxy.CMProxyConstants.ACTIVE,
				fieldLabel: tr.startOnSave,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.CFG_BIG_FIELD_WIDTH
			});

			// Email notification configuration
				this.notificationForm = Ext.create('CMDBuild.view.administration.tasks.common.notificationForm.CMNotificationForm', {
					sender: {
						type: 'sender',
						disabled: false
					},
					templateError: {
						type: 'template',
						disabled: false,
						fieldLabel: tr.notificationForm.templateError,
						name: CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_EMAIL_TEMPLATE_ERROR
					}
				});

				this.notificationFieldset = Ext.create('Ext.form.FieldSet', {
					title: tr.notificationForm.titlePlur,
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

				this.notificationFieldset.fieldWidthsFix();
			// END: Email notification configuration

			Ext.apply(this, {
				items: [
					this.typeField,
					this.idField,
					this.descriptionField,
					this.activeField,
					this.notificationFieldset
				]
			});

			this.callParent(arguments);
		}
	});

})();