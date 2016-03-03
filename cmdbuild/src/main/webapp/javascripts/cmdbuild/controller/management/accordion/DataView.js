(function () {

	Ext.define('CMDBuild.controller.management.accordion.DataView', {
		extend: 'CMDBuild.controller.common.abstract.Accordion',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Classes',
			'CMDBuild.core.proxy.dataView.DataView',
		],

		/**
		 * @cfg {CMDBuild.controller.common.MainViewport}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * @property {CMDBuild.view.management.accordion.DataView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.accordion.DataView', { delegate: this });

			this.cmfg('accordionUpdateStore');
		},

		/**
		 * @param {Number} nodeIdToSelect
		 *
		 * @override
		 */
		accordionUpdateStore: function (nodeIdToSelect) {
			nodeIdToSelect = Ext.isNumber(nodeIdToSelect) ? nodeIdToSelect : null;

			CMDBuild.core.proxy.dataView.DataView.readAll({
				scope: this,
				success: function (response, options, decodedResponse) {
					var dataViews = decodedResponse[CMDBuild.core.constants.Proxy.VIEWS];

					if (!Ext.isEmpty(dataViews)) {
						var params = {};
						params[CMDBuild.core.constants.Proxy.ACTIVE] = true;

						CMDBuild.core.proxy.Classes.readAll({
							params: params,
							loadMask: false,
							scope: this,
							success: function (response, options, decodedResponse) {
								decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES];

								var classesSearchableObject = {};

								Ext.Array.forEach(decodedResponse, function (classObject, i, allClassObjects) {
									classesSearchableObject[classObject[CMDBuild.core.constants.Proxy.NAME]] = classObject;
								}, this);

								var nodes = [];

								Ext.Array.forEach(dataViews, function (viewObject, i, allViewObjects) {
									var nodeObject = {};
									nodeObject[CMDBuild.core.constants.Proxy.TEXT] = viewObject[CMDBuild.core.constants.Proxy.DESCRIPTION];
									nodeObject[CMDBuild.core.constants.Proxy.DESCRIPTION] = viewObject[CMDBuild.core.constants.Proxy.DESCRIPTION];
									nodeObject[CMDBuild.core.constants.Proxy.NAME] = viewObject[CMDBuild.core.constants.Proxy.NAME];
									nodeObject[CMDBuild.core.constants.Proxy.LEAF] = true;

									switch (viewObject[CMDBuild.core.constants.Proxy.TYPE]) {
										case 'FILTER': {
											var viewSourceClassObject = classesSearchableObject[viewObject[CMDBuild.core.constants.Proxy.SOURCE_CLASS_NAME]];

											if (!Ext.isEmpty(viewSourceClassObject)) {
												nodeObject['cmName'] = 'class'; // To act as a regular class node
												nodeObject[CMDBuild.core.constants.Proxy.ENTITY_ID] = viewSourceClassObject[CMDBuild.core.constants.Proxy.ID];
												nodeObject[CMDBuild.core.constants.Proxy.ID] = this.cmfg('accordionBuildId', {
													components: viewObject[CMDBuild.core.constants.Proxy.ID]
												});
												nodeObject[CMDBuild.core.constants.Proxy.SECTION_HIERARCHY] = ['filter'];
												nodeObject[CMDBuild.core.constants.Proxy.FILTER] = viewObject[CMDBuild.core.constants.Proxy.FILTER];
											}
										} break;

										case 'SQL':
										default: {
											nodeObject['cmName'] = this.cmfg('accordionIdentifierGet');
											nodeObject[CMDBuild.core.constants.Proxy.ENTITY_ID] = viewObject[CMDBuild.core.constants.Proxy.ID];
											nodeObject[CMDBuild.core.constants.Proxy.ID] = this.cmfg('accordionBuildId', {
												components: viewObject[CMDBuild.core.constants.Proxy.ID]
											});
											nodeObject[CMDBuild.core.constants.Proxy.SECTION_HIERARCHY] = ['sql'];
											nodeObject[CMDBuild.core.constants.Proxy.SOURCE_FUNCTION] = viewObject[CMDBuild.core.constants.Proxy.SOURCE_FUNCTION];
										}
									}

									nodes.push(nodeObject);
								}, this);

								if (!Ext.isEmpty(nodes)) {
									this.view.getStore().getRootNode().removeAll();
									this.view.getStore().getRootNode().appendChild(nodes);
									this.view.getStore().sort();
								}

								// Alias of this.callParent(arguments), inside proxy function doesn't work
								this.updateStoreCommonEndpoint(nodeIdToSelect);
							}
						});
					}
				}
			});

			this.callParent(arguments);
		}
	});

})();
