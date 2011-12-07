(function() {
	Ext.define("CMDBuild.controller.management.classes.CMCardPanelController", {
		extend: "CMDBuild.controller.management.classes.CMBaseCardPanelController",
		constructor: function(view, supercontroller, widgetControllerManager) {
			this.callParent(arguments);
			this.widgetControllerManager = widgetControllerManager;

			this.CMEVENTS = Ext.apply(this.CMEVENTS,  {
				cardRemoved: "cm-card-removed"
			});

			this.addEvents(this.CMEVENTS.cardRemoved);

			var ev = this.view.CMEVENTS;
			this.mon(this.view, ev.removeCardButtonClick, this.onRemoveCardClick, this);
			this.mon(this.view, ev.cloneCardButtonClick, this.onCloneCardClick, this);
			this.mon(this.view, ev.printCardButtonClick, this.onPrintCardMenuClick, this);
			this.mon(this.view, ev.openGraphButtonClick, this.onShowGraphClick, this);
			this.mon(this.view, ev.widgetButtonClick, this.onWidgetButtonClick, this);
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
		},

		onPrintCardMenuClick: function(format) {
			var me = this;
			if (typeof format != "string") {
				return
			}
			CMDBuild.LoadMask.get().show();
			CMDBuild.Ajax.request({
				url : 'services/json/management/modreport/printcarddetails',
				params : {
					IdClass: me.entryType.get("id"),
					Id: me.card.get("Id"),
					format: format
				},
				method : 'GET',
				scope : this,
				success: function(response) {
					var popup = window.open("services/json/management/modreport/printreportfactory", "Report", "height=400,width=550,status=no,toolbar=no,scrollbars=yes,menubar=no,location=no,resizable");
					if (!popup) {
						CMDBuild.Msg.warn(CMDBuild.Translation.warnings.warning_message,CMDBuild.Translation.warnings.popup_block);
					}
				},
				callback : function() {
					CMDBuild.LoadMask.get().hide();
				}
			});
		},

		setWidgetManager: function(wm) {
			this.widgetManager = wm;
		},

		onWidgetButtonClick: function(w) {
			if (this.widgetControllerManager) {
				this.widgetControllerManager.onWidgetButtonClick(w);
			}
		}
	});
})();