(function() {

	/**
	 * @abstract
	 */
	Ext.define('CMDBuild.controller.management.common.tabs.History', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Attributes'
		],

		/**
		 * @cfg {Mixed}
		 */
		parentDelegate: undefined,

		/**
		 * @property {Object}
		 */
		entryTypeAttributes: {},

		/**
		 * @property {CMDBuild.view.management.classes.tabs.history.GridPanel or CMDBuild.view.management.workflow.tabs.history.GridPanel}
		 */
		grid: undefined,

		/**
		 * Selected card
		 *
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
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.common.tabs.history.HistoryView', {
				delegate: this
			});
		},

		/**
		 * Adds current card to history store for a better visualization of differences from last history record and current one.
		 *
		 * @abstract
		 */
		addCurrentCardToStore: Ext.emptyFn,

		/**
		 * Clear store and re-add all records to avoid RowExpander plugin bug that appens with store add action that won't manage correctly expand/collapse events
		 *
		 * @param {Array or Object} itemsToAdd
		 */
		clearStoreAdd: function(itemsToAdd) {
			var oldStoreDatas = this.grid.getStore().getRange();

			this.grid.getStore().loadData(Ext.Array.merge(oldStoreDatas, itemsToAdd));
		},

		/**
		 * @abstract
		 */
		currentCardRowExpand: Ext.emptyFn,

		/**
		 * @abstract
		 */
		getProxy: Ext.emptyFn,

		/**
		 * Finds same type (card or relation) current record predecessor
		 *
		 * @param {CMDBuild.model.common.tabs.history.classes.CardRecord or CMDBuild.model.common.tabs.history.classes.RelationRecord} record
		 *
		 * @returns {CMDBuild.model.common.tabs.history.classes.CardRecord or CMDBuild.model.common.tabs.history.classes.RelationRecord} predecessor or null
		 */
		getRecordPredecessor: function(record) {
			var i = this.grid.getStore().indexOf(record) + 1;
			var predecessor = null;

			if (!Ext.isEmpty(record) && !Ext.isEmpty(this.grid.getStore())) {
				while (i < this.grid.getStore().getCount() && Ext.isEmpty(predecessor)) {
					var inspectedRecord = this.grid.getStore().getAt(i);

					if (
						!Ext.isEmpty(inspectedRecord)
						&& record.get(CMDBuild.core.proxy.CMProxyConstants.IS_CARD) == inspectedRecord.get(CMDBuild.core.proxy.CMProxyConstants.IS_CARD)
						&& record.get(CMDBuild.core.proxy.CMProxyConstants.IS_RELATION) == inspectedRecord.get(CMDBuild.core.proxy.CMProxyConstants.IS_RELATION)
					) {
						predecessor = inspectedRecord;
					}

					i = i + 1;
				}
			}

			return predecessor;
		},

		/**
		 * @returns {CMDBuild.view.management.common.tabs.history.RowExpander} or null
		 */
		getRowExpanderPlugin: function() {
			var rowExpanderPlugin = null;

			if (
				!Ext.isEmpty(this.grid)
				&& !Ext.isEmpty(this.grid.plugins)
				&& Ext.isArray(this.grid.plugins)
			) {
				Ext.Array.forEach(this.grid.plugins, function(plugin, i, allPlugins) {
					if (plugin instanceof Ext.grid.plugin.RowExpander)
						rowExpanderPlugin = plugin;
				});
			}

			return rowExpanderPlugin;
		},

		/**
		 * @returns {Array}
		 */
		getTabHistoryGridColumns: function() {
			return [
				Ext.create('Ext.grid.column.Date', {
					dataIndex: CMDBuild.core.proxy.CMProxyConstants.BEGIN_DATE,
					text: CMDBuild.Translation.beginDate,
					width: 140,
					format:'d/m/Y H:i:s',
					sortable: false,
					hideable: false,
					menuDisabled: true,
					fixed: true
				}),
				Ext.create('Ext.grid.column.Date', {
					dataIndex: CMDBuild.core.proxy.CMProxyConstants.END_DATE,
					text: CMDBuild.Translation.endDate,
					width: 140,
					format:'d/m/Y H:i:s',
					sortable: false,
					hideable: false,
					menuDisabled: true,
					fixed: true
				}),
				{
					dataIndex: CMDBuild.core.proxy.CMProxyConstants.USER,
					text: CMDBuild.Translation.user,
					sortable: false,
					hideable: false,
					menuDisabled: true,
					flex: 1
				}
			];
		},

		/**
		 * @returns {Ext.data.Store}
		 */
		getTabHistoryGridStore: function() {
			return this.getProxy().getStore();
		},

		onAddCardButtonClick: function() {
			this.view.disable();
		},

		onCloneCard: function() {
			this.view.disable();
		},

		/**
		 * @param {CMDBuild.model.common.tabs.history.classes.CardRecord or CMDBuild.model.common.tabs.history.classes.RelationRecord} record
		 */
		onTabHistoryRowExpand: function(record) {
			if (!Ext.isEmpty(record)) {
				var params = {};

				if (record.get(CMDBuild.core.proxy.CMProxyConstants.IS_CARD)) { // Card row expand
					if (this.selectedEntity.get(CMDBuild.core.proxy.CMProxyConstants.ID) == record.get(CMDBuild.core.proxy.CMProxyConstants.ID)) { // Expanding current card
						this.currentCardRowExpand(record);
					} else {
						params[CMDBuild.core.proxy.CMProxyConstants.CARD_ID] = record.get(CMDBuild.core.proxy.CMProxyConstants.ID); // Historic card ID
						params[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = record.get(CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME);

						this.getProxy().getHistoric({ // Get expanded card data
							params: params,
							scope: this,
							failure: function(response, options, decodedResponse) {
								_error('get historic card failure', this);
							},
							success: function(response, options, decodedResponse) {
								var cardValuesObject = decodedResponse.response[CMDBuild.core.proxy.CMProxyConstants.VALUES];
								var predecessorRecord = this.getRecordPredecessor(record);

								if (!Ext.isEmpty(predecessorRecord)) {
									var predecessorParams = {};
									predecessorParams[CMDBuild.core.proxy.CMProxyConstants.CARD_ID] = predecessorRecord.get(CMDBuild.core.proxy.CMProxyConstants.ID); // Historic card ID
									predecessorParams[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = record.get(CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME);

									this.getProxy().getHistoric({ // Get expanded predecessor's card data
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
					}
				} else { // Relation row expand
					params[CMDBuild.core.proxy.CMProxyConstants.ID] = record.get(CMDBuild.core.proxy.CMProxyConstants.ID); // Historic relation ID
					params[CMDBuild.core.proxy.CMProxyConstants.DOMAIN] = record.get(CMDBuild.core.proxy.CMProxyConstants.DOMAIN);

					this.getProxy().getRelationHistoric({
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
		onTabHistoryPanelShow: function() {
			this.grid.getStore().removeAll(); // Clear store before load new one

			if (!Ext.isEmpty(this.selectedEntity) && this.view.isVisible()) {
				var params = {};
				params[CMDBuild.core.proxy.CMProxyConstants.ACTIVE] = true;
				params[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.selectedEntity.get('IdClass'));

				// Request all class attributes
				CMDBuild.core.proxy.Attributes.read({
					params: params,
					scope: this,
					failure: function(response, options, decodedResponse) {
						_error('get attributes failure', this);
					},
					success: function(response, options, decodedResponse) {
						Ext.Array.forEach(decodedResponse[CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTES], function(attribute, i, allAttributes) {
							this.entryTypeAttributes[attribute[CMDBuild.core.proxy.CMProxyConstants.NAME]] = attribute;
						}, this);

						params = {};
						params[CMDBuild.core.proxy.CMProxyConstants.CARD_ID] = this.selectedEntity.get(CMDBuild.core.proxy.CMProxyConstants.ID);
						params[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.selectedEntity.get('IdClass'));

						this.grid.getStore().load({
							params: params,
							scope: this,
							callback: function(records, operation, success) {
								this.getRowExpanderPlugin().collapseAll();

								if (this.grid.includeRelationsCheckbox.getValue()) {
									this.getProxy().getRelations({
										params: params,
										scope: this,
										failure: function(response, options, decodedResponse) {
											_error('getCardRelationsHistory failure', this);
										},
										success: function(response, options, decodedResponse) {
											var referenceElements = decodedResponse.response.elements;

											// Build reference models
											Ext.Array.forEach(referenceElements, function(element, i, allElements) {
												referenceElements[i] = Ext.create('CMDBuild.model.common.tabs.history.classes.RelationRecord', element);
											});

											this.clearStoreAdd(referenceElements);

											this.addCurrentCardToStore();
										}
									});
								} else {
									this.addCurrentCardToStore();
								}
							}
						});
					}
				});
			}
		},

		// SelectedEntity property functions
			/**
			 * @returns {Mixed}
			 */
			tabHistorySelectedEntityGet: function() {
				return this.selectedEntity;
			},

			/**
			 * @param {Mixed} selectedEntity
			 */
			tabHistorySelectedEntitySet: function(selectedEntity) {
				this.selectedEntity = Ext.isEmpty(selectedEntity) ? undefined : selectedEntity;
			},

		/**
		 * Formats all object1 values as objects { {Boolean} changed: "...", {Mixed} description: "..." }. If value1 is different than value2
		 * modified is true, false otherwise. Strips also HTML tags from "description".
		 *
		 * @param {Object} object1 - currently expanded record
		 * @param {Object} object2 - predecessor record
		 */
		valuesFormattingAndCompare: function(object1, object2) {
			object1 = object1 || {};
			object2 = object2 || {};

			if (!Ext.isEmpty(object1) && Ext.isObject(object1)) {
				Ext.Object.each(object1, function(key, value, myself) {
					var changed = false;

					// Get attribute's description
					var attributeDescription = this.entryTypeAttributes.hasOwnProperty(key) ? this.entryTypeAttributes[key][CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION] : null;
					var attributeIndex = this.entryTypeAttributes.hasOwnProperty(key) ? this.entryTypeAttributes[key][CMDBuild.core.proxy.CMProxyConstants.INDEX] : 0;

					// Build object1 properties models
					var attributeValues = Ext.isObject(value) ? value : { description: value };
					attributeValues[CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTE_DESCRIPTION] = attributeDescription;
					attributeValues[CMDBuild.core.proxy.CMProxyConstants.INDEX] = attributeIndex;

					object1[key] = Ext.create('CMDBuild.model.common.tabs.history.Attribute', attributeValues);

					// Build object2 properties models
					if (!Ext.Object.isEmpty(object2)) {
						if (!object2.hasOwnProperty(key))
							object2[key] = null;

						attributeValues = Ext.isObject(object2[key]) ? object2[key] : { description: object2[key] };
						attributeValues[CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTE_DESCRIPTION] = attributeDescription;
						attributeValues[CMDBuild.core.proxy.CMProxyConstants.INDEX] = attributeIndex;

						object2[key] = Ext.create('CMDBuild.model.common.tabs.history.Attribute', attributeValues);
					}

					changed = Ext.Object.isEmpty(object2) ? false : !Ext.Object.equals(object1[key].getData(), object2[key].getData());

					object1[key].set(CMDBuild.core.proxy.CMProxyConstants.CHANGED, changed);
				}, this);
			}
		}
	});

})();