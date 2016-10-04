(function () {

	/**
	 * Adapter
	 *
	 * @legacy
	 */
	Ext.define('CMDBuild.view.management.workflow.panel.form.TabPanel', {
		extend: 'Ext.tab.Panel',

		/**
		 * @cfg {CMDBuild.controller.management.workflow.panel.form.Form}
		 */
		delegate: undefined,

		border: false,
		cls: 'cmdb-border-right',
		frame: false,
		region: 'center',

		/**
		 * @returns {Void}
		 */
		activateFirstTab: function() {
			this.setActiveTab(0);
		},

		// CMTabbedWidgetDelegate
			/**
			 * @returns {CMDBuild.view.management.workflow.panel.form.tabs.attachments.AttachmentsView}
			 */
			getAttachmentsPanel: function() {
				if (!Ext.isEmpty(this.delegate) && !Ext.isEmpty(this.delegate.controllerTabAttachments))
					return this.delegate.controllerTabAttachments.getView();

				return null;
			},

			/**
			 * @returns {CMDBuild.view.management.workflow.panel.form.tabs.note.NoteView}
			 */
			getNotesPanel: function() {
				if (!Ext.isEmpty(this.delegate) && !Ext.isEmpty(this.delegate.controllerTabNote))
					return this.delegate.controllerTabNote.getView();

				return null;
			},

			/**
			 * @returns {CMDBuild.view.management.workflow.panel.form.tabs.email.Email}
			 */
			getEmailPanel: function() {
				if (!Ext.isEmpty(this.delegate) && !Ext.isEmpty(this.delegate.controllerTabEmail))
					return this.delegate.controllerTabEmail.getView();

				return null;
			},

			/**
			 * Returns false if is not able to manage the widget
			 *
			 * @returns {Boolean}
			 */
			showWidget: function (w) {
				var managedClasses = {
					"CMDBuild.view.management.workflow.panel.form.tabs.attachments.AttachmentsView": function (me) {
						var widgetRelatedPanel = me.getAttachmentsPanel();

						if (!Ext.isEmpty(widgetRelatedPanel) && Ext.isFunction(widgetRelatedPanel.cmActivate))
							widgetRelatedPanel.cmActivate();
					},
					"CMDBuild.view.management.workflow.panel.form.tabs.note.NoteView": function (me) {
						var widgetRelatedPanel = me.getNotesPanel();

						if (!Ext.isEmpty(widgetRelatedPanel) && Ext.isFunction(widgetRelatedPanel.cmActivate))
							widgetRelatedPanel.cmActivate();
					},
					'CMDBuild.view.management.workflow.panel.form.tabs.email.Email': function (me) {
						var widgetRelatedPanel = me.getEmailPanel();

						if (!Ext.isEmpty(widgetRelatedPanel) && Ext.isFunction(widgetRelatedPanel.cmActivate))
							widgetRelatedPanel.cmActivate();
					}
				};

				var fn = managedClasses[Ext.getClassName(w)];

				if (typeof fn == "function") {
					fn(this);
					return true;
				} else {
					return false;
				}
			}
	});

})();
