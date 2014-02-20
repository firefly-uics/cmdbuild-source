(function() {

	Ext.define('CMDBuild.view.administration.tasks.CMTasksForm', {
		extend: 'Ext.form.Panel',

		mixins: {
			cmFormFunctions: 'CMDBUild.view.common.CMFormFunctions'
		},

		delegate: undefined,

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
			this.abortButton = Ext.create('CMDBuild.buttons.AbortButton', {
				handler: function() {
					me.delegate.cmOn('onAbortButtonClick');
				}
			});

			this.cloneButton = Ext.create('Ext.button.Button', {
				iconCls: 'clone',
				text: '@@ Clone task',
				handler: function() {
					me.delegate.cmOn('onCloneButtonClick');
				}
			});

			this.modifyButton = Ext.create('Ext.button.Button', {
				iconCls: 'modify',
				text: '@@ Modify Task',
				handler: function() {
					me.delegate.cmOn('onModifyButtonClick');
				}
			});

			this.nextButton = Ext.create('CMDBuild.buttons.NextButton', {
				handler: function() {
					me.delegate.cmOn('onNextButtonClick');
				}
			});

			this.previousButton = Ext.create('CMDBuild.buttons.PreviousButton', {
				handler: function() {
					me.delegate.cmOn('onPreviousButtonClick');
				}
			});

			this.removeButton = Ext.create('Ext.button.Button', {
				iconCls: 'delete',
				text: '@@ Remove task',
				handler: function() {
					me.delegate.cmOn('onRemoveButtonClick');
				}
			});

			this.saveButton = Ext.create('CMDBuild.buttons.SaveButton', {
				handler: function() {
					me.delegate.cmOn('onSaveButtonClick');
				}
			});
			// END: Buttons configuration

			// Page FieldSets configuration
			this.wizard = Ext.create('CMDBuild.view.administration.tasks.CMTasksWizard');
			this.cmTBar = [this.modifyButton, this.removeButton, this.cloneButton];
			this.cmButtons = [this.previousButton, this.saveButton, this.abortButton, this.nextButton];

			Ext.apply(this, {
				tbar: this.cmTBar,
				items: [this.wizard],
				buttons: this.cmButtons
			});

			this.callParent(arguments);
		},

		/**
		 * Disable type field
		 */
		disableTypeField: function() {
			this.wizard.items.get(0).typeField.setDisabled(true);
		}
	});

})();