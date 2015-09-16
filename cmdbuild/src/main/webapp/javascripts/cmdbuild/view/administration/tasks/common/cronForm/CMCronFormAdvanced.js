(function() {

	var tr = CMDBuild.Translation.administration.tasks.cronForm;

	Ext.define('CMDBuild.view.administration.tasks.common.cronForm.CMCronFormAdvanced', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.common.cronForm.CMCronFormController}
		 */
		delegate: undefined,

		/**
		 * @property {Array}
		 */
		advancedFields: undefined,

		/**
		 * @property {Ext.form.field.Radio}
		 */
		advanceRadio: undefined,

		frame: true,
		layout: 'hbox',
		margin: '0 0 5 0',
		overflowY: 'auto',

		initComponent: function() {
			var me = this;

			this.advanceRadio = Ext.create('Ext.form.field.Radio', {
				name: CMDBuild.core.constants.Proxy.CRON_INPUT_TYPE,
				inputValue: CMDBuild.core.constants.Proxy.ADVANCED,
				boxLabel: tr.advanced,
				width: CMDBuild.LABEL_WIDTH,

				listeners: {
					change: function(radio, value) {
						me.delegate.cmOn('onChangeAdvancedRadio', value);
					}
				}
			});

			this.advancedFields = [
				this.delegate.createCronField(CMDBuild.core.constants.Proxy.MINUTE, tr.minute),
				this.delegate.createCronField(CMDBuild.core.constants.Proxy.HOUR, tr.hour),
				this.delegate.createCronField(CMDBuild.core.constants.Proxy.DAY_OF_MOUNTH, tr.dayOfMounth),
				this.delegate.createCronField(CMDBuild.core.constants.Proxy.MOUNTH, tr.mounth),
				this.delegate.createCronField(CMDBuild.core.constants.Proxy.DAY_OF_WEEK, tr.dayOfWeek)
			];

			Ext.apply(this, {
				items: [
					this.advanceRadio,
					{
						xtype: 'container',
						frame: false,
						border: false,

						items: this.advancedFields
					}
				]
			});

			this.callParent(arguments);
		}
	});

})();