(function() {

	Ext.define('CMDBuild.view.administration.report.jasper.form.Step2Panel', {
		extend: 'Ext.form.Panel',

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		border: false,
		cls: 'x-panel-body-default-framed',
		encoding: 'multipart/form-data',
		fileUpload: true,
		frame: false,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align:'stretch'
		}
	});

})();