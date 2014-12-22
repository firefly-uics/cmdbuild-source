(function () {

	Ext.require('CMDBuild.core.proxy.widgets.ManageEmail');

	Ext.define('CMDBuild.view.management.common.widgets.CMManageEmail', {
		extend: 'Ext.panel.Panel',

		statics: {
			WIDGET_NAME: '.ManageEmail'
		},

		border: false,
		cls: 'x-panel-body-default-framed',
		frame: false,
		loaded: false,

		/**
		 * @property {Object}
		 */
		activity: undefined,

		/**
		 * @property {CMDBuild.view.management.common.widgets.CMEmailGrid}
		 */
		emailGrid: undefined,

		/**
		 * @property {Object}
		 */
		widgetConf: undefined,

		/**
		 * @param {Object} configuration
		 */
		constructor: function(configuration) {
			this.widgetConf = configuration.widget;
			this.activity = configuration.activity.raw || configuration.activity.data;

			this.callParent([this.widgetConf]); // To apply the conf to the panel
		},

		initComponent: function() {
			this.emailGrid = Ext.create('CMDBuild.view.management.common.widgets.CMEmailGrid', {
				autoScroll: true,
				processId: this.activity['Id'],
				readWrite: !this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.READ_ONLY],
				frame: false,
				border: false
			});

			_CMUtils.forwardMethods(this, this.emailGrid, [
				'addTemplateToStore',
				'addToStoreIfNotInIt',
				'hasDraftEmails',
				'removeTemplatesFromStore',
				'getDraftEmails',
				'getNewEmails',
				'removeRecord',
				'setDelegate'
			]);

			Ext.apply(this, {
				items: [this.emailGrid]
			});

			this.callParent(arguments);
		},

		/**
		 * @param {Boolean} modifiedOnly
		 *
		 * @return {Array} outgoingEmails
		 */
		getOutgoing: function(modifiedOnly) {
			var allOutgoing = modifiedOnly ? false : true;
			var outgoingEmails = [];
			var emails = this.emailGrid.getStore().getRange();

			for (var i = 0; i < emails.length; ++i) {
				var currentEmail = emails[i];

				if (allOutgoing || !currentEmail.get('Id') || currentEmail.dirty)
					outgoingEmails.push(currentEmail.data);
			}

			return outgoingEmails;
		},

		/**
		 * @return {Object}
		 */
		getDeletedEmails: function() {
			return this.emailGrid.deletedEmails;
		}
	});

})();