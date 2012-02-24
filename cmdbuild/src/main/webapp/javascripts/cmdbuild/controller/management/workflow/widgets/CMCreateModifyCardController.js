(function() {
	var OUTPUT_NAME = "xa:outputName",
		ID_CLASS = "xa:idClass",
		ID_CARD = "xa:cardId";

	Ext.define("CMDBuild.controller.management.workflow.widgets.CMCreateModifyCard", {
		extend: "CMDBuild.controller.management.classes.CMBaseCardPanelController",

		cmName: "Create/Modify card",

		constructor: function(ui, supercontroller, widgetDef, widgetControllerManager) {
			this.callParent([ui, supercontroller, widgetControllerManager]);

			this.templateResolverIsBusy = false;
			this.idClassToAdd = undefined;
			this.savedCardId = undefined;

			this.templateResolver = new CMDBuild.Management.TemplateResolver({
				clientForm: this.view.clientForm,
				xaVars: this.view.widgetConf,
				serverVars: this.view.activity
			});

			this.idClass = this.getVariable(ID_CLASS);
			this.outputName = this.getVariable(OUTPUT_NAME);

			this.mon(this.view.addCardButton, "cmClick", onAddCardClick, this);
		},

		getCardId: function() {
			// remember that -1 is the id for a new card
			var idCard = this.getVariable(ID_CARD);
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
		},

		// override
		onSaveSuccess: function(form, operation) {
			this.callParent(arguments);
			if (typeof this.superController.hideWidgetsContainer == "function") {
				this.superController.hideWidgetsContainer();
				updateLocalDepsIfReferenceToModifiedClass(this, operation.result.id);
			}
		},

		// **** BaseWFWidget methods

		toString: function() {
			return this.cmName + " WFWidget controller";
		},

		isBusy: function() {
			_debug(this + " is busy");
			return false;
		},

		getData: function() {
			return null;
		},

		getVariable: function(variableName) {
			try {
				return this.templateResolver.getVariable(variableName);
			} catch (e) {
				_debug("There is no template resolver");
				return undefined;
			}
		},

		onEditMode: function() {
			// for the auto-select
			resolveTemplate.call(this);
		},

		beforeActiveView: function() {
			var et = _CMCache.getEntryTypeById(this.idClass);
			this.cardId = this.getCardId();
			this.view.initWidget(this.idClass, this.cardId);
			this.onEntryTypeSelected(et);

			if (!this.cardId || this.cardId == -1 && !et.data.superclass) {
				this.onAddCardButtonClick(this.idClass, reloadFields = true);
			} else {
				var data = {
					IdClass: this.idClass,
					Id: this.cardId
				}, card = {
					get: function(k) {
						return data[k];
					}
				}, me = this;

				if (this.widgetControllerManager) {
					this.widgetControllerManager.buildControllers(card);
				}

				CMDBuild.ServiceProxy.card.get({
					params: data,
					scope: this,
					success: function(a,b, response) {
						var raw = response.card;
						if (raw) {
							var c = new CMDBuild.DummyModel(response.card);
							c.raw = raw;
							this.cmForceEditing = !et.data.superclass // see loadCardStandardCallBack of CMBaseCardPanelController
							this.onCardSelected(c);
						}
					}
				});
			}
		},

		// override
		getData: function() {
			var out = null;
			if (undefined != this.outputName && this.savedCardId) {
				out = {};
				out[this.outputName] = this.savedCardId;
			}

			return out;
		},

		isValid: function() {
			return true;
		}
	});

	function updateLocalDepsIfReferenceToModifiedClass(me, id) {
		var deps = me.templateResolver.getLocalDepsAsField();
		id = id || me.card.get("Id");

		if (deps) {
			for (var key in deps) {
				var field = deps[key];
				if (field &&
					field.CMAttribute &&
					field.CMAttribute.referencedIdClass == me.idClass) {

					field.store.load({
						callback: function() {
							field.setValue(id);
						}
					});
				}
			}
		}
	}

	function onAddCardClick(o) {
		this.onAddCardButtonClick(o.classId);
	}

	function resolveTemplate() {
		resolve.call(this);

		function resolve() {
			this.templateResolverIsBusy = true;

			this.templateResolver.resolveTemplates({
				attributes: [ 'ObjId' ],
				callback: onTemplateResolved,
				scope: this
			});
		}

		function onTemplateResolved(out, ctx) {
			this.templateResolverIsBusy = false;

			addListenerToDeps.call(this);
		}
	}

	function addListenerToDeps() {
		var ld = this.templateResolver.getLocalDepsAsField();
		for (var i in ld) {
			//before the blur if the value is changed
			var field = ld[i];

			if (field) {
				field.mon(field, "change", function(f) {
					f.changed = true;
				}, this);

				field.mon(field, "blur", function(f) {
					if (f.changed) {
						resolveTemplate.call(this);
						f.changed = false;
					}
				}, this);
			}
		}
	}
})();