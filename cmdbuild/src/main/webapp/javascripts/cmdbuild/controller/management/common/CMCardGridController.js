(function() {
	Ext.define("CMDBuild.controller.management.common.CMCardGridController", {

		mixins: {
			observable: "Ext.util.Observable",
			filterMenuButton: "CMDBuild.view.management.common.filter.CMFilterMenuButtonDelegate",
			filterWindow: "CMDBuild.view.management.common.filter.CMFilterWindowDelegate",
			saveFilterWindow: "CMDBuild.view.management.common.filter.CMSaveFilterWindowDelegate"
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

			sd.onEntryTypeDidChange = function(state, entryType, danglingCard) {
				me.onEntryTypeSelected(entryType, danglingCard);
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

		onEntryTypeSelected : function(entryType, danglingCard) {
			if(!entryType) {
				return;
			}

			unApplyFilter(this); // before to filter the store, otherwise it is not founded in the store
			_CMCache.filterStoreByEntryTypeName(entryType.getName());

			var me = this,
				afterStoreUpdated;

			if (danglingCard) {
				afterStoreUpdated = function() {
					me.openCard(danglingCard, retryWithoutFilter = true);
				};
			} else {
				afterStoreUpdated = function cbUpdateStoreForClassId() {
					me.view.loadPage(1, {
						cb: function cbLoadPage(args) {
							var records = args[1];
							if (records && records.length > 0) {
								try {
									me.gridSM.select(0);
								} catch (e) {
									_debug(e);
								}
							}
						}
					});
				};
			}

			me.view.updateStoreForClassId(me.getEntryType().get("id"), {
				cb: afterStoreUpdated
			});

		},

		onAddCardButtonClick: function() {
			this.gridSM.deselectAll();
		},

		onPrintGridMenuClick: function(format) {
			if (typeof format != "string") {
				return;
			}

			var columns = this.view.getVisibleColumns(),
				me = this;

			CMDBuild.LoadMask.get().show();
			CMDBuild.Ajax.request({
				url: 'services/json/management/modreport/printcurrentview',
				params: {
					FilterCategory: me.view.filterCategory,
					IdClass: me.getEntryType().get("id"),
					type: format,
					columns: Ext.JSON.encode(columns)
				},
				success: function(response) {
					var popup = window.open("services/json/management/modreport/printreportfactory", "Report", "height=400,width=550,status=no,toolbar=no,scrollbars=yes,menubar=no,location=no,resizable");
					if (!popup) {
						CMDBuild.Msg.warn(CMDBuild.Translation.warnings.warning_message,CMDBuild.Translation.warnings.popup_block);
					}
				},
				callback : function() {
					CMDBuild.LoadMask.get().hide();
				}
			});
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
			this.openCard(c);
		},

		onCardDeleted: function() {
			this.view.reload();
		},

		onCloneCard: function() {
			this.onAddCardButtonClick();
		},

		openCard: function(p, retryWithoutFilter) {
			var me = this,
				params = {
					retryWithoutFilter: retryWithoutFilter
				};

			fillParams(params, p, me);
	
			CMDBuild.ServiceProxy.card.getPosition({
				params: params,
				failure: function onGetPositionFailure(response, options, decoded) {
					// reconfigure the store and blah blah blah
				},
				success: function onGetPositionSuccess(response, options, resText) {
					var position = resText.position,
						found = position >= 0,
						foundButNotInFilter = resText.notFoundInFilter;
	
					if (found) {
						if (foundButNotInFilter) {
							me._onGetPositionSuccessForcingTheFilter(p, position, resText);
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
			view.clearFilter(function() {
				view.gridSearchField.reset();
				updateStoreAndSelectGivenPosition(me, p.IdClass, position);
			}, skipReload=true);
		},

		_onGetPositionFailureWithoutForcingTheFilter: function(resText) {
			CMDBuild.Msg.info(undefined, CMDBuild.Translation.info.card_not_found);
		},

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
			filter.setLocal(true);
			filter.setName(CMDBuild.Translation.management.findfilter.copyof + " " + filter.getName());
			this.onFilterMenuButtonModifyActionClick(button, filter);
		},

		/**
		 * Called by the CMFilterMenuButton when click
		 * to on the modify icon on a row of the picker
		 * 
		 * @param {object} filter, the filter to modify
		 */
		onFilterMenuButtonModifyActionClick: function(button, filter) {
			var filterWindow = new CMDBuild.view.management.common.filter.CMFilterWindow({
				filter: filter,
				attributes: this.view.classAttributes,
				className: _CMCache.getEntryTypeNameById(this.view.currentClassId)
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
				_CMCache.removeFilter(filter);
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
				if (filter.isLocal()) {
					_CMCache.addFilter(filter);
				} else {
					_CMCache.updateFilter(filter);
				}

				if (saveFilterWindow.referredFilterWindow) {
					me.onCMFilterWindowApplyButtonClick(saveFilterWindow.referredFilterWindow, filter);
				}

				saveFilterWindow.destroy();
				me.view.selectAppliedFilter();

				if (filter.isApplied()) {
					me.view.setFilterButtonLabel(filter.getName());
				} 
			}

			_CMCache.removeFilter(filter);
			filter.setName(name);
			filter.setDescription(description);
			filter.commit();

			var action = filter.isLocal() ? "create" : "update";
			CMDBuild.ServiceProxy.Filter[action](filter, {
				success: onSuccess
			});
		}
	});

	function applyFilter(me, filter) {
		unApplyFilter(me);

		me.appliedFilter = filter;

		if (filter.dirty) {
			var atFirst = true;
			_CMCache.addFilter(filter, atFirst);
		}

		me.view.setFilterButtonLabel(filter.getName());
		me.view.applyFilterToStore(filter.getConfiguration());
		me.view.enableClearFilterButton();
		me.view.reload();

		var applied = true;
		_CMCache.setFilterApplied(filter, applied);
	}

	function unApplyFilter(me) {
		if (me.appliedFilter) {
			var applied = false;
			_CMCache.setFilterApplied(me.appliedFilter, applied);

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

	function fillParams(params, p, me) {
		Ext.apply(params, p, me.view.getStoreExtraParams());
		if (me.view.gridSearchField && me.view.gridSearchField.getValue()) {
			params.query = me.view.gridSearchField.getValue();
		}
	}

	function updateStoreAndSelectGivenPosition(me, idClass, position) {
		var view = me.view;
		view.updateStoreForClassId(idClass, {
			cb: function cbOfUpdateStoreForClassId() {
				var	pageNumber = _CMUtils.grid.getPageNumber(position),
					pageSize = _CMUtils.grid.getPageSize(),
					relativeIndex = position % pageSize;

				view.loadPage(pageNumber, {
					cb: function callBackOfLoadPage(records, operation, success) {
						try {
							me.gridSM.select(relativeIndex);
						} catch (e) {
							view.fireEvent("cmWrongSelection");
							_trace("I was not able to select the record at " + relativeIndex);
						}
					}
				});
			}
		});
	}
})();