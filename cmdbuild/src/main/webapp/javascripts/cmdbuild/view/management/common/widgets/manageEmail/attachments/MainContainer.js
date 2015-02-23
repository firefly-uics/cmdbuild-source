(function() {

	Ext.define('CMDBuild.view.management.common.widgets.manageEmail.attachments.MainContainer', {
		extend: 'Ext.container.Container',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
//			'CMDBuild.core.proxy.EmailTemplates'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.manageEmail.Attachments}
		 */
		delegate: undefined,

		attachmentButtonsContainer: undefined,
		attachmentPanelsContainer: undefined,

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

		initComponent: function() {
			this.attachmentButtonsContainer = Ext.create('CMDBuild.view.management.common.widgets.manageEmail.attachments.ButtonsContainer', {
				delegate: this.delegate
			});

			this.attachmentPanelsContainer = Ext.create('Ext.container.Container', {
				autoScroll: true,
				flex: 1
			});

			Ext.apply(this, {
				items: [this.attachmentButtonsContainer, this.attachmentPanelsContainer],
			});

			this.callParent(arguments);
		},

		/**
		 * Forward method
		 *
		 * @param {Object} component
		 *
		 * @return {Ext.Component}
		 */
		addPanel: function(component) {
			return this.attachmentPanelsContainer.add(component);
		}
	});

})();