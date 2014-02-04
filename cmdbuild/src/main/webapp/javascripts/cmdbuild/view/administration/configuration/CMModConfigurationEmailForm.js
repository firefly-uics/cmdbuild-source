(function() {

	var tr = CMDBuild.Translation.administration.setup.email, // Path to translation
		delegate = null; // Controller handler

	Ext.define("CMDBuild.view.administration.configuration.CMModConfigurationEmailForm", {
		extend: "Ext.form.Panel",
		mixins: {
			cmFormFunctions: 'CMDBUild.view.common.CMFormFunctions'
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
			this.cmTBar = [
				new Ext.button.Button({
					iconCls: 'modify',
					text: tr.modify,
					handler: function() {
						me.delegate.cmOn('onModifyButtonClick', me);
					}
				}),
				new Ext.button.Button({
					iconCls: 'delete',
					text: tr.remove,
					handler: function() {
						me.delegate.cmOn('onRemoveButtonClick', me);
					}
				})
			];

			this.cmButtons = [
				new CMDBuild.buttons.SaveButton({
					handler: function() {
						me.delegate.cmOn('onSaveButtonClick', me);
					}
				}),
				new CMDBuild.buttons.AbortButton({
					handler: function() {
						me.delegate.cmOn('onAbortButtonClick', me);
					}
				})
			];
			// END: Buttons configuration

			// Page FieldSets configuration
			this.emailAccount = new Ext.form.FieldSet({
				title: tr.emailAccount,
				autoHeight: true,
				autoScroll: true,
				defaultType: 'textfield',
				items: [
					{
						xtype: 'hidden',
						name: 'id'
					},
					{
						fieldLabel: tr.name,
						labelWidth: CMDBuild.LABEL_WIDTH,
						allowBlank: false,
						name: 'name'
					},
					{
						fieldLabel: tr.isDefault,
						labelWidth: CMDBuild.LABEL_WIDTH,
						xtype: 'checkbox',
						name: 'isDefault'
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
						name: 'username'
					},
					{
						inputType : 'password',
						fieldLabel: tr.password,
						labelWidth: CMDBuild.LABEL_WIDTH,
						allowBlank: false,
						name: 'password'
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
						name: 'address'
					},
					{
						fieldLabel: tr.smtpServer,
						labelWidth: CMDBuild.CFG_LABEL_WIDTH,
						width: CMDBuild.CFG_BIG_FIELD_WIDTH,
						name: 'smtpServer'
					},
					{
						xtype: 'numberfield',
						fieldLabel: tr.smtpPort,
						labelWidth: CMDBuild.CFG_LABEL_WIDTH,
						minValue: 1,
						maxValue: 65535,
						name: 'smtpPort'
					},
					{
						fieldLabel: tr.enableSsl,
						labelWidth: CMDBuild.CFG_LABEL_WIDTH,
						xtype: 'checkbox',
						name: 'smtpSsl'
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
						bodyCls: 'cmgraypanel',
						defaultType: 'textfield',
						items: [
							{
								fieldLabel: tr.imapServer,
								labelWidth: CMDBuild.CFG_LABEL_WIDTH,
								width: CMDBuild.CFG_BIG_FIELD_WIDTH,
								name: 'imapServer'
							},
							{
								xtype: 'numberfield',
								fieldLabel: tr.imapPort,
								labelWidth: CMDBuild.CFG_LABEL_WIDTH,
								minValue: 1,
								maxValue: 65535,
								name: 'imapPort'
							},
							{
								fieldLabel: tr.enableSsl,
								labelWidth: CMDBuild.CFG_LABEL_WIDTH,
								xtype: 'checkbox',
								name: 'imapSsl'
							}
						],
						cls: 'cmborderbottom'
					},
					{
						padding: '5px 0px',
						split: true,
						frame: false,
						border: false,
						cls: 'x-panel-body-default-framed',
						bodyCls: 'cmgraypanel',
						defaultType: 'textfield',
						items: [
							{
								fieldLabel: tr.incomingFolder,
								labelWidth: CMDBuild.CFG_LABEL_WIDTH,
								width: CMDBuild.CFG_BIG_FIELD_WIDTH,
								name: 'incomingFolder'
							},
							{
								fieldLabel: tr.processedFolder,
								labelWidth: CMDBuild.CFG_LABEL_WIDTH,
								width: CMDBuild.CFG_BIG_FIELD_WIDTH,
								name: 'processedFolder'
							},
							{
								fieldLabel: tr.rejectedFolder,
								labelWidth: CMDBuild.CFG_LABEL_WIDTH,
								width: CMDBuild.CFG_BIG_FIELD_WIDTH,
								name: 'rejectedFolder'
							},
							{
								fieldLabel: tr.enableMoveRejectedNotMatching,
								labelWidth: CMDBuild.CFG_LABEL_WIDTH,
								xtype: 'checkbox',
								name: 'enableMoveRejectedNotMatching'
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
						bodyCls: 'cmgraypanel',
						margins: '0px 3px 0px 0px',
						autoScroll: true,
						border: false,
						flex: 1,
						items: [this.emailAccount, this.credentials]
					},
					{
						region: 'center',
						bodyCls: 'cmgraypanel',
						margins: '0px 0px 0px 3px',
						autoScroll: true,
						border: false,
						flex: 1,
						items: [this.outgoing, this.incoming]
					}
				]
			});

			Ext.apply(this, {
				tbar: this.cmTBar,
				items: [this.wrapper],
				buttons: this.cmButtons
			});

			this.callParent(arguments);
			this.disableModify();
			this.disableCMButtons();
		},

		/**
		 * Disable isDefault checkbox, if it's checked, to avoid edit actions
		 */
		disableDefaultCheckbox: function() {
			var isDefault = this.getForm().findField('isDefault');

			if (isDefault.getValue()) {
				isDefault.disable();
			}
		}
	});

})();