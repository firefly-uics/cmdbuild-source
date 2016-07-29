(function() {

	Ext.define('CMDBuild.controller.management.classes.map.thematism.ThematismMainWindow', {
		extend : 'CMDBuild.controller.common.abstract.Base',

		requires : [ 'CMDBuild.view.management.classes.map.thematism.ThematismMainWindow' ],

		/**
		 * @cfg {??}
		 */
		parentDelegate : undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions : [ "onPanelGridAndFormFilterAdvancedFilterEditorViewShow" ],

		/**
		 * @property {CMDBuild.view.administration.userAndGroup.user.UserView}
		 */
		view : undefined,

		/**
		 * @param {Object}
		 *            configurationObject
		 * @param {String}
		 *            configurationObject.title
		 * 
		 * @returns {Void}
		 * 
		 * @override
		 */
		constructor : function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.classes.map.thematism.ThematismMainWindow', {
				delegate : this,
				title : "@@ Thematism",
				interactionDocument : this.interactionDocument
			});

		},
		show : function() {
			this.view.show();
		},

		/**
		 * @param {Object}
		 *            card
		 * @param {Number}
		 *            card.Id
		 * @param {Number}
		 *            card.IdClass
		 * 
		 * @returns {Void}
		 */
		onPanelGridAndFormFilterAdvancedFilterEditorViewShow : function(card) {
			console.log("---->>>>>*****");
		},
		onCardZoom : function(card) {
			this.interactionDocument.centerOnCard(card);
		}
	});

})();
