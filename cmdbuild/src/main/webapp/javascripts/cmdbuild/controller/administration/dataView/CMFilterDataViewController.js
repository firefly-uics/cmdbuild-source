Ext.define("CMDBuild.controller.administration.dataview.CMFilerDataViewController", {
	extend: "CMDBuild.controller.CMBasePanelController",

	mixins: {
		gridFormPanelDelegate: "CMDBuild.delegate.administration.common.basepanel.CMGridAndFormPanelDelegate",
		specificFilterFormDelegate: "CMDBuild.delegate.administration.common.dataview.CMFilterDataViewFormDelegate"
	},

	constructor: function(view) {
		this.mixins.gridFormPanelDelegate.constructor.call(this, view);
		this.fieldManager = null;
		this.gridConfigurator = null;
		this.className = null;
		this.record = null;

		this.callParent(arguments);
	},

	onViewOnFront: function(selection) {
		if (this.fieldManager == null) {
			this.fieldManager = new CMDBuild.delegate.administration.common.dataview.CMFilterDataViewFormFieldsManager();
			this.fieldManager.addDelegate(this);
			this.view.buildFields(this.fieldManager);
			this.view.disableModify();
		}

		if (this.gridConfigurator == null) {
			this.gridConfigurator = new CMDBuild.delegate.administration.common.dataview.CMFilterDataViewGridConfigurator();
			this.view.configureGrid(this.gridConfigurator);
		}
	},

	// as specificFilterFormDelegate

	/**
	 * 
	 * @param {CMDBuild.view.administration.common.CMFilterDataViewFormFiledsBuilder} builder
	 * the builder that call this method
	 * @param {string} className
	 * the name of the selected class
	 */
	onFilterDataViewFormBuilderClassSelected: function(builder, className) {
		if (className) {
			this.className = className;
		}

		_debug("onFilterDataViewFormBuilderClassSelected", builder, className);
	},

	/**
	 * 
	 * @param {CMDBuild.view.administration.common.CMFilterDataViewFormFiledsBuilder} builder
	 * the builder that call this method
	 */
	onFilterDataViewFormBuilderAddFilterButtonClick: function(builder) {
		if (this.className) {
			var entryType = _CMCache.getEntryTypeByName(this.className);
			_CMCache.getAttributeList(entryType.getId(), function(attributes) {
				var filter = new CMDBuild.model.CMFilterModel({
					entryType: entryType,
					local: true,
					name: CMDBuild.Translation.management.findfilter.newfilter + " " + _CMUtils.nextId()
				});

				var filterWindow = new CMDBuild.view.management.common.filter.CMFilterWindow({
					filter: filter,
					attributes: attributes,
					className: entryType.getName()
				});

//					filterWindow.addDelegate(this);
				filterWindow.show();
			});

		}
		_debug("onFilterDataViewFormBuilderAddFilterButtonClick", builder);
	},

	/**
	 * 
	 * @param {CMDBuild.view.administration.common.CMFilterDataViewFormFiledsBuilder} builder
	 * the builder that call this method
	 * @param {Ext.grid.Panel} grid
	 * the filter grid
	 * @param {Ext.data.Model} record
	 * the record that holds the filter data
	 */
	onFilterDataViewFormBuilderFilterSelected: function(builder, grid, record) {
		_debug("onFilterDataViewFormBuilderFilterSelected", builder, grid, record);
	}
});