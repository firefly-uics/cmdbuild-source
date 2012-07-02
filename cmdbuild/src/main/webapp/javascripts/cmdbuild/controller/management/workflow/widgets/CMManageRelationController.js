(function() {
	// TODO 1) find a lot of time
	// TODO 2) when I have found it, do mixing to apply the baseWFController methods
	// I know that code reuse != copy and paste

	var ID_CLASS = "xa:idClass",
		ID_CARD = "xa:id",
		CARD_CQL_SELECTOR = "objId",
		reader = null;

	Ext.define("CMDBuild.controller.management.workflow.widgets.CMManageRelationController", {
		extend: "CMDBuild.controller.management.classes.CMCardRelationsController",

		statics: {
			WIDGET_NAME: ".ManageRelation"
		},

		constructor: function(view, ownerController, widgetDef, clientForm, card) {
			this.callParent(arguments);

			this.widgetConf = widgetDef;
			reader = CMDBuild.management.model.widget.ManageRelationConfigurationReader;
			ensureEntryType(this);

			this.templateResolver = new CMDBuild.Management.TemplateResolver({
				clientForm: clientForm,
				xaVars: widgetDef,
				serverVars: card
			});

			this.templateResolverIsBusy = false;
			this.ownerController = ownerController;
			this.outputName = reader.outputName(this.widgetConf);
			this.wiewIdenrifier = reader.id(this.widgetConf);
			this.readOnly = !(reader.singleSelection(this.widgetConf) 
				|| reader.multiSelection(this.widgetConf));

			this.idClass = this.targetEntryType.getId();
			this.domain = _CMCache.getDomainByName(reader.domainName(this.widgetConf));

			this.callBacks = Ext.apply(this.callBacks, {
				'action-relation-deletecard': this.onDeleteCard
			});
		},

		onDeleteCard: function(model) {
			this.cardToDelete = model;
			this.onDeleteRelationClick(model);
		},

		// override
		onFollowRelationClick: function(model) {
			this.callParent(arguments);
			this.ownerController.hideWidgetsContainer();
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

		getCardId: function(cb) {
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
			this.view.addRelationButton.setDomainsForEntryType(this.targetEntryType,
				this.domain.getId());

			var me = this;
			this.templateResolver.resolveTemplates({
				attributes: [CARD_CQL_SELECTOR],
				callback: function(o) {
					me.cardId = o[CARD_CQL_SELECTOR];
					me.card = getFakeCard(me);

					if (me.cardId > 0) {
						me.loadData();
						me.view.addRelationButton.enable();
					} else {
						me.view.fillWithData();
						me.view.addRelationButton.disable();
					}
				},
				scope: this
			});

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
			if (!this.readOnly && reader.required(this.widgetConf)) {
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
			var el = this.view.getEl(),
				domain = this.domain;

			if (domain == null) {
				_debug("It is not possible to lad data for null domain");
				return;
			}

			if (el) {
				el.mask();
			}

			buildAdapterForExpandNode.call(this);

			CMDBuild.ServiceProxy.relations.getList({
				params: {
					Id: this.cardId,
					IdClass: this.idClass,
					domainId: domain.getId(),
					src: getSrc.call(this)
				},
				scope: this,
				success: function(a,b, response) {
					if (el) { 
						el.unmask();
					};

					this.view.fillWithData(response.domains);
				}
			});
		},

		getLabel: function() {
			var label = "";
			if (this.widgetConf) {
				label = reader.label(this.widgetConf);
			}

			return label;
		}
	});

	function ensureEntryType(me) {
		me.targetEntryType = _CMCache.getEntryTypeByName(reader.className(me.widgetConf));

		if (me.targetEntryType == null) {
			throw {error: "There is no entry type for this widget", widget: me.widgetConf};
		}
	}

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
		};
	}

	function getSrc() {

		// TODO: manage the direction

		return "_1";

//		var src,
//			directedDomain = this.widgetConf.domainId.split("_");
//
//		if (directedDomain[1] == "D") {
//			src = "_1";
//		} else {
//			src = "_2";
//		}
//		return src;
	}

	function resolveTemplate() {
		resolve.call(this);

		function resolve() {
			this.templateResolverIsBusy = true;

			this.templateResolver.resolveTemplates({
				attributes: [CARD_CQL_SELECTOR],
				callback: onTemplateResolved,
				scope: this
			});
		}

		function onTemplateResolved(out, ctx) {
			this.templateResolverIsBusy = false;

			this.templateResolver.bindLocalDepsChange(resolveTemplate, this);
		}
	}

	// a object that fake a card,
	// is passed at the ModifyRelationWindow
	function getFakeCard(me) {
		var data = {
			IdClass: me.idClass,
			Id: me.cardId
		};

		return {
			get: function(k) {
				return data[k];
			}
		};
	}
})();