(function() {

	var CANVAS_ID = "scenejsCanvas";

	Ext.define("CMDBuild.bim.management.view.CMBimWindow", {
		extend: "CMDBuild.PopupWindow",

		initComponent: function() {
			this.CANVAS_ID = CANVAS_ID;

			/*
			 * IMPORTANT!!
			 * There are unsolvable problems
			 * trying to destroy the sceneJs.
			 * 
			 * So do not destroy the
			 * window, but only hide, and
			 * reuse the same window.
			 */
			this.closeAction = 'hide';

			this.plain = true;
			this.frame = false;

			this.items = [{
				frame: false,
				plain: true,
				border: false,
				html: '<canvas class="bim-canvas" id="' + CANVAS_ID + '"></canvas>'
			}];

			this.defaultSize = 0.95;
			this.callParent(arguments);
		}
	});

})();