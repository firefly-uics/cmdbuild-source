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
		STARTING_VIEW = TABLE_VIEW_NAME;

	Ext.define("CMDBuild.controller.management.workflow.widgets.CMLinkCardsController", {
		extend: "CMDBuild.controller.management.workflow.widget.CMBaseWFWidgetController",
		cmName: "Link cards",

		activeView: function() {
			this.beforeActiveView();
			this.view.cmActivate();
		},

		constructor: function() {
			this.callParent(arguments);

			this.currentView = STARTING_VIEW;
			this.templateResolverIsBusy = false;
			this.alertIfChangeDefaultSelection = false;

			this.model = new CMDBuild.Management.LinkCardsModel({
				singleSelect: this.singleSelect
			});

			this.view.setModel(this.model);

			this.templateResolver = new CMDBuild.Management.TemplateResolver({
				clientForm: this.view.clientForm,
				xaVars: this.view.widgetConf,
				serverVars: this.view.activity
			});

			this.view.on("select", onSelect, this);
			this.view.on("deselect", onDeselect, this);
		},

		beforeActiveView: function() {
			this.alertIfChangeDefaultSelection = true;
			var classId = this.templateResolver.getVariable(CLASS_ID),
				cqlQuery = this.templateResolver.getVariable(FILTER);
			
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
			}

		},

		onEditMode: function() {
			// for the auto-select
			resolveTemplate.call(this);
		},

		isBusy: function() {
			return this.templateResolverIsBusy;
		},

		getData: function() {
			var out = {};
			if (undefined != this.outputName) {
				out[this.outputName] = this.model.getSelections();
			}
			return out;
		}
	});

	function onSelect(cardId) {
		this.model.select(cardId);
	}

	function onDeselect(cardId) {
		this.model.deselect(cardId);
	}

	function addViewEventListeners(view) {
//		view.on("CM_save", function() {
//			if (undefined != view.outputName) {
//				var out = {};
//				out[view.outputName] = this.getData.call(this);
//				view.react(out);
//			}
//		}, this);
//
//		view.on("CM_toggle_map", function() {
//			var v = this.view;
//			if (v.cardGrid.isVisible()) {
//				v.getLayout().setActiveItem(v.mapPanel.id);
//				v.mapButton.setIconClass("table");
//				v.mapButton.setText(CMDBuild.Translation.management.modcard.add_relations_window.list_tab);				
//			} else {
//				v.getLayout().setActiveItem(v.cardGrid.id);
//				v.mapButton.setIconClass("map");
//				v.mapButton.setText(CMDBuild.Translation.management.modcard.tabs.map);
//				this.gridController.loadPageForLastSelection(this.mapController.getLastSelection());		
//			}
//		}, this);
	};

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
		//TODO vedere se funzionano
		var ld = this.templateResolver.getLocalDepsAsField();
		for (var i in ld) {
			//before the blur if the value is changed
			if (ld[i]) {
				ld[i].on('change', resolveTemplate, this, {single: true});
			}
		}
	}
})();