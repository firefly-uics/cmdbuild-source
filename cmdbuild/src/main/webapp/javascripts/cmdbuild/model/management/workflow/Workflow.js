(function () {

	Ext.require([
		'CMDBuild.core.constants.Global',
		'CMDBuild.core.constants.Proxy'
	]);

	/**
	 * Full workflow model
	 *
	 * @link CMDBuild.model.CMProcessInstance
	 * @link CMDBuild.model.classes.Class
	 */
	Ext.define('CMDBuild.model.management.workflow.Workflow', { // TODO: waiting for refactor (rename and structure)
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.CAPABILITIES, type: 'auto', defaultValue: {} },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.IS_STARTABLE, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.IS_SUPER_CLASS, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' }
		],

		/**
		 * @param {Object} data
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (data) {
			data = Ext.isObject(data) ? data : {};
			data[CMDBuild.core.constants.Proxy.CAPABILITIES] = Ext.decode(data['ui_card_edit_mode']);
			data[CMDBuild.core.constants.Proxy.DESCRIPTION] = Ext.isString(data[CMDBuild.core.constants.Proxy.TEXT]) ? data[CMDBuild.core.constants.Proxy.TEXT] : data[CMDBuild.core.constants.Proxy.DESCRIPTION];
			data[CMDBuild.core.constants.Proxy.IS_STARTABLE] = Ext.isBoolean(data['startable']) ? data['startable'] : data[CMDBuild.core.constants.Proxy.IS_STARTABLE];
			data[CMDBuild.core.constants.Proxy.IS_SUPER_CLASS] = Ext.isBoolean(data['superclass']) ? data['superclass'] : data[CMDBuild.core.constants.Proxy.IS_SUPER_CLASS];
			data[CMDBuild.core.constants.Proxy.PERMISSIONS] = {
				create: data['priv_create'],
				write: data['priv_write']
			};

			this.callParent(arguments);
		},

		get: function (name) {
			/**
			 * Legacy code for retro compatibility
			 *
			 * @legacy
			 *
			 * TODO: could be deleted????
			 */
			name = name == 'Id' ? CMDBuild.core.constants.Proxy.ID : name;
			name = name == 'IdClass' ? CMDBuild.core.constants.Proxy.CLASS_ID : name;
			name = name == 'IdClass_value' ? CMDBuild.core.constants.Proxy.CLASS_DESCRIPTION : name;

			return this.callParent(arguments);
		}
	});

})();

//
//
//Ext.define("CMDBuild.model.CMProcessInstance", {
//		extend: "Ext.data.Model",
//
//		fields: [
//			"beginDate",
//			"beginDateAsLong",
//			"classDescription",
//			"className",
//			"endDate",
//			"flowStatus",
//			{name: "id", type: "integer", useNull: true },
//			{name: "classId", type: "integer"},
//			{name: "values", type: "auto"},
//			{name: "activityInstanceInfoList", type: "auto", defaultValue: []},
//			{name: "user", type: "string"}
//		],
//
//		STATE: {
//			OPEN: "OPEN",
//			SUSPENDED: "SUSPENDED",
//			COMPLETED: "COMPLETED",
//			TERMINATED: "TERMINATED",
//			ABORTED: "ABORTED",
//			UNSUPPORTED: "UNSUPPORTED"
//		},
//

//
//		getActivityInfoList: function() {
//			return this.get("activityInstanceInfoList") || [];
//		},
//
//		getId: function() {
//			return this.get("id") || null;
//		},
//
//		isNew: function() {
//			return this.getId() == null;
//		},
//
//		getValues: function() {
//			return this.get("values") || {};
//		},
//
//		getClassId: function() {
//			return this.get("classId") || null;
//		},
//
//		getClassDescription: function() {
//			return this.get("classDescription");
//		},
//
//		applyValues: function(values) {
//			if (values) {
//				this.data.values = Ext.apply(this.getValues(), values);
//			}
//		},
//
//		getFlowStatus: function() {
//			return this.get("flowStatus");
//		},
//
//		isStateOpen: function() {
//			return this.getFlowStatus() == this.STATE.OPEN;
//		},
//
//		isStateCompleted: function() {
//			return this.getFlowStatus() == this.STATE.COMPLETED;
//		},
//
//		isStateSuspended: function() {
//			return this.getFlowStatus() == this.STATE.SUSPENDED;
//		},
//
//		setNotes: function(notes) {
//			this.data.values.Notes = notes;
//		},
//
//		updateBeginDate: function(data) {
//			if (data.beginDate && data.beginDateAsLong) {
//				this.set("beginDate", data.beginDate);
//				this.set("beginDateAsLong", data.beginDateAsLong);
//			}
//		},
//
//		asDummyModel: function() {
//			var values = this.getValues();
//			// add the old serialization data that
//			// could be called in the template resolver
//			values.Id = this.getId();
//			values.IdClass = this.getClassId();
//			values.IdClass_value = this.getClassDescription();
//
//			return new CMDBuild.DummyModel(values);
//		}
//	});