(function() {

	Ext.define('CMDBuild.view.administration.accordion.Domain', {
		extend: 'CMDBuild.view.common.AbstractAccordion',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.domain.Domain'
		],

		/**
		 * @cfg {CMDBuild.controller.common.AbstractAccordionController}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		title: CMDBuild.Translation.domains,

		expandForAdd: function() {
			this.expand();

			_CMMainViewportController.bringTofrontPanelByCmName(this.cmName);
			_CMMainViewportController.panelControllers[this.cmName].cmfg('onDomainAddButtonClick');
		},

		/**
		 * @param {Number} nodeIdToSelect
		 *
		 * @override
		 */
		updateStore: function(nodeIdToSelect) {
			nodeIdToSelect = Ext.isNumber(nodeIdToSelect) ? nodeIdToSelect : null;

			CMDBuild.core.proxy.domain.Domain.readAll({
				loadMask: false,
				scope: this,
				success: function(result, options, decodedResult) {
					decodedResult = decodedResult[CMDBuild.core.constants.Proxy.DOMAINS];

					if (!Ext.isEmpty(decodedResult)) {
						var nodes = [];

						Ext.Array.forEach(decodedResult, function(domainObject, i, allDomainObjects) {
							nodes.push({
								text: domainObject[CMDBuild.core.constants.Proxy.DESCRIPTION],
								description: domainObject[CMDBuild.core.constants.Proxy.DESCRIPTION],
								iconCls: 'domain',
								id: domainObject['idDomain'],
								cmName: this.cmName,
								leaf: true,
							});
						}, this);

						this.getStore().getRootNode().removeAll();
						this.getStore().getRootNode().appendChild(nodes);
						this.getStore().sort();

						// Alias of this.callParent(arguments), inside proxy function doesn't work
						if (!Ext.isEmpty(this.delegate))
							this.delegate.cmfg('onAccordionUpdateStore', nodeIdToSelect);
					}
				}
			});
		}
	});

})();