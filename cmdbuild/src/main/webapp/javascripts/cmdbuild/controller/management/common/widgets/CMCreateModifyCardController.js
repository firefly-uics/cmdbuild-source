(function() {
	Ext.define("CMDBuild.controller.management.common.widgets.CMCreateModifyCardController", {
		extend: "CMDBuild.controller.management.classes.CMBaseCardPanelController",

		mixins : {
			observable : "Ext.util.Observable"
		},

		statics: {
			WIDGET_NAME: CMDBuild.view.management.common.widgets.CMCreateModifyCard.WIDGET_NAME
		},

		cmName: "Create/Modify card",

		constructor: function(ui, supercontroller, widget, clientForm, card) {
			var widgetControllerManager = new CMDBuild.controller.management.classes.CMWidgetManager(ui.getWidgetManager());

			this.callParent([ui, supercontroller, widgetControllerManager]);

			this.mixins.observable.constructor.call(this, arguments);

			this.widget = widget;
			this.clientForm = clientForm;
			this.templateResolverIsBusy = false;
			this.idClassToAdd = undefined;
			this.savedCardId = undefined;
			this.wiewIdenrifier = widget.id;

			this.templateResolver = new CMDBuild.Management.TemplateResolver({
				clientForm: clientForm,
				xaVars: widget,
				serverVars: card.raw || card.data
			});

			this.mon(this.view.addCardButton, "cmClick", onAddCardClick, this);
		},

		// override
		onSaveSuccess: function(form, operation) {
			this.callParent(arguments);
			this.savedCardId = operation.result.id || this.cardId;

			if (typeof this.superController.hideWidgetsContainer == "function") {
				this.superController.hideWidgetsContainer();
				updateLocalDepsIfReferenceToModifiedClass(this);
			}
		},

		// override
		loadCardStandardCallBack: function(card) {
			var me = this;
			this.card = card;
			this.loadFields(card.get("IdClass"), function() {
				me.view.loadCard(card, bothpanel = true);
				if (me.isEditable(card)) {
					me.view.editMode();
				}
			});
		},

		getCQLOfTheCardId: function() {
			return this.widget.idcardcqlselector;
		},

		isWidgetEditable: function(controller) {
			return (!this.widget.readonly) 
				&& this.clientForm.owner._isInEditMode; // Ugly, but in the world there are also ugly stuff
		},

		// **** BaseWFWidget methods

		beforeActiveView: function() {
			var me = this;
			this.card = null;
			this.targetClassName = this.widget.targetClass;
			this.entryType = _CMCache.getEntryTypeByName(this.targetClassName);

			this.view.initWidget(this.entryType, this.isWidgetEditable());

			this.templateResolver.resolveTemplates({
				attributes: ["idcardcqlselector"],
				callback: function(o) {
					me.cardId = normalizeIdCard(o["idcardcqlselector"]);
					loadAndFillFields(me);
				}
			});
		},

		isBusy: function() {
			_debug(this + " is busy");
			return false;
		},

		getData: function() {
			var out = null;
			if (undefined != this.outputName && this.savedCardId) {
				out = {};
				out[this.outputName] = this.savedCardId;
			}

			return out;
		},

		getVariable: function(variableName) {
			try {
				return this.templateResolver.getVariable(variableName);
			} catch (e) {
				_debug("There is no template resolver");
				return undefined;
			}
		},

		isValid: function() {
			return true;
		},

		isEditable: function() {
			return this.callParent(arguments) && this.isWidgetEditable();
		},

		toString: function() {
			return this.cmName + " WFWidget controller";
		}
	});

	function loadAndFillFields(me) {
		var classId = me.entryType.getId();
		var isANewCard = me.cardId == -1;

		if (isANewCard) {
				me.card = new CMDBuild.DummyModel({
					Id: -1,
					IdClass: classId
				});
				me.loadCard();
		} else {
			me.loadCard(loadRemoteData = true, {
				Id: me.cardId,
				IdClass: classId
			});
		}
	}

	function normalizeIdCard(idCard) {
		// remember that -1 is the id for a new card
		if (!idCard) {
			return -1;
		}

		if (typeof idCard == "string") {
			idCard = parseInt(idCard);
			if (isNaN(idCard)) {
				idCard = -1;
			}
		}

		return idCard;
	}

	function updateLocalDepsIfReferenceToModifiedClass(me) {
		// we will synch the id of the modifyed
		// card with the reference that points to it
		// This is allowed only if the CQL used to get the id
		// of the card to modify is a simple pointer to a form field,
		// es {cliengt:field_name}

		var referenceRX = /^\{client:(\w+)\}$/;
		var cql = me.getCQLOfTheCardId();
		var match = referenceRX.exec(cql);
		if (match != null) {
			var referenceName = match[1];
			if (referenceName) {
				var field = getFieldByName(me, referenceName);
				if (field &&
					field.CMAttribute &&
					field.CMAttribute.referencedIdClass == me.idClass) {

					field.store.load({
						callback: function() {
							field.setValue(me.savedCardId);
						}
					});
				}
			}
		}
	}

	function getFieldByName(me, name) {
		return me.clientForm.getFields().findBy(
			function findCriteria(f) {
				if (!f.CMAttribute) {
					return false;
				} else {
					return f.CMAttribute.name == name;
				}
			}
		);
	}

	function onAddCardClick(o) {
		loadAndFillFields(this, o.classId, cardId = -1);
	}
})();