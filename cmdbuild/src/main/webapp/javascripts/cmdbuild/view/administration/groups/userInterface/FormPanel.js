(function() {

	Ext.define('CMDBuild.view.administration.groups.userInterface.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.administration.groups.UserInterface}
		 */
		delegate: undefined,

		bodyCls: 'cmgraypanel',
		border: false,
		frame: false,
		overflowY: 'auto',
		split: true,

		layout: {
			type: 'vbox',
			align:'stretch'
		},

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'bottom',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.Save', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onGroupUserInterfaceSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onGroupUserInterfaceAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					Ext.create('Ext.panel.Panel', {
						bodyCls: 'cmgraypanel',
						cls: 'cmborderbottom',
						frame: false,
						border: false,
						items: [
							Ext.create('Ext.form.CheckboxGroup', {
								fieldLabel: CMDBuild.Translation.administration.modsecurity.uiconfiguration.disabled_modules,
								labelWidth: CMDBuild.LABEL_WIDTH,
								columns: 1,
								items: [
									{
										boxLabel: CMDBuild.Translation.management.modcard.treetitle,
										name: CMDBuild.core.proxy.CMProxyConstants.CLASS,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.management.modworkflow.treetitle,
										name: CMDBuild.core.proxy.CMProxyConstants.PROCESS,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.management.modreport.treetitle,
										name: CMDBuild.core.proxy.CMProxyConstants.REPORT,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.management.modview.title,
										name: CMDBuild.core.proxy.CMProxyConstants.DATA_VIEW,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.administration.modDashboard.title,
										name: CMDBuild.core.proxy.CMProxyConstants.DASHBOARD,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.management.modutilities.title
											+ ' - ' + CMDBuild.Translation.management.modutilities.changepassword.title, // TODO: short mode
										name: CMDBuild.core.proxy.CMProxyConstants.CHANGE_PASSWORD,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.management.modutilities.title
											+ ' - ' + CMDBuild.Translation.management.modutilities.bulkupdate.title, // TODO: short mode
										name: 'bulkupdate',
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.management.modutilities.title
											+ ' - ' + CMDBuild.Translation.management.modutilities.csv.title, // TODO: short mode
										name: 'importcsv',
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.management.modutilities.title
											+ ' - ' + CMDBuild.Translation.management.modutilities.csv.title_export, // TODO: short mode
										name: 'exportcsv',
										inputValue: true,
										uncheckedValue: false
									}
								]
							})
						]
					}),
					Ext.create('Ext.panel.Panel', {
						bodyCls: 'cmgraypanel',
						cls: 'cmborderbottom',
						frame: false,
						border: false,
						items: [
							Ext.create('Ext.form.CheckboxGroup', {
								fieldLabel: CMDBuild.Translation.administration.modsecurity.uiconfiguration.disabled_class_tabs, // TODO: rename translation
								labelWidth: CMDBuild.LABEL_WIDTH,
								columns: 1,
								items: [
									{
										boxLabel: CMDBuild.Translation.management.modcard.tabs.detail,
										name: CMDBuild.core.proxy.CMProxyConstants.CLASS_DETAIL_TAB,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.management.modcard.tabs.notes,
										name: CMDBuild.core.proxy.CMProxyConstants.CLASS_NOTE_TAB,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.management.modcard.tabs.relations,
										name: CMDBuild.core.proxy.CMProxyConstants.CLASS_RELATION_TAB,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.management.modcard.tabs.history,
										name: CMDBuild.core.proxy.CMProxyConstants.CLASS_HISTORY_TAB,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.management.modcard.tabs.attachments,
										name: CMDBuild.core.proxy.CMProxyConstants.CLASS_ATTACHMENT_TAB,
										inputValue: true,
										uncheckedValue: false
									}
								]
							})
						]
					}),
					Ext.create('Ext.panel.Panel', {
						bodyCls: 'cmgraypanel',
						cls: 'cmborderbottom',
						frame: false,
						border: false,
						items: [
							Ext.create('Ext.form.CheckboxGroup', {
								fieldLabel: CMDBuild.Translation.administration.modsecurity.uiconfiguration.disabled_process_tabs,
								labelWidth: CMDBuild.LABEL_WIDTH,
								columns: 1,
								items: [
									{
										boxLabel: CMDBuild.Translation.management.modcard.tabs.notes,
										name: CMDBuild.core.proxy.CMProxyConstants.PROCESS_NOTE_TAB,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.management.modcard.tabs.relations,
										name: CMDBuild.core.proxy.CMProxyConstants.PROCESS_RELATION_TAB,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.management.modcard.tabs.history,
										name: CMDBuild.core.proxy.CMProxyConstants.PROCESS_HISTORY_TAB,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.management.modcard.tabs.attachments,
										name: CMDBuild.core.proxy.CMProxyConstants.PROCESS_ATTACHMENT_TAB,
										inputValue: true,
										uncheckedValue: false
									}
								]
							})
						]
					}),
					Ext.create('Ext.panel.Panel', {
						bodyCls: 'cmgraypanel',
						cls: 'cmborderbottom',
						frame: false,
						border: false,
						items: [
							Ext.create('Ext.form.CheckboxGroup', {
								fieldLabel: CMDBuild.Translation.administration.modsecurity.uiconfiguration.other,
								labelWidth: CMDBuild.LABEL_WIDTH,
								columns: 1,
								items: [
									{
										boxLabel: CMDBuild.Translation.administration.modsecurity.uiconfiguration.generic_properties.hide_side_panel,
										name: CMDBuild.core.proxy.CMProxyConstants.HIDE_SIDE_PANEL,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.administration.modsecurity.uiconfiguration.generic_properties.full_screen_navigation,
										name: CMDBuild.core.proxy.CMProxyConstants.FULL_SCREEN_MODE,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.administration.modsecurity.uiconfiguration.generic_properties.card_simple_history,
										name: CMDBuild.core.proxy.CMProxyConstants.SIMPLE_HISTORY_MODE_FOR_CARD,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.administration.modsecurity.uiconfiguration.generic_properties.process_simple_history,
										name: CMDBuild.core.proxy.CMProxyConstants.SIMPLE_HISTORY_MODE_FOR_PROCESS,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.administration.modsecurity.uiconfiguration.generic_properties.always_enabled_widgets,
										name: CMDBuild.core.proxy.CMProxyConstants.PROCESS_WIDGET_ALWAYS_ENABLED,
										inputValue: true,
										uncheckedValue: false
									}
								]
							})
						]
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();