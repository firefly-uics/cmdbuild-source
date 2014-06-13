(function() {

	Ext.require('CMDBuild.core.proxy.CMProxyTasks');

	// TODO: to update without extends CMDynamicKeyValueGrid
	Ext.define('CMDBuild.controller.administration.tasks.common.workflowForm.CMWorkflowFormController', {

		comboField: undefined,
		gridField: undefined,

		/**
		 * Gatherer function to catch events
		 *
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onSelectAttributeCombo':
					return this.onSelectAttributeCombo(param);

				case 'onSelectWorkflow':
					return this.onSelectWorkflow(param);

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * Workflow attribute store builder for onWorkflowSelected event
		 *
		 * @param (Object) attributes
		 *
		 * @return (Object) store
		 */
		buildWorkflowAttributesStore: function(attributes) {
			if (attributes) {
				var store = Ext.create('Ext.data.Store', {
					autoLoad: true,
					fields: [CMDBuild.ServiceProxy.parameter.VALUE],
					data: []
				});

				for (var key in attributes) {
					var bufferObj = {};

					bufferObj[CMDBuild.ServiceProxy.parameter.VALUE] = key;

					store.add(bufferObj);
				}

				return store;
			}
		},

		/**
		 * @param (Object) attributes
		 *
		 * @return (Object) out
		 */
		cleanServerAttributes: function(attributes) {
			var out = {};

			for (item in attributes)
				out[attributes[item].name] = '';

			return out;
		},

		// GETters functions
			/**
			 * @return (String)
			 */
			getValueCombo: function() {
				return this.comboField.getValue();
			},

			/**
			 * @return (Object)
			 */
			getValueGrid: function() {
				return this.gridField.getData();
			},

		/**
		 * @return (Boolean)
		 */
		isEmptyCombo: function() {
			return Ext.isEmpty(this.comboField.getValue());
		},

		/**
		 * @return (Integer) rowIndex
		 */
		onSelectAttributeCombo: function(rowIndex) {
			this.gridField.cellEditing.startEditByPosition({ row: rowIndex, column: 1 });
		},

		/**
		 * @param (String) name
		 * @param (Boolean) modify
		 */
		onSelectWorkflow: function(name, modify) {
			var me = this;

			if (Ext.isEmpty(modify))
				modify = false;

			CMDBuild.core.proxy.CMProxyTasks.getWorkflowAttributes({
				params: {
					className: name
				},
				success: function(response) {
					var decodedResponse = Ext.JSON.decode(response.responseText);

					me.gridField.keyEditorConfig.store = me.buildWorkflowAttributesStore(me.cleanServerAttributes(decodedResponse.attributes));

					// To setup updated editor store
					if (!Ext.isEmpty(me.gridField.columns[0]) && Ext.isFunction(me.gridField.columns[0].setEditor))
						me.gridField.columns[0].setEditor(me.gridField.keyEditorConfig);

					if (!modify) {
						me.gridField.store.removeAll();
						me.gridField.store.insert(0, Ext.create('CMDBuild.model.CMModelTasks.common.workflowForm'));
						me.gridField.cellEditing.startEditByPosition({ row: 0, column: 0 });
						me.setDisabledAttributesGrid(false);
					}
				}
			});
		},

		// SETters functions
			/**
			 * Set combo as required/unrequired
			 *
			 * @param (Boolean) state
			 */
			setAllowBlankCombo: function(state) {
				this.comboField.allowBlank = state;
			},

			/**
			 * @param (Boolean) state
			 */
			setDisabledAttributesGrid: function(state) {
				this.gridField.setDisabled(state);
			},

			/**
			 * @param (String) value
			 */
			setValueCombo: function(value) {
				if (!Ext.isEmpty(value)) {
					this.comboField.setValue(value);
					this.onSelectWorkflow(value, true);
				}
			},

			/**
			 * @param (Object) value
			 */
			setValueGrid: function(value) {
				if (!Ext.isEmpty(value))
					this.gridField.fillWithData(value);
			},

		/**
		 * Workflow form validation
		 *
		 * @param (Boolean) enable
		 */
		validate: function(enable) {
			this.setAllowBlankCombo(
				!(this.isEmptyCombo() && enable)
			);
		}
	});

})();