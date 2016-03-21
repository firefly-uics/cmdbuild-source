(function () {

	Ext.define('CMDBuild.view.administration.class.IconForm', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		bodyCls: 'cmdb-gray-panel-no-padding',
		border: false,
		encoding: 'multipart/form-data',
		fileUpload: true,
		frame: false,
		monitorValid: true,

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

		initComponent: function () {
			Ext.apply(this, {
				items: [
					Ext.create('Ext.form.field.File', {
						name: 'icon', // TODO
						fieldLabel: CMDBuild.Translation.file,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG
					}),
					Ext.create('Ext.form.FieldContainer', {
						fieldLabel: CMDBuild.Translation.current,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,

						items: [
							Ext.create('Ext.Img', {
								src: '',
								height: 50,
								width: 50
							})
						]
					}),
					Ext.create('Ext.container.Container', {
						padding: '5',

						style: {
							borderTop: '1px solid #d0d0d0'
						},

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.text.Upload', {
								scope: this,

								handler: function (button, e) {
//									this.delegate.cmfg('onWorkflowTabPropertiesSaveButtonClick'); // TODO
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
