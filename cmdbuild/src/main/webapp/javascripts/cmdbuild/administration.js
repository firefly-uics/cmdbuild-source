Ext.onReady(function() {
	Ext.QuickTips.init();
	CMDBuild.InitHeader();
	
	CMDBuild.identifiers = {};
	
	var splash = new CMDBuild.Splash('splashScreen','splashScreen');
	splash.setText(CMDBuild.Translation.common.loading_mask.configuration);
	splash.show();
	
	CMDBuild.ConcurrentAjax.execute({
		loadMask: false,
		requests: [{
			url: 'services/json/schema/setup/getconfiguration',
			params: { name: 'cmdbuild' },
			maskMsg: CMDBuild.Translation.common.loading_mask.configuration,
			success: function(response, options, decoded) {
				CMDBuild.Config.cmdbuild = decoded.data;
			}
		},{
            url: 'services/json/schema/setup/getconfiguration',
            params: { name: 'workflow' },
            success: function(response, options, decoded) {
                CMDBuild.Config.workflow = decoded.data;
                CMDBuild.Config.workflow.enabled = ('true' == CMDBuild.Config.workflow.enabled);
            }
        },{
            url: 'services/json/schema/setup/getconfiguration',
            params: { name: 'gis' },
            success: function(response, options, decoded) {
                splash.setText(CMDBuild.Translation.common.loading_mask.classes);
                CMDBuild.Config.gis = decoded.data;
                CMDBuild.Config.gis.enabled = ('true' == CMDBuild.Config.gis.enabled);
            }
        },{
			url: "services/json/schema/modclass/getallclasses",
			params: {
				active: false
			},
			maskMsg: CMDBuild.Translation.common.loading_mask.classes,
			success: function(response, options, decoded) {
				splash.setText(CMDBuild.Translation.common.loading_mask.classes);
				CMDBuild.Cache.setTables(decoded.classes);
			}
		},{
        	url: 'services/json/schema/modlookup/tree',
        	maskMsg: CMDBuild.Translation.common.loading_mask.lookup,
            success: function(response, options, decoded) {
				CMDBuild.Cache.setTables(decoded);
            }
        },{
        	url: 'services/json/schema/modreport/menutree',
        	maskMsg: CMDBuild.Translation.common.loading_mask.report,
            success: function(response, options, decoded) {
        		splash.setText(CMDBuild.Translation.common.loading_mask.menu);
        		CMDBuild.Cache.setTables(decoded);
            }
        },{
        	url: 'services/json/schema/modsecurity/getgrouplist',
        	maskMsg: CMDBuild.Translation.common.loading_mask.menu,
            success: function(response, options, decoded) {
        		CMDBuild.Cache.setTables(decoded.groups);
        		splash.setText(CMDBuild.Translation.common.loading_mask.group);
            }
        }],
		fn: function() {
			displayViewport();
		}
	});
	
	function displayViewport() {
		var domainTree = new CMDBuild.administration.domain.CMDomainAccordion({
			eventType: "domain",
			controllerType: "CMDomainAccordionController"
		});
		
		var modDomain = new CMDBuild.administration.domain.ModDomain();
		new CMDBuild.administration.domain.ModDomainController(modDomain, domainTree);

		CMDBuild.identifiers.accordion = {
			domain: domainTree.id
		};

		var viewport = new CMDBuild.MainViewport({
			colorsConst: CMDBuild.Constants.colors.gray,
			controllerType: "AdminViewportController",
			trees: [
				new CMDBuild.Administration.ClassTree({
					controllerType: "ClassTreePanelController"
				}),
				new CMDBuild.Administration.WorkflowTree({
					eventType: "processclass",
					controllerType: "WorkflowTreePanelController"
				}),
				domainTree,
				new CMDBuild.TreePanel({
					border: false,
					rootVisible: false,
					title: CMDBuild.Translation.administration.modLookup.lookupTypes,
					root: CMDBuild.TreeUtility.getTree(CMDBuild.Constants.cachedTableType.lookuptype,
							undefined, undefined, sorted=true),
					eventType: CMDBuild.Constants.cachedTableType.lookuptype,
					controllerType: "LookupTreePanelController"
				}),
				new CMDBuild.TreePanel({
					border: false,
					rootVisible: true,
					title: CMDBuild.Translation.administration.modreport.title,
					root: CMDBuild.TreeUtility.getTree(CMDBuild.Constants.cachedTableType.report,
							undefined, undefined, sorted=true),
					eventType: CMDBuild.Constants.cachedTableType.report
				}),
				new CMDBuild.Administration.MenuTree({
					eventType: CMDBuild.Constants.cachedTableType.group,
					controllerType: "MenuTreePanelController"
				}),
				new CMDBuild.Administration.SecurityTree({
					eventType: CMDBuild.Constants.cachedTableType.group,
					controllerType: "SecurityTreePanelController"
				}),
				new CMDBuild.Administration.GisTree({
					controllerType: "GisTreePanelController"
				}),
				new CMDBuild.Administration.SetupTree()
			],
			modules: [
				modDomain,
				new CMDBuild.Administration.ModClass(),
				new CMDBuild.Administration.ModWorkflow(),
				new CMDBuild.Administration.ModLookup(),
				new CMDBuild.Administration.ModReport(),
				new CMDBuild.Administration.ModMenu(),
				new CMDBuild.Administration.ModSecurity(),
				new CMDBuild.Administration.ModUser(),
				new CMDBuild.Administration.ModSetupGenericOption(),
				new CMDBuild.Administration.ModSetupGraph(),
				new CMDBuild.Administration.ModLegacydms(),
				new CMDBuild.Administration.ModIcons(),
				new CMDBuild.Administration.ModLayerOrder(),
				new CMDBuild.Administration.ModGeoServer(),
				new CMDBuild.Administration.ModExternalServices(),
				new CMDBuild.Administration.ModSetupServer(),
				new CMDBuild.Administration.ModSetupWorkflow(),
				new CMDBuild.Administration.ModSetupEmail(),
				new CMDBuild.Administration.ModSetupGis(),
				new CMDBuild.UnconfiguredModPanel()
			]
		});
		
		(function() {
		splash.hide();
		}).defer(500);
		
		var creditsLink = Ext.get('cmdbuild_credits_link');
		creditsLink.on('click', function(e) {
			splash.showAsPopUp();
		}, this);
		
		var instanceName = Ext.get('instance_name');
		instanceName.dom.innerHTML = CMDBuild.Config.cmdbuild.instance_name || "";
	}
});
