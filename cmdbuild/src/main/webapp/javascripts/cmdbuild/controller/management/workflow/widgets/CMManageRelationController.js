(function() {
	// TODO 1) find a lot of time
	// TODO 2) when I have found it, do mixing to apply the baseWFController methods
	// code reuse != copy and paste

	var OUTPUT_NAME = "xa:outputName",
		ID_CLASS = "xa:idClass",
		ID_CARD = "xa:id";

	Ext.define("CMDBuild.controller.management.workflow.widgets.CMManageRelationController", {
		extend: "CMDBuild.controller.management.classes.CMCardRelationsController",
		cmName: "Create/Modify card",

		constructor: function(view, ownerCtrl) {
			this.callParent(arguments);

			this.widgetConf = this.view.widgetConf;
			this.outputName = this.widgetConf.outputName;
			this.singleSelect = this.widgetConf.SingleSelect;
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
				'action-card-modify': this.onModifyCard,
				'action-card-delete': this.onDeleteCard
			})
		},

		onModifyCard: function() {
			alert("On modify card");
		},

		onDeleteCard: function() {
			alert("On delete card");
		},

		// baseWFWidget Functions
		activeView: function() {
			this.beforeActiveView();
			this.view.cmActivate();
		},

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
		// baseWFWidget Functions

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
})();