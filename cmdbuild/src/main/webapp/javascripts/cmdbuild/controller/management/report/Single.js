(function() {

	Ext.define('CMDBuild.controller.management.report.Single', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.Message',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.index.Json',
			'CMDBuild.proxy.report.Report'
		],

		/**
		 * @cfg {CMDBuild.controller.common.MainViewport}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onSingleReportDownloadButtonClick',
			'onSingleReportModuleInit = onModuleInit',
			'onSingleReportTypeButtonClick',
			'reportSingleSelectedReportParametersSet = selectedReportParametersSet',
			'reportSingleUpdateReport = updateReport',
			'singleReportSelectedReportRecordGet = selectedReportRecordGet'
		],

		/**
		 * @property {CMDBuild.controller.management.report.Parameters}
		 */
		controllerParametersWindow: undefined,

		/**
		 * All server calls parameters
		 *
		 * @property {Object}
		 *
		 * Ex. {
		 * 		{Object} create, create call parameters
		 * 		{Object} update update call parameters
		 * }
		 *
		 * @private
		 */
		currentReportParameters: {},

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

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
		 * @cfg {CMDBuild.view.management.report.SinglePanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.report.SinglePanel', { delegate: this });

			// Build sub-controllers
			this.controllerParametersWindow = Ext.create('CMDBuild.controller.management.report.Parameters', { parentDelegate: this });
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
				CMDBuild.proxy.report.Report.create({
					params: this.currentReportParametersGet({ callIdentifier: 'create' }),
					scope: this,
					success: function(response, options, decodedResponse) {
						if(decodedResponse.filled) { // Report with no parameters
							this.showReport(forceDownload);
						} else { // Show parameters window
							// FIX: in IE PDF is painted on top of the regular page content so remove it before display parameter window
							// Workaround to detect IE 11 witch is not supported from Ext 4.2
							if (Ext.isIE || !!navigator.userAgent.match(/Trident.*rv[ :]*11\./))
								this.view.removeAll();

							if (this.currentReportParametersIsEmpty('update')) {
								this.controllerParametersWindow.cmfg('reportParametersWindowReconfigureAndShow', {
									attributes: decodedResponse.attribute,
									forceDownload: forceDownload
								});
							} else {
								this.cmfg('reportSingleUpdateReport', forceDownload);
							}
						}
					}
				});
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
			reportSingleSelectedReportParametersSet: function(parameters) {
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
		 * Show report with force download
		 */
		onSingleReportDownloadButtonClick: function() {
			this.showReport(true);
		},

		/**
		 * Setup view items and controllers on accordion click
		 *
		 * @param {Object} parameters
		 * @param {CMDBuild.model.common.Accordion} parameters.node
		 *
		 * @override
		 */
		onSingleReportModuleInit: function(parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};

			this.cmfg('reportSingleSelectedReportParametersSet'); // Reset class property

			if (
				Ext.isObject(parameters.node) && !Ext.Object.isEmpty(parameters.node)
				&& !Ext.isEmpty(parameters.node.get(CMDBuild.core.constants.Proxy.ID))
				&& parameters.node.get(CMDBuild.core.constants.Proxy.ID) != CMDBuild.core.constants.Proxy.CUSTOM
			) {
				this.setViewTitle(parameters.node.get(CMDBuild.core.constants.Proxy.TEXT));

				this.cmfg('reportSingleSelectedReportParametersSet', {
					callIdentifier: 'create',
					params: {
						extension: parameters.node.get(CMDBuild.core.constants.Proxy.SECTION_HIERARCHY)[0],
						id: parameters.node.get(CMDBuild.core.constants.Proxy.ENTITY_ID)
					}
				});

				this.createReport();

				this.onModuleInit(parameters); // Custom callParent() implementation
			}
		},

		/**
		 * @param {String} type
		 */
		onSingleReportTypeButtonClick: function(type) {
			if (Ext.Array.contains(this.managedReportTypes, type)) {
				this.cmfg('reportSingleSelectedReportParametersSet', {
					callIdentifier: 'create',
					params: {
						extension: type,
						id: this.currentReportParametersGet({
							callIdentifier: 'create',
							property: CMDBuild.core.constants.Proxy.ID
						})
					}
				});

				this.createReport();
			} else {
				CMDBuild.core.Message.error(
					CMDBuild.Translation.error,
					CMDBuild.Translation.errors.unmanagedReportType,
					false
				);
			}
		},

		/**
		 * Get created report from server and display it in iframe
		 *
		 * @param {Boolean} forceDownload
		 */
		showReport: function(forceDownload) {
			forceDownload = forceDownload || false;

			var params = {};
			params[CMDBuild.core.constants.Proxy.FORCE_DOWNLOAD_PARAM_KEY] = true;

			if (forceDownload) { // Force download mode
				CMDBuild.proxy.report.Report.download({
					buildRuntimeForm: true,
					params: params
				});
			} else { // Add to view display mode
				this.view.removeAll();

				this.view.add({
					xtype: 'component',

					autoEl: {
						tag: 'iframe',
						src: CMDBuild.proxy.index.Json.report.factory.print + '?donotdelete=true' // Add parameter to avoid report delete
					}
				});
			}
		},

		/**
		 * @param {Boolean} forceDownload
		 */
		reportSingleUpdateReport: function(forceDownload) {
			if (!this.currentReportParametersIsEmpty('update')) {
				CMDBuild.proxy.report.Report.update({
					params: this.currentReportParametersGet({ callIdentifier: 'update' }),
					scope: this,
					success: function(response, options, decodedResponse) {
						this.showReport(forceDownload);
					}
				});
			}
		},

		/**
		 * TODO: implementation to get/set node properties (mainly report name/description for Properties window title)
		 */
		singleReportSelectedReportRecordGet: Ext.emptyFn
	});

})();