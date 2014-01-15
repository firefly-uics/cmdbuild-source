(function() {
	var tr = CMDBuild.Translation.administration.setup.email; // Path to translation

	Ext.define("CMDBuild.controller.administration.configuration.CMModConfigurationEmailControllerDelegate", {
		selectFirstRow: function() {},

		/**
		 * @param {CMDBuild.view.administration.common.basepanel.CMGridAndFormPanel} panel
		 * called from the panel after a click on the add button
		 */
		onAddButtonClick: function(panel) {},

		/**
		 * called after the save button click
		 * @param {CMDBuild.view.administration.common.basepanel.CMForm} form
		 */
		onSaveButtonClick: function(form) {},

		/**
		 * called after the confirmation of a remove
		 * @param {CMDBuild.view.administration.common.basepanel.CMForm} form
		 */
		onRemoveConfirmed: function(form) {},

		// as Form Delegate
		onModifyButtonClick: function(form) {},

		onRemoveButtonClick: function(form) {},

		onSaveButtonClick: function(form) {},

		onAbortButtonClick: function(form) {},

		// as Grid delegate
		onCMGridSelect: function(grid, record) {}
	});

	Ext.define("CMDBuild.view.administration.configuration.CMModConfigurationEmail", {
		extend: "Ext.panel.Panel",

		title: tr.title,
		frame: false,
		border: true,
		layout: 'border',

		initComponent: function() {
			var me = this;

			if (!this.delegate) {
				this.delegate = new CMDBuild.controller.administration.configuration.CMModConfigurationEmailControllerDelegate();
			}

			this.addButton = new Ext.Button({
				iconCls: 'add',
				text: tr.add,
				handler: function() {
					me.callDelegates("onGridAndFormPanelAddButtonClick", me);
				}
			});

			this.emailGrid = new CMDBuild.view.administration.configuration.CMModConfigurationEmailGrid({
				region: 'center'
			});

			this.emailForm = new CMDBuild.view.administration.configuration.CMModConfigurationEmailForm({
				region: 'south',
				height: '70%'
			});

			Ext.apply(this, {
				tbar: [this.addButton],
				items: [this.emailGrid, this.emailForm]
			});

			this.callParent(arguments);
		}
	});

})();