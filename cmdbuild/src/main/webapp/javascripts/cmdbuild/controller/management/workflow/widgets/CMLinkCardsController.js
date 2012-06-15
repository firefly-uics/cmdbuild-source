(function() {
	/*
	 * The grid must be reload when is shown, so resolve the template and load it
	 * 
	 * If there is a defaultSelection, when the activity form goes in edit mode resolve
	 * the template to calculate the selection and if needed add dependencies to the fields
	 */

	var FILTER = "Filter",
		CLASS_ID = "xa:ClassId",
		DEFAULT_SELECTION = "DefaultSelection",
		TABLE_VIEW_NAME = "table",
		MAP_VIEW_NAME = "map",
		STARTING_VIEW = TABLE_VIEW_NAME,
		TRUE = "1";

	Ext.define("CMDBuild.controller.management.workflow.widgets.CMLinkCardsController", {
		extend: "CMDBuild.controller.management.workflow.widget.CMBaseWFWidgetController",
		cmName: "Link cards",

		mixins: {
			observable: "Ext.util.Observable"
		},

		constructor: function() {
			this.callParent(arguments);

			this.currentView = STARTING_VIEW;
			this.templateResolverIsBusy = false;
			this.alertIfChangeDefaultSelection = false;

			this.singleSelect = this.widgetConf.SingleSelect;
			this.noSelect = this.widgetConf.NoSelect;

			this.model = new CMDBuild.Management.LinkCardsModel({
				singleSelect: this.singleSelect
			});

			this.callBacks = {
				'action-card-edit': this.onEditCardkClick,
				"action-card-show": this.onShowCardkClick
			};

			this.view.setModel(this.model);

			this.templateResolver = new CMDBuild.Management.TemplateResolver({
				clientForm: this.view.clientForm,
				xaVars: this.view.widgetConf,
				serverVars: this.view.activity
			});
			this.isFiltered = !!this.view.widgetConf[FILTER];

			this.mon(this.view.grid, 'beforeitemclick', cellclickHandler, this);
			this.mon(this.view.grid, 'itemdblclick', onItemDoubleclick, this);

			this.mon(this.view, "select", onSelect, this);
			this.mon(this.view, "deselect", onDeselect, this);

			if (this.view.hasMap()) {
				var me = this;
				listenToggleMapEvents(me);
				buildMapController(me);
			}
		},

		// override
		beforeActiveView: function() {
			new _CMUtils.PollingFunction({
				success: function() {
					this.alertIfChangeDefaultSelection = true;

					var tr = this.templateResolver,
						classId = tr.getVariable(CLASS_ID);

					// CQL filter and regular filter cannot be merged now.
					// The button should be enabled only if no other filter is present.
					if (this.isFiltered) {
						this.view.grid.openFilterButton.disable();
						resolveFilterTemplate(this, classId);
					} else {
						this.view.updateGrid(classId);
						this.view.grid.openFilterButton.enable();
					}
				},
				failure: function failure() {
					CMDBuild.Msg.error(null,CMDBuild.Translation.errors.busy_wf_widgets, false);
				},
				checkFn: function() {
					// I want exit if I'm not busy
					return !this.isBusy();
				},
				cbScope: this,
				checkFnScope: this
			}).run();
		},

		// override
		onEditMode: function() {
			// for the auto-select
			resolveDefaultSelectionTemplate(this);
		},

		// override
		isBusy: function() {
			return this.templateResolverIsBusy;
		},

		// override
		getData: function() {
			var out = null;
			if (!this.noSelect && this.outputName) {
				out = {};
				out[this.outputName] = this.model.getSelections();
			}
			return out;
		},

		// override
		isValid: function() {
			if (!this.noSelect && this.widgetConf.Required == TRUE) {
				return this.model.hasSelection();
			} else {
				return true;
			}
		},

		syncSelections: function() {
			this.model._silent = true;
			this.view.syncSelections();
			this.model._silent = false;
		},

		onEditCardkClick: function(model) {
			var editable = true,
				w = getCardWindow(model, editable);

			w.on("destroy", function() {
				this.view.grid.reload();
			}, this, {single: true});

			w.show();
		},

		onShowCardkClick: function(model) {
			var editable = false,
				w = getCardWindow(model, editable);

			w.show();
		}
	});

	function listenToggleMapEvents(me) {
		me.mon(me.view, "CM_toggle_map", function() {
			var v = me.view;
			if (v.grid.isVisible()) {
				v.showMap();
				v.mapButton.setIconCls("table");
				v.mapButton.setText(CMDBuild.Translation.management.modcard.add_relations_window.list_tab);
				if (me.mapController) {
					me.mapController.centerMapOnSelection();
				}
			} else {
				v.showGrid();
				v.mapButton.setIconCls("map");
				v.mapButton.setText(CMDBuild.Translation.management.modcard.tabs.map);
				loadPageForLastSelection.call(me, me.mapController.getLastSelection());
			}
		}, me);
	}

	function buildMapController(me) {
		me.mapController = new CMDBuild.controller.management.workflow.widgets.CMLinkCardsMapController({
			view: me.view.mapPanel, 
			ownerController: me,
			model: me.model,
			widgetConf: me.widgetConf
		});
	}

	function resolveFilterTemplate(me, classId) {
		var view = me.view;
		me.templateResolver.resolveTemplates({
			attributes: [FILTER],
			callback: function(out, ctx) {
				var cardReqParams = me.view.getTemplateResolver().buildCQLQueryParameters(out[FILTER], ctx);
				me.view.updateGrid(classId, cardReqParams);

				me.templateResolver.bindLocalDepsChange(function() {
					me.view.reset();
				});
			}
		});
	}

	function getCardWindow(model, editable) {
		var w = new CMDBuild.view.management.common.CMCardWindow({
			cmEditMode: editable,
			withButtons: editable,
			title: model.get("IdClass_value")
		});

		new CMDBuild.controller.management.common.CMCardWindowController(w, {
			entryType: model.get("IdClass"),
			card: model.get("Id"),
			cmEditMode: editable
		});

		return w;
	}

	// used only on toggle the map
	function loadPageForLastSelection(selection) {
		if (selection != null) {
			var params = {
				"retryWithoutFilter": true,
				IdClass:this.widgetConf.ClassId,
				Id: selection
			}, 
			me = this, 
			grid = this.view.grid;

			me.model._silent = true;

			CMDBuild.ServiceProxy.card.getPosition({
				params: params,
				success: function onGetPositionSuccess(response, options, resText) {
					var position = resText.position,
						found = position >= 0,
						foundButNotInFilter = resText.notFoundInFilter;
	
					if (found) {
						var	pageNumber = grid.getPageNumber(position);
						grid.loadPage(pageNumber, {
							scope: me,
							cb: function() {
								me.model._silent = false;
							}
						});
					}
				}
			});

		} else {
			this.syncSelections();
		}
	}

	function onSelect(cardId) {
		this.model.select(cardId);
	}

	function onDeselect(cardId) {
		this.model.deselect(cardId);
	}


	function alertIfNeeded(me) {
		if (me.alertIfChangeDefaultSelection) {
			CMDBuild.Msg.warn(null, Ext.String.format(CMDBuild.Translation.warnings.link_cards_changed_values
					, me.view.widgetConf.ButtonLabel || me.view.id)
					, popup=false);

			me.alertIfChangeDefaultSelection = false;
		}
	}

	function resolveDefaultSelectionTemplate(me) {

		me.templateResolverIsBusy = true;
		me.view.reset();
		alertIfNeeded(me);

		me.templateResolver.resolveTemplates({
			attributes: [DEFAULT_SELECTION],
			callback: function onDefaultSelectionTemplateResolved(out, ctx) {
				var defaultSelection = me.templateResolver.buildCQLQueryParameters(out[DEFAULT_SELECTION], ctx);
				// do the request only if there are a default selection
				if (defaultSelection) {
					CMDBuild.ServiceProxy.getCardList({
						params: defaultSelection,
						callback: function callback(request, options, response) {
							var resp = Ext.JSON.decode(response.responseText);

							if (resp.rows) {
								for ( var i = 0, l = resp.rows.length; i < l; i++) {
									var r = resp.rows[i];
									me.model.select(r.Id);
								}
							}
			
							me.templateResolverIsBusy = false;
						}
					});
	
					me.templateResolver.bindLocalDepsChange(function() {
						resolveDefaultSelectionTemplate(me);
					});

				} else {
					me.templateResolverIsBusy = false;
				}
			}
		});
	}

	function cellclickHandler(grid, model, htmlelement, rowIndex, event, opt) {
		var className = event.target.className; 

		if (this.callBacks[className]) {
			this.callBacks[className].call(this, model);
		}
	}

	function onItemDoubleclick(grid, model, html, index, e, options) {
		if (!this.widgetConf.AllowCardEditing) {
			return;
		}

		var priv = _CMUtils.getClassPrivileges(model.get("IdClass"));
		if (priv && priv.write) {
			this.onEditCardkClick(model);
		} else {
			this.onShowCardkClick(model);
		}
	}
})();
