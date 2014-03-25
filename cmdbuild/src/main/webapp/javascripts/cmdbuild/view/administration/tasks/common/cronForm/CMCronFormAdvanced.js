(function() {

	var tr = CMDBuild.Translation.administration.tasks.cronForm;

	Ext.define('CMDBuild.view.administration.tasks.common.cronForm.CMCronFormAdvanced', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,

		frame: true,
		layout: 'hbox',
		margin: '0 0 5 0',

		initComponent: function() {
			var me = this;

			this.advanceRadio = Ext.create('Ext.form.field.Radio', {
				name: 'cronInputType',
				inputValue: 'advanced',
				boxLabel: tr.advanced,
				width: CMDBuild.LABEL_WIDTH,

				listeners: {
					change: function(radio, value) {
						me.delegate.setDisabledAdvancedFields(!value);
						me.delegate.setDisabledBaseCombo(value);
					}
				}
			});

			this.advancedFields = [
				this.delegate.createCronField('minute', tr.minute),
				this.delegate.createCronField('hour', tr.hour),
				this.delegate.createCronField('dayOfMounth', tr.dayOfMounth),
				this.delegate.createCronField('mounth', tr.mounth),
				this.delegate.createCronField('dayOfWeek', tr.dayOfWeek)
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