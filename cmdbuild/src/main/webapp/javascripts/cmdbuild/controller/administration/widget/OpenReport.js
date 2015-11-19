(function() {

	Ext.define('CMDBuild.controller.administration.widget.OpenReport', {
		extend: 'CMDBuild.controller.administration.widget.AbstractWidgetDefinitionFormController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.widgets.OpenReport',
			'CMDBuild.model.widget.openReport.ReportCombo'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.widget.CMWidgetDefinitionController}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {}
		 */
		classId: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onWidgetOpenReportReportSelect',
			'widgetOpenReportDefinitionGet = widgetDefinitionGet',
			'widgetOpenReportLoadRecord = widgetLoadRecord'
		],

		/**
		 * @cfg {CMDBuild.view.administration.widget.form.OpenReport}
		 */
		view: undefined,

		/**
		 * @cfg {String}
		 */
		widgetName: '.OpenReport',

		/**
		 * @param {Object} configurationObject
		 * @param {Number} configurationObject.classId
		 * @param {CMDBuild.view.administration.widget.form.OpenReport} configurationObject.view
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			// To enable/disable the combo-box with the related check
			this.view.forceFormatCheck.setValue = Ext.Function.createSequence( // TODO: implementation of custom field
				this.view.forceFormatCheck.setValue,
				function(value) {
					if (!this.forceFormatCheck.disabled) {
						this.forceFormatOptions.setDisabled(!value);

						if (value && typeof this.forceFormatOptions.getValue() != 'string') {
							this.forceFormatOptions.setValue(
								this.forceFormatOptions.store.first().get(this.forceFormatOptions.valueField)
							);
						} else {
							this.forceFormatOptions.setValue();
						}
					}
				},
				this.view
			);
		},

		/**
		 * @param {Array} attributes
		 *
		 * @returns {Object} out
		 *
		 * @private
		 */
		cleanServerAttributes: function(attributes) {
			var out = {};

			for (var i = 0; i < attributes.length; ++i) {
				var attr = attributes[i];

				out[attr.name] = '';
			}

			return out;
		},

		/**
		 * @param {String} attribute
		 *
		 * @return {Mixed}
		 *
		 * @private
		 */
		presetGridGet: function(attribute) {
			switch (attribute) {
				case CMDBuild.core.constants.Proxy.DATA:
					return this.presetGridGetData();

				case CMDBuild.core.constants.Proxy.READ_ONLY:
					return this.presetGridGetReadOnly();

				default:
					return {};
			}
		},

		/**
		 * @returns {Object} data
		 *
		 * @private
		 */
		presetGridGetData: function() {
			var data = {};

			Ext.Array.forEach(this.view.presetGrid.getStore().getRange(), function(record, i, allRecords) {
				if (
					!Ext.isEmpty(record.get(CMDBuild.core.constants.Proxy.NAME))
					&& !Ext.isEmpty(record.get(CMDBuild.core.constants.Proxy.VALUE))
				) {
					data[record.get(CMDBuild.core.constants.Proxy.NAME)] = record.get(CMDBuild.core.constants.Proxy.VALUE);
				}
			}, this);

			return data;
		},

		/**
		 * @returns {Array} readOnly
		 *
		 * @private
		 */
		presetGridGetReadOnly: function() {
			var readOnly = [];

			Ext.Array.forEach(this.view.presetGrid.getStore().getRange(), function(record, i, allRecords) {
				if (
					!Ext.isEmpty(record.get(CMDBuild.core.constants.Proxy.NAME))
					&& !Ext.isEmpty(record.get(CMDBuild.core.constants.Proxy.VALUE))
					&& record.get(CMDBuild.core.constants.Proxy.READ_ONLY)
				) {
					readOnly.push(record.get(CMDBuild.core.constants.Proxy.NAME));
				}
			}, this);

			return readOnly;
		},

		/**
		 * @param {Object} data - Ex. { name: value }
		 * @param {Array} readOnlyAttributes
		 *
		 * @private
		 */
		presetGridLoadData: function(data, readOnlyAttributes) {
			readOnlyAttributes = Ext.isEmpty(readOnlyAttributes) ? [] : readOnlyAttributes;

			if (!Ext.Object.isEmpty(data)) {
				var formattedDataObject = [];

				Ext.Object.each(data, function(key, value, myself) {
					// Remove already existing rows
					var storeReportIndex = this.view.presetGrid.getStore().find(CMDBuild.core.constants.Proxy.NAME, key);

					if (storeReportIndex >= 0)
						this.view.presetGrid.getStore().removeAt(storeReportIndex);

					// Add data objects to store
					var modelObject = {};
					modelObject[CMDBuild.core.constants.Proxy.NAME] = key;
					modelObject[CMDBuild.core.constants.Proxy.READ_ONLY] = Ext.Array.contains(readOnlyAttributes, key);
					modelObject[CMDBuild.core.constants.Proxy.VALUE] = value;

					formattedDataObject.push(Ext.create('CMDBuild.model.widget.openReport.PresetGrid', modelObject));
				}, this);

				this.view.presetGrid.getStore().loadRecords(formattedDataObject, { addRecords: true });
			}
		},

		/**
		 * Calls presetGridLoadData to fill presetGrid store with empty fields and then recall for fill with real data
		 *
		 * @param {Object} parameters
		 * @param {Object} parameters.presets
		 * @param {Object} parameters.readOnlyAttributes
		 * @param {Object} parameters.selectedReport
		 */
		onWidgetOpenReportReportSelect: function(parameters) {
			if (
				!Ext.Object.isEmpty(parameters)
				&& !Ext.isEmpty(parameters.selectedReport)
				&& Ext.isFunction(parameters.selectedReport.get)
			) {
				parameters.presets = Ext.isEmpty(parameters.presets) ? {} : parameters.presets;
				parameters.readOnlyAttributes = Ext.isEmpty(parameters.readOnlyAttributes) ? [] : parameters.readOnlyAttributes;

				var params = {};
				params[CMDBuild.core.constants.Proxy.EXTENSION] = CMDBuild.core.constants.Proxy.PDF;
				params[CMDBuild.core.constants.Proxy.ID] = parameters.selectedReport.get(CMDBuild.core.constants.Proxy.ID);
				params[CMDBuild.core.constants.Proxy.TYPE] = 'CUSTOM';

				CMDBuild.core.proxy.widgets.OpenReport.create({
					scope: this,
					params: params,
					success: function(response, options, decodedResponse) {
						var data = decodedResponse['filled'] ? []: this.cleanServerAttributes(decodedResponse[CMDBuild.core.constants.Proxy.ATTRIBUTE]); // TODO

						// Reset presetGrid store
						this.view.presetGrid.getStore().removeAll();

						this.presetGridLoadData(data);

						if (!Ext.Object.isEmpty(parameters.presets))
							this.presetGridLoadData(parameters.presets, parameters.readOnlyAttributes);
					}
				});
			}
		},

		/**
		 * @return {Object} returnObject
		 */
		widgetOpenReportDefinitionGet: function() {
			var returnObject = this.widgetDefinitionGet();
			returnObject[CMDBuild.core.constants.Proxy.REPORT_CODE] = this.view.reportCode.getValue();
			returnObject[CMDBuild.core.constants.Proxy.FORCE_FORMAT] = this.view.forceFormatOptions.getValue();
			returnObject[CMDBuild.core.constants.Proxy.PRESET] = this.presetGridGet(CMDBuild.core.constants.Proxy.DATA);
			returnObject[CMDBuild.core.constants.Proxy.READ_ONLY_ATTRIBUTES] = this.presetGridGet(CMDBuild.core.constants.Proxy.READ_ONLY);

			return returnObject;
		},


		/**
		 * Fills form with widget data
		 *
		 * @param {CMDBuild.model.widget.WidgetDefinition} record
		 */
		widgetOpenReportLoadRecord: function(record) {
			this.view.loadRecord(record);
			this.view.reportCode.setValue(record.get(CMDBuild.core.constants.Proxy.REPORT_CODE));

			if (!Ext.isEmpty(record.get(CMDBuild.core.constants.Proxy.FORCE_FORMAT))) {
				this.view.forceFormatCheck.setValue(true);
				this.view.forceFormatOptions.setValue(record.get(CMDBuild.core.constants.Proxy.FORCE_FORMAT));
			}

			// Find selected report ID and manually calls onReportSelected to fill presetGrid
			this.view.reportCode.getStore().on('load', function(store, records, successful, eOpts) {
				var storeReportIndex = store.find(
					CMDBuild.core.constants.Proxy.TITLE,
					record.get(CMDBuild.core.constants.Proxy.REPORT_CODE)
				);

				if(storeReportIndex >= 0)
					this.cmfg('onWidgetOpenReportReportSelect', {
						presets: record.get(CMDBuild.core.constants.Proxy.PRESET),
						readOnlyAttributes: record.get(CMDBuild.core.constants.Proxy.READ_ONLY_ATTRIBUTES),
						selectedReport: store.getAt(storeReportIndex)
					});
			}, this);

			this.presetGridLoadData(record.get(CMDBuild.core.constants.Proxy.PRESET), record.get(CMDBuild.core.constants.Proxy.READ_ONLY_ATTRIBUTES));
		}
	});

})();