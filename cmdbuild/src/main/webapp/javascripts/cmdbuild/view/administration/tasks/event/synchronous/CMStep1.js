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
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 *
		 * @overwrite
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		// GETters functions
			/**
			 * @return {String}
			 */
			getValueGroups: function() {
				return this.view.groups.getValue();
			},

			/**
			 * @return {String}
			 */
			getValueId: function() {
				return this.view.idField.getValue();
			},

			/**
			 * @return {String}
			 */
			getValuePhase: function() {
				return this.view.phaseCombo.getValue();
			},

		/**
		 * @return {Boolean}
		 */
		isEmptyClass: function() {
			return Ext.isEmpty(this.view.classNameCombo.getValue());
		},

		// SETters functions
			/**
			 * @param {Array} itemsToSelect
			 */
			selectGroups: function(itemsToSelect) {
				this.view.groups.setValue(itemsToSelect);
			},

			/**
			 * @param {Boolean} state
			 */
			setAllowBlankPhaseCombo: function(state) {
				this.view.phaseCombo.allowBlank = state;
			},

			/**
			 * @param {Boolean} state
			 */
			setDisabledButtonNext: function(state) {
				this.parentDelegate.setDisabledButtonNext(state);
			},

			/**
			 * @param {Boolean} state
			 */
			setDisabledTypeField: function(state) {
				this.view.typeField.setDisabled(state);
			},

			/**
			 * @param {Boolean} state
			 */
			setValueActive: function(state) {
				this.view.activeField.setValue(state);
			},

			/**
			 * @param {String} value
			 */
			setValueClassName: function(value) {
				this.view.classNameCombo.setValue(value);

				// Manually select event fire
				this.cmOn('onClassSelected', { className: value });
			},

			/**
			 * @param {String} value
			 */
			setValueDescription: function(value) {
				this.view.descriptionField.setValue(value);
			},

			/**
			 * @param {String} value
			 */
			setValueId: function(value) {
				this.view.idField.setValue(value);
			},

			/**
			 * @param {String} value
			 */
			setValuePhase: function(value) {
				this.view.phaseCombo.setValue(value);
			}
	});

	Ext.define('CMDBuild.view.administration.tasks.event.synchronous.CMStep1', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,

		border: false,
		frame: true,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align:'stretch'
		},

		defaults: {
			maxWidth: CMDBuild.CFG_BIG_FIELD_WIDTH,
			anchor: '100%'
		},

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.event.synchronous.CMStep1Delegate', this);

			this.typeField = Ext.create('Ext.form.field.Text', {
				name: CMDBuild.core.proxy.CMProxyConstants.TYPE,
				fieldLabel: tr.type,
				labelWidth: CMDBuild.LABEL_WIDTH,
				value: tr.tasksTypes.event + ' ' + tr.tasksTypes.eventTypes.synchronous.toLowerCase(),
				disabled: true,
				cmImmutable: true,
				readOnly: true,
				submitValue: false
			});

			this.idField = Ext.create('Ext.form.field.Hidden', {
				name: CMDBuild.core.proxy.CMProxyConstants.ID
			});

			this.descriptionField = Ext.create('Ext.form.field.Text', {
				name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
				fieldLabel: CMDBuild.Translation.description_,
				labelWidth: CMDBuild.LABEL_WIDTH,
				allowBlank: false
			});

			this.activeField = Ext.create('Ext.form.field.Checkbox', {
				name: CMDBuild.core.proxy.CMProxyConstants.ACTIVE,
				fieldLabel: tr.startOnSave,
				labelWidth: CMDBuild.LABEL_WIDTH
			});

			this.phaseCombo = Ext.create('Ext.form.field.ComboBox', {
				name: CMDBuild.core.proxy.CMProxyConstants.PHASE,
				fieldLabel: tr.taskEvent.phase,
				labelWidth: CMDBuild.LABEL_WIDTH,
				store: CMDBuild.core.proxy.CMProxyTasks.getPhases(),
				valueField: CMDBuild.core.proxy.CMProxyConstants.VALUE,
				displayField: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
				maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
				queryMode: 'local',
				forceSelection: true,
				editable: false
			});

			this.groups = Ext.create('CMDBuild.view.common.field.CMGroupSelectionList', {
				name: CMDBuild.core.proxy.CMProxyConstants.GROUPS,
				fieldLabel: tr.taskEvent.groupsToApply,
				height: 300,
				valueField: CMDBuild.core.proxy.CMProxyConstants.NAME,
				labelWidth: CMDBuild.LABEL_WIDTH,
				maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
				considerAsFieldToDisable: true,
				anchor: '100%'
			});

			this.classNameCombo = Ext.create('Ext.form.field.ComboBox', {
				name: CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME,
				fieldLabel: CMDBuild.Translation.classLabel,
				labelWidth: CMDBuild.LABEL_WIDTH,
				valueField: CMDBuild.core.proxy.CMProxyConstants.NAME,
				displayField: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
				maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
				allowBlank: false,
				forceSelection: true,
				editable: false,

				store: _CMCache.getClassesStore(),
				queryMode: 'local',

				listeners: {
					select: function(combo, records, options) {
						me.delegate.cmOn('onClassSelected', { className: this.getValue() });
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
			activate: function(view, eOpts) {
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