(function () {

	/**
	 * Required managed functions from upper structure:
	 * 	- panelGridAndFormGridFilterApply
	 * 	- panelGridAndFormGridFilterClear
	 */
	Ext.define('CMDBuild.controller.common.field.filter.runtimeParameters.RuntimeParameters', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.common.field.filter.RuntimeParameters'
		],

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'fieldFilterRuntimeParametersShow',
			'onFieldFilterRuntimeParametersAbortButtonClick',
			'onFieldFilterRuntimeParametersApplyButtonClick'
		],

		/**
		 * @parameter {CMDBuild.model.common.panel.gridAndForm.filter.advanced.Filter}
		 */
		filter: undefined,

		/**
		 * @property {CMDBuild.view.common.field.filter.runtimeParameters.RuntimeParametersWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {Object} configObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.common.field.filter.runtimeParameters.RuntimeParametersWindow', { delegate: this });

			// Shorthands
			this.form = this.view.form;
		},

		/**
		 * @param {Array} runtimeParameters
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		buildFields: function (runtimeParameters) {
			this.form.removeAll();

			// Error handling
				if (!Ext.isArray(runtimeParameters) || Ext.isEmpty(runtimeParameters))
					return _error('buildFields(): unmanaged runtimeParameters parameter', this, runtimeParameters);
			// END: Error handling

			var fieldManager = Ext.create('CMDBuild.core.fieldManager.FieldManager', { parentDelegate: this });

			Ext.Array.each(runtimeParameters, function (runtimeParameter, i, allRuntimeParameters) {
				if (fieldManager.isAttributeManaged(runtimeParameter[CMDBuild.core.constants.Proxy.TYPE])) {
					var attributeCustom = Ext.create('CMDBuild.model.common.attributes.Attribute', runtimeParameter);
					attributeCustom.setAdaptedData(runtimeParameter);

					fieldManager.attributeModelSet(attributeCustom);
					fieldManager.add(this.form, fieldManager.buildField());
				} else { /** @deprecated - Old field manager */
					var field = CMDBuild.Management.FieldManager.getFieldForAttr(runtimeParameter, false, false);

					if (!Ext.isEmpty(field)) {
						field.maxWidth = field.width || CMDBuild.core.constants.FieldWidths.STANDARD_MEDIUM;

						if (runtimeParameter.defaultvalue)
							field.setValue(runtimeParameter.defaultvalue);

						this.form.add(field);
					}
				}
			}, this);
		},

		/**
		 * @param {Ext.data.Model} filter
		 *
		 * @returns {Void}
		 */
		fieldFilterRuntimeParametersShow: function (filter) {
			this.filter = undefined;

			// Error handling
				if (!Ext.isObject(filter) || Ext.Object.isEmpty(filter))
					return _error('fieldFilterRuntimeParametersShow(): unmanaged filter parameter', this, filter);

				if (!Ext.isBoolean(filter.isFilterAdvancedCompatible) || filter.isFilterAdvancedCompatible)
					return _error('fieldFilterRuntimeParametersShow(): filter parameter not compatible', this, filter);
			// END: Error handling

			var runtimeParameters = filter.getEmptyRuntimeParameters(),
				runtimeParametersNames = [];

			Ext.Array.each(runtimeParameters, function (runtimeParameter, i, allRuntimeParameters) {
				if (
					Ext.isObject(runtimeParameter) && !Ext.Object.isEmpty(runtimeParameter)
					&& !Ext.isEmpty(runtimeParameter[CMDBuild.core.constants.Proxy.ATTRIBUTE])
				) {
					runtimeParametersNames.push(runtimeParameter[CMDBuild.core.constants.Proxy.ATTRIBUTE]);
				}
			}, this);

			// Error handling
				if (!Ext.isArray(runtimeParameters) || Ext.isEmpty(runtimeParameters))
					return _error('fieldFilterRuntimeParametersShow(): no runtime parameters found in filter object', this, filter);
			// END: Error handling

			var params = {};
			params[CMDBuild.core.constants.Proxy.CLASS_NAME] = filter.get(CMDBuild.core.constants.Proxy.ENTRY_TYPE);

			CMDBuild.proxy.common.field.filter.RuntimeParameters.readAllAttributes({
				params: params,
				scope: this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.ATTRIBUTES];

					var runtimeParametersAttributes = [];

					if (Ext.isArray(decodedResponse) && !Ext.isEmpty(decodedResponse)) {
						Ext.Array.each(decodedResponse, function (attributeObject, i, allAttributeObjects) {
							if (
								Ext.isObject(attributeObject) && !Ext.Object.isEmpty(attributeObject)
								&& Ext.Array.contains(runtimeParametersNames, attributeObject[CMDBuild.core.constants.Proxy.NAME])
							) {
								runtimeParametersAttributes.push(Ext.apply(attributeObject, { fieldmode: 'write' })); // Force writable to be editable by user
							}
						}, this);

						this.buildFields(runtimeParametersAttributes);
						this.setViewTitle(filter.get(CMDBuild.core.constants.Proxy.DESCRIPTION));

						this.filter = filter;

						this.view.show();
					}
				}
			});
		},

		/**
		 * @returns {Void}
		 */
		onFieldFilterRuntimeParametersAbortButtonClick: function () {
			this.cmfg('panelGridAndFormGridFilterClear');

			this.view.close();
		},

		/**
		 * @returns {Void}
		 */
		onFieldFilterRuntimeParametersApplyButtonClick: function () {
			if (this.form.getForm().isValid()) {
				this.filter.setRuntimeParameterValue(this.form.getValues());

				this.cmfg('panelGridAndFormGridFilterApply', {
					filter: this.filter,
					type: 'advanced'
				});

				this.view.close();
			}
		}
	});

})();
