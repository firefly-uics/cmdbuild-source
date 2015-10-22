(function() {

	Ext.define('CMDBuild.view.management.accordion.DataView', {
		extend: 'CMDBuild.view.common.AbstractAccordion',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Classes',
			'CMDBuild.core.proxy.dataView.DataView',
			'CMDBuild.model.common.accordion.DataView'
		],

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		/**
		 * @cfg {String}
		 */
		storeModelName: 'CMDBuild.model.common.accordion.DataView',

		title: CMDBuild.Translation.views,

		/**
		 * @param {Number} nodeIdToSelect
		 *
		 * @override
		 */
		updateStore: function(nodeIdToSelect) {
			nodeIdToSelect = Ext.isNumber(nodeIdToSelect) ? nodeIdToSelect : null;

			CMDBuild.core.proxy.dataView.DataView.readAll({
				scope: this,
				success: function(response, options, decodedResponse) {
					var dataViews = decodedResponse[CMDBuild.core.constants.Proxy.VIEWS];

					if (!Ext.isEmpty(dataViews)) {
						var params = {};
						params[CMDBuild.core.constants.Proxy.ACTIVE] = true;

						CMDBuild.core.proxy.Classes.readAll({
							params: params,
							loadMask: false,
							scope: this,
							success: function(response, options, decodedResponse) {
								decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES];

								var classesSearchableObject = {};

								Ext.Array.forEach(decodedResponse, function(classObject, i, allClassObjects) {
									classesSearchableObject[classObject[CMDBuild.core.constants.Proxy.NAME]] = classObject;
								}, this);

								var nodes = [];

								Ext.Array.forEach(dataViews, function(viewObject, i, allViewObjects) {
									var node = {
										text: viewObject[CMDBuild.core.constants.Proxy.DESCRIPTION],
										description: viewObject[CMDBuild.core.constants.Proxy.DESCRIPTION],
										name: viewObject[CMDBuild.core.constants.Proxy.NAME],
										leaf: true
									};

									switch (viewObject[CMDBuild.core.constants.Proxy.TYPE]) {
										case 'FILTER': {
											var viewSourceClassObject = classesSearchableObject[viewObject[CMDBuild.core.constants.Proxy.SOURCE_CLASS_NAME]];

											if (!Ext.isEmpty(viewSourceClassObject)) {
												node['cmName'] = 'class'; // To act as a regular class node
												node[CMDBuild.core.constants.Proxy.ENTITY_ID] = viewSourceClassObject[CMDBuild.core.constants.Proxy.ID];
												node[CMDBuild.core.constants.Proxy.FILTER] = viewObject[CMDBuild.core.constants.Proxy.FILTER];
												node[CMDBuild.core.constants.Proxy.ID] = viewObject[CMDBuild.core.constants.Proxy.ID];
												node[CMDBuild.core.constants.Proxy.SECTION_HIERARCHY] = ['filter'];
											}
										} break;

										case 'SQL':
										default: {
											node['cmName'] = this.cmName;
											node[CMDBuild.core.constants.Proxy.ID] = viewObject[CMDBuild.core.constants.Proxy.ID];
											node[CMDBuild.core.constants.Proxy.SECTION_HIERARCHY] = ['sql'];
											node[CMDBuild.core.constants.Proxy.SOURCE_FUNCTION] = viewObject[CMDBuild.core.constants.Proxy.SOURCE_FUNCTION];
										}
									}
									nodes.push(node);
								}, this);

								this.getStore().getRootNode().removeAll();
								this.getStore().getRootNode().appendChild(nodes);

								this.getStore().sort();

								// Alias of this.callParent(arguments), inside proxy function doesn't work
								if (!Ext.isEmpty(this.delegate))
									this.delegate.cmfg('onAccordionUpdateStore', nodeIdToSelect);
							}
						});
					}
				}
			});
		}
	});

})();