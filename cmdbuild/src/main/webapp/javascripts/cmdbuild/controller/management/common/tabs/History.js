(function() {

	/**
	 * @abstract
	 */
	Ext.define('CMDBuild.controller.management.common.tabs.History', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.common.tabs.history.Classes',
			'CMDBuild.model.common.tabs.history.RelationRecord'
		],

//		mixins: {
//			observable: 'Ext.util.Observable'// TODO delete
//		},

		/**
		 * @cfg {Mixed}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'getGridColumns', // TODO rename con history
			'getGridStore', // TODO rename con history
			'getRowExpanderPlugin', // TODO rename con history
			'onHistoryIncludeRelationCheck',
			'onHistoryRowExpand',
			'onHistoryTabPanelShow'
		],

		/**
		 * @property {CMDBuild.cache.CMEntryTypeModel}
		 */
		entryType: undefined,

		/**
		 * @property {CMDBuild.view.management.common.tabs.history.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {Mixed}
		 */
		selectedEntity: undefined,

		/**
		 * @property {CMDBuild.view.management.common.tabs.history.HistoryView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Mixed} configurationObject.parentDelegate
		 */
		constructor: function(configurationObject) {
//			this.mixins.observable.constructor.call(this, arguments); // TODO delete

			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.common.tabs.history.HistoryView', {
				delegate: this
			});

//			this.grid = Ext.create('CMDBuild.view.management.common.tabs.history.GridPanel', {
//				delegate: this
//			});
//
//			this.view.add(this.grid);

//			// Shorthands
//			this.grid = this.view.grid;

//			this.buildCardModuleStateDelegate();
		},

//		buildCardModuleStateDelegate: function() { // TODO delete
//			var me = this;
//
//			this.cardStateDelegate = new CMDBuild.state.CMCardModuleStateDelegate();
//
//			this.cardStateDelegate.onEntryTypeDidChange = function(state, entryType) {
//				me.onEntryTypeSelected(entryType);
//			};
//
//			this.cardStateDelegate.onCardDidChange = function(state, card) {
//				Ext.suspendLayouts();
//				me.onCardSelected(card);
//				Ext.resumeLayouts();
//			};
//
//			_CMCardModuleState.addDelegate(this.cardStateDelegate);
//
//			if (!Ext.isEmpty(this.view))
//				this.mon(this.view, 'destroy', function(view) {
//					_CMCardModuleState.removeDelegate(this.cardStateDelegate);
//
//					delete this.cardStateDelegate;
//				}, this);
//		},

		/**
		 * @return {Array} columns
		 */
		getExtraColumns: function() {
			var columns = [];

			if (!_CMUIConfiguration.isSimpleHistoryModeForCard()) {
				columns = [
					{
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.IS_CARD,
						text: CMDBuild.Translation.management.modcard.history_columns.attributes,
						width: 65,
						align: 'center',
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,

						renderer: function(value, metaData, record) {
							return value ? '<img src="images/icons/tick.png" alt="' + CMDBuild.Translation.management.modcard.history_columns.attributes + '" />' : null;
						}
					},
					{
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.IS_RELATION,
						text: CMDBuild.Translation.management.modcard.history_columns.relation,
						width: 65,
						align: 'center',
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,

						renderer: function(value, metaData, record) {
							return value ? '<img src="images/icons/tick.png" alt="' + CMDBuild.Translation.management.modcard.history_columns.relation + '" />' : null;
						}
					},
					{
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.DOMAIN,
						text: CMDBuild.Translation.management.modcard.history_columns.domain,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,
						flex: 1
					},
					{
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.DESTINATION_DESCRIPTION,
						text: CMDBuild.Translation.descriptionLabel,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,
						flex: 1
					}
				];
			}

			return columns;
		},

		/**
		 * @return {Array}
		 */
		getGridColumns: function() {
			var defaultColumns = [
				Ext.create('Ext.grid.column.Date', {
					dataIndex: CMDBuild.core.proxy.CMProxyConstants.BEGIN_DATE,
					text: CMDBuild.Translation.management.modcard.history_columns.begin_date,
					width: 140,
					format:'d/m/Y H:i:s',
					sortable: false,
					hideable: false,
					menuDisabled: true,
					fixed: true
				}),
				Ext.create('Ext.grid.column.Date', {
					dataIndex: CMDBuild.core.proxy.CMProxyConstants.END_DATE,
					text: CMDBuild.Translation.management.modcard.history_columns.end_date,
					width: 140,
					format:'d/m/Y H:i:s',
					sortable: false,
					hideable: false,
					menuDisabled: true,
					fixed: true
				}),
				{
					dataIndex: CMDBuild.core.proxy.CMProxyConstants.USER,
					text: CMDBuild.Translation.management.modcard.history_columns.user,
					flex: 1,
					sortable: false,
					hideable: false,
					menuDisabled: true,
					fixed: true
				}
			];

			return Ext.Array.push(defaultColumns, this.getExtraColumns());
		},

		/**
		 * @abstract
		 */
		getGridStore: Ext.emptyFn,

		onAddCardButtonClick: function() {
			this.view.disable();
		},

		/**
		 * @param {Object} card
		 */
		onCardSelected: function(card) {
			this.selectedEntity = card;

			this.onHistoryTabPanelShow();
		},
//		onCardSelected: function(card) { // TODO che sia da implementare questo controllo???
//			this.callParent(arguments);
//
//			if (card) {
//				if (this.entryType.get("tableType") != CMDBuild.Constants.cachedTableType.simpletable) {
//					var existingCard = (!!this.card);
//					this.view.setDisabled(!existingCard);
//
//					if (this.view.tabIsActive(this.view)) {
//						this.load();
//					} else {
//						this.mon(this.view, "activate", this.load, this, {single: true});
//					}
//				} else {
//					this.view.disable();
//				}
//			}
//		},

		onCloneCard: function() {
			this.view.disable();
		},

		/**
		 * @param {CMDBuild.cache.CMEntryTypeModel} entryType
		 */
		onEntryTypeSelected: function(entryType) {
			this.entryType = entryType;

			this.view.disable();
		},

		/**
		 * Reloads store to be consistent with includeRelationsCheckbox state
		 */
		onHistoryIncludeRelationCheck: function() {
			this.onHistoryTabPanelShow();
		},

		/**
		 * @param {CMDBuild.model.common.tabs.history.CardRecord or CMDBuild.model.common.tabs.history.RelationRecord} record
		 */
		onHistoryRowExpand: function(record) {
			if (
				!Ext.isEmpty(record)
				&& Ext.Object.isEmpty(record.get(CMDBuild.core.proxy.CMProxyConstants.VALUES)) // Optimization to avoid to ask already owned data
			) {
				var params = {};

				if (record.get(CMDBuild.core.proxy.CMProxyConstants.IS_CARD)) { // Is card record
					params[CMDBuild.core.proxy.CMProxyConstants.CARD_ID] = record.get(CMDBuild.core.proxy.CMProxyConstants.ID); // Historic card ID
					params[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = record.get(CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME);

					CMDBuild.core.proxy.common.tabs.history.Classes.getHistoric({
						params: params,
						scope: this,
						failure: function(response, options, decodedResponse) {
							_error('get historic card failure', this);
						},
						success: function(response, options, decodedResponse) {
							var cardValuesObject = decodedResponse.response[CMDBuild.core.proxy.CMProxyConstants.VALUES];
							var predecessorRecord = null;

							// Find expanded record predecessor record
							this.grid.getStore().findBy(function(storeRecord, id) {
								if (Ext.Object.equals(record, storeRecord)) {
									// Find predecessor card store record
									for (var i = this.grid.getStore().indexOfId(id) + 1; i < this.grid.getStore().getCount(); i++) {
										var inspectedRecord = this.grid.getStore().getAt(i);

										if (
											Ext.isEmpty(predecessorRecord)
											&& !Ext.isEmpty(inspectedRecord)
											&& inspectedRecord.get(CMDBuild.core.proxy.CMProxyConstants.IS_CARD)
										) {
											predecessorRecord = inspectedRecord;

											return true;
										}
									}
								}

								return false;
							}, this);

							if (!Ext.isEmpty(predecessorRecord)) {
								var predecessorParams = {};
								predecessorParams[CMDBuild.core.proxy.CMProxyConstants.CARD_ID] = predecessorRecord.get(CMDBuild.core.proxy.CMProxyConstants.ID); // Historic card ID
								predecessorParams[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = record.get(CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME);

								CMDBuild.core.proxy.common.tabs.history.Classes.getHistoric({
									params: predecessorParams,
									scope: this,
									failure: function(response, options, decodedResponse) {
										_error('get historic predecessor card failure', this);
									},
									success: function(response, options, decodedResponse) {
										this.valuesFormattingAndCompare(cardValuesObject, decodedResponse.response[CMDBuild.core.proxy.CMProxyConstants.VALUES]);

										// Setup record property with historic card details to use XTemplate functionalities to render
										record.set(CMDBuild.core.proxy.CMProxyConstants.VALUES, cardValuesObject);
									}
								});
							} else {
								this.valuesFormattingAndCompare(cardValuesObject);

								// Setup record property with historic card details to use XTemplate functionalities to render
								record.set(CMDBuild.core.proxy.CMProxyConstants.VALUES, cardValuesObject);
							}
						}
					});
				} else { // Is relation record
					params[CMDBuild.core.proxy.CMProxyConstants.ID] = record.get(CMDBuild.core.proxy.CMProxyConstants.ID); // Historic relation ID
					params[CMDBuild.core.proxy.CMProxyConstants.DOMAIN] = record.get(CMDBuild.core.proxy.CMProxyConstants.DOMAIN);

					CMDBuild.core.proxy.common.tabs.history.Classes.getRelationHistoric({
						params: params,
						scope: this,
						failure: function(response, options, decodedResponse) {
							_error('get historic relation failure', this);
						},
						success: function(response, options, decodedResponse) {
							var cardValuesObject = decodedResponse.response[CMDBuild.core.proxy.CMProxyConstants.VALUES];

							this.valuesFormattingAndCompare(cardValuesObject);

							// Setup record property with historic relation details to use XTemplate functionalities to render
							record.set(CMDBuild.core.proxy.CMProxyConstants.VALUES, cardValuesObject);
						}
					});
				}
			}
		},

		/**
		 * Loads store and if includeRelationsCheckbox is checked fills store with relations rows
		 */
		onHistoryTabPanelShow: function() {
			var isSelectedEntityEmpty = Ext.isEmpty(this.selectedEntity);

			this.view.setDisabled(isSelectedEntityEmpty);
			this.grid.getStore().removeAll(); // Clear store before load new one

			if (!isSelectedEntityEmpty) {
				var params = {};
				params[CMDBuild.core.proxy.CMProxyConstants.CARD_ID] = this.selectedEntity.get(CMDBuild.core.proxy.CMProxyConstants.ID);
				params[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.selectedEntity.get('IdClass'));

				this.grid.getStore().load({
					params: params,
					scope: this,
					callback: function(records, operation, success) {
						if (this.grid.includeRelationsCheckbox.getValue())
							CMDBuild.core.proxy.common.tabs.history.Classes.getRelations({
								params: params,
								scope: this,
								failure: function(response, options, decodedResponse) {
									_error('getCardRelationsHistory failure', this);
								},
								success: function(response, options, decodedResponse) {
									var referenceElements = decodedResponse.response.elements;

									Ext.Array.forEach(referenceElements, function(element, i, allElements) {
										referenceElements[i] = Ext.create('CMDBuild.model.common.tabs.history.RelationRecord', element);
									});

									// Clear store and re-add all records to avoid RowExpander plugin bug that appens with store add action that won't manage
									// correctly expand/collapse events.
									this.grid.getStore().removeAll();
									this.grid.getStore().add(Ext.Array.merge(records, referenceElements));
								}
							});
					}
				});
			}
		},

		/**
		 * Formats all object1 values as objects { {Mixed} description: "...", {Boolean} changed: "..." }. If value1 is different than value2 modified is true,
		 * false otherwise.
		 *
		 * @param {Object} object1
		 * @param {Object} object2
		 */
		valuesFormattingAndCompare: function(object1, object2) {
			if (!Ext.isEmpty(object1) && Ext.isObject(object1)) {
				Ext.Object.each(object1, function(key, value, myself) {
					if (Ext.isObject(value)) {
						object1[key].changed = false;

						if (
							!Ext.isEmpty(value[CMDBuild.core.proxy.CMProxyConstants.ID])
							&& !Ext.isEmpty(object2)
							&& !Ext.isEmpty(object2[key][CMDBuild.core.proxy.CMProxyConstants.ID])
						) {
							object1[key].changed = (value[CMDBuild.core.proxy.CMProxyConstants.ID] != object2[key][CMDBuild.core.proxy.CMProxyConstants.ID]);
						}
					} else {
						object1[key] = {
							description: value,
							changed: false
						};

						if (!Ext.isEmpty(object2) && !Ext.isEmpty(object2[key]))
							object1[key].changed = (value != object2[key]);
					}
				});
			}
		}
	});

})();