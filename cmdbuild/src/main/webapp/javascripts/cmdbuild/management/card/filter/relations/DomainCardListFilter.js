/**
 * This is the filter that allow you to filter the list of card in reletion
 * 
 * @class CMDBuild.Management.FilterRelationsDomainListFilter
 * @extends CMDBuild.Management.Attributes
 */
CMDBuild.Management.DomainCardListFilter = Ext.extend(CMDBuild.Management.Attributes, {
	//custom attributes
 	attributeList: {},
	idClass: -1,
	filterType: '',
	subfiltered: true,
	translation: CMDBuild.Translation.management.findfilter,
	title: '',
	initComponent: function() {
 		this.filterButton = new Ext.Button({
			text: this.translation.go_filter,
			iconCls: 'ok',
			handler: this.sendRequest,
			scope: this
		});
		
		CMDBuild.Management.DomainCardListFilter.superclass.initComponent.apply(this, arguments);
		
		this.on('beforedestroy', function(component) {
			component.unsubscribe('cmdb-init-domaincardlist', component.selectClass, this);
			var params = {};
			if(this.subfiltered){
				params = {
					FilterCategory: this.filterType,
					FilterSubcategory: this.ownerWindow.getId()
				}
			}
			CMDBuild.Ajax.request({
				url: 'services/json/management/modcard/resetcardfilter',
				method: 'POST',
				params: params
			});
		}, this);
	},
	
	selectClass: function(params) {
		this.attributeList = params.classAttributes;
		this.IdClass = params.classId;
		this.fillMenu();
		this.fieldsPanel.removeAll(true);
		this.fieldsPanel.doLayout();
		this.cleanFildsetCategory();
	},
	
	sendRequest: function() {
		var params = {};
		var filterParams = this.getForm().getValues();
		
		filterParams['IdClass'] =  this.IdClass;
    	
    	if (this.subfiltered) {
    		filterParams['FilterCategory'] = this.filterType;
    		filterParams['FilterSubcategory'] = this.ownerWindow.getId();
    	}
		
		for (key in filterParams) {
			//if(!this.subfiltered && (key != 'FilterSubcategory' && key != 'FilterCategory'))
			params[key] = filterParams[key];
		}
		
		CMDBuild.Ajax.request({
			url: this.url,
			params: params,
	   		method: 'POST',
			scope: this,		
			success: function(response) {
				this.fireEvent(CMDBuild.Management.Relations.FILTER_SUCCESS_EVENT_NAME);
			}
		});
    }
});