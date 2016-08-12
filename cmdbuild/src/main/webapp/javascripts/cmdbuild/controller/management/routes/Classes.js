(function () {

	Ext.define('CMDBuild.controller.management.routes.Classes', {
		extend: 'CMDBuild.controller.common.abstract.Routes',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.proxy.management.routes.Classes'
		],

		/**
		 * @property {CMDBuild.model.management.routes.Classes}
		 *
		 * @private
		 */
		paramsModel: undefined,

		/**
		 * @property {Array}
		 *
		 * @private
		 */
		supportedPrintFormats: [
			CMDBuild.core.constants.Proxy.PDF,
			CMDBuild.core.constants.Proxy.CSV
		],

		/**
		 * @param {Object} params
		 * @param {String} params.classIdentifier
		 * @param {String} params.clientFilter
		 * @param {String} path
		 * @param {Object} router
		 *
		 * @returns {Void}
		 */
		detail: function (params, path, router) {
			if (this.paramsValidation(params)) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVE] = true;

				CMDBuild.proxy.management.routes.Classes.read({ // FIXME: waiting for refactor (server endpoint)
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES];

						if (Ext.isArray(decodedResponse) && !Ext.isEmpty(decodedResponse)) {
							var classObject = Ext.Array.findBy(decodedResponse, function (classObject, i) {
								return this.paramsModel.get(CMDBuild.core.constants.Proxy.CLASS_IDENTIFIER) == classObject[CMDBuild.core.constants.Proxy.NAME];
							}, this);

							if (Ext.isObject(classObject) && !Ext.Object.isEmpty(classObject)) {
								return this.manageIdentifierClass(classObject);
							} else {
								CMDBuild.core.Message.error(
									CMDBuild.Translation.common.failure,
									CMDBuild.Translation.errors.routesInvalidClassIdentifier + ' (' + this.paramsModel.get(CMDBuild.core.constants.Proxy.CLASS_IDENTIFIER) + ')',
									false
								);
							}
						}
					}
				});
			}
		},

		/**
		 * Apply clientFilter to grid
		 *
		 * @param {Object} classObject
		 * @param {Function} callback
		 *
		 * @returns {Void}
		 *
		 * @private
		 *
		 * FIXME: waiting for module refactor
		 */
		manageFilterClient: function(classObject, callback) {
			callback = Ext.isFunction(callback) ? callback : Ext.emptyFn;

			if (
				Ext.isObject(this.paramsModel.get(CMDBuild.core.constants.Proxy.CLIENT_FILTER))
				&& !Ext.Object.isEmpty(this.paramsModel.get(CMDBuild.core.constants.Proxy.CLIENT_FILTER))
			) {
				Ext.Function.createDelayed(function () {
					var classModel = Ext.create('CMDBuild.cache.CMEntryTypeModel', classObject);
					classModel.set(CMDBuild.core.constants.Proxy.FILTER, Ext.encode(this.paramsModel.get(CMDBuild.core.constants.Proxy.CLIENT_FILTER))); // Inject filter in entryType object

					CMDBuild.global.controller.MainViewport.cmfg('mainViewportModuleControllerGet', 'class').onViewOnFront(classModel);

					Ext.Function.createDelayed(function () {
						Ext.callback(callback, this);
					}, 500, this)();
				}, 1500, this)();
			} else {
				Ext.callback(callback, this);
			}
		},

		/**
		 * @param {Object} classObject
		 * @param {Function} callback
		 *
		 * @returns {Void}
		 *
		 * @private
		 *
		 * FIXME: waiting for module refactor
		 */
		manageIdentifierClass: function (classObject, callback) {
			callback = Ext.isFunction(callback) ? callback : Ext.emptyFn;

			// Use runtime configuration to select class
			CMDBuild.configuration.runtime.set(CMDBuild.core.constants.Proxy.STARTING_CLASS_ID, classObject[CMDBuild.core.constants.Proxy.ID]);

			this.manageFilterClient(classObject, callback);
		},

		/**
		 * @param {Object} params
		 *
		 * @return  {Boolean}
		 *
		 * @override
		 * @private
		 */
		paramsValidation: function (params) {
			this.paramsModel = Ext.create('CMDBuild.model.management.routes.Classes', params);

			// Class identifier validation
			if (Ext.isEmpty(this.paramsModel.get(CMDBuild.core.constants.Proxy.CLASS_IDENTIFIER))) {
				CMDBuild.core.Message.error(
					CMDBuild.Translation.common.failure,
					CMDBuild.Translation.errors.routesInvalidClassIdentifier + ' (' + this.paramsModel.get(CMDBuild.core.constants.Proxy.CLASS_IDENTIFIER) + ')',
					false
				);

				return false;
			}

			// Client filter validation
			if (!Ext.isEmpty(this.paramsModel.get(CMDBuild.core.constants.Proxy.CLIENT_FILTER))) {
				// FIXME: validate filter with server side call
			}

			// Print format validation
			if (!Ext.Array.contains(this.supportedPrintFormats, this.paramsModel.get(CMDBuild.core.constants.Proxy.FORMAT))) {
				CMDBuild.core.Message.error(
					CMDBuild.Translation.common.failure,
					CMDBuild.Translation.errors.routesInvalidPrintFormat + ' (' + this.paramsModel.get(CMDBuild.core.constants.Proxy.FORMAT) + ')',
					false
				);

				return false;
			}

			return this.callParent(arguments);
		},

		/**
		 * @param {Object} params
		 * @param {String} params.format
		 * @param {String} path
		 * @param {Object} router
		 *
		 * @returns {Void}
		 *
		 * FIXME: waiting for module refactor
		 */
		print: function (params, path, router) {
			if (this.paramsValidation(params)) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVE] = true;

				CMDBuild.proxy.management.routes.Classes.read({ // FIXME: waiting for refactor (server endpoint)
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES];

						if (Ext.isArray(decodedResponse) && !Ext.isEmpty(decodedResponse)) {
							var classObject = Ext.Array.findBy(decodedResponse, function (classObject, i) {
								return this.paramsModel.get(CMDBuild.core.constants.Proxy.CLASS_IDENTIFIER) == classObject[CMDBuild.core.constants.Proxy.NAME];
							}, this);

							if (Ext.isObject(classObject) && !Ext.Object.isEmpty(classObject)) {
								return this.manageIdentifierClass(classObject, function () {
									Ext.Function.createDelayed(function () {
										CMDBuild.global.controller.MainViewport.cmfg('mainViewportModuleControllerGet', 'class').gridController.onPrintGridMenuClick(
											this.paramsModel.get(CMDBuild.core.constants.Proxy.FORMAT)
										);
									}, 500, this)();
								});
							} else {
								CMDBuild.core.Message.error(
									CMDBuild.Translation.common.failure,
									CMDBuild.Translation.errors.routesInvalidClassIdentifier + ' (' + this.paramsModel.get(CMDBuild.core.constants.Proxy.CLASS_IDENTIFIER) + ')',
									false
								);
							}
						}
					}
				});
			}
		}
	});

})();
