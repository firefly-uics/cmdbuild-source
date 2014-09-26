(function() {

	/*
	 * The grid must be reload when is shown, so resolve the template and load it.
	 * If there is a defaultSelection, when the activity form goes in edit mode resolve the template to calculate the selection and if needed add dependencies to the fields.
	 */

	Ext.require('CMDBuild.model.widget.ModelLinkCards');

	Ext.define('CMDBuild.controller.management.common.widgets.linkCards.LinkCardsController', {

		mixins: {
			observable: 'Ext.util.Observable',
			widgetcontroller: 'CMDBuild.controller.management.common.widgets.CMWidgetController'
		},

		statics: {
			WIDGET_NAME: CMDBuild.view.management.common.widgets.linkCards.LinkCards.WIDGET_NAME
		},

		/**
		 * @param {CMDBuild.view.management.common.widgets.CMOpenReport} view
		 * @param {CMDBuild.controller.management.common.CMWidgetManagerController} ownerController
		 * @param {Object} widgetDef
		 * @param {Ext.form.Basic} clientForm
		 * @param {CMDBuild.model.CMActivityInstance} card
		 *
		 * @override
		 */
		constructor: function(view, ownerController, widgetDef, clientForm, card) {
			var me = this;

			this.mixins.observable.constructor.call(this);
			this.mixins.widgetcontroller.constructor.apply(this, arguments);

			this.widget = widgetDef;

			if (!_CMCache.isEntryTypeByName(this.widgetConf.className))
				throw 'LinkCardsController constructor: className not valid';

			this.targetEntryType = _CMCache.getEntryTypeByName(this.widget.className);

			this.templateResolverIsBusy = false; // Is busy when load the default selection
			this.alertIfChangeDefaultSelection = false;
			this.singleSelect = this.widget.singleSelect;
			this.readOnly = this.widget.readOnly;

			this.view.delegate = this;
			this.grid = this.view.grid;
			this.view.grid.delegate = this;
			this.view.widget = this.widget;

			this.callBacks = {
				'action-card-edit': this.onEditCardkClick,
				'action-card-show': this.onShowCardkClick
			};

			this.model = Ext.create('CMDBuild.model.widget.ModelLinkCards', {
				singleSelect: this.singleSelect
			});
			this.setViewModel();

			this.templateResolver = new CMDBuild.Management.TemplateResolver({
				clientForm: clientForm,
				xaVars: me._extractVariablesForTemplateResolver(),
				serverVars: this.getTemplateResolverServerVars()
			});

			if (this.view.hasMap()) {
				if (this.view.hasMap()) {
					this.mapController = Ext.create('CMDBuild.controller.management.common.widgets.linkCards.LinkCardsMapController', {
						view: this.view.getMapPanel(),
						model: this.model,
						parentDelegate: this,
						widgetConf: this.widget
					});
				} else {
					this.mapController = {
						onEntryTypeSelected: Ext.emptyFn,
						onAddCardButtonClick: Ext.emptyFn,
						onCardSaved: Ext.emptyFn,
						getValues: function() { return false; },
						refresh: Ext.emptyFn,
						editMode: Ext.emptyFn,
						displayMode: Ext.emptyFn
					};
				}
			}

			this.mon(this.grid, 'beforeload', this.onBeforeLoad, this);
			// There is a problem with the loadMask, if remove the delay the selection is done before the unMask, then it is reset
			this.mon(this.grid, 'load', Ext.Function.createDelayed(this.onLoad, 1), this);
		},

		/**
		 * Gatherer function to catch events
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onCellClick':
					return this.onCellClick(param);

				case 'onDeselect':
					return this.onDeselect(param);

				case 'onGridShow':
					return this.onGridShow();

				case 'onItemDoubleclick':
					return this.onItemDoubleclick(param);

				case 'onToggleMapButtonClick' :
					return this.onToggleMapButtonClick();

				case 'onSelect':
					return this.onSelect(param.record);

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * @return {Object} variables
		 */
		_extractVariablesForTemplateResolver: function() {
			var variables = {};
			variables[CMDBuild.core.proxy.CMProxyConstants.DEFAULT_SELECTION] = this.widget.defaultSelection;
			variables[CMDBuild.core.proxy.CMProxyConstants.FILTER] = this.widget.filter;

			Ext.apply(variables, this.widget.templates || {});

			return variables;
		},

		alertIfNeeded: function() {
			if (this.alertIfChangeDefaultSelection) {
				CMDBuild.Msg.warn(
					null,
					Ext.String.format(
						CMDBuild.Translation.warnings.link_cards_changed_values,
						this.widget.label || this.view.id
					),
					false
				);

				this.alertIfChangeDefaultSelection = false;
			}
		},

		/**
		 * @override
		 */
		beforeActiveView: function() {
			// When the linkCard is not busy load the grid
			if (!Ext.isEmpty(this.targetEntryType)) {
				var me = this;
				var classId = this.targetEntryType.getId();
				var cqlQuery = this.widget.filter;

				new _CMUtils.PollingFunction({
					success: function() {
						me.alertIfChangeDefaultSelection = true;

						// CQL filter and regular filter cannot be merged now.
						// The filter button should be enabled only if no other filter is present.
						if (cqlQuery) {
							me.resolveFilterTemplate(cqlQuery, classId);
							me.grid.disableFilterMenuButton();
						} else {
							me.updateViewGrid(classId);
							me.grid.enableFilterMenuButton();
						}
					},
					failure: function() {
						CMDBuild.Msg.error(null, CMDBuild.Translation.errors.busy_wf_widgets, false);
					},
					checkFn: function() {
						// I want exit if I'm not busy
						return !me.isBusy();
					},
					cbScope: me,
					checkFnScope: me
				}).run();
			}
		},

		/**
		 * Local solution for a global issue.
		 * The card model is a CMDBuild.Dummymodel, it takes a map and set all the key as fields of the model, so there are no type specification.
		 * Server side I want that the Ids are integer, so now cast it in this function, but the real solution is to find a way to say to the card that its id is a number.
		 *
		 * @param {Array} input
		 *
		 * @ŗeturn {Array} output
		 */
		convertElementsFromStringToInt: function(input) {
			var output = [];

			if (Ext.isArray(input))
				for (var i = 0; i < input.length; ++i) {
					var element = parseInt(input[i]);

					if (element)
						output.push(element);
				}

			return output;
		},

		/**
		 * @param {Ext.data.Model} model
		 * @param {Boolean] editable
		 */
		getCardWindow: function(model, editable) {
			var cardWindow = Ext.create('CMDBuild.view.management.common.CMCardWindow', {
				cmEditMode: editable,
				withButtons: editable,
				title: model.get('IdClass_value')
			});

			new CMDBuild.controller.management.common.CMCardWindowController(
				cardWindow,
				{
					entryType: model.get('IdClass'),
					card: model.get('Id'),
					cmEditMode: editable
				}
			);

			return cardWindow;
		},

		/**
		 * @return {Object} out
		 *
		 * @override
		 */
		getData: function() {
			var out = null;

			if (!this.readOnly) {
				out = {};
				out['output'] = this.convertElementsFromStringToInt(this.model.getSelections());
			}

			return out;
		},

		/**
		 * @return {String} label
		 */
		getLabel: function() {
			return this.widget.label;
		},

		/**
		 * @return {Object} targetEntryType
		 */
		getTargetEntryType: function() {
			return this.targetEntryType;
		},

		/**
		 * @return {Boolean} templateResolverIsBusy
		 *
		 * @override
		 */
		isBusy: function() {
			return this.templateResolverIsBusy;
		},

		/**
		 * @return {Boolean}
		 *
		 * @override
		 */
		isValid: function() {
			if (!this.readOnly && this.widget.required) {
				return this.model.hasSelection();
			} else {
				return true;
			}
		},

		onBeforeLoad: function() {
			this.model.freeze();
		},

		/**
		 * @param {Object} params
		 * 	{
		 * 		{Ext.data.Model} record
		 * 		{Ext.EventObject} event
		 * 	}
		 */
		onCellClick: function(params) {
			var className = params.event.target[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME];

			if (this.callBacks[className]) {
				this.callBacks[className].call(this, params.record);
			}
		},

		/**
		 * @param {Object} params
		 * 	{
		 * 		{Ext.data.Model} record
		 * 	}
		 */
		onDeselect: function(params) {
			this.model.deselect(params.record.get('Id'));
		},

		/**
		 * Loads grid's page for last selection and select
		 */
		onGridShow: function() {
			var lastSelection = _CMCardModuleState.card;

			if (!Ext.isEmpty(lastSelection)) {
				var params = {};
				params[CMDBuild.core.proxy.CMProxyConstants.CARD_ID] = lastSelection.get('Id');
				params[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = this.widget.className;
				params[CMDBuild.core.proxy.CMProxyConstants.RETRY_WITHOUT_FILTER] = true;
				params[CMDBuild.core.proxy.CMProxyConstants.FILTER] = this.grid.getStore().getProxy().extraParams[CMDBuild.core.proxy.CMProxyConstants.FILTER];

				this.model._silent = true;

				CMDBuild.ServiceProxy.card.getPosition({
					scope: this,
					params: params,
					success: function(result, options, decodedResult) {
						var position = decodedResult.position;

						if (position >= 0) {
							var	pageNumber = _CMUtils.grid.getPageNumber(position);

							this.grid.loadPage(
								pageNumber,
								{
									scope: this,
									cb: function() {
										this.onSelect(options.params[CMDBuild.core.proxy.CMProxyConstants.CARD_ID]);
									}
								}
							);
						}
					}
				});
			}
		},

		/**
		 * @param {Ext.data.Model} model
		 */
		onEditCardkClick: function(model) {
			var cardWindow = this.getCardWindow(model, true);

			cardWindow.on(
				'destroy',
				function() {
					this.grid.reload();
				},
				this,
				{ single: true }
			);

			cardWindow.show();
		},

		/**
		 * For the auto-select
		 *
		 * @override
		 */
		onEditMode: function() {
			this.resolveDefaultSelectionTemplate();
		},

		/**
		 * @param {Object} params
		 * 	{
		 * 		{Ext.data.Model} record
		 * 	}
		 */
		onItemDoubleclick: function(params) {
			if (this.widget.allowCardEditing) {
				var priv = _CMUtils.getClassPrivileges(params.record.get('IdClass'));

				if (priv && priv.write) {
					this.onEditCardkClick(params.record);
				} else {
					this.onShowCardkClick(params.record);
				}
			}
		},

		onLoad: function() {
			this.model.defreeze();
		},

		/**
		 * @param {Int or Object} record
		 */
		onSelect: function(record) {
			var cardId = undefined;

			if (typeof record == 'number') {
				cardId = record;
			} else {
				cardId = record.get('Id');
			}

			if (!Ext.isEmpty(cardId)) {
				if (typeof record != 'number')
					_CMCardModuleState.setCard(record);

				this.grid.getSelectionModel().select(
					this.grid.getStore().find(CMDBuild.core.proxy.CMProxyConstants.ID, cardId)
				);
				this.model.select(cardId);
			} else {
				this.grid.getSelectionModel().reset();
				this.model.reset();
			}
		},

		/**
		 * @param {Ext.data.Model} model
		 */
		onShowCardkClick: function(model) {
			this.getCardWindow(model, false).show();
		},

		onToggleMapButtonClick: function() {
			if (this.view.hasMap()) {
				if (this.grid.isVisible()) {
					this.view.showMap();
					this.view.mapButton.setIconCls('table');
					this.view.mapButton.setText(CMDBuild.Translation.management.modcard.add_relations_window.list_tab);
				} else {
					this.view.showGrid();
					this.view.mapButton.setIconCls('map');
					this.view.mapButton.setText(CMDBuild.Translation.management.modcard.tabs.map);
				}
			}
		},

		resolveDefaultSelectionTemplate: function() {
			var me = this;

			this.templateResolverIsBusy = true;
			this.viewReset();
			this.alertIfNeeded();

			this.templateResolver.resolveTemplates({
				attributes: [CMDBuild.core.proxy.CMProxyConstants.DEFAULT_SELECTION],
				callback: function(out, ctx) {
					var defaultSelection = me.templateResolver.buildCQLQueryParameters(out[CMDBuild.core.proxy.CMProxyConstants.DEFAULT_SELECTION], ctx);

					// Do the request only if there are a default selection
					if (defaultSelection) {
						CMDBuild.ServiceProxy.getCardList({
							params: defaultSelection,
							callback: function(request, options, response) {
								var resp = Ext.JSON.decode(response.responseText);

								if (resp.rows)
									for (var i = 0; i < resp.rows.length; i++) {
										var r = resp.rows[i];

										me.model.select(r['Id']);
									}

								me.templateResolverIsBusy = false;
							}
						});

						me.templateResolver.bindLocalDepsChange(function() {
							me.resolveDefaultSelectionTemplate();
						});
					} else {
						me.templateResolverIsBusy = false;
					}
				}
			});
		},

		/**
		 * @param {String} cqlQuery
		 * @param {Int} classId
		 */
		resolveFilterTemplate: function(cqlQuery, classId) {
			var me = this;

			this.templateResolver.resolveTemplates({
				attributes: [CMDBuild.core.proxy.CMProxyConstants.FILTER],
				callback: function(out, ctx) {
					var cardReqParams = me.templateResolver.buildCQLQueryParameters(cqlQuery, ctx);
					me.updateViewGrid(classId, cardReqParams);

					me.templateResolver.bindLocalDepsChange(function() {
						me.viewReset();
					});
				}
			});
		},

		setViewModel: function() {
			this.view.model = this.model;
		},

		/**
		 * @param {Int} classId
		 * @param {Object} cqlParams
		 */
		updateViewGrid: function(classId, cqlParams) {
			this.grid.CQL = cqlParams;
			this.grid.store.proxy.extraParams = this.grid.getStoreExtraParams();
			this.grid.updateStoreForClassId(classId);
		},

		viewReset: function() {
			if (this.view.selectionModel && typeof this.view.selectionModel.reset == 'function')
				this.view.selectionModel.reset();

			this.model.reset();
		}
	});

})();