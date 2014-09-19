(function () {

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
				processId: this.activity.Id,
				readWrite: !this.widgetConf.readOnly,
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

			this.items = [this.emailGrid];

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