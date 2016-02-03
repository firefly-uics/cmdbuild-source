(function() {

	Ext.define('CMDBuild.view.configure.step.Step1', {
		extend: 'Ext.form.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.configure.Configure}
		 */
		delegate: undefined,

		border: false,
		bodyCls: 'cmdb-blue-panel-no-padding',
		frame: false,

		layout: {
			type: 'vbox',
			align:'stretch'
		},

		initComponent: function() {
			Ext.apply(this, {
				items: [
					Ext.create('CMDBuild.view.common.field.comboBox.Language', {
						name: CMDBuild.core.constants.Proxy.LANGUAGE,
						fieldLabel: CMDBuild.Translation.chooseDefaultLanguage,
						labelWidth: CMDBuild.LABEL_WIDTH_CONFIGURATION,
						maxWidth: CMDBuild.CFG_MEDIUM_FIELD_WIDTH
					}),
					Ext.create('Ext.form.field.Checkbox', {
						name: CMDBuild.core.constants.Proxy.LANGUAGE_PROMPT,
						fieldLabel: CMDBuild.Translation.showLanguageSelectionLoginBox,
						labelWidth: CMDBuild.LABEL_WIDTH_CONFIGURATION,
						maxWidth: CMDBuild.CFG_MEDIUM_FIELD_WIDTH,
						inputValue: true,
						uncheckedValue: false
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function(panel, eOpts) {
				this.delegate.cmfg('onConfigurationViewportWizardPanelShow', {
					displayNextButton: true,
					displayPreviusButton: true
				});
			}
		}
	});

})();