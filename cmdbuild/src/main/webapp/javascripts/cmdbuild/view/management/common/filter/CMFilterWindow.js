(function() {

	Ext.define("CMDBuild.view.management.common.filter.CMFilterWindowDelegate", {
		/**
		 * @params {CMDBuild.view.management.common.filter.CMFilterWindow} filterWindow
		 * The filter window that call the delegate
		 */
		onCMFilterWindowApplyButtonClick: Ext.emptyFn,

		/**
		 * @params {CMDBuild.view.management.common.filter.CMFilterWindow} filterWindow
		 * The filter window that call the delegate
		 */
		onCMFilterWindowSaveAndApplyButtonClick: Ext.emptyFn,

		/**
		 * @params {CMDBuild.view.management.common.filter.CMFilterWindow} filterWindow
		 * The filter window that call the delegate
		 */
		onCMFilterWindowAbortButtonClick: Ext.emptyFn
	});

	Ext.define("CMDBuild.view.management.common.filter.CMFilterWindow", {
		extend : "CMDBuild.PopupWindow",

		mixins: {
			delegable: "CMDBuild.core.CMDelegable"
		},

		// configuration
		attributes: {},
		className: '',
		filter: undefined,
		// configuration

		constructor: function() {
			this.mixins.delegable.constructor.call(this,
				"CMDBuild.view.management.common.filter.CMFilterWindowDelegate");

			this.callParent(arguments);
		},

		initComponent : function() {

			this.filterAttributesPanel = new CMDBuild.view.management.common.filter.CMFilterAttributes({
				attributes: this.attributes,
				className: this.className
			});

//		this.relations = new CMDBuild.view.management.common.filter.CMRelations({
//			attributes: this.attributes,
//			className: this.className
//		});

			this.title = CMDBuild.Translation.management.findfilter.window_title;
			var et = _CMCache.getEntryTypeByName(this.className);
			if (et) {
				this.title += " - " + et.getDescription();
			}

			this.items = [
				this.filterAttributesPanel
//			, this.relations
			];

			this.layout = "accordion";
			this.buttonAlign = "center";
			var me = this;

			this.buttons = [{
				text: "@@ Apply", //CMDBuild.Translation.common.btns.confirm,
				handler: function() {
					me.callDelegates("onCMFilterWindowApplyButtonClick", [me, me.getFilter()]);
				}
			},{
				text: "@@ SaveAndApply",
				handler: function() {
					me.callDelegates("onCMFilterWindowSaveAndApplyButtonClick", [me, me.getFilter()]);
				}
			},{
				text: "@@ Abort", // CMDBuild.Translation.common.btns.abort,
				handler: function() {
					me.callDelegates("onCMFilterWindowAbortButtonClick", [me]);
				}
			}];

			this.callParent(arguments);

			me.on("show", function() {
				this.filterAttributesPanel.setData(me.filter.getAttributeConfiguration());
			});
		},

		getFilter: function() {
			// TODO check if is really dirty, if there are change in the filter
			this.filter.setDirty();

			this.filter.setAttributeConfiguration(this.filterAttributesPanel.getData());
			this.filter.setRelationConfiguration([]);

			return this.filter;
		}
	});

	Ext.define("CMDBuild.view.management.common.filter.CMSaveFilterWindowDelegate", {
		/**
		 * @param {CMDBuild.view.management.common.filter.CMSaveFilterWindow} window
		 * the window that calls the delegate
		 * @param {CMDBuild.model.CMFilterModel} filter
		 * the filter to save
		 * @param {String} name
		 * the name set in the form
		 * @param {String} the description set in the form
		 */
		onSaveFilterWindowConfirm: Ext.emptyFn
	});

	Ext.define("CMDBuild.view.management.common.filter.CMSaveFilterWindow", {
		extend: "Ext.window.Window",

		mixins: {
			delegable: "CMDBuild.core.CMDelegable"
		},

		// configuration
		filter: undefined, // a CMDBuild.model.CMFilterModel,
		referredFilterWindow: undefined, // a CMFilterWindow, used outside to know the referred filter window and close it
		// configuration

		constructor: function() {
			this.mixins.delegable.constructor.call(this,
				"CMDBuild.view.management.common.filter.CMSaveFilterWindowDelegate");

			this.callParent(arguments);
		},

		initComponent: function() {
			this.modal = true;
			this.bodyPadding = "5px 5px 1px 5px";

			var canEditTheName = this.filter.isLocal();
			this.nameField = new Ext.form.field.Text({
				name: 'name',
				fieldLabel: '@@ Name',
				value: this.filter.getName(),
				disabled: !canEditTheName,
				width: CMDBuild.BIG_FIELD_WIDTH,
				allowBlank: false //requires a non-empty value
			});

			this.descriptionField = new Ext.form.field.TextArea({
				name: 'description',
				fieldLabel: '@@ Description',
				value: this.filter.getDescription(),
				width: CMDBuild.BIG_FIELD_WIDTH,
				allowBlank: false //requires a non-empty value
			});

			this.items = [this.nameField, this.descriptionField];

			var me = this;
			this.buttonAlign = "center";
			this.buttons = [{
				text: "@@ Confirm",
				handler: function() {
					var name = me.nameField.getValue();
					var description = me.descriptionField.getValue();

					me.callDelegates("onSaveFilterWindowConfirm", [me, me.filter, name, description]);
				}
			}, {
				text: "@@ Abort",
				handler: function() {
					me.destroy();
				}
			}];

			this.callParent(arguments);
		}
	})
})();