(function () {

	Ext.define('CMDBuild.controller.management.common.widgets.customForm.Import', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.proxy.Card',
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Csv',
			'CMDBuild.core.RequestBarrier'
		],

		/**
		 * @cfg {Mixed}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onWidgetCustomFormImportAbortButtonClick',
			'onWidgetCustomFormImportModeChange',
			'onWidgetCustomFormImportUploadButtonClick'
		],

		/**
		 * @property {CMDBuild.view.management.common.widgets.customForm.import.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.management.common.widgets.customForm.import.ImportWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Mixed} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.common.widgets.customForm.import.ImportWindow', { delegate: this });

			// Shorthands
			this.form = this.view.form;

			// Show window
			if (!Ext.isEmpty(this.view))
				this.view.show();
		},

		/**
		 * Complete CSV translation data and forward call to parent delegate:
		 * 	- Lookup: from description to id
		 * 	- Reference: from code to id
		 *
		 * @param {Array} csvData
		 *
		 * @private
		 */
		dataManageAndForward: function(csvData) {
			if (
				!Ext.isEmpty(csvData) && Ext.isArray(csvData)
				&& !this.cmfg('widgetCustomFormConfigurationIsAttributeEmpty',  CMDBuild.core.proxy.CMProxyConstants.MODEL)
			) {
				var barrierId = 'dataManageBarrier';

				CMDBuild.core.RequestBarrier.init(barrierId, function() {
					// Forwards to parent delegate
					this.cmfg('widgetCustomFormImportData', this.importDataModeManager(csvData));
					this.cmfg('onWidgetCustomFormImportAbortButtonClick');

//					CMDBuild.LoadMask.get().hide();
					this.view.setLoading(false);
				}, this);

				Ext.Array.forEach(this.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.proxy.CMProxyConstants.MODEL), function(attribute, i, allAttributes) {
					switch (attribute.get(CMDBuild.core.proxy.CMProxyConstants.TYPE)) {
						case 'lookup': {
							this.dataManageLookup(csvData, attribute, barrierId);
						} break;

						case 'reference': {
							this.dataManageReference(csvData, attribute, barrierId);
						} break;
					}
				}, this);
				
				CMDBuild.core.RequestBarrier.finalize(barrierId);
			}
		},

		/**
		 * @param {Array} csvData
		 * @param {CMDBuild.model.widget.customForm.Attribute} attribute
		 * @param {String} barrierId
		 *
		 * @private
		 */
		dataManageLookup: function(csvData, attribute, barrierId) {
			if (
				!Ext.isEmpty(csvData) && Ext.isArray(csvData)
				&& !Ext.isEmpty(attribute)
				&& !Ext.isEmpty(barrierId) && Ext.isString(barrierId)
			) {
				var attributeName = attribute.get(CMDBuild.core.proxy.CMProxyConstants.NAME);

				var params = {};
				params[CMDBuild.core.proxy.CMProxyConstants.TYPE] = attribute.get(CMDBuild.core.proxy.CMProxyConstants.LOOKUP_TYPE);
				params[CMDBuild.core.proxy.CMProxyConstants.ACTIVE] = true;

				CMDBuild.ServiceProxy.lookup.get({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.proxy.CMProxyConstants.ROWS];

						Ext.Array.forEach(csvData, function(recordObject, i, allRecordObjects) {
							if (!Ext.isEmpty(recordObject[attributeName])) {
								var selectedLookup = Ext.Array.findBy(decodedResponse, function(lookupObject, i) {
									return lookupObject['Description'] == recordObject[attributeName];
								}, this);

								if (!Ext.isEmpty(selectedLookup))
									csvData[i][attributeName] = selectedLookup['Id'];
							}
						}, this);
					},
					callback: CMDBuild.core.RequestBarrier.getCallback(barrierId)
				});
			} else {
				_error('malformed parameters in Lookup data manage', this);
			}
		},

		/**
		 * @param {Array} csvData
		 * @param {CMDBuild.model.widget.customForm.Attribute} attribute
		 * @param {String} barrierId
		 *
		 * @private
		 */
		dataManageReference: function(csvData, attribute, barrierId) {
			if (
				!Ext.isEmpty(csvData) && Ext.isArray(csvData)
				&& !Ext.isEmpty(attribute)
				&& !Ext.isEmpty(barrierId) && Ext.isString(barrierId)
			) {
				var attributeName = attribute.get(CMDBuild.core.proxy.CMProxyConstants.NAME);
				var requiredCardAdvancedFilterArray = [];

				Ext.Array.forEach(csvData, function(recordObject, i, allRecordObjects) {
					if (!Ext.isEmpty(recordObject[attributeName]))
						requiredCardAdvancedFilterArray.push({
							simple: {
								attribute: 'Code',
								operator: 'equal',
								value: [recordObject[attributeName]],
								parameterType: 'fixed'
							}
						});
				}, this);

				var params = {};
				params[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = attribute.get(CMDBuild.core.proxy.CMProxyConstants.TARGET_CLASS);

				if (!Ext.isEmpty(requiredCardAdvancedFilterArray))
					params[CMDBuild.core.proxy.CMProxyConstants.FILTER] = Ext.encode({ // Filters request to get only required cards
						attribute: { or: requiredCardAdvancedFilterArray }
					});

				CMDBuild.core.proxy.Card.getList({
					params: params,
					loadMask: false,
					scope: this,
					success: function(response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.proxy.CMProxyConstants.ROWS];

						var referencedCardsMap = {};

						// Build referencedCardsMap
						Ext.Array.forEach(decodedResponse, function(cardObject, i, allCardObjects) {
							referencedCardsMap[cardObject['Code']] = cardObject;
						}, this);

						Ext.Array.forEach(csvData, function(recordObject, i, allRecordObjects) {
							if (!Ext.isEmpty(recordObject[attributeName])) {
								var selectedCard = referencedCardsMap[recordObject[attributeName]];

								if (!Ext.isEmpty(selectedCard))
									csvData[i][attributeName] = selectedCard['Id'];
							}
						}, this);
					},
					callback: CMDBuild.core.RequestBarrier.getCallback(barrierId)
				});
			} else {
				_error('malformed parameters in Reference data manage', this);
			}
		},

		/**
		 * @param {Array} csvData
		 *
		 * @private
		 */
		importDataModeManager: function(csvData) {
			csvData = Ext.Array.clean(csvData);

			if (!Ext.isEmpty(csvData) && Ext.isArray(csvData))
				switch (this.form.modeCombo.getValue()) {
					case 'add':
						return Ext.Array.push(this.cmfg('widgetCustomFormDataGet'), csvData);

					case 'merge':
						return this.importDataModeManagerMerge(csvData);

					case 'replace':
					default:
						return csvData;
				}
		},

		/**
		 * @param {Array} csvData
		 *
		 * @returns {Array}
		 *
		 * @private
		 */
		importDataModeManagerMerge: function(csvData) {
			csvData = Ext.Array.clean(csvData);

			var keyAttributes = Ext.Array.clean(this.form.keyAttributesMultiselect.getValue());

			if (
				!Ext.isEmpty(csvData) && Ext.isArray(csvData)
				&& !Ext.isEmpty(keyAttributes) && Ext.isArray(keyAttributes)
				&& this.isValidKeyCsvAttributes(keyAttributes, csvData)
				&& this.isValidGridStoreKeyAttributes(keyAttributes)
			) {
				var outputData = [];

				Ext.Array.forEach(this.cmfg('widgetCustomFormDataGet'), function(storeRowObject, i, allStoreRowObjects) {
					if (Ext.isObject(storeRowObject) && !Ext.Object.isEmpty(storeRowObject)) {
						var foundCsvRowObject = Ext.Array.findBy(csvData, function(csvRowObject, i, allCsvRowObjects) {
							var isValid = true;

							isValid = Ext.Array.each(keyAttributes, function(name, i, allNames) {
								return String(csvRowObject[name]) == String(storeRowObject[name]);
							}, this);

							return Ext.isBoolean(isValid);
						}, this);

						if (!Ext.Object.isEmpty(foundCsvRowObject)) {
							outputData.push(Ext.Object.merge(storeRowObject, foundCsvRowObject));
						} else {
							outputData.push(storeRowObject);
						}
					}
				}, this);

				return outputData;
			}

			return this.cmfg('widgetCustomFormDataGet');
		},

		/**
		 * Check key attributes value tuples local store uniqueness
		 *
		 * @param {Array} keyAttributes
		 * @param {Array} csvData
		 *
		 * @returns {Boolean}
		 *
		 * @private
		 */
		isValidKeyCsvAttributes: function(keyAttributes, csvData) {
			if (
				!Ext.isEmpty(keyAttributes) && Ext.isArray(keyAttributes)
				&& !Ext.isEmpty(csvData) && Ext.isArray(csvData)
			) {
				var isValid = true;
				var keyAttributeCsvValues = [];

				// Build keyAttributeCsvValues array with append algorithm
				Ext.Array.forEach(csvData, function(csvRowObject, i, allCsvRowObjects) {
					var key = '';

					Ext.Array.forEach(keyAttributes, function(name, i, allNames) {
						key += csvRowObject[name];

						isValid = !Ext.isEmpty(csvRowObject[name]);
					}, this);

					keyAttributeCsvValues.push(key);
				}, this);

				// Check uniqueness of keyAttributes
				if (!Ext.isEmpty(keyAttributeCsvValues) && isValid) {
					isValid = Ext.Array.equals(Ext.Array.unique(keyAttributeCsvValues), keyAttributeCsvValues);
				} else {
					return CMDBuild.core.Message.error(
						CMDBuild.Translation.error,
						'CSV file invalid key attribute/s', // TODO
						false
					);
				}

				return isValid;
			}

			return false;
		},

		/**
		 * Check key attributes value tuples local store uniqueness
		 *
		 * @param {Array} keyAttributes
		 *
		 * @returns {Boolean}
		 *
		 * @private
		 */
		isValidGridStoreKeyAttributes: function(keyAttributes) {
			if (!Ext.isEmpty(keyAttributes) && Ext.isArray(keyAttributes)) {
				var isValid = true;
				var keyAttributeCsvValues = [];

				// Build keyAttributeCsvValues array with append algorithm
				Ext.Array.forEach(this.cmfg('widgetCustomFormDataGet'), function(storeRowObject, i, allStoreRowObjects) {
					var key = '';

					Ext.Array.forEach(keyAttributes, function(name, i, allNames) {
						key += storeRowObject[name];

						isValid = !Ext.isEmpty(storeRowObject[name]);
					}, this);

					keyAttributeCsvValues.push(key);
				}, this);

				// Check uniqueness of keyAttributes
				if (!Ext.isEmpty(keyAttributeCsvValues) && isValid) {
					isValid = Ext.Array.equals(Ext.Array.unique(keyAttributeCsvValues), keyAttributeCsvValues);
				} else {
					return CMDBuild.core.Message.error(
						CMDBuild.Translation.error,
						'local store invalid key attribute/s', // TODO
						false
					);
				}

				return isValid;
			}

			return false;
		},

		onWidgetCustomFormImportAbortButtonClick: function() {
			this.view.destroy();
		},

		onWidgetCustomFormImportModeChange: function() {
			this.form.keyAttributesMultiselect.setDisabled(
				this.form.modeCombo.getValue() != 'merge'
			);
		},

		/**
		 * Uses importCSV calls to store and get CSV data from server and check if CSV has right fields
		 */
		onWidgetCustomFormImportUploadButtonClick: function() {
			if (this.validate(this.form)) {
//				CMDBuild.LoadMask.get().show();
				this.view.setLoading(true);
				CMDBuild.core.proxy.Csv.decode({
					form: this.form.getForm(),
					scope: this,
					failure: function(form, action) {
//						CMDBuild.LoadMask.get().hide();
						this.view.setLoading(false);

						CMDBuild.Msg.error(
							CMDBuild.Translation.common.failure,
							CMDBuild.Translation.errors.csvUploadOrDecodeFailure,
							false
						);
					},
					success: function(form, action) {
						var decodedRows = [];

						Ext.Array.forEach(action.result.response.elements, function(rowDataObject, i, allRowDataObjects) {
							if (!Ext.isEmpty(rowDataObject) && !Ext.isEmpty(rowDataObject.entries))
								decodedRows.push(rowDataObject.entries);
						}, this);

						this.dataManageAndForward(decodedRows);
					}
				});
			}
		}
	});

})();