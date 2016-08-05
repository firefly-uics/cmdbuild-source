(function () {

	Ext.define('CMDBuild.controller.management.classes.map.CM16CardGrid', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.view.management.classes.map.CM16CardGrid'
		],

		/**
		 * @cfg {??}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onCardGridShow',
			'onCardNavigation',
			'onCardZoom'
		],

		/**
		 * @property {CMDBuild.view.administration.userAndGroup.user.UserView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {String} configurationObject.title
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.classes.map.CM16CardGrid', { 
				delegate: this,
				title : "CM16-" + configurationObject.title,
				interactionDocument : this.interactionDocument,
				mainGrid : this.mainGrid
			});

		},

		/**
		 * @param {Object} card
		 * @param {Number} card.Id
		 * @param {Number} card.IdClass
		 * 
		 * @returns {Void}
		 */
		onCardNavigation: function(card) {
			CMDBuild.global.controller.MainViewport.cmfg('mainViewportCardSelect', card);
		},
		onCardZoom: function(card) {
			this.interactionDocument.centerOnCard(card);
		}
	});

})();
