(function() {

	/*
	 * The grid must be reload when is shown, so resolve the template and load it.
	 * If there is a defaultSelection, when the activity form goes in edit mode resolve the template to calculate the selection and if needed add dependencies to the fields.
	 */

	var tr = CMDBuild.Translation;

	Ext.define('CMDBuild.controller.management.common.widgets.linkCards.LinkCardsController', {
		extend: 'CMDBuild.controller.management.common.widgets.CMWidgetController',

		mixins: {
			observable: 'Ext.util.Observable'
		},

		statics: {
			WIDGET_NAME: CMDBuild.view.management.common.widgets.linkCards.LinkCards.WIDGET_NAME
		},

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.model.widget.ModelLinkCards'
		],

		/**
		 * @cfg {Boolean}
		 */
		alertIfChangeDefaultSelection: false,

		/**
		 * @property {CMDBuild.model.CMActivityInstance}
		 */
		card: undefined,

		/**
		 * @property {{Ext.form.Basic}}
		 */
		clientForm: undefined,

		/**
		 * @property {CMDBuild.view.management.common.widgets.linkCards.LinkCardsGrid}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.controller.management.common.widgets.linkCards.LinkCardsMapController}
		 */
		mapController: undefined,

		/**
		 * @property {CMDBuild.model.widget.ModelLinkCards}
		 */
		model: undefined,

		/**
		 * @cfg {Boolean}
		 */
		readOnly: undefined,

		/**
		 * @property {Ext.selection.RowModel} or {CMDBuild.selection.CMMultiPageSelectionModel}
		 */
		selectionModel: undefined,

		/**
		 * @cfg {Boolean}
		 */
		singleSelect: undefined,

		/**
		 * @cfg {String}
		 */
		outputName: undefined,

		/**
		 * @property {CMDBuild.controller.management.common.CMWidgetManagerController}
		 */
		ownerController: undefined,

		/**
		 * @property {Object}
		 */
		targetClass: undefined,

		/**
		 * @property {CMDBuild.Management.TemplateResolver}
		 */
		templateResolver: undefined,

		/**
		 * Is busy when load the default selection
		 *
		 * @property {Boolean}
		 */
		templateResolverIsBusy: false,

		/**
		 * @property {CMDBuild.view.management.common.widgets.linkCards.LinkCards}
		 */
		view: undefined,

		/**
		 * @property {Object}
		 */
		widgetConf: undefined,

		/**
		 * @param {CMDBuild.view.management.common.widgets.linkCards.LinkCards} view
		 * @param {CMDBuild.controller.management.common.CMWidgetManagerController} ownerController
		 * @param {Object} widgetConf
		 * @param {Ext.form.Basic} clientForm
		 * @param {CMDBuild.model.CMActivityInstance} card
		 *
		 * @override
		 */
		constructor: function(view, ownerController, widgetConf, clientForm, card) {
			var me = this;
			var targetClassName = null;

			this.mixins.observable.constructor.call(this);

			this.callParent(arguments);

			// Try to get targetClassName from a source (widgetConf.className or widgetConf.filter)
			if (
				!Ext.isEmpty(this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME])
				&& _CMCache.isEntryTypeByName(this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME])
			) {
				targetClassName = this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME];
			} else if (
				!Ext.isEmpty(this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.FILTER])
				&& _CMCache.isEntryTypeByName(
					this.getClassNameFromFilterString(this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.FILTER])
				)
			) {
				targetClassName = this.getClassNameFromFilterString(this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.FILTER]);
			} else {
				return CMDBuild.Msg.error(
					CMDBuild.Translation.error,
					CMDBuild.Translation.errors.widgetLinkCardsNoClassNameError,
					false
				);
			}

			// Set local tergetClass and set it also in CMCardModuleState avoiding delegates call
			this.targetClass = _CMCache.getEntryTypeByName(targetClassName);
			_CMCardModuleState.setEntryType(this.targetClass, null, null, false);

			this.singleSelect = this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.SINGLE_SELECT];
			this.readOnly = this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.READ_ONLY];

			this.view.delegate = this;
			this.grid = this.view.grid;
			this.grid.delegate = this;
			this.view.widgetConf = this.widgetConf;
			this.selectionModel = this.grid.getSelectionModel();

			this.model = Ext.create('CMDBuild.model.widget.ModelLinkCards', {
				singleSelect: this.singleSelect
			});

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
						widgetConf: this.widgetConf
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
				case 'onDeselect':
					return this.onDeselect(param);

				case 'onGridPageChange':
					return this.onGridPageChange();

				case 'onGridShow':
					return this.onGridShow();

				case 'onItemDoubleclick':
					return this.onItemDoubleclick(param);

				case 'onToggleGridFilterButtonClick':
					return this.onToggleGridFilterButtonClick();

				case 'onToggleMapButtonClick' :
					return this.onToggleMapButtonClick();

				case 'onRowEditButtonClick':
					return this.onRowEditButtonClick(param);

				case 'onRowViewButtonClick':
					return this.onRowViewButtonClick(param);

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
			variables[CMDBuild.core.proxy.CMProxyConstants.DEFAULT_SELECTION] = this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.DEFAULT_SELECTION];
			variables[CMDBuild.core.proxy.CMProxyConstants.FILTER] = this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.FILTER];

			Ext.apply(variables, this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.TEMPLATES] || {});

			return variables;
		},

		alertIfNeeded: function() {
			if (this.alertIfChangeDefaultSelection) {
				CMDBuild.Msg.warn(
					null,
					Ext.String.format(
						tr.warnings.link_cards_changed_values,
						this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.LABEL] || this.view.id
					),
					false
				);

				this.alertIfChangeDefaultSelection = false;
			}
		},

		/**
		 * When the linkCard is not busy load the grid
		 *
		 * @override
		 */
		beforeActiveView: function() {
			if (!Ext.isEmpty(this.targetClass)) {
				var me = this;
				var classId = this.targetClass.getId();
				var cqlQuery = this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.FILTER];

				// Disable toggle grid filter button
				if (
					this.widgetConf.hasOwnProperty(CMDBuild.core.proxy.CMProxyConstants.DISABLE_GRID_FILTER_TOGGLER)
					&& this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.DISABLE_GRID_FILTER_TOGGLER]
				) {
					this.view.toggleGridFilterButton.setDisabled(true);
				}

				new _CMUtils.PollingFunction({
					success: function() {
						me.alertIfChangeDefaultSelection = true;

						// CQL filter and regular filter cannot be merged now.
						// The filter button should be enabled only if no other filter is present.
						if (cqlQuery) {
							me.resolveFilterTemplate(cqlQuery, classId);
						} else {
							me.updateViewGrid(classId);
						}

						me.grid.disableFilterMenuButton();
						me.onGridShow();
					},
					failure: function() {
						CMDBuild.Msg.error(null, tr.errors.busy_wf_widgets, false);
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
		 * Takes in input a linkCards cql filter string (so is assumed that the filter is like "from className ..."), splits for "from" and the for " " so we get className
		 *
		 * @param {String} cqlFilter
		 *
		 * @return {String}
		 */
		getClassNameFromFilterString: function(cqlFilter) {
			var splitFilter = cqlFilter.trim().split('from');
			splitFilter = splitFilter[1].trim();

			return splitFilter.split(' ')[0];
		},

		/**
		 * Return selection data. Converts lat, lon metadata only if widget is in singleSelect mode, otherwise metadata will be passed to server without being edited.
		 *
		 * @return {Object} out
		 * 	{
		 * 		output: {
		 * 			cardId: {
		 * 				... metadata ...
		 * 			},
		 * 			cardId2: {},
		 * 			...
		 * 		},
		 * 		metadataOutput: configuration value
		 * 	}
		 *
		 * @override
		 */
		getData: function() {
			var out = null;

			if (!this.readOnly) {
				var modelSelections = this.model.getSelections();
				var widgetConfMetadata = this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.METADATA];

				// Output metadata codification only for single select mode
				if (this.singleSelect && !Ext.Object.isEmpty(widgetConfMetadata)) {
					for (var confIndex in widgetConfMetadata) {
						switch (widgetConfMetadata[confIndex]) {
							case 'POINT': {
								var selectionKey = Ext.Object.getKeys(modelSelections)[0];

								if (!Ext.Object.isEmpty(modelSelections[selectionKey])) {
									var lat = modelSelections[selectionKey][CMDBuild.core.proxy.CMProxyConstants.LATITUDE];
									var lon = modelSelections[selectionKey][CMDBuild.core.proxy.CMProxyConstants.LONGITUDE];

									modelSelections[selectionKey] = {};
									modelSelections[selectionKey][confIndex] = new OpenLayers.Geometry.Point(lon, lat).toString();
								}
							} break;

							default:
								throw 'ERROR: LinkCardsController getData wrong widget metadata configuration (' + widgetConfMetadata[confIndex] + ')';
						}
					}
				}

				out = {};
				out[CMDBuild.core.proxy.CMProxyConstants.OUTPUT] = modelSelections;
				out[CMDBuild.core.proxy.CMProxyConstants.METADATA_OUTPUT] = this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.METADATA_OUTPUT]; // Simple property echo
			}

			return out;
		},

		/**
		 * @return {Object} targetClass
		 */
		getTargetClass: function() {
			return this.targetClass;
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
			if (!this.readOnly && this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.REQUIRED]) {
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
		 * 	}
		 */
		onDeselect: function(params) {
			this.model.deselect(params.record.get('Id'));
		},

		/**
		 * For auto-select of defaultSelection
		 *
		 * @override
		 */
		onEditMode: function() {
			this.resolveDefaultSelectionTemplate();
		},

		/**
		 * Event to select right cards on grid page change
		 */
		onGridPageChange: function() {
			var modelSelections = this.model.getSelections();

			for (var index in modelSelections)
				this.selectionModel.select(
					this.grid.getStore().find(CMDBuild.core.proxy.CMProxyConstants.ID, index)
				);
		},

		/**
		 * Loads grid's page for last selection and select
		 *
		 * @param {Boolean} disableFilter
		 */
		onGridShow: function(disableFilter) {
			disableFilter = Ext.isEmpty(disableFilter) ? false : true;

			var lastSelectionId = this.model.getLastSelection();

			if (!Ext.isEmpty(lastSelectionId)) {
				var params = {};
				params[CMDBuild.core.proxy.CMProxyConstants.CARD_ID] = lastSelectionId;
				params[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME];
				params[CMDBuild.core.proxy.CMProxyConstants.RETRY_WITHOUT_FILTER] = false;
				params[CMDBuild.core.proxy.CMProxyConstants.SORT] = Ext.encode(this.grid.getStore().sorters.getRange());

				if (!disableFilter)
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
										this.selectionModel.select(
											this.grid.getStore().find(CMDBuild.core.proxy.CMProxyConstants.ID, lastSelectionId)
										);

										// Retry without grid store filter or server answer out of filter
										if (!this.selectionModel.hasSelection() && this.view.toggleGridFilterButton.filterEnabled) {
											this.onToggleGridFilterButtonClick(false);
											this.onGridShow();
										}

										this.model._silent = false;
									}
								}
							);
						} else if (this.view.toggleGridFilterButton.filterEnabled) {
							this.onToggleGridFilterButtonClick(false);
							this.onGridShow(true);
						}
					}
				});
			}
		},

		/**
		 * @param {Ext.data.Model} params - record
		 */
		onItemDoubleclick: function(params) {
			if (this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.ALLOW_CARD_EDITING]) {
				var priv = _CMUtils.getClassPrivileges(params.get('IdClass'));

				if (priv && priv.write) {
					this.onRowEditButtonClick(params);
				} else {
					this.onRowViewButtonClick(params);
				}
			}
		},

		onLoad: function() {
			this.model.defreeze();
		},

		/**
		 * @param {Ext.data.Model} model
		 */
		onRowEditButtonClick: function(model) {
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
		 * @param {Ext.data.Model} model
		 */
		onRowViewButtonClick: function(model) {
			this.getCardWindow(model, false).show();
		},

		/**
		 * @param {Object} record
		 */
		onSelect: function(record) {
			if (!Ext.isEmpty(record.get('Id'))) {
				_CMCardModuleState.setCard(record, null, false);

				this.model.select(record.get('Id'));
			} else {
				this.selectionModel.deselectAll();
				this.model.reset();
			}
		},

		/**
		 * Disable grid store filter
		 *
		 * @param {Boolean} forceState
		 */
		onToggleGridFilterButtonClick: function(forceState) {
			var classId = this.targetClass.getId();
			var cqlQuery = this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.FILTER];

			if (!Ext.isEmpty(forceState))
				this.view.toggleGridFilterButton.filterEnabled = !forceState;

			if (this.view.toggleGridFilterButton.filterEnabled) {
				this.resolveFilterTemplate(null, classId);

				this.view.toggleGridFilterButton.setIconCls('find');
				this.view.toggleGridFilterButton.setText(tr.enableGridFilter);
			} else {
				this.resolveFilterTemplate(cqlQuery, classId);

				this.view.toggleGridFilterButton.setIconCls('clear_filter');
				this.view.toggleGridFilterButton.setText(tr.disableGridFilter);
			}

			this.view.toggleGridFilterButton.filterEnabled = !this.view.toggleGridFilterButton.filterEnabled;
		},

		onToggleMapButtonClick: function() {
			if (this.view.hasMap()) {
				if (this.grid.isVisible()) {
					this.view.showMap();
					this.view.mapButton.setIconCls('table');
					this.view.mapButton.setText(tr.management.modcard.add_relations_window.list_tab);

					this.view.toggleGridFilterButton.setDisabled(true);
				} else {
					this.view.showGrid();
					this.view.mapButton.setIconCls('map');
					this.view.mapButton.setText(tr.management.modcard.tabs.map);

					this.view.toggleGridFilterButton.setDisabled(false);
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

										_CMCardModuleState.setCard(
											{
												Id: r['Id'],
												IdClass: r['IdClass']
											},
											null,
											false
										);

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

						me.model.reset();
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
			this.selectionModel.deselectAll();
			this.model.reset();
		}
	});

})();