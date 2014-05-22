(function() {

	var tr = CMDBuild.Translation.administration.tasks;

	Ext.define('CMDBuild.view.administration.tasks.event.asynchronous.CMStep1Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		parentDelegate: undefined,
		view: undefined,
		taskType: 'event_asynchronous',

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
			getValueId: function() {
				return this.view.idField.getValue();
			},

		isEmptyClass: function() {
			return Ext.isEmpty(this.view.className.getValue());
		},

		// SETters functions
			setDisabledButtonNext: function(state) {
				this.parentDelegate.setDisabledButtonNext(state);
			},

			setDisabledTypeField: function(state) {
				this.view.typeField.setDisabled(state);
			},

			setValueActive: function(value) {
				this.view.activeField.setValue(value);
			},

			setValueDescription: function(value) {
				this.view.descriptionField.setValue(value);
			},

			setValueId: function(value) {
				this.view.idField.setValue(value);
			}
	});

	Ext.define('CMDBuild.view.administration.tasks.event.asynchronous.CMStep1', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,

		border: false,
		height: '100%',
		overflowY: 'auto',

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.event.asynchronous.CMStep1Delegate', this);

			this.typeField = Ext.create('Ext.form.field.Text', {
				fieldLabel: tr.type,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name: CMDBuild.core.proxy.CMProxyConstants.TYPE,
				value: tr.tasksTypes.event + ' ' + tr.tasksTypes.eventTypes.asynchronous.toLowerCase(),
				width: CMDBuild.CFG_BIG_FIELD_WIDTH,
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
				width: CMDBuild.CFG_BIG_FIELD_WIDTH,
				allowBlank: false
			});

			this.activeField = Ext.create('Ext.form.field.Checkbox', {
				name: CMDBuild.core.proxy.CMProxyConstants.ACTIVE,
				fieldLabel: tr.startOnSave,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.CFG_BIG_FIELD_WIDTH
			});

			this.className = Ext.create('Ext.form.field.ComboBox', {
				name: CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME,
				fieldLabel: CMDBuild.Translation.targetClass,
				labelWidth: CMDBuild.LABEL_WIDTH,
				store: _CMCache.getClassesStore(),
				valueField: CMDBuild.core.proxy.CMProxyConstants.NAME,
				displayField: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				queryMode: 'local',
				allowBlank: false,
				forceSelection: true,
				editable: false,

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
					this.className
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			/**
			 * Disable next button only if class is not selected
			 */
			show: function(view, eOpts) {
				if (this.delegate.isEmptyClass())
					this.delegate.setDisabledButtonNext(true);
			}
		}
	});

})();