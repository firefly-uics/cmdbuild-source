(function () {

	Ext.define('CMDBuild.view.common.panel.gridAndForm.filter.advanced.filterEditor.attributes.AttributesView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.common.panel.gridAndForm.filter.advanced.filterEditor.Attributes}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.common.panel.gridAndForm.filter.advanced.filterEditor.attributes.FormPanel}
		 */
		form: undefined,

		border: false,
		frame: false,
		layout: 'fit',
		title: CMDBuild.Translation.attributes,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				items: [
					this.form = Ext.create('CMDBuild.view.common.panel.gridAndForm.filter.advanced.filterEditor.attributes.FormPanel', { delegate: this.delegate })
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function (panel, eOpts) {
				this.delegate.cmfg('onPanelGridAndFormFilterAdvancedFilterEditorAttributesViewShow');
			}
		}
	});

})();
