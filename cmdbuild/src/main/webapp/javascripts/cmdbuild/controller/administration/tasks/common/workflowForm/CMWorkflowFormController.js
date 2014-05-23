(function() {

	Ext.require('CMDBuild.core.proxy.CMProxyTasks');

	Ext.define('CMDBuild.controller.administration.tasks.common.workflowForm.CMWorkflowFormController', {

		comboField: undefined,
		gridField: undefined,
		gridEditorPlugin: undefined,

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
			if (!Ext.isEmpty(attributes)) {
				var store = Ext.create('Ext.data.Store', {
					autoLoad: true,
					fields: [CMDBuild.core.proxy.CMProxyConstants.VALUE],
					data: []
				});

				for (var key in attributes)
					store.add({ value: key });

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

			for (var item in attributes)
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
				var data = [];

				// To validate and filter grid rows
				this.gridField.getStore().each(function(record) {
					if (
						!Ext.isEmpty(record.get(CMDBuild.core.proxy.CMProxyConstants.NAME))
						&& !Ext.isEmpty(record.get(CMDBuild.core.proxy.CMProxyConstants.VALUE))
					) {
						var buffer = {};

						buffer[CMDBuild.core.proxy.CMProxyConstants.NAME] = record.get(CMDBuild.core.proxy.CMProxyConstants.NAME);
						buffer[CMDBuild.core.proxy.CMProxyConstants.VALUE] = record.get(CMDBuild.core.proxy.CMProxyConstants.VALUE);

						data.push(buffer);
					}
				});

				return data;
			},

		/**
		 * @return (Boolean)
		 */
		isEmptyCombo: function() {
			return Ext.isEmpty(this.comboField.getValue());
		},

		/**
		 * @param (Int) rowIndex
		 */
		onSelectAttributeCombo: function(rowIndex) {
			this.gridEditorPlugin.startEditByPosition({ row: rowIndex, column: 1 });
		},

		/**
		 * @param (String) className
		 * @param (Boolean) modify
		 */
		onSelectWorkflow: function(className, modify) {
_debug('onSelectWorkflow');
_debug(this.gridField);
			if (!Ext.isEmpty(className)) {
				var me = this;

				if (Ext.isEmpty(modify))
					modify = false;

				CMDBuild.core.proxy.CMProxyTasks.getWorkflowAttributes({
//					scope: this,
					params: {
						className: className
					},
					success: function(response) {
						var decodedResponse = Ext.JSON.decode(response.responseText);
_debug(decodedResponse);
_debug(me.gridField.columns[0]);
						me.gridField.columns[0].setEditor({
							xtype: 'combo',
							valueField: CMDBuild.core.proxy.CMProxyConstants.VALUE,
							displayField: CMDBuild.core.proxy.CMProxyConstants.VALUE,
							forceSelection: true,
							editable: false,
							allowBlank: false,

							store: me.buildWorkflowAttributesStore(me.cleanServerAttributes(decodedResponse.attributes)),
							queryMode: 'local',

							listeners: {
								select: function(combo, records, eOpts) {
									me.cmOn('onSelectAttributeCombo', me.gridField.store.indexOf(me.gridField.getSelectionModel().getSelection()[0]));
								}
							}
						});

						if (!modify) {
							me.gridField.store.removeAll();
							me.gridField.store.insert(0, Ext.create('CMDBuild.model.CMModelTasks.common.workflowForm'));
							me.gridEditorPlugin.startEditByPosition({ row: 0, column: 0 });
							me.setDisabledAttributesGrid(false);
						}
					}
				});
			}
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
			 * Rewrite of loadData
			 *
			 * @param (Object) value
			 */
			setValueGrid: function(value) {
				var store = this.gridField.getStore();
				store.removeAll();

				if (!Ext.isEmpty(value)) {
					for (var key in value) {
						var recordConf = {};

						recordConf[CMDBuild.core.proxy.CMProxyConstants.NAME] = key;
						recordConf[CMDBuild.core.proxy.CMProxyConstants.VALUE] = value[key] || '';

						store.add(recordConf);
					}
				} else {
					var recordConf = {};

					recordConf[CMDBuild.core.proxy.CMProxyConstants.NAME] = '';
					recordConf[CMDBuild.core.proxy.CMProxyConstants.VALUE] = '';

					store.add(recordConf);
				}
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