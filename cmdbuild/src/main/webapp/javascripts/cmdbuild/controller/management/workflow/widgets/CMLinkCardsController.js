(function() {
	/*
	 * The grid must be reload when is shown, so resolve the template and load it
	 * 
	 * If there is a defaultSelection, when the activity form goes in edit mode resolve
	 * the template to calculate the selection and if needed add dependencies to the fields
	 */

	var FILTER = "xa:Filter",
		CLASS_ID = "xa:ClassId",
		DEFAULT_SELECTION = "xa:DefaultSelection",
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

			this.view.grid.on('beforeitemclick', cellclickHandler, this);
			this.view.grid.on("itemdblclick", onItemDoubleclick, this);

			this.templateResolver = new CMDBuild.Management.TemplateResolver({
				clientForm: this.view.clientForm,
				xaVars: this.view.widgetConf,
				serverVars: this.view.activity
			});

			this.view.on("select", onSelect, this);
			this.view.on("deselect", onDeselect, this);

			this.view.on("CM_toggle_map", function() {
				var v = this.view;
				if (v.grid.isVisible()) {
					v.showMap();
					v.mapButton.setIconCls("table");
					v.mapButton.setText(CMDBuild.Translation.management.modcard.add_relations_window.list_tab);
					this.mapController.centerMapOnSelection();
				} else {
					v.showGrid();
					v.mapButton.setIconCls("map");
					v.mapButton.setText(CMDBuild.Translation.management.modcard.tabs.map);
					loadPageForLastSelection.call(this, this.mapController.getLastSelection());
				}
			}, this);

			if (this.view.hasMap()) {
				this.mapController = 
					new CMDBuild.controller.management.workflow.widgets.CMLinkCardsMapController({
						view: this.view.mapPanel, 
						ownerController: this,
						model: this.model,
						widgetConf: this.widgetConf
					});
			}
		},

		// override
		beforeActiveView: function() {
			new _CMUtils.PollingFunction({
				success: function() {
					this.alertIfChangeDefaultSelection = true;
					var classId = this.templateResolver.getVariable(CLASS_ID),
						cqlQuery = this.templateResolver.getVariable(FILTER);

					// CQL filter and regular filter cannot be merged now.
					// The button should be enabled only if no other filter is present.
					if (cqlQuery) {
						this.view.grid.openFilterButton.disable();
						this.templateResolver.resolveTemplates({
							attributes: [ FILTER ],
							scope: this.view,
							callback: function(out, ctx) {
								var cardReqParams = this.getTemplateResolver().buildCQLQueryParameters(cqlQuery, ctx);
								this.updateGrid(classId, cardReqParams);
							}
						});
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
					return !this.isBusy()
				},
				cbScope: this,
				checkFnScope: this
			}).run();
		},

		// override
		onEditMode: function() {
			// for the auto-select
			resolveTemplate.call(this);
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
			this.view.syncSelections()
			this.model._silent = false;
		},

		onEditCardkClick: function(model) {
			var w = getCardWindow(model, editable = true);

			w.on("destroy", function() {
				this.view.grid.reload();
			}, this, {single: true});

			new CMDBuild.controller.management.common.CMCardWindowController(w);
			w.show();
		},

		onShowCardkClick: function(model) {
			var w = getCardWindow(model, editable = false);
			w.show();
		}
	});

	function getCardWindow(model, editable) {
		return new CMDBuild.view.management.common.CMCardWindow({
			cmEditMode: editable,
			withButtons: editable,
			classId: model.get("IdClass"), // classid of the destination
			cardId: model.get("Id"), // id of the card destination
			title: model.get("IdClass_value")
		});
	}

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
						})
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

	function resolveTemplate() {
		resolve.call(this);

		function resolve() {
			this.templateResolverIsBusy = true;

			this.model.reset();
			if (this.alertIfChangeDefaultSelection) {
				CMDBuild.Msg.warn(null, Ext.String.format(CMDBuild.Translation.warnings.link_cards_changed_values
						, this.view.widgetConf.ButtonLabel || this.view.id)
						, popup=false);

				this.alertIfChangeDefaultSelection = false;
			}

			this.templateResolver.resolveTemplates({
				attributes: [ 'DefaultSelection' ],
				callback: onTemplateResolved,
				scope: this
			});
		}

		function onTemplateResolved(out, ctx) {
			function callback(request, options, response) {
				var resp = Ext.JSON.decode(response.responseText);

				if (resp.rows) {
					for ( var i = 0, l = resp.rows.length; i < l; i++) {
						var r = resp.rows[i];
						this.model.select(r.Id);
					}
				}

				this.templateResolverIsBusy = false;
			}

			// do the request only if there are a default selection
			var defaultSelection = this.templateResolver.buildCQLQueryParameters(out.DefaultSelection, ctx);
			if (defaultSelection) {
				CMDBuild.ServiceProxy.getCardList({
					params: defaultSelection,
					callback: Ext.bind(callback,this)
				});

				addListenerToDeps.call(this);
			} else {
				this.templateResolverIsBusy = false;
			}
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