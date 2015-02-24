(function() {

	Ext.define('CMDBuild.view.management.common.widgets.manageEmail.attachments.MainContainer', {
		extend: 'Ext.container.Container',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.manageEmail.Attachments}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.management.common.widgets.manageEmail.attachments.ButtonsContainer}
		 */
		attachmentButtonsContainer: undefined,

		/**
		 * @property {Ext.container.Container}
		 */
		attachmentPanelsContainer: undefined,

		/**
		 * @cfg {Boolean}
		 */
		readOnly: false,

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

		initComponent: function() {
			if (CMDBuild.Config.dms.enabled == 'true') { // TODO: use a model for CMDBuild.Config to convert attributes from string
				this.attachmentButtonsContainer = Ext.create('CMDBuild.view.management.common.widgets.manageEmail.attachments.ButtonsContainer', {
					delegate: this.delegate,
					readOnly: this.readOnly
				});

				this.attachmentPanelsContainer = Ext.create('Ext.container.Container', {
					autoScroll: true,
					flex: 1
				});

				Ext.apply(this, {
					items: [this.attachmentButtonsContainer, this.attachmentPanelsContainer],
				});
			}

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