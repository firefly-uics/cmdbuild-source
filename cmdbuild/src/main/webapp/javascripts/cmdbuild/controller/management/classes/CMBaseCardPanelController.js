(function() {
	Ext.define("CMDBuild.controller.management.classes.CMBaseCardPanelController", {
		extend: "CMDBuild.controller.management.classes.CMModCardSubController",
		constructor: function(view, supercontroller, widgetControllerManager) {
			this.callParent(arguments);

			if (widgetControllerManager) {
				this.widgetControllerManager = widgetControllerManager;
			} else {
				var widgetManager = new CMDBuild.view.management.classes.CMWidgetManager(this.view);
				this.widgetControllerManager = new CMDBuild.controller.management.classes.CMWidgetManager(widgetManager);
			}

			this.CMEVENTS = {
				cardSaved: "cm-card-saved"
			};

			this.addEvents(this.CMEVENTS.cardSaved);

			var ev = this.view.CMEVENTS;
			this.mon(this.view, ev.modifyCardButtonClick, function() { this.onModifyCardClick.apply(this, arguments); }, this);
			this.mon(this.view, ev.saveCardButtonClick, function() { this.onSaveCardClick.apply(this, arguments); }, this);
			this.mon(this.view, ev.abortButtonClick, function() { this.onAbortCardClick.apply(this, arguments); }, this);
			this.mon(this.view, ev.widgetButtonClick, this.onWidgetButtonClick, this);
		},

		onEntryTypeSelected: function() {
			this.callParent(arguments);
			this.view.displayMode();
			this.loadFields(this.entryType.get("id"));
		},

		onCardSelected: function(card) {
			this.callParent(arguments);
			this.view.reset();
			this.view.displayMode();

			if (!this.entryType || !this.card) { return; }

			// If the current entryType is a superclass the record has only the value defined
			// in the super class. So, load the card remotly and pass it to the form.
			var loadRemoteData = this.entryType.get("superclass");

			// If the entryType id and the id of the card are different
			// the fields are not right, refill the form before the loadCard
			var reloadFields = this.entryType.get("id") != this.card.get("IdClass");

			if (this.widgetControllerManager) {
				this.widgetControllerManager.buildControllers(card);
			}

			var me = this;
			if (reloadFields) {
				this.loadFields(this.card.get("IdClass"), function() {
					me.loadCard(loadRemoteData);
				});
			} else {
				me.loadCard(loadRemoteData);
			}
		},

		onModifyCardClick: function() {
			if (this.isEditable(this.card)) {
				this.cloneCard = false;
				this.view.editMode();
			}
		},

		onSaveCardClick: function() {
			var me = this,
				params = {
					IdClass: this.card.get("IdClass"),
					Id: this.cloneCard ? -1 : this.card.get("Id")
				};

			addMapvaluesToSend(me, params);

			if (thereAraNotWrongAttributes(me)) {
				this.doFormSubmit(params);
			}
		},

		doFormSubmit: function(params) {
			var form = this.view.getForm(),
				me = this;

			CMDBuild.LoadMask.get().show();
			form.submit({
				method : 'POST',
				url : 'services/json/management/modcard/updatecard',
				scope: this,
				params: params,
				success : this.onSaveSuccess,
				failure : function() {
					CMDBuild.LoadMask.get().hide();
				}
			});
		},

		onSaveSuccess: function(form, operation) {
			var me = this;
			CMDBuild.LoadMask.get().hide();
			me.view.displayMode();
			var cardData = {
				Id: operation.result.id || me.card.get("Id"),// if is a new card, the id is given by the request
				IdClass: me.entryType.get("id")
			};
			me.fireEvent(me.CMEVENTS.cardSaved, cardData);
		},

		onAbortCardClick: function() {
			this.onCardSelected(this.card);
		},

		onAddCardButtonClick: function(classIdOfNewCard) {
			if (!classIdOfNewCard) {
				return;
			}

			this.onCardSelected(new CMDBuild.DummyModel({
				IdClass: classIdOfNewCard,
				Id: -1
			}));

			this.view.editMode();
		},

		loadFields: function(entryTypeId, cb) {
			var me = this;
			_CMCache.getAttributeList(entryTypeId, function(attributes) {
				me.view.fillForm(attributes, editMode = false);
				if (cb) {
					cb();
				}
			});
		},

		loadCard: function(loadRemoteData, params, cb) {
			var me = this;
			if (loadRemoteData) {
				params = params || {
					Id: me.card.get("Id"),
					IdClass: me.card.get("IdClass")
				};

				CMDBuild.ServiceProxy.card.get({
					params: params,
					success: function(a,b, response) {
						var data = response.card;
						if (me.card) {
						// Merge the data of the selected card with
						// the remote data loaded from the server.
						// the reason is that in the activity list
						// the card have data that are not returned from the
						// server, so use the data already in the record
							data = Ext.apply((me.card.raw || me.card.data), data);
						}
						var card = new CMDBuild.DummyModel(data);
						(typeof cb == "function") ? cb(card) : me.loadCardStandardCallBack(card)
					}
				});
			} else {
				me.loadCardStandardCallBack(me.card);
			}
		},

		loadCardStandardCallBack: function(card) {
			var me = this;
			me.view.loadCard(card);
			if (card) {
				if (me.isEditable(card)) {
					me.view.displayMode(enableTBar = true);
				} else {
					me.view.displayModeForNotEditableCard();
				}
			}
		},

		isEditable: function(card) {
			var data = card.raw || card.data;
			return data.priv_write;
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

	function addMapvaluesToSend(me, params) {
		// TODO manage map values
//		var mapValuesToSend = me.mapController.getValues();
//		if (mapValuesToSend) {
//			params["geoAttributes"] = mapValuesToSend;
//		}
		return params;
	}

	function thereAraNotWrongAttributes(me) {
		var invalidAttributes = me.view.getInvalidAttributeAsHTML();
		if (invalidAttributes != null) {
			var msg = Ext.String.format("<p class=\"{0}\">{1}</p>", CMDBuild.Constants.css.error_msg, CMDBuild.Translation.errors.invalid_attributes);
			CMDBuild.Msg.error(null, msg + invalidAttributes, false);
			return false;
		} else {
			return true;
		}
	}
})();