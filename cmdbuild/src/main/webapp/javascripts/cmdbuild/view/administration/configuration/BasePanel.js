(function() {

	Ext.define('CMDBuild.view.administration.configuration.BasePanel', {
		extend: 'Ext.form.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Main}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		baseTitle: CMDBuild.Translation.setup,

		/**
		 * @cfg {String}
		 *
		 * @abstract
		 */
		configFileName: undefined,

		/**
		 * @cfg {String}
		 */
		titleSeparator: ' - ',

		buttonAlign: 'center',
		frame: true,
		overflowY: 'auto',

		fieldDefaults: {
			labelAlign: 'left',
			labelWidth: CMDBuild.CFG_LABEL_WIDTH,
			width: CMDBuild.CFG_MEDIUM_FIELD_WIDTH
		},

		initComponent: function() {
			Ext.apply(this, {
				buttons: [
					Ext.create('CMDBuild.buttons.SaveButton', {
						scope: this,

						handler: function() {
							this.delegate.cmOn('onConfigurationSaveButtonClick');
						}
					}),
					Ext.create('CMDBuild.buttons.AbortButton', {
						scope: this,

						handler: function() {
							this.delegate.cmOn('onConfigurationAbortButtonClick');
						}
					})
				]
			});

			this.callParent(arguments);
		},

		/**
		 * Template method called in the callback function of the form submit
		 *
		 * @param {Object} saveDataObject
		 *
		 * TODO: move to controller
		 **/
		afterSubmit: function(saveDataObject) {
			_debug('Before submit of the templateModSetup');
		}
	});

})();