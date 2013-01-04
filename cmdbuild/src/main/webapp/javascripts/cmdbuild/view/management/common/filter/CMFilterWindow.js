(function() {

	Ext.define("CMDBuild.view.management.common.filter.CMFilterWindowDelegate", {
		/**
		 * @params {CMDBuild.view.management.common.filter.CMFilterWindow} filterWindow
		 * The filter window that call the delegate
		 */
		onCMFilterWindowApplyButtonClick: Ext.emptyFn
	});

	Ext.define("CMDBuild.view.management.common.filter.CMFilterWindow", {
		extend : "CMDBuild.PopupWindow",

		mixins: {
			delegable: "CMDBuild.core.CMDelegable"
		},

		// configuration
		attributes: {},
		className: '',
		// configuration

		constructor: function() {
			this.mixins.delegable.constructor.call(this,
				"CMDBuild.view.management.common.filter.CMFilterWindowDelegate");

			this.callParent(arguments);
		},

		initComponent : function() {

			this.filterAttributesPanel = new CMDBuild.view.management.common.filter.CMFilterAttributes({
				attributes: this.attributes,
				className: this.className
			});

//		this.relations = new CMDBuild.view.management.common.filter.CMRelations({
//			attributes: this.attributes,
//			className: this.className
//		});

			this.title = CMDBuild.Translation.management.findfilter.window_title;
			var et = _CMCache.getEntryTypeByName(this.className);
			if (et) {
				this.title += " - " + et.getDescription();
			}

			this.items = [
				this.filterAttributesPanel
//			, this.relations
			];

			this.layout = "accordion";
			this.buttonAlign = "center";
			var me = this;
			this.buttons = [{
				text: CMDBuild.Translation.common.btns.confirm,
				handler: function() {
					me.callDelegates("onCMFilterWindowApplyButtonClick", [me]);
				}
			}, {
				text: CMDBuild.Translation.common.btns.abort,
				scope: this,
				handler: this.destroy
			}];

			this.callParent(arguments);
		},

		getFilter: function() {
			var filter = {
				attribute: this.filterAttributesPanel.getData(),
				relation: []
			};

			return filter;
		}
	});
})();