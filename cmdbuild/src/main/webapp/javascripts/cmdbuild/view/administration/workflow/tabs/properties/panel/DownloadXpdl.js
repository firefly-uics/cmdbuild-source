(function() {

	Ext.define('CMDBuild.view.administration.workflow.tabs.properties.panel.DownloadXpdl', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.workflow.tabs.properties.XpdlVersion',
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.administration.workflow.tabs.Properties}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		versionCombo: undefined,

		bodyCls: 'cmgraypanel-nopadding',
		border: false,
		frame: false,

		layout: {
			type: 'hbox',
			align: 'stretch'
		},

		initComponent: function() {
			Ext.apply(this, {
				items: [
					this.versionCombo = Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.constants.Proxy.VERSION,
						fieldLabel: CMDBuild.Translation.version,
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
						displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
						valueField: CMDBuild.core.constants.Proxy.ID,
						flex: 1,
						margin: '0 5 5 0',
						editable: false,
						autoSelect: true,
						forceSelection: true,

						store: Ext.create('Ext.data.ArrayStore', { // TODO: waiting for refactor (for a better version store build)
							model: 'CMDBuild.model.workflow.tabs.properties.XpdlVersion',
							data: [],
							sorters: [
								{ property : CMDBuild.core.constants.Proxy.INDEX, direction: 'DESC' }
							]
						}),
						queryMode: 'local'
					}),
					Ext.create('Ext.container.Container', {
						flex: 1,

						items: [
							Ext.create('CMDBuild.core.buttons.text.Download', {
								text: CMDBuild.Translation.downloadTemplate,
								margins: '0 0 0 5',
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onWorkflowTabPropertiesDownloadXpdlPanelDownloadButtonClick');
								}
							})
						]
					})

				]
			});

			this.callParent(arguments);
		}
	});

})();