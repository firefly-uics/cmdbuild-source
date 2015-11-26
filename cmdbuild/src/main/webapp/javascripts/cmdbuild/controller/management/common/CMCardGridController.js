(function() {
	Ext.define("CMDBuild.controller.management.common.CMCardGridController", {

		mixins: {
			observable: "Ext.util.Observable",
			filterMenuButton: "CMDBuild.delegate.common.filter.CMFilterMenuButtonDelegate",
			filterWindow: "CMDBuild.view.management.common.filter.CMFilterWindowDelegate",
			saveFilterWindow: "CMDBuild.view.management.common.filter.CMSaveFilterWindowDelegate",
			runtimeFilterParamsWindow: "CMDBuild.delegate.common.filter.CMRuntimeParameterWindowDelegate"
		},

		constructor: function(view, supercontroller) {

			this.mixins.observable.constructor.call(this, arguments);

			if (typeof view == "undefined") {
				throw ("OOO snap, you have not passed a view to me");
			} else {
				this.view = view;
			}

			this.supercontroller = supercontroller;
			this.gridSM = this.view.getSelectionModel();

			this.CMEVENTS = {
				cardSelected: "cm-card-selected",
				wrongSelection: "cm-wrong-selection",
				gridVisible: "cm-visible-grid",
				itemdblclick: "itemdblclick",
				load: "load"
			};

			this.addEvents(this.CMEVENTS.cardSelected);
			this.addEvents(this.CMEVENTS.wrongSelection);
			this.addEvents(this.CMEVENTS.gridVisible);
			this.relayEvents(this.view, ["itemdblclick", "load"]);

			this.mon(this.gridSM, "selectionchange", this.onCardSelected, this);
			this.mon(this.view, "cmWrongSelection", this.onWrongSelection, this);
			this.mon(this.view, "cmVisible", this.onGridIsVisible, this);
			this.mon(this.view.printGridMenu, "click", this.onPrintGridMenuClick, this);

			this.stateDelegate = this.buildStateDelegate();
			if (this.view.filterMenuButton) {
				this.view.filterMenuButton.addDelegate(this);
			}
		},

		buildStateDelegate: function() {
			var sd = new CMDBuild.state.CMCardModuleStateDelegate();
			var me = this;

			sd.onEntryTypeDidChange = function(state, entryType, danglingCard, viewFilter) {
				me.onEntryTypeSelected(entryType, danglingCard, viewFilter);
			};

			sd.onCardDidChange = function(state, card) {
				if (!card) {
					return;
				}

				if (!me.view.isVisible(deep)) {
					return;
				}

				var currentSelection = me.gridSM.getSelection();
				if (Ext.isArray(currentSelection)
						&& currentSelection.length>0) {

					currentSelection = currentSelection[0];
				}

				var id = currentSelection.get("Id");
				if (id && id == card.get("Id")) {
					return;
				} else {
					me.openCard({
						Id: card.get("Id"),
						IdClass: card.get("IdClass")
					});
				}
			};

			_CMCardModuleState.addDelegate(sd);
		},

		getEntryType: function() {
			return _CMCardModuleState.entryType;
		},

		onEntryTypeSelected : function(entryType, danglingCard, viewFilter) {
			if (!entryType) {
				return;
			}

			unApplyFilter(this);

			var me = this,
				afterStoreUpdated;

			if (danglingCard) {
				afterStoreUpdated = function() {
					me.openCard(danglingCard, retryWithoutFilter = true);
				};
			} else {
				afterStoreUpdated = function() {
					if (viewFilter) {
						var filter = {};

						// If viewFilter is a CMFilterModel instance we have a default filter so force button enable
						if (Ext.getClassName(viewFilter) == 'CMDBuild.model.CMFilterModel') {
							filter = viewFilter;

							me.view.enableFilterMenuButton();
						} else {
							filter = new CMDBuild.model.CMFilterModel({ configuration: Ext.decode(viewFilter) });
						}

						applyFilter(me, filter);
					} else {
						me.view.loadPage(1, {
							cb: function(args) {
								var records = args[1];

								if (records && records.length > 0) {
									try {
										me.gridSM.select(0);
									} catch (e) {
										_debug(e);
									}
								}

								// Not a good implementation but don't exists another way
								if (!args[2]) {
									CMDBuild.core.Message.error(null, {
										text: CMDBuild.Translation.errors.unknown_error
									});
								}
							}
						});
					}
				};
			}

			if (viewFilter) {
				me.view.disableFilterMenuButton();
			} else {
				me.view.enableFilterMenuButton();
			}

			me.view.updateStoreForClassId(me.getEntryType().get("id"), {
				cb: afterStoreUpdated
			});

		},

		onAddCardButtonClick: function() {
			this.gridSM.deselectAll();
		},

		/**
		 * @params {String} format
		 */
		onPrintGridMenuClick: function(format) {
			if (!Ext.isEmpty(format)) {
				var params = Ext.apply({}, this.view.getStore().proxy.extraParams);
				params[CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTES] = Ext.encode(this.view.getVisibleColumns());
				params[CMDBuild.core.proxy.CMProxyConstants.SORT] = Ext.encode(this.view.getStore().getSorters());
				params[CMDBuild.core.proxy.CMProxyConstants.TYPE] = format;

				Ext.create('CMDBuild.controller.common.entryTypeGrid.printTool.PrintWindow', {
					parentDelegate: this,
					format: format,
					mode: 'view',
					parameters: params
				});
			}
		},

		onCardSelected: function(sm, selection) {
			if (Ext.isArray(selection)) {
				if (selection.length > 0) {
					_CMCardModuleState.setCard(selection[0]);
				}
			}
		},

		onWrongSelection: function() {
			this.fireEvent(this.CMEVENTS.wrongSelection);
		},

		onGridIsVisible: function(visible) {
			if (visible) {
				if (_CMCardModuleState.card) {
					this.openCard({
						Id: _CMCardModuleState.card.get("Id"),
						IdClass: _CMCardModuleState.card.get("IdClass")
					});
				}
			}

			var selection = this.gridSM.getSelection();
			this.fireEvent(this.CMEVENTS.gridVisible, visible, selection);
		},

		onCardSaved: function(c) {
			var retryIfTheCardIsNotInFilter = true;
			this.openCard(c, retryIfTheCardIsNotInFilter);
		},

		onCardDeleted: function() {
			this.view.reload();
		},

		onCloneCard: function() {
			this.gridSM.deselectAll();
		},

		/**
		 *
		 * @param {object} p
		 * @param {int} p.IdClass the id of the class
		 * @param {int} p.Id the id of the card to open
		 * @param {boolean} retryWithoutFilter
		 */
		openCard: function(p, retryWithoutFilter) {
			var me = this;
			var store = this.view.getStore();

			if (!store ||
					!store.proxy ||
						!store.proxy.extraParams) {
				return;
			}

			// Take the current store configuration
			// to have the sort and filter
			var params = Ext.apply({}, store.proxy.extraParams);
			params[_CMProxy.parameter.CARD_ID] = p.Id;
			params[_CMProxy.parameter.CLASS_NAME] = _CMCache.getEntryTypeNameById(p.IdClass);
			params[_CMProxy.parameter.RETRY_WITHOUT_FILTER] = retryWithoutFilter;
			params[_CMProxy.parameter.SORT] = Ext.encode(getSorting(store));

			CMDBuild.ServiceProxy.card.getPosition({
				params: params,
				failure: function onGetPositionFailure(response, options, decoded) {
					// reconfigure the store and blah blah blah
				},
				success: function onGetPositionSuccess(response, options, resText) {
					var position = resText.position,
						found = position >= 0,
						foundButNotInFilter = resText.outOfFilter;

					if (found) {
						if (foundButNotInFilter) {
							me._onGetPositionSuccessForcingTheFilter(p, position, resText);
							me.view.gridSearchField.onUnapplyFilter();
						} else {
							updateStoreAndSelectGivenPosition(me, p.IdClass, position);
						}
					} else {
						if (retryWithoutFilter) {
							CMDBuild.Msg.error(CMDBuild.Translation.common.failure,
									Ext.String.format(CMDBuild.Translation.errors.reasons.CARD_NOTFOUND, p.IdClass));
						} else {
							me._onGetPositionFailureWithoutForcingTheFilter(resText);
						}

						me.view.store.loadPage(1);
					}
				}
			});
		},

		reload: function(reselect) {
			this.view.reload(reselect);
		},

		_onGetPositionSuccessForcingTheFilter: function(p, position, resText) {
			var me = this;
			var view = me.view;
			unApplyFilter(me);
			updateStoreAndSelectGivenPosition(me, p.IdClass, position);
		},

		_onGetPositionFailureWithoutForcingTheFilter: function(resText) {
			CMDBuild.Msg.info(undefined, CMDBuild.Translation.info.card_not_found);
		},
		// protected
		unApplyFilter: unApplyFilter,

		// As filterMentuButtonDelegate

		/**
		 * Called by the CMFilterMenuButton when click
		 * to on the save icon on a row of the picker
		 *
		 * @param {object} filter, the filter to save
		 * @param {CMDBuild.view.management.common.filter.CMFilterMenuButton} button
		 * the button that calls the delegate
		 */
		onFilterMenuButtonSaveActionClick: function(button, filter) {
			if (!filter.dirty) {
				return;
			}

			showSaveFilterDialog(this, filter);
		},

		/**
		 * Called by the CMFilterMenuButton when click
		 * to the clear button
		 */
		onFilterMenuButtonClearActionClick: function(button) {
			unApplyFilter(this);
			this.view.reload();
		},

		/**
		 * Called by the CMFilterMenuButton when click
		 * to on the apply icon on a row of the picker
		 *
		 * @param {object} filter, the filter to apply
		 */
		onFilterMenuButtonApplyActionClick: function(button, filter) {
			applyFilter(this, filter);
		},

		/**
		 * Called by the CMFilterMenuButton when click
		 * to the new button
		 */
		onFilterMenuButtonNewActionClick: function(button) {
			var filter = new CMDBuild.model.CMFilterModel({
				entryType: this.getEntryType(),
				local: true,
				name: CMDBuild.Translation.management.findfilter.newfilter + " " + _CMUtils.nextId()
			});

			this.onFilterMenuButtonModifyActionClick(button, filter);
		},

		/**
		 * Called by the CMFilterMenuButton when click
		 * to on the modify icon on a row of the picker
		 *
		 * @param {object} filter, the filter to modify
		 * @param {CMDBuild.view.management.common.filter.CMFilterMenuButton} button
		 * the button that calls the delegate
		 */
		onFilterMenuButtonCloneActionClick: function(button, filter) {
			filter.set("id", "");
			filter.setLocal(true);
			filter.setName(CMDBuild.Translation.management.findfilter.copyof + " " + filter.getName());
			this.onFilterMenuButtonModifyActionClick(button, filter);
		},

		/**
		 * Called by the CMFilterMenuButton when click to on the modify icon on a row of the picker
		 *
		 * @param {Object} filter - the filter to modify
		 */
		onFilterMenuButtonModifyActionClick: function(button, filter) {
			var filterWindow = Ext.create('CMDBuild.view.management.common.filter.CMFilterWindow', {
				filter: filter,
				attributes: this.view.classAttributes,
				className: _CMCache.getEntryTypeNameById(this.view.currentClassId),
				filterTabToEnable: {
					attributeTab: true,
					relationTab: true,
					functionTab: false
				}
			});

			filterWindow.addDelegate(this);
			filterWindow.show();
		},

		/**
		 * Called by the CMFilterMenuButton when click
		 * to on the remove icon on a row of the picker
		 *
		 * @param {object} filter, the filter to remove
		 */
		onFilterMenuButtonRemoveActionClick: function(button, filter) {
			var me = this;

			function onSuccess() {
				if (filter.isApplied()) {
					me.onFilterMenuButtonClearActionClick(button);
				}

				removeFilterFromStore(me, filter);
				button.setFilterButtonLabel();
			}

			function makeRequest(btn) {
				if (btn != 'yes') {
					return;
				}

				if (filter.isLocal()) {
					onSuccess();
				} else {
					CMDBuild.ServiceProxy.Filter.remove(filter, {
						success: onSuccess
					});
				}
			};

			Ext.Msg.confirm(CMDBuild.Translation.management.findfilter.msg.attention, CMDBuild.Translation.common.confirmpopup.areyousure, makeRequest, this);
		},

		// as cmFilterWindow

		/**
		 * @params {CMDBuild.view.management.common.filter.CMFilterWindow} filterWindow
		 * The filter window that call the delegate
		 */
		onCMFilterWindowSaveAndApplyButtonClick: function(filterWindow, filter) {
			showSaveFilterDialog(this, filter, filterWindow);
		},

		/**
		 * @params {CMDBuild.view.management.common.filter.CMFilterWindow} filterWindow
		 * The filter window that call the delegate
		 */
		onCMFilterWindowApplyButtonClick: function(filterWindow, filter) {
			applyFilter(this, filter);
			if (filterWindow) {
				filterWindow.destroy();
			}
		},

		/**
		 * @params {CMDBuild.view.management.common.filter.CMFilterWindow} filterWindow
		 * The filter window that call the delegate
		 */
		onCMFilterWindowAbortButtonClick: function(filterWindow) {
			filterWindow.destroy();
		},

		// as saveFilterWindow

		/**
		 * @param {CMDBuild.view.management.common.filter.CMSaveFilterWindow} window
		 * the window that calls the delegate
		 * @param {CMDBuild.model.CMFilterModel} filter
		 * the filter to save
		 * @param {String} name
		 * the name set in the form
		 * @param {String} the description set in the form
		 */
		onSaveFilterWindowConfirm: function(saveFilterWindow, filter, name, description) {
			var me = this;
			function onSuccess() {
				me.view.filterMenuButton.load();

				if (saveFilterWindow.referredFilterWindow) {
					me.onCMFilterWindowApplyButtonClick(saveFilterWindow.referredFilterWindow, filter);
				}

				saveFilterWindow.destroy();
				me.view.selectAppliedFilter();

				if (filter.isApplied()) {
					var s = (Ext.String.trim(filter.getDescription()) == "") ? filter.getName() : filter.getDescription();
					me.view.setFilterButtonLabel(s);
				}
			}

			removeFilterFromStore(me, filter);

			filter.setName(name);
			filter.setDescription(description);
			filter.commit();

			var action = filter.getId() ? "update" : "create";
			CMDBuild.ServiceProxy.Filter[action](filter, {
				success: onSuccess
			});
		},

		// as runtimeFilterParamsWindow
		onRuntimeParameterWindowSaveButtonClick: function(runtimeParameterWindow, filter) {
			applyFilter(this, filter, runtimeParameterWindow.runtimeAttributes);
			runtimeParameterWindow.destroy();
		}

	});

	function getFilterStore(me) {
		return me.view.filterMenuButton.getFilterStore();
	}

	function removeFilterFromStore(me, filter) {
		_CMCache.removeFilter(getFilterStore(me), filter);
	}

	function addFilterToStore(me, filter, atFirst) {
		_CMCache.addFilter(getFilterStore(me), filter, atFirst);
	}

	function updateFilterToStore(me, filter) {
		_CMCache.updateFilter(getFilterStore(me), filter);
	}

	function setStoredFilterApplied(me, filter) {
		var applied = true;
		_CMCache.setFilterApplied(getFilterStore(me), filter, applied);
	}

	function setStoreFilterUnapplied(me, filter) {
		var applied = false;
		_CMCache.setFilterApplied(getFilterStore(me), filter, applied);
	}

	function applyFilter(me, filter, runtimeAttributeFields) {
		if (filter.getRuntimeParameters().length > 0 && !runtimeAttributeFields) {
			showRuntimeParameterWindow(me, filter);
		} else {
			unApplyFilter(me);

			me.appliedFilter = filter;

			if (filter.dirty) {
				var atFirst = true;
				addFilterToStore(me, filter, atFirst);
			}

			var s = (Ext.String.trim(filter.getDescription()) == "") ? filter.getName() : filter.getDescription();
			me.view.setFilterButtonLabel(s);
			me.view.applyFilterToStore( //
					filter.getConfigurationMergedWithRuntimeAttributes(runtimeAttributeFields) //
				);

			me.view.enableClearFilterButton();
			me.view.loadPage(1);

			setStoredFilterApplied(me, filter);
		}
	}

	function showRuntimeParameterWindow(me, filter) {
		var runtimeAttributeConfigurations = filter.getRuntimeParameters();
		var runtimeAttributes = [];
		var showWindowToFillRuntimeParameters = runtimeAttributeConfigurations.length > 0;
		if (showWindowToFillRuntimeParameters) {
			var referredEntryTypeName = filter.getEntryType();
			var referredEntryType = _CMCache.getEntryTypeByName(referredEntryTypeName);

			if (referredEntryType) {
				_CMCache.getAttributeList(referredEntryType.getId(), //
						function(attributes) { //
							for (var i=0; i<runtimeAttributeConfigurations.length; ++i) {
								var runtimeAttributeToSearch = runtimeAttributeConfigurations[i];

								for (var j=0; j<attributes.length; ++j) {
									/*
									 * Force the attribute to be writable
									 * to allow the user to edit it
									 * in the RealTimeParameterWindow
									 */
									var attribute = Ext.apply({}, attributes[j]);
									attribute.fieldmode = "write";

									if (attribute.name == runtimeAttributeToSearch.attribute) {
										var field = CMDBuild.Management.FieldManager.getFieldForAttr(attribute);
										field._cmOperator = runtimeAttributeToSearch.operator;
										runtimeAttributes.push(field);

										break;
									}
								}
							}
						} //
					); //
			}

			var runtimeParametersWindow = new CMDBuild.view.management.common.filter.CMRuntimeParameterWindow({
				runtimeAttributes: runtimeAttributes,
				filter: filter,
				title: filter.getName()
			});

			runtimeParametersWindow.addDelegate(me);

			runtimeParametersWindow.show();
		}

		return showWindowToFillRuntimeParameters;
	}

	function unApplyFilter(me) {
		if (me.appliedFilter) {
			setStoreFilterUnapplied(me, me.appliedFilter);
			me.appliedFilter = undefined;
		}

		me.view.setFilterButtonLabel();
		me.view.applyFilterToStore({});
		me.view.disableClearFilterButton();
	}

	function showSaveFilterDialog(me, filter, referredFilterWindow) {
		var saveFilterWindow = new CMDBuild.view.management.common.filter.CMSaveFilterWindow({
			filter: filter,
			referredFilterWindow: referredFilterWindow
		});

		saveFilterWindow.addDelegate(me);
		saveFilterWindow.show();
	}

	function updateStoreAndSelectGivenPosition(me, idClass, position) {
		var view = me.view;
		view.updateStoreForClassId(idClass, {
			cb: function cbOfUpdateStoreForClassId() {
				var	pageNumber = _CMUtils.grid.getPageNumber(position),
					pageSize = _CMUtils.grid.getPageSize(),
					relativeIndex = position % pageSize;

				view.loadPage(pageNumber, {
					cb: function() {
						var parameters = arguments[0];

						try {
							me.gridSM.deselectAll();
							me.gridSM.select(relativeIndex);

							// Force row expanding on select
							if (!Ext.isEmpty(me.view.plugins[0]) && me.view.plugins[0].ptype == 'activityrowexpander')
								me.view.plugins[0].toggleRow(relativeIndex, me.view.getStore().getAt(relativeIndex));
						} catch (e) {
							view.fireEvent("cmWrongSelection");
							_trace("I was not able to select the record at " + relativeIndex);
						}

						if (!parameters[2]) {
							CMDBuild.core.Message.error(null, {
								text: CMDBuild.Translation.errors.unknown_error
							});
						}
					}
				});
			}
		});
	}

	function getSorting(store) {
		var sorters = store.getSorters();
		var out = [];
		for (var i=0, l=sorters.length; i<l; ++i) {
			var s = sorters[i];
			out.push({
				property: s.property,
				direction: s.direction
			});
		}

		return out;
	}
})();