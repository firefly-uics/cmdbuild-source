(function() {

	Ext.define('CMDBuild.controller.administration.widget.AbstractWidgetDefinitionFormController', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.widget.CMWidgetDefinitionController}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Boolean}
		 */
		enableDelegateApply: true,

		/**
		 * @cfg {String}
		 */
		widgetName: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Object} configurationObject.parentDelegate
		 * @param {Object} configurationObject.view
		 */
		constructor: function(configurationObject) {
			if (Ext.isEmpty(this.widgetName))
				return _error('widgetName configuration param not defined', this);

			if (
				!Ext.Object.isEmpty(configurationObject)
				&& !Ext.isEmpty(configurationObject.view)
			) {
				this.callParent(arguments);

				// Inject delegate to view
				if (this.enableDelegateApply)
					this.view.delegate = this;
			} else {
				_error('wrong configuration object or empty view property', this);
			}
		},

		/**
		 * @returns {Object}
		 *
		 * @abstract
		 */
		widgetDefinitionGet: function() {
			var widgetDefinition = {};
			widgetDefinition[CMDBuild.core.constants.Proxy.TYPE] = this.widgetName;
			widgetDefinition[CMDBuild.core.constants.Proxy.LABEL] = this.view.buttonLabel.getValue();
			widgetDefinition[CMDBuild.core.constants.Proxy.ACTIVE] = this.view.active.getValue();
			widgetDefinition['alwaysenabled'] = this.view.alwaysEnabled.getValue();

			return widgetDefinition;
		},

		/**
		 * @param {Object} record
		 *
		 * @abstract
		 */
		widgetLoadRecord: Ext.emptyFn,




		afterEnableEditing: Ext.emptyFn,

		disableNonFieldElements: Ext.emptyFn,

		enableNonFieldElements: Ext.emptyFn,

//		/**
//		 * @param {CMDBuild.model.widget.WidgetDefinition} model
//		 */
//		fillFormWithModel: function(model) {
//			if (!Ext.isEmpty(model) && model.$className == 'CMDBuild.model.widget.WidgetDefinition')
//				this.view.fillWithModel(model);
//		},

		setDefaultValues: function() {
			this.view.active.setValue(true);
		}
	});

})();