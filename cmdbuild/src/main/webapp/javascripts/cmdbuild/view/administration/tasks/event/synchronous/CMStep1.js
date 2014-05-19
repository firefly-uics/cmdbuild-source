(function() {

	var tr = CMDBuild.Translation.administration.tasks;

	Ext.define('CMDBuild.view.administration.tasks.event.synchronous.CMStep1Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		parentDelegate: undefined,

		taskType: 'event_synchronous',
		view: undefined,

		/**
		 * Gatherer function to catch events
		 *
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		// overwrite
		cmOn: function(name, param, callBack) {
			switch (name) {
				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		// GETters functions
			/**
			 * @return (String)
			 */
			getValueGroups: function() {
				return this.view.groups.getValue();
			},

			/**
			 * @return (String)
			 */
			getValueId: function() {
				return this.view.idField.getValue();
			},

			/**
			 * @return (String)
			 */
			getValuePhase: function() {
				return this.view.phaseCombo.getValue();
			},

		/**
		 * @return (Boolean)
		 */
		isEmptyClass: function() {
			return Ext.isEmpty(this.view.classNameCombo.getValue());
		},

		// SETters functions
			/**
			 * @param (Array) itemsToSelect
			 */
			selectGroups: function(itemsToSelect) {
				this.view.groups.setValue(itemsToSelect);
			},

			/**
			 * @param (Boolean) state
			 */
			setAllowBlankPhaseCombo: function(state) {
				this.view.phaseCombo.allowBlank = state;
			},

			/**
			 * @param (Boolean) state
			 */
			setDisabledButtonNext: function(state) {
				this.parentDelegate.setDisabledButtonNext(state);
			},

			/**
			 * @param (Boolean) state
			 */
			setDisabledTypeField: function(state) {
				this.view.typeField.setDisabled(state);
			},

			/**
			 * @param (Boolean) state
			 */
			setValueActive: function(state) {
				this.view.activeField.setValue(state);
			},

			/**
			 * @param (String) value
			 */
			setValueClassName: function(value) {
				// HACK to avoid forceSelection timing problem witch don't permits to set combobox value
				this.view.classNameCombo.forceSelection = false;
				this.view.classNameCombo.setValue(value);
				this.view.classNameCombo.forceSelection = true;

				// Manually select event fire
				this.cmOn('onClassSelected', { className: value });
			},

			/**
			 * @param (String) value
			 */
			setValueDescription: function(value) {
				this.view.descriptionField.setValue(value);
			},

			/**
			 * @param (String) value
			 */
			setValueId: function(value) {
				this.view.idField.setValue(value);
			},

			/**
			 * @param (String) value
			 */
			setValuePhase: function(value) {
				// HACK to avoid forceSelection timing problem witch don't permits to set combobox value
				this.view.phaseCombo.forceSelection = false;
				this.view.phaseCombo.setValue(value);
				this.view.phaseCombo.forceSelection = true;
			}
	});

	Ext.define('CMDBuild.view.administration.tasks.event.synchronous.CMStep1', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,

		border: false,
		height: '100%',
		overflowY: 'auto',

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.event.synchronous.CMStep1Delegate', this);

			this.typeField = Ext.create('Ext.form.field.Text', {
				name: CMDBuild.ServiceProxy.parameter.TYPE,
				fieldLabel: tr.type,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.CFG_BIG_FIELD_WIDTH,
				value: tr.tasksTypes.event + ' ' + tr.tasksTypes.eventTypes.synchronous.toLowerCase(),
				disabled: true,
				cmImmutable: true,
				readOnly: true,
				submitValue: false
			});

			this.idField = Ext.create('Ext.form.field.Hidden', {
				name: CMDBuild.ServiceProxy.parameter.ID
			});

			this.descriptionField = Ext.create('Ext.form.field.Text', {
				name: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
				fieldLabel: CMDBuild.Translation.description_,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.CFG_BIG_FIELD_WIDTH,
				allowBlank: false
			});

			this.activeField = Ext.create('Ext.form.field.Checkbox', {
				name: CMDBuild.ServiceProxy.parameter.ACTIVE,
				fieldLabel: tr.startOnSave,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.CFG_BIG_FIELD_WIDTH
			});

			this.phaseCombo = Ext.create('Ext.form.field.ComboBox', {
				name: CMDBuild.ServiceProxy.parameter.PHASE,
				fieldLabel: tr.taskEvent.phase,
				labelWidth: CMDBuild.LABEL_WIDTH,
				store: CMDBuild.core.proxy.CMProxyTasks.getPhases(),
				valueField: CMDBuild.ServiceProxy.parameter.VALUE,
				displayField: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				queryMode: 'local',
				forceSelection: true,
				editable: false
			});

			this.groups = Ext.create('CMDBuild.view.common.field.CMGroupSelectionList', {
				name: CMDBuild.ServiceProxy.parameter.GROUPS,
				fieldLabel: tr.taskEvent.groupsToApply,
				height: 300,
				valueField: CMDBuild.ServiceProxy.parameter.NAME,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				considerAsFieldToDisable: true
			});

			this.classNameCombo = Ext.create('Ext.form.field.ComboBox', {
				name: CMDBuild.ServiceProxy.parameter.CLASS_NAME,
				fieldLabel: CMDBuild.Translation.targetClass,
				labelWidth: CMDBuild.LABEL_WIDTH,
				store: _CMCache.getClassesStore(),
				valueField: CMDBuild.ServiceProxy.parameter.NAME,
				displayField: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				queryMode: 'local',
				allowBlank: false,
				forceSelection: true,
				editable: false,

				listeners: {
					select: function(combo, records, options) {
						me.delegate.cmOn('onClassSelected', { className: records[0].get(CMDBuild.ServiceProxy.parameter.NAME) });
					}
				}
			});

			Ext.apply(this, {
				items: [
					this.typeField,
					this.idField,
					this.descriptionField,
					this.activeField,
					this.phaseCombo,
					this.groups,
					this.classNameCombo
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function(view, eOpts) {
				// Disable next button only if class is not selected
				if (this.delegate.isEmptyClass())
					this.delegate.setDisabledButtonNext(true);

				// Select all groups by default only if there aren't other selections
				if (this.groups.getValue().length == 0)
					this.groups.selectAll();
			}
		}
	});

})();