(function() {

	Ext.define('CMDBuild.view.management.dataView.DataViewView', {
		extend: 'Ext.form.Panel',

		/**
		 * @cfg {CMDBuild.controller.management.dataView.DataView}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		baseTitle: CMDBuild.Translation.views,

		border: true,
		frame: false,
		layout: 'fit',

		tools: [
			{
				type: 'minimize',

				handler: function(event, toolEl, panel) {
					_CMUIState.onlyForm();
				}
			},
			{
				type: 'maximize',

				handler: function(event, toolEl, panel) {
					_CMUIState.onlyGrid();
				}
			},
			{
				type: 'restore',

				handler: function(event, toolEl, panel) {
					_CMUIState.fullScreenOff();
				}
			}
		]
	});

})();