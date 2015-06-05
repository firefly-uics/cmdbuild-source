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

			this.grid.getStore().removeAll();
			this.grid.getStore().add(Ext.Array.merge(oldStoreDatas, itemsToAdd));
		},

		/**
		 * @return {Array} columns
		 */
		getExtraColumns: function() {
			var columns = [];

			if (!_CMUIConfiguration.isSimpleHistoryModeForCard()) {
				columns = [
					{
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.IS_CARD,
						text: CMDBuild.Translation.attributes,
						width: 65,
						align: 'center',
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,

						renderer: function(value, metaData, record) {
							return value ? '<img src="images/icons/tick.png" alt="' + CMDBuild.Translation.attributes + '" />' : null;
						}
					},
					{
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.IS_RELATION,
						text: CMDBuild.Translation.relation,
						width: 65,
						align: 'center',
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,

						renderer: function(value, metaData, record) {
							return value ? '<img src="images/icons/tick.png" alt="' + CMDBuild.Translation.relation + '" />' : null;
						}
					},
					{
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.DOMAIN,
						text: CMDBuild.Translation.domain,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						flex: 1
					},
					{
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.DESTINATION_DESCRIPTION,
						text: CMDBuild.Translation.descriptionLabel,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						flex: 1
					}
				];
			}

			return columns;
		},

		/**
		 * @return {Array}
		 */
		getTabHistoryGridColumns: function() {
			var defaultColumns = [
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

			return Ext.Array.push(defaultColumns, this.getExtraColumns());
		},

		/**
		 * @return {Ext.data.Store}
		 */
		getTabHistoryGridStore: function() {
			return this.getProxy().getStore();
		},

		/**
		 * @return {Object}
		 *
		 * @abstract
		 */
		getProxy: Ext.emptyFn,

		onAddCardButtonClick: function() {
			this.view.disable();
		},

		onCloneCard: function() {
			this.view.disable();
		},

		/**
		 * Reloads store to be consistent with includeRelationsCheckbox state
		 */
		onTabHistoryIncludeRelationCheck: function() {
			this.onTabHistoryPanelShow();
		},

		/**
		 * @param {CMDBuild.model.common.tabs.history.classes.CardRecord or CMDBuild.model.common.tabs.history.classes.RelationRecord} record
		 */
		onTabHistoryRowExpand: function(record) {
			if (
				!Ext.isEmpty(record)
				&& Ext.Object.isEmpty(record.get(CMDBuild.core.proxy.CMProxyConstants.VALUES)) // Optimization to avoid to ask already owned data
			) {
				var params = {};

				if (record.get(CMDBuild.core.proxy.CMProxyConstants.IS_CARD)) { // Is card record
					params[CMDBuild.core.proxy.CMProxyConstants.CARD_ID] = record.get(CMDBuild.core.proxy.CMProxyConstants.ID); // Historic card ID
					params[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = record.get(CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME);

					this.getProxy().getHistoric({
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

								this.getProxy().getHistoric({
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
					}
				});

				params = {};
				params[CMDBuild.core.proxy.CMProxyConstants.CARD_ID] = this.selectedEntity.get(CMDBuild.core.proxy.CMProxyConstants.ID);
				params[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.selectedEntity.get('IdClass'));

				this.grid.getStore().load({
					params: params,
					scope: this,
					callback: function(records, operation, success) {
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
		},

		// SelectedEntity property functions
			/**
			 * @return {Mixed}
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
			if (!Ext.isEmpty(object1) && Ext.isObject(object1)) {
				Ext.Object.each(object1, function(key, value, myself) {
					// Get attribute's description
					var attributeDescription = this.entryTypeAttributes.hasOwnProperty(key) ? this.entryTypeAttributes[key][CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION] : null;

					if (Ext.isObject(value)) {
						object1[key][CMDBuild.core.proxy.CMProxyConstants.CHANGED] = false;

						if (!Ext.isEmpty(object2) && object2.hasOwnProperty(key))
							object1[key][CMDBuild.core.proxy.CMProxyConstants.CHANGED] = (
								value[CMDBuild.core.proxy.CMProxyConstants.ID] != object2[key][CMDBuild.core.proxy.CMProxyConstants.ID]
								|| value[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION] != object2[key][CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION]
							);

						// Strip HTML tags
						if (!Ext.isEmpty(value[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION]))
							object1[key][CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION] = Ext.util.Format.stripTags(
								value[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION]
							);

						object1[key][CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTE_DESCRIPTION] = attributeDescription;
					} else {
						object1[key] = {};
						object1[key][CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTE_DESCRIPTION] = attributeDescription;
						object1[key][CMDBuild.core.proxy.CMProxyConstants.CHANGED] = (!Ext.isEmpty(object2) && !Ext.isEmpty(object2[key])) ? (value != object2[key]) : false;
						object1[key][CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION] = Ext.util.Format.stripTags(value); // Strip HTML tags
					}
				}, this);
			}
		}
	});

})();