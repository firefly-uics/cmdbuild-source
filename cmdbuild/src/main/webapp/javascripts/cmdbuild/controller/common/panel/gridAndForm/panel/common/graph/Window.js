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
			'onPanelGridAndFormGraphWindowShow'
		],

		/**
		 * @property {CMDBuild.view.common.panel.gridAndForm.panel.common.graph.WindowView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Object} configurationObject.cardId
		 * @param {Object} configurationObject.classId
		 * @param {Object} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			// Error handling
				if (!Ext.isNumber(this.cardId) || Ext.isEmpty(this.cardId))
					return _error('constructor(): unmanaged cardId parameter', this, this.cardId);

				if (!Ext.isNumber(this.classId) || Ext.isEmpty(this.classId))
					return _error('constructor(): unmanaged classId parameter', this, this.classId);
			// END: Error handling

			this.basePath = window.location.toString().split('/');
			this.basePath = Ext.Array.slice(this.basePath, 0, this.basePath.length - 1).join('/');

			var params = {};
			params[CMDBuild.core.constants.Proxy.ACTIVE] = true;

			CMDBuild.proxy.common.panel.gridAndForm.panel.common.Graph.readAllEntryTypes({
				params: params,
				scope: this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES];

					if (!Ext.isEmpty(decodedResponse) && Ext.isArray(decodedResponse)) {
						var targetClassObject = Ext.Array.findBy(decodedResponse, function (item, i) {
							return item[CMDBuild.core.constants.Proxy.ID] == this.classId;
						}, this);

						if (Ext.isObject(targetClassObject) && !Ext.Object.isEmpty(targetClassObject)) {
							this.className = targetClassObject[CMDBuild.core.constants.Proxy.NAME];

							this.view = Ext.create('CMDBuild.view.common.panel.gridAndForm.panel.common.graph.WindowView', { delegate: this });
							this.view.show();
						} else {
							_error('constructor(): class not found', this, this.classId);
						}
					}
				}
			});
		},

		/**
		 * @returns {Void}
		 */
		onPanelGridAndFormGraphWindowShow: function () {
			this.view.removeAll();
			this.view.add({
				xtype: 'component',

				autoEl: {
					tag: 'iframe',
					src: this.basePath
						+ '/javascripts/cmdbuild-network/?basePath=' + this.basePath
						+ '&classId=' + this.className
						+ '&cardId=' + this.cardId
						+ '&frameworkVersion=' + CMDBuild.core.configurations.CustomPage.getVersion()
						+ '&language=' + CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.LANGUAGE)
				}
			});
		}
	});

})();
