(function() {

	Ext.define("CMDBuild.view.administration.tasks.CMTasksPanel", {
		extend: "Ext.form.Panel",
		mixins: {
			cmFormFunctions: "CMDBUild.view.common.CMFormFunctions"
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
			this.modifyButton = new Ext.button.Button({
				iconCls: 'modify',
				text: "@@ Modify Task",
				handler: function() {
					me.delegate.cmOn('onModifyButtonClick', {}, null);
				}
			});
			
			this.removeButton = new Ext.button.Button({
				iconCls: 'delete',
				text: "@@ Remove task",
				handler: function() {
					me.delegate.cmOn('onRemoveButtonClick', {}, null);
				}
			});

			this.saveButton = new CMDBuild.buttons.SaveButton({
				handler: function() {
					me.delegate.cmOn('onSaveButtonClick', {}, null);
				}
			});

			this.abortButton = new CMDBuild.buttons.AbortButton({
				handler: function() {
					me.delegate.cmOn('onCancelButtonClick', {}, null);
				}
			});
			this.previousButton = new CMDBuild.buttons.PreviousButton({
				handler: function() {
					me.delegate.cmOn('onPreviousButtonClick', {}, null);
				}
			});

			this.nextButton = new CMDBuild.buttons.NextButton({
				handler: function() {
					me.delegate.cmOn('onNextButtonClick', {}, null);
				}
			});
			// END: Buttons configuration

			// Page FieldSets configuration
			this.wizard = new CMDBuild.view.administration.tasks.CMTasksWizard();
			Ext.apply(this, {
				tbar: [this.modifyButton, this.removeButton],
				items: [this.wizard],
				buttons: [this.previousButton, this.saveButton, this.abortButton, this.nextButton]
			});

			this.callParent(arguments);
			//this.delegate.cmOn("onInizializeWizardButtons", {}, undefined);
			//this.disableModify();
		},

		/**
		 * @param {xx} xx
		 * Description
		 */
		onTaskSelected: function(currentTask) {
//			var me = this, store = this.defaultGroupStore;
//			this.reset();
//			this.disableModify(enableCMTBar = true);
//			this.updateDisableActionTextAndIconClass(emailAccount.get("isActive"));
//			store.load( {
//				params : {
//					userid: user.get("userid")
//				},
//				callback: function() {
//					var defaultGroup = store.findRecord('isdefault', true);
//					if (defaultGroup) {
//						user.set("defaultgroup", defaultGroup.getId());
//					}
//					me.getForm().loadRecord(user);
//				}
//			});
		}
	});

})();