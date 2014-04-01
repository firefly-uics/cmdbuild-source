(function() {

	var tr = CMDBuild.Translation.administration.tasks;

	Ext.define('CMDBuild.view.administration.tasks.event.synchronous.CMStep1Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		parentDelegate: undefined,
		view: undefined,

		/**
		 * Gatherer function to catch events
		 *
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

//		getClassName: function() {
//			return this.view.classes.getValue();
//		},

		getValueId: function() {
			return this.view.idField.getValue();
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
		},



		showFilterChooserPicker: function(className) {
			var me = this;
//			var className = this.className;
			var filter = this.filter || new CMDBuild.model.CMFilterModel({
				entryType: className,
				local: true,
				name: CMDBuild.Translation.management.findfilter.newfilter + " " + _CMUtils.nextId()
			});

			var entryType = _CMCache.getEntryTypeByName(className);

			_CMCache.getAttributeList(entryType.getId(), function(attributes) {

				var filterWindow = new CMDBuild.view.common.field.CMFilterChooserWindow({
					filter: filter,
					attributes: attributes,
					className: className
				});

				filterWindow.show();
			});
		},
	});

	Ext.define('CMDBuild.view.administration.tasks.event.synchronous.CMStep1', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,
		taskType: 'workflow',

		border: false,
		height: '100%',
		overflowY: 'auto',

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.event.synchronous.CMStep1Delegate', this);

			this.typeField = Ext.create('Ext.form.field.Text', {
				fieldLabel: tr.type,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name: CMDBuild.ServiceProxy.parameter.TYPE,
				width: CMDBuild.CFG_BIG_FIELD_WIDTH,
				value: me.taskType,
				disabled: true,
				cmImmutable: true,
				readOnly: true
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
				width: CMDBuild.ADM_BIG_FIELD_WIDTH
			});

			this.phase = Ext.create('Ext.form.field.ComboBox', {
				name: 'phase',
				fieldLabel: 'tr.phase',
				labelWidth: CMDBuild.LABEL_WIDTH,
				store: Ext.create('Ext.data.SimpleStore', {
					fields: [CMDBuild.ServiceProxy.parameter.VALUE, CMDBuild.ServiceProxy.parameter.DESCRIPTION],
					data: [
						['afterCreate', 'tr.afterCreate'],
						['beforeCreate', 'tr.beforeCreate'],
						['afterUpdate', 'tr.afterUpdate'],
						['beforeUpdate', 'tr.beforeUpdate'],
						['beforeDelete', 'tr.beforeDelete']
					]
				}),
				valueField: CMDBuild.ServiceProxy.parameter.VALUE,
				displayField: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				queryMode: 'local',
				forceSelection: true,
				editable: false,

//				listeners: {
//					select: function(combo, record, index) {
//						me.delegate.setValueAdvancedFields(record[0].get(CMDBuild.ServiceProxy.parameter.VALUE));
//					}
//				}
			});

			this.groups = Ext.create('CMDBuild.view.common.field.CMGroupSelectionList', {
				fieldLabel: 'tr.groupsToApply',
				height: 300,
				valueField: CMDBuild.ServiceProxy.parameter.NAME,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				considerAsFieldToDisable: true
			});

			this.classes = Ext.create('Ext.form.field.ComboBox', {
				name: CMDBuild.ServiceProxy.parameter.CLASS_NAME,
				fieldLabel: CMDBuild.Translation.targetClass,
				labelWidth: CMDBuild.LABEL_WIDTH,
				store: _CMCache.getClassesAndProcessesAndDahboardsStore(),
				valueField: CMDBuild.ServiceProxy.parameter.NAME,
				displayField: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				queryMode: 'local',
				forceSelection: true,
				editable: false,

				listeners: {
					select: function(combo, records, options) {
//						me.delegate.cmOn("onClassSelected", { className: records[0].get(this.valueField) });
						me.delegate.showFilterChooserPicker(records[0].get(this.valueField));
					}
				}
			});

//			this.filterChooser = new CMDBuild.view.common.field.CMFilterChooser({
//				fieldLabel: CMDBuild.Translation.filter,
//				labelWidth: CMDBuild.LABEL_WIDTH,
//				name: 'FILTER'
//			});

			Ext.apply(this, {
				items: [
					this.typeField,
					this.idField,
					this.descriptionField,
					this.activeField,
					this.phase,
					this.groups,
					this.classes,
//					this.filterChooser
				]
			});

			this.callParent(arguments);
		}
	});

})();
