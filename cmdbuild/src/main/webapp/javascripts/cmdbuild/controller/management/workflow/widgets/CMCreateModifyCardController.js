(function() {
	var OUTPUT_NAME = "xa:outputName",
		ID_CLASS = "xa:idClass",
		ID_CARD = "xa:id";

	Ext.define("CMDBuild.controller.management.workflow.widgets.CMCreateModifyCard", {
		extend: "CMDBuild.controller.management.workflow.widget.CMBaseWFWidgetController",
		cmName: "Create/Modify card",

		constructor: function() {
			this.callParent(arguments);

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

			this.view.mon(this.view.cancelButton, "click", onAbortCardClick, this);
			this.view.mon(this.view.saveButton, "click", onSaveCardClick, this);
			this.view.mon(this.view.addCardButton, "cmClick", onAddCardClick, this);
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
		onEditMode: function() {
			// for the auto-select
			resolveTemplate.call(this);
		},

		// override
		beforeActiveView: function() {
			this.cardId = this.getCardId();
			this.view.initWidget(this.idClass, this.cardId);
		},

		// override
		getData: function() {
			var out = null;
			if (undefined != this.outputName && this.savedCardId) {
				out = {};
				out[this.outputName] = this.savedCardId;
			}

			return out;
		}
	});

	function onAddCardClick(p) {
		this.idClassToAdd = p.classId;
		this.view.onAddCardButtonClick(this.idClassToAdd, reloadFields = true);
	}

	function onAbortCardClick() {
		this.ownerController.showActivityPanel();
	}

	function onSaveCardClick() {
		var params = {},
			form = this.view.getForm(),
			view = this.view,
			invalidAttributes = this.view.getInvalidAttributeAsHTML();
		
		params = {
			// this.idClassToAdd has a value only if was pushed the add button for
			// super-classes
			IdClass: this.idClassToAdd || this.idClass,
			Id: this.cardId
		};

		if (invalidAttributes == null) {
			CMDBuild.LoadMask.get().show();
			form.submit({
				method : 'POST',
				url : 'services/json/management/modcard/updatecard',
				scope: this,
				params: params,

				success : function(form, action) {
					CMDBuild.LoadMask.get().hide();
					this.savedCardId = action.result.id;
					_CMCache.onClassContentChanged(params.IdClass);
					onAbortCardClick.call(this);
				},

				failure : function() {
					CMDBuild.LoadMask.get().hide();
				}
			});
		} else {
			var msg = Ext.String.format("<p class=\"{0}\">{1}</p>", CMDBuild.Constants.css.error_msg, CMDBuild.Translation.errors.invalid_attributes);
			CMDBuild.Msg.error(null, msg + invalidAttributes, false);
		}

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