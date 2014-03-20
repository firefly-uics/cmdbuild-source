<%
	final String locale = sessionVars.getLanguage();

	if ("it".equals(locale)) {
%>
	<script type="text/javascript" src="javascripts/cmdbuild/locale/it.js"></script>
<%
	} else {
%>
	<script type="text/javascript" src="javascripts/cmdbuild/locale/en.js"></script>
<%
	}
%>


<!-- MODELS -->
<!-- some model use the proxy constants, so import that at first -->
<script type="text/javascript" src="javascripts/cmdbuild/core/serviceProxy/ServiceProxyConstants.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/model/CMCacheModels.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/model/CMDomainModels.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/model/CMGroupModels.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/model/CMDashboardModels.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/model/CMAttachmentModels.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/model/CMDataViewModel.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/model/CMEmailTemplateModel.js"></script>


<!-- SELECTION -->
<script type="text/javascript" src="javascripts/cmdbuild/selection/CMMultiPageSelectionModel.js"></script>

<!--CHACHE -->
<script type="text/javascript" src="javascripts/cmdbuild/cache/CMCacheFilterFunctions.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/cache/CMCacheClassFunctions.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/cache/CMCacheReportFunctions.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/cache/CMCacheLookupFunctions.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/cache/CMCacheGroupsFunctions.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/cache/CMCacheDomainFunctions.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/cache/CMCacheDashboardFunctions.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/cache/CMCacheAttachmentCategoryFunctions.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/cache/CMCacheGISFunctions.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/cache/CMCache.js"></script>

<!--  THE OTHERS -->

<script type="text/javascript" src="javascripts/cmdbuild/core/CMLoadingBar.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/core/Msg.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/core/PopupWindow.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/core/LoginWindow.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/core/Ajax.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/core/Constants.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/core/CMDelegable.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/core/tree/TreeUtilities.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/core/Utils.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/core/xml/XMLUtility.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/core/buttons/Buttons.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/core/buttons/CMClassesMenuButton.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/core/buttons/AddCardMenuButton.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/core/buttons/AddRelationMenuButton.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/core/buttons/PrintMenuButton.js"></script>

<!-- FORM STUFF -->
<script type="text/javascript" src="javascripts/cmdbuild/form/CallbackPlugin.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/form/CustomVTypes.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/form/FormOverride.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/form/FormPlugin.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/form/HexColorField.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/form/RangeSlidersFieldSet.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/form/SetValueOnLoadPlugin.js"></script>

<!-- WIDGETS BUILDERS -->
<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/widgetBuilders/BaseAttribute.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/widgetBuilders/SimpleQueryAttribute.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/widgetBuilders/RangeQueryAttribute.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/widgetBuilders/TextualQueryAttribute.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/widgetBuilders/DecimalAttribute.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/widgetBuilders/DoubleAttribute.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/widgetBuilders/IntegerAttribute.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/widgetBuilders/IPAddressAttribute.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/widgetBuilders/DateAttribute.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/widgetBuilders/TimeStampAttribute.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/widgetBuilders/TimeAttribute.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/widgetBuilders/BooleanAttribute.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/widgetBuilders/ComboAttribute.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/widgetBuilders/LookupAttribute.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/widgetBuilders/ReferenceAttribute.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/widgetBuilders/ForeignKeyAttribute.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/widgetBuilders/StringAttribute.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/widgetBuilders/CharAttribute.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/widgetBuilders/CustomListAttribute.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/widgetBuilders/TextAttribute.js"></script>

<!-- CUSTOM FIELDS -->
<script type="text/javascript" src="javascripts/cmdbuild/view/common/field/CMBaseCombo.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/common/field/CMErasableCombo.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/common/field/CMGroupSelectionList.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/common/field/FieldManager.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/common/field/BooleanDisplayField.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/common/field/CMDisplayField.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/common/field/CMHtmlEditorField.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/common/field/LookupField.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/common/field/GridSearchField.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/common/field/LocaleSearchField.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/common/field/SearchableCombo.js"></script>	
<script type="text/javascript" src="javascripts/cmdbuild/view/common/field/CMToggleButtonToShowReferenceAttributes.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/common/field/ReferenceField.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/common/field/ForeignKeyField.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/form/IconsCombo.js"></script>

<!--  PROXYES -->
<script type="text/javascript" src="javascripts/cmdbuild/core/serviceProxy/ServiceProxy.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/core/serviceProxy/CMAdministrationProxy.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/core/serviceProxy/CMConfigurationProxy.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/core/serviceProxy/CMGISProxy.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/core/serviceProxy/CMIconProxy.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/core/serviceProxy/CMWidgetConfigurationProxy.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/core/serviceProxy/CMDashboardProxy.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/core/serviceProxy/CMAttachmentProxy.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/core/serviceProxy/CMFilterProxy.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/core/serviceProxy/CMDataViewProxy.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/core/serviceProxy/CMEmailTemplateProxy.js"></script>


<!-- DELEGATES -->
	<!-- COMMON -->
		<!-- FILTER -->
		<script type="text/javascript" src="javascripts/cmdbuild/delegate/common/filter/CMFilterMenuButtonDelegate.js"></script>

<!--  VIEWS -->
<script type="text/javascript" src="javascripts/cmdbuild/view/common/CMBaseAccordion.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/common/report/CMReportAccordion.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/common/report/CMReportGrid.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/common/CMUnconfiguredModPanel.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/common/CMFormFuncions.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/common/workflow/CMProcessAccordion.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/CMMainViewport.js"></script>

<!-- TODO: remove from management -->

<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/CMCardGrid.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/CMCardListWindow.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/CMReferenceSearchWindow.js"></script>

<!-- FILTER -->
<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/filter/CMFilterMenuButton.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/filter/CMFilterWindow.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/common/filter/CMFilterConfigurationWindow.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/filter/CMFilterAttribute.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/controller/management/common/CMCardGridController.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/filter/relations/CMDomainGrid.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/filter/CMFilterRelation.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/view/common/filter/CMFilterChooser.js"></script>

	<!-- DASHBOARD -->
	<script type="text/javascript" src="javascripts/cmdbuild/view/common/chart/CMChartPortletForm.js"></script>
	<script type="text/javascript" src="javascripts/cmdbuild/view/common/chart/CMChartPortlet.js"></script>	

<!--  CONTROLLERS -->
<script type="text/javascript" src="javascripts/cmdbuild/controller/accordion/CMBaseAccordionController.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/controller/common/CMBasePanelController.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/controller/common/CMMainViewportController.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/controller/common/CMUnconfiguredModPanelController.js"></script>

	<!-- DASHBOARD -->
<script type="text/javascript" src="javascripts/cmdbuild/controller/common/CMDashboardColumnController.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/controller/common/chart/CMChartPortletController.js"></script>

<%@ include file="bim.jsp"%>