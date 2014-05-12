(function() {

	Ext.require('CMDBuild.core.proxy.CMProxyTasks');

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
				var data = [];

				for (var key in attributes) {
					data.push({ value: key });
				}

				return Ext.create('Ext.data.Store', {
					fields: [CMDBuild.ServiceProxy.parameter.VALUE],
					data: data,
					autoLoad: true
				});
			}
		},

		/**
		 * @param (Object) attributes
		 *
		 * @return (Object) out
		 */
		cleanServerAttributes: function(attributes) {
			var out = {};

			for (var i = 0, l = attributes.length; i < l; ++i) {
				var attr = attributes[i];

				out[attr.name] = '';
			}

			return out;
		},

		getValueCombo: function() {
			return this.comboField.getValue();
		},

		getValueGrid: function() {
			return this.gridField.getData();
		},

		isEmptyCombo: function() {
			return Ext.isEmpty(this.comboField.getValue());
		},

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

					if (!modify) {
						me.gridField.store.removeAll();
						me.gridField.store.insert(0, { key: '', value: '' });
						me.gridField.cellEditing.startEditByPosition({ row: 0, column: 0 });
						me.setDisabledAttributesGrid(false);
					}
				}
			});
		},

		/**
		 * Set combo as required/unrequired
		 *
		 * @param (Boolean) state
		 */
		setAllowBlankCombo: function(state) {
			this.comboField.allowBlank = state;
		},

		setDisabledAttributesGrid: function(state) {
			this.gridField.setDisabled(state);
		},

		setValueCombo: function(workflowName) {
			if (!Ext.isEmpty(workflowName)) {
				this.comboField.setValue(workflowName);
				this.onSelectWorkflow(workflowName, true);
			}
		},

		setValueGrid: function(data) {
			if (!Ext.isEmpty(data))
				this.gridField.fillWithData(data);
		},

		/**
		 * Workflow form validation
		 *
		 * @param (Boolean) enable
		 *
		 * @return (Boolean)
		 */
		validate: function(enable) {
			if (this.isEmptyCombo() && enable) {
				this.setAllowBlankCombo(false);
			} else {
				this.setAllowBlankCombo(true);
			}
		}
	});

})();