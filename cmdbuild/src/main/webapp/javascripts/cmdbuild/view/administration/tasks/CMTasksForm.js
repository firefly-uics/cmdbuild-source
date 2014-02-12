(function() {

	var delegate = null; // Controller handler

	Ext.define("CMDBuild.view.administration.tasks.CMTasksForm", {
		extend: "Ext.form.Panel",

		mixins: {
			cmFormFunctions: 'CMDBUild.view.common.CMFormFunctions'
		},

		autoScroll: false,
		buttonAlign: 'center',
		layout: 'fit',
		split: true,
		frame: false,
		border: false,
		cls: 'x-panel-body-default-framed cmbordertop',
		bodyCls: 'cmgraypanel',

		initComponent: function() {
			var me = this;

			// Buttons configuration
			this.abortButton = new CMDBuild.buttons.AbortButton({
				handler: function() {
					me.delegate.cmOn('onCancelButtonClick', {}, null);
				}
			});

			this.cloneButton = new Ext.button.Button({
				iconCls : 'clone',
				text : '@@ Clone task',
				handler: function() {
					me.delegate.cmOn('onCloneButtonClick', {}, null);
				}
			});

			this.modifyButton = new Ext.button.Button({
				iconCls: 'modify',
				text: '@@ Modify Task',
				handler: function() {
					me.delegate.cmOn('onModifyButtonClick', {}, null);
				}
			});

			this.nextButton = new CMDBuild.buttons.NextButton({
				handler: function() {
					me.delegate.cmOn('onNextButtonClick', {}, null);
				}
			});

			this.previousButton = new CMDBuild.buttons.PreviousButton({
				handler: function() {
					me.delegate.cmOn('onPreviousButtonClick', {}, null);
				}
			});

			this.removeButton = new Ext.button.Button({
				iconCls: 'delete',
				text: '@@ Remove task',
				handler: function() {
					me.delegate.cmOn('onRemoveButtonClick', {}, null);
				}
			});

			this.saveButton = new CMDBuild.buttons.SaveButton({
				handler: function() {
					me.delegate.cmOn('onSaveButtonClick', {}, null);
				}
			});
			// END: Buttons configuration

			// Page FieldSets configuration
			this.wizard = new CMDBuild.view.administration.tasks.CMTasksWizard();
			this.cmTBar = [this.modifyButton, this.removeButton, this.cloneButton];
			this.cmButtons = [this.previousButton, this.saveButton, this.abortButton, this.nextButton];

			Ext.apply(this, {
				tbar: this.cmTBar,
				items: [this.wizard],
				buttons: this.cmButtons
			});

			this.callParent(arguments);
		}
	});

})();