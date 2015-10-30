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
			'onCustomFormImportAbortButtonClick',
			'onCustomFormImportUploadButtonClick'
		],

		/**
		 * @property {CMDBuild.view.management.common.widgets.customForm.import.FormPanel}
		 */
		form: undefined,

		/**
		 * @cfg {Boolean}
		 */
		modeDisabled: false,

		/**
		 * @property {CMDBuild.view.management.common.widgets.customForm.ImportWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Mixed} configurationObject.parentDelegate
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.common.widgets.customForm.import.ImportWindow', {
				delegate: this,
				modeDisabled: this.modeDisabled
			});

			// Shorthands
			this.form = this.view.form;

			// Show window
			if (!Ext.isEmpty(this.view))
				this.view.show();
		},

		onCustomFormImportAbortButtonClick: function() {
			this.view.destroy();
		},

		/**
		 * Uses importCSV calls to store and get CSV data from server and check if CSV has right fields
		 */
		onCustomFormImportUploadButtonClick: function() {
			if (this.validate(this.form)) {
				CMDBuild.LoadMask.get().show();
				CMDBuild.core.proxy.Csv.decode({
					form: this.form.getForm(),
					scope: this,
					failure: function(form, action) {
						CMDBuild.LoadMask.get().hide();

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
		},

		/**
		 * Complete CSV translation data and forward call to parent delegate:
		 * 	- Lookup: from description to id
		 * 	- Reference: from code to id
		 *
		 * @param {Array} data
		 */
		dataManageAndForward: function(data) {
			if (!this.cmfg('widgetConfigurationIsAttributeEmpty',  CMDBuild.core.proxy.CMProxyConstants.MODEL)) {
				var barrierId = 'dataManageBarrier';

				CMDBuild.core.RequestBarrier.init(barrierId, function() {
					// Forwards to parent delegate
					this.cmfg('importData', {
						append: this.form.importModeCombo.getValue() == 'add',
						rowsObjects: data
					});

					this.onCustomFormImportAbortButtonClick();

					CMDBuild.LoadMask.get().hide();
				}, this);

				Ext.Array.forEach(this.cmfg('widgetConfigurationGet', CMDBuild.core.proxy.CMProxyConstants.MODEL), function(attribute, i, allAttributes) {
					switch (attribute.get(CMDBuild.core.proxy.CMProxyConstants.TYPE)) {
						case 'LOOKUP': {
							this.dataManageLookup(data, attribute, barrierId);
						} break;

						case 'REFERENCE': {
							this.dataManageReference(data, attribute, barrierId);
						} break;
					}
				}, this);
			}
		},

		/**
		 * @param {Array} data
		 * @param {CMDBuild.model.widget.customForm.Attribute} attribute
		 * @param {String} barrierId
		 */
		dataManageLookup: function(data, attribute, barrierId) {
			if (
				!Ext.isEmpty(data) && Ext.isArray(data)
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

						Ext.Array.forEach(data, function(recordObject, i, allRecordObjects) {
							if (!Ext.isEmpty(recordObject[attributeName])) {
								var selectedLookup = Ext.Array.findBy(decodedResponse, function(lookupObject, i) {
									return lookupObject['Description'] == recordObject[attributeName];
								}, this);

								if (!Ext.isEmpty(selectedLookup))
									data[i][attributeName] = selectedLookup['Id'];
							}
						}, this);
					},
					callback: CMDBuild.core.RequestBarrier.getCallback(barrierId)
				});
			} else {
				_error('Malformed parameters in Lookup data manage', this);
			}
		},

		/**
		 * @param {Array} data
		 * @param {CMDBuild.model.widget.customForm.Attribute} attribute
		 * @param {String} barrierId
		 */
		dataManageReference: function(data, attribute, barrierId) {
			if (
				!Ext.isEmpty(data) && Ext.isArray(data)
				&& !Ext.isEmpty(attribute)
				&& !Ext.isEmpty(barrierId) && Ext.isString(barrierId)
			) {
				var attributeName = attribute.get(CMDBuild.core.proxy.CMProxyConstants.NAME);
				var requiredCardAdvancedFilterArray = [];

				Ext.Array.forEach(data, function(recordObject, i, allRecordObjects) {
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

						Ext.Array.forEach(data, function(recordObject, i, allRecordObjects) {
							if (!Ext.isEmpty(recordObject[attributeName])) {
								var selectedCard = referencedCardsMap[recordObject[attributeName]];

								if (!Ext.isEmpty(selectedCard))
									data[i][attributeName] = selectedCard['Id'];
							}
						}, this);
					},
					callback: CMDBuild.core.RequestBarrier.getCallback(barrierId)
				});
			} else {
				_error('Malformed parameters in Reference data manage', this);
			}
		}
	});

})();