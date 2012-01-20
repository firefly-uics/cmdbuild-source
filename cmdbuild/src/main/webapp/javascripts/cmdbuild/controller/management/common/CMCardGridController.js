(function() {
	Ext.define("CMDBuild.controller.management.common.CMCardGridController", {

		mixins: {
			observable: "Ext.util.Observable"
		},

		constructor: function(view, supercontroller) {
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
		},

		onEntryTypeSelected : function(entryType) {
			this.entryType = entryType;
			var me = this;

			me.view.updateStoreForClassId(me.entryType.get("id"), {
				cb: function cbUpdateStoreForClassId() {
					me.view.loadPage(1, {
						cb: function cbLoadPage() {
							try {
								me.gridSM.select(0);
							} catch (e) {
								_debug(e);
							}
						}
					});
				}
			});

			me.view.openFilterButton.enable();
			me.view.clearFilterButton.disable();
			me.view.gridSearchField.reset();
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
					IdClass: me.entryType.get("id"),
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
			var me = this;
			if (Ext.isArray(selection)) {
				if (selection.length > 0) {
					me.fireEvent(me.CMEVENTS.cardSelected, selection[0]);
				}
			}
		},

		onWrongSelection: function() {
			this.fireEvent(this.CMEVENTS.wrongSelection);
		},

		onGridIsVisible: function(visible) {
			var selection = this.gridSM.getSelection();
			this.fireEvent(this.CMEVENTS.gridVisible, visible, selection);
		},

		onCardSaved: function(c) {
			this.view.openCard(c);
		},

		onCardDeleted: function() {
			this.view.reload();
		},

		openCard: function(p, retryWithoutFilter) {
			var me = this,
				params = {
					retryWithoutFilter: retryWithoutFilter
				};

			Ext.apply(params, p, me.view.getStoreExtraParams());
	
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
							updateStoreAndSelectGivenPosition.call(me, p.IdClass, position);
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

		getPageNumber: function getPageNumber(cardPosition) {
			var pageSize = parseInt(CMDBuild.Config.cmdbuild.rowlimit),
				pageNumber = 1;
	
			if (cardPosition == 0) {
				return pageNumber;
			}
	
			if (cardPosition) {
				pageNumber = parseInt(cardPosition) / pageSize;
			}
	
			return pageNumber + 1;
		},

		_onGetPositionSuccessForcingTheFilter: function() {
			var me = this.view;
			me.clearFilter(function() {
				me.gridSearchField.reset();
				updateStoreAndSelectGivenPosition.call(me, p.IdClass, position);
			}, skipReload=true);
		},

		_onGetPositionFailureWithoutForcingTheFilter: function(resText) {
			CMDBuild.Msg.info(undefined, CMDBuild.Translation.info.card_not_found);
		}
	});

	function updateStoreAndSelectGivenPosition(idClass, position) {
		var me = this;

		this.view.updateStoreForClassId(idClass, {
			cb: function cbOfUpdateStoreForClassId() {
				var	pageNumber = me.getPageNumber(position),
					pageSize = parseInt(CMDBuild.Config.cmdbuild.rowlimit),
					relativeIndex = position % pageSize;

				me.view.loadPage(pageNumber, {
					cb: function callBackOfLoadPage(records, operation, success) {
						try {
							me.gridSM.select(relativeIndex);
						} catch (e) {
							me.view.fireEvent("cmWrongSelection");
							_debug("I was not able to select the record at " + relativeIndex);
							_trace();
						}
					}
				});
			}
		});
	}
})();