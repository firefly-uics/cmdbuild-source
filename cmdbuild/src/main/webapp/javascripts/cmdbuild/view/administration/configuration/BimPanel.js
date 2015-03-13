(function() {

	Ext.define('CMDBuild.view.administration.configuration.BimPanel', {
		extend: 'CMDBuild.view.administration.configuration.BasePanel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Main}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		configFileName: 'bim',

		initComponent: function() {
			this.enabledCheckBox = Ext.create('Ext.ux.form.XCheckbox', {
				name: CMDBuild.core.proxy.CMProxyConstants.ENABLED,
				fieldLabel: CMDBuild.Translation.enabled
			});

			Ext.apply(this, {
				title: this.baseTitle + this.titleSeparator + CMDBuild.Translation.bim,
				items: [
					this.enabledCheckBox,
					{
						xtype: 'textfield',
						name: CMDBuild.core.proxy.CMProxyConstants.URL,
						fieldLabel: CMDBuild.Translation.url,
						width: CMDBuild.CFG_BIG_FIELD_WIDTH,
					},
					{
						xtype: 'textfield',
						name: 'username',
						fieldLabel: CMDBuild.Translation.username
					},
					{
						xtype: 'textfield',
						name: CMDBuild.core.proxy.CMProxyConstants.PASSWORD,
						fieldLabel: CMDBuild.Translation.password,
						inputType: 'password'
					}
				]
			});

			this.callParent(arguments);
		},

		/**
		 * @param {Object} saveDataObject
		 *
		 * @override
		 */
		afterSubmit: function(saveDataObject) {
			CMDBuild.Config.workflow.enabled = this.enabledCheckBox.getValue();

			if (CMDBuild.Config.workflow.enabled) {
				_CMMainViewportController.enableAccordionByName('bim');
			} else {
				_CMMainViewportController.disableAccordionByName('bim');
			}
		}
	});

})();