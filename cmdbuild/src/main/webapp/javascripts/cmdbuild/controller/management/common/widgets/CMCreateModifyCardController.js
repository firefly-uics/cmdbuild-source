(function() {

	Ext.define("CMDBuild.controller.management.classes.widgets.CMCreateModifyCardWidgetReader", {
		configure: function(controller) {
			controller.templateResolver.resolveTemplates({
				attributes: ["idcardcqlselector"],
				callback: function(o) {
					controller.idClass = controller.widgetDef.targetClass;
					controller.cardId = normalizeIdCard(o["idcardcqlselector"]);
				}
			});
		},

		getCQLOfTheCardId: function(widgetDef) {
			return widgetDef.idcardcqlselector
		},

		isEditable: function(controller) {
			return (!controller.widgetDef.readonly) 
				&& controller.clientForm.owner._isInEditMode; // Ugly, but in the world there are also ugly stuff
		}
	});

	Ext.define("CMDBuild.controller.management.workflow.widgets.CMCreateModifyCardWidgetReader", {
		configure: function(controller) {
			controller.templateResolver.resolveTemplates({
				attributes: ["outputName","idClass","ObjId"],
				callback: function(o) {
					controller.idClass = o["idClass"];
					controller.outputName = o["outputName"],
					controller.cardId = normalizeIdCard(o["ObjId"]);
				}
			});
		},

		getCQLOfTheCardId: function(widgetDef) {
			return widgetDef.ObjId;
		},

		isEditable: function(controller) {
			return !controller.widgetDef.ReadOnly;
		}
	});

	Ext.define("CMDBuild.controller.management.common.widgets.CMCreateModifyCardController", {
		extend: "CMDBuild.controller.management.classes.CMBaseCardPanelController",

		statics: {
			WIDGET_NAME: CMDBuild.view.management.common.widgets.CMCreateModifyCard.WIDGET_NAME
		},

		cmName: "Create/Modify card",

		constructor: function(ui, supercontroller, widgetDef, clientForm, card, reader, widgetControllerManager) {
			this.callParent([ui, supercontroller, widgetControllerManager]);

			this.widgetDef = widgetDef;
			this.clientForm = clientForm;
			this.templateResolverIsBusy = false;
			this.idClassToAdd = undefined;
			this.savedCardId = undefined;
			this.wiewIdenrifier = widgetDef.identifier;
			this.reader = reader;

			this.templateResolver = new CMDBuild.Management.TemplateResolver({
				clientForm: clientForm,
				xaVars: widgetDef,
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

		// **** BaseWFWidget methods

		beforeActiveView: function() {
			this.card = null;
			this.reader.configure(this);
			this.entryType = _CMCache.getEntryTypeById(this.idClass);
			this.view.initWidget(this.idClass, this.cardId, this.reader.isEditable(this));
			loadAndFillFields(this, this.idClass, this.cardId);
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
			return this.callParent(arguments) && this.reader.isEditable(this);
		},

		toString: function() {
			return this.cmName + " WFWidget controller";
		}
	});

	function loadAndFillFields(me, classId, cardId) {
		var isANewCard = cardId == -1;

		if (isANewCard) {
				me.card = new CMDBuild.DummyModel({
					Id: -1,
					IdClass: classId
				});
				me.loadCard();
		} else {
			me.loadCard(loadRemoteData = true, {
				Id: cardId,
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
		var cql = me.reader.getCQLOfTheCardId(me.widgetDef);
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