(function() {

	Ext.define("CMDBuild.controller.administration.classes.CMModClassController", {
		extend: "CMDBuild.controller.CMBasePanelController",
		constructor: function() {
			this.callParent(arguments);
			this.buildSubcontrollers();

			this.view.addClassButton.on("click", this.onAddClassButtonClick, this);
			this.view.printSchema.on("click", this.onPrintSchema, this);

			this.registerToCacheEvents();
		},

		//private and overridden in subclasses
		buildSubcontrollers: function() {
			this.subControllers = [
				this.classFormController = new CMDBuild.controller.administration.classes.CMClassFormController(this.view.classForm),
				this.domainTabController = new CMDBuild.controller.administration.classes.CMDomainTabController(this.view.domainGrid),
				this.geoAttributesController = new CMDBuild.controller.administration.classes.CMGeoAttributeController(this.view.geoAttributesPanel),
				this.attributePanelController = new CMDBuild.controller.administration.classes.CMClassAttributeController(this.view.attributesPanel),
				this.widgetDefinitionController = new CMDBuild.controller.administration.widget.CMWidgetDefinitionController(this.view.widgetPanel)
			];
			var me = this;
			this.subControllers.relay = function(fn) {
				Ext.Array.each(me.subControllers, fn);
			}
		},

		//private and overridden in subclasses
		registerToCacheEvents: function() {
			_CMCache.on("cm_class_deleted", this.view.onClassDeleted, this.view);
		},

		//private and overridden in subclasses
		onViewOnFront: function(selection) {
			if (selection) {
				this.view.onClassSelected(selection.data);
				this.subControllers.relay(function(subcontroller, index, subcontrollers) {
					subcontroller.onClassSelected(selection.data.id);
				});
			}
		},

		/**
		 * @params {String} format
		 */
		onPrintSchema: function(format) {
			if (!Ext.isEmpty(format)) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.FORMAT] = format;

				Ext.create('CMDBuild.controller.common.entryTypeGrid.printTool.PrintWindow', {
					parentDelegate: this,
					format: format,
					mode: 'schema',
					parameters: params
				});
			}
		},

		onAddClassButtonClick: function () {
			this.subControllers.relay(function(subcontroller, index, subcontrollers) {
				subcontroller.onAddClassButtonClick();
			});

			this.view.onAddClassButtonClick();
			_CMMainViewportController.deselectAccordionByName("class");

		}
	});

})();