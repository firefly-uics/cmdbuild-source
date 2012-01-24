(function() {
	var tr =  CMDBuild.Translation.management.modworkflow,
		modeConvertionMatrix = {
			VIEW: "read",
			UPDATE: "write",
			REQUIRED: "required"
		};

	Ext.define("CMDBuild.view.management.workflow.CMActivityPanel", {
		extend: "CMDBuild.view.management.common.CMFormWithWidgetButtons",

		constructor: function() {
			this.callParent(arguments);

			this.CMEVENTS.advanceCardButtonClick = this.form.CMEVENTS.advanceCardButtonClick;
			this.addEvents(this.CMEVENTS.advanceCardButtonClick);
			this.relayEvents(this.form, [this.CMEVENTS.advanceCardButtonClick]);

			_CMUtils.forwardMethods(this, this.form, ["disableStopButton", "updateInfo"]);
		},

		buildForm: function() {
			return new CMDBuild.view.management.workflow.CMActivityPanel.Form({
				region: "center",
				cmOwner: this
			});
		},

		clear: function() {
			this.form.removeAll(destroy = true);
			this.form.updateInfo();
			this.widgets.removeAll(destroy = true);
			this.widgets.hide();
			this.displayMode();
		}
	});

	Ext.define("CMDBuild.view.management.workflow.CMActivityPanel.Form", {
		extend: "CMDBuild.view.management.classes.CMCardForm",
		constructor: function() {
			this.callParent(arguments);

			this.CMEVENTS.advanceCardButtonClick = "cm-advance";
			this.addEvents(this.CMEVENTS.advanceCardButtonClick);
		},

		// override
		buildTBar: function() {
			this.withToolBar = true;
			this.callParent(arguments);

			this.processStepName = new Ext.button.Button({
				overCls: Ext.button.Button.baseCls,
				pressedCls: Ext.button.Button.baseCls,
				disable: Ext.emptyFn
			});

			this.processStepCode = new Ext.button.Button({
				overCls: Ext.button.Button.baseCls,
				pressedCls: Ext.button.Button.baseCls,
				disable: Ext.emptyFn
			});
			
			this.modifyCardButton.setText(tr.modify_card)
			this.deleteCardButton.setText(tr.delete_card)
			this.cmTBar = [
				this.modifyCardButton,
				this.deleteCardButton,
				CMDBuild.Config.graph.enabled=="true" ? this.graphButton : '-',
				'->','-',
				this.processStepName,
				'-',
				this.processStepCode,
				' '
			];
		},

		// override
		buildButtons: function() {
			this.withButtons = true;
			this.callParent(arguments);
			var me = this;
			this.advanceButton = new Ext.button.Button({
				text: CMDBuild.Translation.common.buttons.workflow.advance,
				handler: function() {
					me.fireEvent(me.CMEVENTS.advanceCardButtonClick);
				}
			}),

			this.cmButtons = [this.saveButton, this.advanceButton, this.cancelButton];
		},

		disableStopButton : function() {
			this.deleteCardButton.disable();
		},

		updateInfo : function(card) {
			card = card || {};
			var data = card.raw || card.data || {};

			this.processStepName.setText(data.activityPerformerName || "");
			this.processStepCode.setText(data.Code || "");
		},

		canReconfigureTheForm: function() {
			return this.cmOwner.isTheActivePanel();
		}
	});

})();