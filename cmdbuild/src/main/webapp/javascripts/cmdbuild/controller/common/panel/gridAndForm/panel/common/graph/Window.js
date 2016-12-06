(function () {

	Ext.define('CMDBuild.controller.common.panel.gridAndForm.panel.common.graph.Window', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.configurations.CustomPage',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.common.panel.gridAndForm.panel.common.Graph'
		],

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @property {String}
		 *
		 * @private
		 */
		basePath: undefined,

		/**
		 * @cfg {Number}
		 */
		cardId: undefined,

		/**
		 * @property {String}
		 *
		 * @private
		 */
		className: undefined,

		/**
		 * @cfg {String}
		 */
		classId: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onPanelGridAndFormGraphWindowConfigureAndShow'
		],

		/**
		 * @property {CMDBuild.view.common.panel.gridAndForm.panel.common.graph.WindowView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Object} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.common.panel.gridAndForm.panel.common.graph.WindowView', { delegate: this });
		},

		/**
		 * @param {Object} parameters
		 * @param {Number} parameters.cardId
		 * @param {Number} parameters.classId
		 *
		 * @returns {Void}
		 */
		onPanelGridAndFormGraphWindowConfigureAndShow: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};

			// Error handling
				if (!Ext.isNumber(parameters.cardId) || Ext.isEmpty(parameters.cardId))
					return _error('constructor(): unmanaged cardId parameter', this, parameters.cardId);

				if (!Ext.isNumber(parameters.classId) || Ext.isEmpty(parameters.classId))
					return _error('constructor(): unmanaged classId parameter', this, parameters.classId);
			// END: Error handling

			var basePath = window.location.toString().split('/');
			basePath = Ext.Array.slice(basePath, 0, basePath.length - 1).join('/');

			var params = {};
			params[CMDBuild.core.constants.Proxy.ACTIVE] = true;

			CMDBuild.proxy.common.panel.gridAndForm.panel.common.Graph.readAllEntryTypes({
				params: params,
				scope: this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES];

					if (!Ext.isEmpty(decodedResponse) && Ext.isArray(decodedResponse)) {
						var targetClassObject = Ext.Array.findBy(decodedResponse, function (item, i) {
							return item[CMDBuild.core.constants.Proxy.ID] == parameters.classId;
						}, this);

						if (Ext.isObject(targetClassObject) && !Ext.Object.isEmpty(targetClassObject)) {
							this.view.removeAll();
							this.view.add({
								xtype: 'component',

								autoEl: {
									tag: 'iframe',
									src: basePath
										+ '/javascripts/cmdbuild-network/?basePath=' + basePath
										+ '&classId=' + targetClassObject[CMDBuild.core.constants.Proxy.NAME] // EntryType name
										+ '&cardId=' + parameters.cardId
										+ '&frameworkVersion=' + CMDBuild.core.configurations.CustomPage.getVersion()
										+ '&language=' + CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.LANGUAGE)
								}
							});
							this.view.show();
						} else {
							_error('constructor(): entryType not found', this, parameters.classId);
						}
					}
				}
			});
		}
	});

})();
