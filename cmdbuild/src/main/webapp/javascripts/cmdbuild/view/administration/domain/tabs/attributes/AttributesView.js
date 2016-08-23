(function() {

	/**
	 * This class have some custom code different from linked ones
	 *
	 * @link CMDBuild.view.administration.classes.CMAttributes
	 * @link CMDBuild.view.administration.workflow.CMAttributes
	 */

	Ext.define('CMDBuild.view.administration.domain.tabs.attributes.AttributesView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @property {CMDBuild.view.administration.domain.tabs.attributes.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.domain.tabs.attributes.GridPanel}
		 */
		grid: undefined,

		layout: 'border',
		title: CMDBuild.Translation.attributes,

		initComponent: function() {
			Ext.apply(this, {
				items: [
					this.grid = Ext.create('CMDBuild.view.administration.domain.tabs.attributes.GridPanel', {
						delegate: this.delegate,
						region: 'north',
						split: true,
						height: '30%'
					}),
					this.form = Ext.create('CMDBuild.view.administration.domain.tabs.attributes.FormPanel', {
						delegate: this.delegate,
						region: 'center'
					})
				]
			});

			this.callParent(arguments);

			this.form.disableModify();
		},

		onAddAttributeClick: function() {
			this.form.onAddAttributeClick(params=null, enableAll=true);
			this.grid.getSelectionModel().deselectAll();
		}
	});

})();