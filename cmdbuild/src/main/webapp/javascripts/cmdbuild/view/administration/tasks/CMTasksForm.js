(function() {

	var tr = CMDBuild.Translation.administration.tasks;

	/**
	 * Form for task configuration (a.k.a. "wizard")
	 */
	Ext.define('CMDBuild.view.administration.tasks.CMTasksForm', {
		extend: 'Ext.form.Panel',

		mixins: {
			cmFormFunctions: 'CMDBUild.view.common.CMFormFunctions'
		},

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {Mixed} Task specific controller
		 */
		delegate: undefined,

		activeItem: 0,
		bodyCls: 'cmgraypanel',
		border: false,
		buttonAlign: 'center',
		cls: 'x-panel-body-default-framed cmbordertop',
		frame: false,
		split: true,

		layout: {
			type: 'card',
			align:'stretch'
		},

		defaults: {
			layout: 'fit'
		},

		initComponent: function() {
			// Buttons configuration
			this.abortButton = Ext.create('CMDBuild.core.buttons.Abort', {
				scope: this,

				handler: function(button, e) {
					this.delegate.cmOn('onAbortButtonClick');
				}
			});

			this.cloneButton = Ext.create('CMDBuild.core.buttons.Clone', {
				text: tr.clone,
				scope: this,

				handler: function(button, e) {
					this.delegate.cmOn('onCloneButtonClick');
				}
			});

			this.modifyButton = Ext.create('CMDBuild.core.buttons.Modify', {
				text: tr.modify,
				scope: this,

				handler: function(button, e) {
					this.delegate.cmOn('onModifyButtonClick');
				}
			});

			this.nextButton = Ext.create('CMDBuild.core.buttons.Next', {
				scope: this,

				handler: function(button, e) {
					this.delegate.cmOn('onNextButtonClick');
				}
			});

			this.previousButton = Ext.create('CMDBuild.core.buttons.Previous', {
				scope: this,

				handler: function(button, e) {
					this.delegate.cmOn('onPreviousButtonClick');
				}
			});

			this.removeButton = Ext.create('CMDBuild.core.buttons.Delete', {
				text: tr.remove,
				scope: this,

				handler: function(button, e) {
					this.delegate.cmOn('onRemoveButtonClick');
				}
			});

			this.saveButton = Ext.create('CMDBuild.core.buttons.Save', {
				scope: this,

				handler: function(button, e) {
					this.delegate.cmOn('onSaveButtonClick');
				}
			});
			// END: Buttons configuration

			this.cmTBar = [this.modifyButton, this.removeButton, this.cloneButton];
			this.cmButtons = [this.previousButton, this.saveButton, this.abortButton, this.nextButton];

			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,
						items: this.cmTBar
					})
				],
				buttons: this.cmButtons
			});

			this.callParent(arguments);

			this.disableModify();
		}
	});

})();