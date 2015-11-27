(function() {

	Ext.define('CMDBuild.controller.management.report.Custom', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.core.proxy.report.Report'
		],

		mixins: ['CMDBuild.controller.management.report.Single'], // Import functions to avoid to duplicate

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onReportCustomGenerateButtonClick',
			'onReportCustomShow = onReportShow',
			'reportCustomSelectedReportParametersSet = selectedReportParametersSet',
			'reportCustomSelectedReportRecordGet = selectedReportRecordGet',
			'reportCustomShowReport = showReport',
			'reportCustomUpdateReport = updateReport'
		],

		/**
		 * All server calls parameters
		 *
		 * @property {Object}
		 *  Ex. {
		 * 		{Object} create, create call parameters
		 * 		{Object} update update call parameters
		 *  }
		 *
		 * @private
		 */
		currentReportParameters: {},

		/**
		 * @property {CMDBuild.model.report.Grid}
		 *
		 * @private
		 */
		currentReportRecord: undefined,

		/**
		 * Witch report types will be viewed inside modal pop-up
		 *
		 * @cfg {Array}
		 */
		forceDownloadTypes: [
			CMDBuild.core.constants.Proxy.ODT,
			CMDBuild.core.constants.Proxy.RTF
		],

		/**
		 * @property {CMDBuild.view.management.report.custom.GridPanel}
		 */
		grid: undefined,

		/**
		 * @cfg {Array}
		 */
		managedCurrentReportParametersCallIdentifiers: ['create', 'update'],

		/**
		 * @cfg {Array}
		 */
		managedReportTypes: [
			CMDBuild.core.constants.Proxy.CSV,
			CMDBuild.core.constants.Proxy.ODT,
			CMDBuild.core.constants.Proxy.PDF,
			CMDBuild.core.constants.Proxy.RTF
		],

		/**
		 * @cfg {CMDBuild.view.management.report.custom.CustomView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Mixed} configurationObject.parentDelegate
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.report.custom.CustomView', { delegate: this });

			// Shorthands
			this.grid = this.view.grid;
		},

		/**
		 * @param {Boolean} forceDownload
		 */
		createReport: function(forceDownload) {
			forceDownload = forceDownload || false;

			if (
				!Ext.isEmpty(this.currentReportParametersGet({
					callIdentifier: 'create',
					property: CMDBuild.core.constants.Proxy.ID
				}))
			) {
				CMDBuild.core.proxy.report.Report.create({
					params: this.currentReportParametersGet({ callIdentifier: 'create' }),
					scope: this,
					failure: function(response, options, decodedResponse) {
						CMDBuild.core.Message.error(
							CMDBuild.Translation.error,
							CMDBuild.Translation.errors.createReportFilure,
							false
						);
					},
					success: function(response, options, decodedResponse) {
						if(decodedResponse.filled) { // Report with no parameters
							this.cmfg('reportCustomShowReport', forceDownload);
						} else { // Show parameters window
							Ext.create('CMDBuild.controller.management.report.Parameters', {
								parentDelegate: this,
								attributeList: decodedResponse.attribute,
								forceDownload: forceDownload
							});
						}
					}
				});
			}
		},

		// CurrentReportRecord methods
			/**
			 * @param {Array or String} attributePath
			 *
			 * @return {Mixed or undefined}
			 */
			reportCustomSelectedReportRecordGet: function(attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'currentReportRecord';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {CMDBuild.model.report.Grid} record
			 *
			 * @private
			 */
			reportCustomSelectedReport: function(record) {
				if (!Ext.Object.isEmpty(record)) {
					var parameters = {};
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.report.Grid';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'currentReportRecord';
					parameters[CMDBuild.core.constants.Proxy.VALUE] = record;

					this.propertyManageSet(parameters);
				}
			},

		/**
		 * @param {Object} reportInfo
		 */
		onReportCustomGenerateButtonClick: function(reportInfo) {
			if (Ext.Array.contains(this.managedReportTypes, reportInfo[CMDBuild.core.constants.Proxy.TYPE])) {
				this.cmfg('reportCustomSelectedReportParametersSet', {
					callIdentifier: 'create',
					params: {
						extension: reportInfo[CMDBuild.core.constants.Proxy.TYPE],
						id: reportInfo[CMDBuild.core.constants.Proxy.RECORD].get(CMDBuild.core.constants.Proxy.ID)
					}
				});

				this.reportCustomSelectedReport(reportInfo[CMDBuild.core.constants.Proxy.RECORD]);

				// Force download true for PDF and CSV
				this.createReport(Ext.Array.contains(this.forceDownloadTypes, reportInfo[CMDBuild.core.constants.Proxy.TYPE]));
			} else {
				CMDBuild.core.Message.error(
					CMDBuild.Translation.error,
					CMDBuild.Translation.errors.unmanagedReportType,
					false
				);
			}
		},

		onReportCustomShow: function() {
			if (!this.cmfg('reportSelectedAccordionIsEmpty')) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.TYPE] = this.cmfg('reportSelectedAccordionGet', CMDBuild.core.constants.Proxy.TYPE);

				this.grid.getStore().load({ params: params });
			}
		},

		// CurrentReportParameters methods
			/**
			 * @param {Object} parameters
			 * @param {String} parameters.callIdentifier
			 * @param {String} parameters.property
			 *
			 * @returns {Object}
			 */
			currentReportParametersGet: function(parameters) {
				var callIdentifier = parameters.callIdentifier;
				var property = parameters.property;

				if (
					!Ext.isEmpty(callIdentifier)
					&& Ext.isString(callIdentifier)
					&& Ext.Array.contains(this.managedCurrentReportParametersCallIdentifiers, callIdentifier)
				) {
					if (!Ext.isEmpty(property) && Ext.isString(property) && !Ext.isEmpty(this.currentReportParameters[callIdentifier]))
						return this.currentReportParameters[callIdentifier][property];

					return this.currentReportParameters[callIdentifier];
				}

				return this.currentReportParameters;
			},

			/**
			 * @param {String} callIdentifier
			 *
			 * @returns {Boolean}
			 */
			currentReportParametersIsEmpty: function(callIdentifier) {
				if (
					!Ext.isEmpty(callIdentifier)
					&& Ext.isString(callIdentifier)
					&& Ext.Array.contains(this.managedCurrentReportParametersCallIdentifiers, callIdentifier)
				) {
					return Ext.isEmpty(this.currentReportParametersGet({ callIdentifier: callIdentifier }));
				}

				return Ext.isEmpty(this.currentReportParametersGet());
			},

			/**
			 * @param {Object} parameters
			 * @param {Object} parameters.params
			 * @param {String} parameters.callIdentifier - managed identifiers (create, update)
			 */
			reportCustomSelectedReportParametersSet: function(parameters) {
				if (!Ext.isEmpty(parameters) && Ext.isObject(parameters)) {
					var params = parameters.params || null;
					var callIdentifier = parameters.callIdentifier || null;

					switch(callIdentifier) {
						case 'create': {
							this.currentReportParameters['create'] = Ext.applyIf(params, { // Apply default values
								extension: CMDBuild.core.constants.Proxy.PDF,
								type: 'CUSTOM'
							});
						} break;

						case 'update': {
							this.currentReportParameters['update'] = params;
						} break;

						default: {
							_error('unsupported report parameter call identifier', this);
						}
					}
				} else {
					this.currentReportParameters = {};
				}
			},

		/**
		 * Get created report from server and display it in popup window
		 *
		 * @param {Boolean} forceDownload
		 */
		reportCustomShowReport: function(forceDownload) {
			forceDownload = forceDownload || false;

			if (forceDownload) { // Force download mode
				var params = {};
				params[CMDBuild.core.constants.Proxy.FORCE_DOWNLOAD_PARAM_KEY] = true;

				var form = Ext.create('Ext.form.Panel', {
					standardSubmit: true,
					url: CMDBuild.core.proxy.Index.report.printReportFactory + '?donotdelete=true' // Add parameter to avoid report delete
				});

				form.submit({
					target: '_blank',
					params: params
				});

				Ext.defer(function() { // Form cleanup
					form.close();
				}, 100);
			} else { // Pop-up display mode
				Ext.create('CMDBuild.controller.management.report.Modal', {
					parentDelegate: this,
					extension: this.currentReportParametersGet({
						callIdentifier: 'create',
						property: CMDBuild.core.constants.Proxy.EXTENSION
					})
				});
			}
		},

		/**
		 * @param {Boolean} forceDownload
		 */
		reportCustomUpdateReport: function(forceDownload) {
			if (!this.currentReportParametersIsEmpty('update')) {
				CMDBuild.core.proxy.report.Report.update({
					params: this.currentReportParametersGet({ callIdentifier: 'update' }),
					scope: this,
					success: function(response, options, decodedResponse) {
						this.cmfg('reportCustomShowReport', forceDownload);
					}
				});
			}
		}
	});

})();