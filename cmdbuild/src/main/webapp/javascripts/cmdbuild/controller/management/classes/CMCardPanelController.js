(function() {
	Ext.define("CMDBuild.controller.management.classes.CMCardPanelController", {

		extend: "CMDBuild.controller.management.classes.CMBaseCardPanelController",

		mixins : {
			observable : "Ext.util.Observable"
		},

		hasListeners: {},

		constructor: function(view, supercontroller, widgetControllerManager) {
			this.callParent(arguments);

			this.CMEVENTS = Ext.apply(this.CMEVENTS,  {
				cardRemoved: "cm-card-removed",
				cloneCard: "cm-card-clone"
			});

			this.addEvents(
				this.CMEVENTS.cardRemoved,
				this.CMEVENTS.cloneCard,
				this.CMEVENTS.cardSaved,
				this.CMEVENTS.editModeDidAcitvate,
				this.CMEVENTS.displayModeDidActivate
			);

			var ev = this.view.CMEVENTS;
			this.mon(this.view, ev.removeCardButtonClick, this.onRemoveCardClick, this);
			this.mon(this.view, ev.cloneCardButtonClick, this.onCloneCardClick, this);
			this.mon(this.view, ev.printCardButtonClick, this.onPrintCardMenuClick, this);
			this.mon(this.view, ev.openGraphButtonClick, this.onShowGraphClick, this);
		},

		onRemoveCardClick: function() {
			var me = this,
				idCard = me.card.get("Id"),
				idClass = me.entryType.get("id");

			function makeRequest(btn) {
				if (btn != 'yes') {
					return;
				}

				CMDBuild.LoadMask.get().show();
				CMDBuild.ServiceProxy.card.remove({
					scope : this,
					important: true,
					params : {
						IdClass: idClass,
						Id: idCard
					},
					success : function() {
						me.fireEvent(me.CMEVENTS.cardRemoved, idCard, idClass);
					},
					callback : function() {
						CMDBuild.LoadMask.get().hide();
					}
				});
			};

			Ext.Msg.confirm(CMDBuild.Translation.management.findfilter.msg.attention, CMDBuild.Translation.management.modcard.delete_card_confirm , makeRequest, this);
		},

		onCloneCardClick: function() {
			this.onModifyCardClick();
			this.cloneCard = true;
			this.fireEvent(this.CMEVENTS.cloneCard);
		},

		onAbortCardClick: function() {
			if (this.cloneCard) {
				this.onCardSelected(null);
				this.cloneCard = false;
			} else {
				this.callParent(arguments);
			}

			_CMUIState.onlyGridIfFullScreen();
		},

		onSaveSuccess: function() {
			this.callParent(arguments);
			_CMUIState.onlyGridIfFullScreen();
		},

		onPrintCardMenuClick: function(format) {
			if (typeof format != "string") {
				return;
			}

			var me = this;
			var params = {};
			params[_CMProxy.parameter.CLASS_NAME] = me.entryType
					.getName();
			params[_CMProxy.parameter.CARD_ID] = me.card
					.get("Id");
			params[_CMProxy.parameter.FORMAT] = format;

			CMDBuild.LoadMask.get().show();
			CMDBuild.Ajax.request({
				url: 'services/json/management/modreport/printcarddetails',
				params: params,
				method: 'GET',
				scope: this,
				success: function(response) {
					var popup = window.open(
							"services/json/management/modreport/printreportfactory", //
							"Report", //
							"height=400,width=550,status=no,toolbar=no,scrollbars=yes,menubar=no,location=no,resizable"); //

					if (!popup) {
						CMDBuild.Msg.warn( //
							CMDBuild.Translation.warnings.warning_message, //
							CMDBuild.Translation.warnings.popup_block //
						); //
					}

				},
				callback: function() {
					CMDBuild.LoadMask.get().hide();
				}
			});
		}
	});
})();