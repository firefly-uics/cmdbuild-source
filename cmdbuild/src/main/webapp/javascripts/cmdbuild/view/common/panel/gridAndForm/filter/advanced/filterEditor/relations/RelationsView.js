(function () {

	Ext.define('CMDBuild.view.common.panel.gridAndForm.filter.advanced.filterEditor.relations.RelationsView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.common.panel.gridAndForm.filter.advanced.filterEditor.relations.Relations}
		 */
		delegate: undefined,

		border: false,
		frame: false,
		layout: 'border',
		title: CMDBuild.Translation.relations,

		listeners: {
			show: function (panel, eOpts) {
				this.delegate.cmfg('onPanelGridAndFormFilterAdvancedFilterEditorRelationsViewShow');
			}
		}
	});

})();
