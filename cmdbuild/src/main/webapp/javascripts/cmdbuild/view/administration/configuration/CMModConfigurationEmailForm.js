(function() {

	var tr = CMDBuild.Translation.administration.setup.email; // Path to translation

	Ext.define("CMDBuild.view.administration.configuration.CMModConfigurationEmailForm", {
		extend: "Ext.form.Panel",
		mixins: {
			cmFormFunctions: "CMDBUild.view.common.CMFormFunctions"
		},

		autoScroll: false,
		buttonAlign: 'center',
		layout: 'fit',
		split: true,
		frame: false,
		border: false,
		cls: 'x-panel-body-default-framed cmbordertop',
		bodyCls: 'cmgraypanel',

		initComponent: function() {
			var me = this;

			// Buttons configuration
			this.modifyButton = new Ext.button.Button({
				iconCls: 'modify',
				text: tr.modify,
				handler: function() {
					me.callDelegates('onFormModifyButtonClick', me);
				}
			});

			this.removeButton = new Ext.button.Button({
				iconCls: 'delete',
				text: tr.remove,
				handler: function() {
					me.callDelegates('onFormRemoveButtonClick', me);
				}
			});

			this.saveButton = new CMDBuild.buttons.SaveButton({
				handler: function() {
					me.callDelegates('onFormSaveButtonClick', me);
				}
			});

			this.abortButton = new CMDBuild.buttons.AbortButton({
				handler: function() {
					me.callDelegates('onFormAbortButtonClick', me);
				}
			});
			// END: Buttons configuration

			// Page FieldSets configuration
			this.emailAccount = new Ext.form.FieldSet({
				title: tr.emailAccount,
				autoHeight: true,
				autoScroll: true,
				defaultType: 'textfield',
				items: [
					{
						fieldLabel: tr.name,
						labelWidth: CMDBuild.LABEL_WIDTH,
						allowBlank: false,
						name: 'account.name'
					},
					{
						fieldLabel: tr.isDefault,
						labelWidth: CMDBuild.LABEL_WIDTH,
						xtype: 'checkbox',
						name: 'account.isDefault'
					},
					{
						fieldLabel: tr.active,
						labelWidth: CMDBuild.LABEL_WIDTH,
						xtype: 'checkbox',
						name: 'account.active'
					}
				]
			});

			this.credentials = new Ext.form.FieldSet({
				title: tr.credentials,
				autoHeight: true,
				autoScroll: true,
				defaultType: 'textfield',
				items: [
					{
						fieldLabel: tr.username,
						labelWidth: CMDBuild.LABEL_WIDTH,
						allowBlank: false,
						name: 'credentials.username'
					},
					{
						fieldLabel: tr.password,
						labelWidth: CMDBuild.LABEL_WIDTH,
						allowBlank: false,
						name: 'credentials.password'
					}
				]
			});

			this.outgoing = new Ext.form.FieldSet({
				title: tr.outgoing,
				autoHeight: true,
				autoScroll: true,
				defaultType: 'textfield',
				items: [
					{
						fieldLabel: tr.emailAddress,
						labelWidth: CMDBuild.CFG_LABEL_WIDTH,
						width: CMDBuild.CFG_BIG_FIELD_WIDTH,
						allowBlank: false,
						name: 'outgoing.emailAddress'
					},
					{
						fieldLabel: tr.smtpServer,
						labelWidth: CMDBuild.CFG_LABEL_WIDTH,
						width: CMDBuild.CFG_BIG_FIELD_WIDTH,
						allowBlank: false,
						name: 'outgoing.smtpServer'
					},
					{
						xtype: 'numberfield',
						fieldLabel: tr.smtpPort,
						labelWidth: CMDBuild.CFG_LABEL_WIDTH,
						allowBlank: false,
						minValue: 0,
						maxValue: 65535,
						name: 'outgoing.smtpPort'
					},
					{
						fieldLabel: tr.enableSsl,
						labelWidth: CMDBuild.CFG_LABEL_WIDTH,
						xtype: 'checkbox',
						name: 'outgoing.smtpEnableSsl'
					}
				]
			});

			this.incoming = new Ext.form.FieldSet({
				title: tr.incoming,
				autoHeight: true,
				autoScroll: true,
				items: [
					{
						padding: '0px 0px 5px 0px',
						split: true,
						frame: false,
						border: false,
						cls: 'x-panel-body-default-framed',
						defaultType: 'textfield',
						items: [
							{
								fieldLabel: tr.imapServer,
								labelWidth: CMDBuild.CFG_LABEL_WIDTH,
								width: CMDBuild.CFG_BIG_FIELD_WIDTH,
								allowBlank: false,
								name: 'incoming.imapServer'
							},
							{
								xtype: 'numberfield',
								fieldLabel: tr.imapPort,
								labelWidth: CMDBuild.CFG_LABEL_WIDTH,
								allowBlank: false,
								minValue: 0,
								maxValue: 65535,
								name: 'outgoing.imapPort'
							},
							{
								fieldLabel: tr.enableSsl,
								labelWidth: CMDBuild.CFG_LABEL_WIDTH,
								xtype: 'checkbox',
								name: 'incoming.imapEnableSsl'
							}
						],
						cls: 'cmborderbottom'
					},
					{
						padding: '5px 0px',
						split: true,
						frame: false,
						border: false,
						cls: "x-panel-body-default-framed",
						defaultType: 'textfield',
						items: [
							{
								fieldLabel: tr.incomingFolder,
								labelWidth: CMDBuild.CFG_LABEL_WIDTH,
								width: CMDBuild.CFG_BIG_FIELD_WIDTH,
								allowBlank: false,
								name: 'incoming.incomingFolder'
							},
							{
								fieldLabel: tr.processedFolder,
								labelWidth: CMDBuild.CFG_LABEL_WIDTH,
								width: CMDBuild.CFG_BIG_FIELD_WIDTH,
								allowBlank: false,
								name: 'incoming.processedFolder'
							},
							{
								fieldLabel: tr.rejectedFolder,
								labelWidth: CMDBuild.CFG_LABEL_WIDTH,
								width: CMDBuild.CFG_BIG_FIELD_WIDTH,
								allowBlank: false,
								name: 'incoming.rejectedFolder'
							},
							{
								fieldLabel: tr.enableMoveRejectedNotMatching,
								labelWidth: CMDBuild.CFG_LABEL_WIDTH,
								xtype: 'checkbox',
								name: 'incoming.enableMoveRejectedNotMatching'
							}
						]
					}
				]
			});
			// END: Page FieldSets configuration

			// Splitted-view wrapper
			this.wrapper = new Ext.form.Panel({
				region: 'center',
				layout: {
					type: 'hbox',
					align:'stretch'
				},
				frame: true,
				items: [
					{
						region: 'west',
						margins: '0px 3px 0px 0px',
						autoScroll: true,
						border: false,
						flex: 1,
						items: [this.emailAccount, this.credentials]
					},
					{
						region: 'center',
						margins: '0px 0px 0px 3px',
						autoScroll: true,
						border: false,
						flex: 1,
						items: [this.outgoing, this.incoming]
					}
				]
			});

			Ext.apply(this, {
				tbar: [this.modifyButton, this.removeButton],
				items: [this.wrapper],
				buttons: [this.saveButton, this.abortButton]
			});

			this.callParent(arguments);
			this.disableModify();
		}
	});

})();