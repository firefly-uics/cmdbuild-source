(function() {

	Ext.define('CMDBuild.view.administration.group.userInterface.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: ['CMDBuild.core.proxy.Constants'],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.administration.group.UserInterface}
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
						itemId: CMDBuild.core.proxy.Constants.TOOLBAR_BOTTOM,
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
								fieldLabel: CMDBuild.Translation.disabledFeatures,
								labelWidth: CMDBuild.LABEL_WIDTH,
								columns: 1,
								items: [
									{
										boxLabel: CMDBuild.Translation.cards,
										name: CMDBuild.core.proxy.Constants.CLASS,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.processes,
										name: CMDBuild.core.proxy.Constants.PROCESS,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.report,
										name: CMDBuild.core.proxy.Constants.REPORT,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.views,
										name: CMDBuild.core.proxy.Constants.DATA_VIEW,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.dashboard,
										name: CMDBuild.core.proxy.Constants.DASHBOARD,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.utilityChangePassword,
										name: CMDBuild.core.proxy.Constants.CHANGE_PASSWORD,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.utilityMultipleUpdate,
										name: 'bulkupdate',
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.utilityImportCsv,
										name: 'importcsv',
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.utilityExportCsv,
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
								fieldLabel: CMDBuild.Translation.disabledTabsInClassesModule,
								labelWidth: CMDBuild.LABEL_WIDTH,
								columns: 1,
								items: [
									{
										boxLabel: CMDBuild.Translation.detail,
										name: CMDBuild.core.proxy.Constants.CLASS_DETAIL_TAB,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.notes,
										name: CMDBuild.core.proxy.Constants.CLASS_NOTE_TAB,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.relations,
										name: CMDBuild.core.proxy.Constants.CLASS_RELATION_TAB,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.history,
										name: CMDBuild.core.proxy.Constants.CLASS_HISTORY_TAB,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.attachments,
										name: CMDBuild.core.proxy.Constants.CLASS_ATTACHMENT_TAB,
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
								fieldLabel: CMDBuild.Translation.disabledTabsInProcessesModule,
								labelWidth: CMDBuild.LABEL_WIDTH,
								columns: 1,
								items: [
									{
										boxLabel: CMDBuild.Translation.notes,
										name: CMDBuild.core.proxy.Constants.PROCESS_NOTE_TAB,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.relations,
										name: CMDBuild.core.proxy.Constants.PROCESS_RELATION_TAB,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.history,
										name: CMDBuild.core.proxy.Constants.PROCESS_HISTORY_TAB,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.attachments,
										name: CMDBuild.core.proxy.Constants.PROCESS_ATTACHMENT_TAB,
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
								fieldLabel: CMDBuild.Translation.otherOptions,
								labelWidth: CMDBuild.LABEL_WIDTH,
								columns: 1,
								items: [
									{
										boxLabel: CMDBuild.Translation.hideSidePanel,
										name: CMDBuild.core.proxy.Constants.HIDE_SIDE_PANEL,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.fullScreenNavigation,
										name: CMDBuild.core.proxy.Constants.FULL_SCREEN_MODE,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.simpleHistoryForCards,
										name: CMDBuild.core.proxy.Constants.SIMPLE_HISTORY_MODE_FOR_CARD,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.simpleHistoryForProcesses,
										name: CMDBuild.core.proxy.Constants.SIMPLE_HISTORY_MODE_FOR_PROCESS,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.processWidgetsAlwaysEnabled,
										name: CMDBuild.core.proxy.Constants.PROCESS_WIDGET_ALWAYS_ENABLED,
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