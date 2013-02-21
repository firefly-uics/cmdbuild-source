(function() {

	Ext.define("CMDBuild.controller.administration.dataview.CMFilerDataViewController", {
		extend: "CMDBuild.controller.CMBasePanelController",
	
		mixins: {
			filterWindowDelegate: "CMDBuild.view.management.common.filter.CMFilterWindowDelegate",
			gridFormPanelDelegate: "CMDBuild.delegate.administration.common.basepanel.CMGridAndFormPanelDelegate",
			saveFilterWindow: "CMDBuild.view.management.common.filter.CMSaveFilterWindowDelegate",
			specificFilterFormDelegate: "CMDBuild.delegate.administration.common.dataview.CMFilterDataViewFormDelegate"
		},
	
		constructor: function(view) {
			this.callParent(arguments);
			this.mixins.gridFormPanelDelegate.constructor.call(this, view);
	
			this.fieldManager = null;
			this.gridConfigurator = null;
			this.className = null;
			this.record = null;
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
		},
	
		/**
		 * 
		 * @param {CMDBuild.view.administration.common.CMFilterDataViewFormFiledsBuilder} builder
		 * the builder that call this method
		 */
		onFilterDataViewFormBuilderAddFilterButtonClick: function(builder) {
			if (this.className) {
				var entryType = _CMCache.getEntryTypeByName(this.className);
				var me = this;
				_CMCache.getAttributeList(entryType.getId(), function(attributes) {
					var filter = new CMDBuild.model.CMFilterModel({
						entryType: entryType,
						local: true,
						name: CMDBuild.Translation.management.findfilter.newfilter + " " + _CMUtils.nextId()
					});
	
					var filterWindow = new CMDBuild.view.common.filter.CMFilterConfigurationWindow({
						filter: filter,
						attributes: attributes,
						className: entryType.getName()
					});
	
					filterWindow.addDelegate(me);
					filterWindow.show();
				});
	
			}
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
		},
	
		// as filterWindowDelegate
	
		/**
		 * @params {CMDBuild.view.management.common.filter.CMFilterWindow} filterWindow
		 * The filter window that call the delegate
		 */
		onCMFilterWindowApplyButtonClick: function() {
			// does not manage this here
		},
	
		/**
		 * @params {CMDBuild.view.management.common.filter.CMFilterWindow} filterWindow
		 * The filter window that call the delegate
		 */
		onCMFilterWindowSaveButtonClick: function(filterWindow, filter) {
			showSaveFilterDialog(this, filter, filterWindow);
		},
	
		/**
		 * @params {CMDBuild.view.management.common.filter.CMFilterWindow} filterWindow
		 * The filter window that call the delegate
		 */
		onCMFilterWindowSaveAndApplyButtonClick: function() {
			// does not manage this here
		},
	
		/**
		 * @params {CMDBuild.view.management.common.filter.CMFilterWindow} filterWindow
		 * The filter window that call the delegate
		 */
		onCMFilterWindowAbortButtonClick: function(filterWindow) {
			filterWindow.destroy();
		},

		// as saveFilterWindow

		/**
		 * @param {CMDBuild.view.management.common.filter.CMSaveFilterWindow} window
		 * the window that calls the delegate
		 * @param {CMDBuild.model.CMFilterModel} filter
		 * the filter to save
		 * @param {String} name
		 * the name set in the form
		 * @param {String} the description set in the form
		 */
		onSaveFilterWindowConfirm: function(saveFilterWindow, filter, name, description) {
			var me = this;

			filter.setName(name);
			filter.setDescription(description);
			filter.commit();

			_CMProxy.Filter.create(filter, {
				success: function onSuccess(request, configuration, response) {
					saveFilterWindow.referredFilterWindow.destroy(); // ugly but time is short
					saveFilterWindow.destroy();

					var newFilter = new CMDBuild.model.CMFilterModel(response.filter); 
					// looking for the position to load the right store page.
					_CMProxy.Filter.position(newFilter, {
						success: function filterPositionSuccess(request, configuration, response) {
							var position = response.position;
							var	pageNumber = _CMUtils.grid.getPageNumber(position);

							var store = me.fieldManager.filterStore;

							// store.loadPage does not allow the definition of a callBack
							store.on("load", function() {
								me.fieldManager.selectFilter(newFilter);
							}, null, {
								single: true
							});

							store.loadPage(Math.floor(pageNumber));
						}
					});

				}
			});
		}
	});

	function showSaveFilterDialog(me, filter, referredFilterWindow) {
		var saveFilterWindow = new CMDBuild.view.management.common.filter.CMSaveFilterWindow({
			filter: filter,
			referredFilterWindow: referredFilterWindow
		});

		saveFilterWindow.addDelegate(me);
		saveFilterWindow.show();
	}
})();