(function() {
	// TODO 1) find a lot of time
	// TODO 2) when I have found it, do mixing to apply the baseWFController methods
	// I know that code reuse != copy and paste

	var OUTPUT_NAME = "xa:outputName",
		ID_CLASS = "xa:idClass",
		ID_CARD = "xa:id",
		TRUE = "1";

	Ext.define("CMDBuild.controller.management.workflow.widgets.CMManageRelationController", {
		extend: "CMDBuild.controller.management.classes.CMCardRelationsController",
		cmName: "Create/Modify card",

		constructor: function(view, ownerController, widgetDef) {
			this.callParent(arguments);

			this.widgetConf = widgetDef;
			this.ownerController = ownerController;
			this.outputName = this.widgetConf.outputName;
			this.noSelect = !(this.widgetConf.enabledFunctions['single'] || this.widgetConf.enabledFunctions['multi']);
			this.wiewIdenrifier = this.widgetConf.identifier;

			this.templateResolver = new CMDBuild.Management.TemplateResolver({
				clientForm: this.view.clientForm,
				xaVars: this.view.widgetConf,
				serverVars: this.view.activity
			});

			this.idClass = this.getVariable(ID_CLASS);

			this.outputName = this.getVariable(OUTPUT_NAME);

			this.templateResolverIsBusy = false;

			this.callBacks = Ext.apply(this.callBacks, {
				'action-relation-deletecard': this.onDeleteCard
			});

			this.card = getFakeCard(this);
		},

		onDeleteCard: function(model) {
			this.cardToDelete = model;
			this.onDeleteRelationClick(model);
		},

		// override
		onDeleteRelationSuccess: function() {
			if (this.cardToDelete) {
				removeCard.call(this);
			} else {
				this.defaultOperationSuccess();
			}
		},

		defaultOperationSuccess: function() {
			this.loadData();
		},

		// baseWFWidget Functions
		toString: function() {
			return this.cmName + " WFWidget controller";
		},

		isBusy: function() {
			_debug(this + " is busy");
			return false;
		},

		getVariable: function(variableName) {
			try {
				return this.templateResolver.getVariable(variableName);
			} catch (e) {
				_debug("There is no template resolver");
				return undefined;
			}
		},
		// end baseWFWidget Functions

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

		onEditMode: function() {
			resolveTemplate.call(this);
		},

		// override
		beforeActiveView: function() {
			var et = _CMCache.getEntryTypeById(this.idClass);
			this.view.addRelationButton.setDomainsForEntryType(et);

			this.cardId = this.getCardId();
			if (this.cardId > 0) {
				this.loadData();
				this.view.addRelationButton.enable();
			} else {
				this.view.fillWithData();
				this.view.addRelationButton.disable();
			}
		},

		getData: function() {
			var out = null;
			if (undefined != this.outputName) {
				out = {};
				var data = [],
					nodes = Ext.query('input[name='+this.outputName+']');

				Ext.each(nodes, function(item) {
					if(item.checked) {
						data.push(item.value);
					}
				});

				out[this.outputName] = data;
			}

			return out;
		},

		isValid: function() {
			if (!this.noSelect && this.widgetConf.Required == TRUE) {
				try {
					return this.getData()[this.outputName].length > 0;
				} catch (e) {
					// if here data is null or data has not this.outputName,
					// so the ww is not valid
					return false;
				}

			} else {
				return true;
			}
		},

		// override
		loadData: function() {
			var el = this.view.getEl();
			if (el) {
				el.mask();
			}

			buildAdapterForExpandNode.call(this);

			CMDBuild.ServiceProxy.relations.getList({
				params: {
					Id: this.cardId,
					IdClass: this.idClass,
					domainId: this.widgetConf.domainIdNoDir,
					src: getSrc.call(this)
				},
				scope: this,
				success: function(a,b, response) {
					el.unmask();
					this.view.fillWithData(response.domains);
				}
			});
		}
	});

	function removeCard() {
		if (this.cardToDelete) {
			var me = this;
			CMDBuild.LoadMask.get().show();
			CMDBuild.ServiceProxy.card.remove({
				important: true,
				params : {
					"IdClass": me.cardToDelete.get("dst_cid"),
					"Id": me.cardToDelete.get("dst_id")
				},
				callback : function() {
					CMDBuild.LoadMask.get().hide();
					delete me.cardToDelete;
					me.loadData();
				}
			});
		}
	}

	function buildAdapterForExpandNode() {
		var data = {
			Id: this.cardId,
			IdClass: this.idClass
		};
		this.currentCard = {
			get: function(k) {
				return data[k];
			}
		}
	}

	function getSrc() {
		var src,
			directedDomain = this.widgetConf.domainId.split("_");

		if (directedDomain[1] == "D") {
			src = "_1";
		} else {
			src = "_2";
		}
		return src;
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
			var field = ld[i];

			if (field) {
				// Extjs 4 fires the change event for any key press.
				// we mark the field as changed and use this flag
				// on blur, to run the template resolver only when
				// the user leaves the field
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

	// a object that fake a card,
	// is passed at the ModifyRelationWindow
	function getFakeCard(me) {
		var data = {
			Id: me.getVariable(ID_CARD),
			IdClass: me.getVariable(ID_CLASS)
		}

		return {
			get: function(k) {
				return data[k];
			}
		}
	}
})();