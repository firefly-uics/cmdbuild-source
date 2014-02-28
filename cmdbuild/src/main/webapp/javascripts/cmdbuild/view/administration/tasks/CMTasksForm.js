(function() {

	var tr = CMDBuild.Translation.administration.tasks; // Path to translation

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
				text: tr.clone,
				handler: function() {
					me.delegate.cmOn('onCloneButtonClick');
				}
			});

			this.modifyButton = Ext.create('Ext.button.Button', {
				iconCls: 'modify',
				text: tr.modify,
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
				text: tr.remove,
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
			this.wizard = Ext.create('CMDBuild.view.administration.tasks.CMTasksWizard', {
				previousButton: this.previousButton,
				nextButton: this.nextButton
			});
			this.cmTBar = [this.modifyButton, this.removeButton, this.cloneButton];
			this.cmButtons = [this.previousButton, this.saveButton, this.abortButton, this.nextButton];

			Ext.apply(this, {
				tbar: this.cmTBar,
				items: [this.wizard],
				buttons: this.cmButtons
			});

			this.callParent(arguments);
			this.disableModify();
			this.disableCMButtons();
		},

		disableTypeField: function() {
			this.wizard.items.get(0).typeField.setDisabled(true);
		},

		resetIdField: function() {
			this.wizard.items.get(0).idField.setValue();
		},

		submitForm:function(){
		    var record = formPanel.getForm().getRecord();
		    formPanel.getForm().updateRecord(record);
		    record.save();
		};
	});

})();