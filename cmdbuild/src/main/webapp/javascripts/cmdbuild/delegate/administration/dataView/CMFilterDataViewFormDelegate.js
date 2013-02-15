Ext.define("CMDBuild.delegate.administration.common.dataview.CMFilterDataViewFormDelegate", {
	/**
	 * 
	 * @param {CMDBuild.view.administration.common.CMFilterDataViewFormFiledsBuilder} builder
	 * the builder that call this method
	 * @param {string} className
	 * the id of the selected class
	 */
	onFilterDataViewFormBuilderClassSelected: function(builder, className) {},

	/**
	 * 
	 * @param {CMDBuild.view.administration.common.CMFilterDataViewFormFiledsBuilder} builder
	 * the builder that call this method
	 */
	onFilterDataViewFormBuilderAddFilterButtonClick: function(builder) {},

	/**
	 * 
	 * @param {CMDBuild.view.administration.common.CMFilterDataViewFormFiledsBuilder} builder
	 * the builder that call this method
	 * @param {Ext.grid.Panel} grid
	 * the filter grid
	 * @param {Ext.data.Model} record
	 * the record that holds the filter data
	 */
	onFilterDataViewFormBuilderFilterSelected: function(builder, grid, record) {}
});