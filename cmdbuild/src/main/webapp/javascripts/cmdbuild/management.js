Ext.onReady(function() {
	Ext.QuickTips.init();
	CMDBuild.InitHeader();
	
	var splash = new CMDBuild.Splash('splashScreen','splashScreen');
	splash.setText(CMDBuild.Translation.common.loading_mask.configuration);
	splash.show();
	
	CMDBuild.ConcurrentAjax.execute({
		loadMask: false,
		requests: [{
                url: 'services/json/schema/setup/getconfiguration',
                params: {
                    name: 'cmdbuild'
                },
                maskMsg: CMDBuild.Translation.common.loading_mask.configuration,
                success: function(response, options,
                        decoded) {
                    CMDBuild.Config.cmdbuild = decoded.data;
                }
            },{
                url: 'services/json/schema/setup/getconfiguration',
                params: {
                    name: 'legacydms'
                },
                maskMsg: CMDBuild.Translation.common.loading_mask.configuration,
                success: function(response, options,
                        decoded) {
                    CMDBuild.Config.dms = decoded.data;
                }
            },{
                url: 'services/json/schema/setup/getconfiguration',
                params: {
                    name: 'graph'
                },
                maskMsg: CMDBuild.Translation.common.loading_mask.configuration,
                success: function(response, options,
                        decoded) {
                    CMDBuild.Config.graph = decoded.data;
                }
            },{
                url: 'services/json/schema/setup/getconfiguration',
                params: {
                    name: 'gis'
                },
                success: function(response, options,
                        decoded) {
                    CMDBuild.Config.gis = decoded.data;
                    CMDBuild.Config.gis.enabled = ('true' == CMDBuild.Config.gis.enabled);
                }
            },{
                url: 'services/json/schema/setup/getconfiguration',
                params: {
                    name: 'workflow'
                },
                maskMsg: CMDBuild.Translation.common.loading_mask.configuration,
                success: function(response, options,
                        decoded) {
                    CMDBuild.Config.workflow = decoded.data;
                }
            },{
                url: "services/json/schema/modclass/getallclasses",
                params: {
                    active: true
                },
                maskMsg: CMDBuild.Translation.common.loading_mask.classes,
                success: function(response, options, decoded) {
                    splash.setText(CMDBuild.Translation.common.loading_mask.classes);
                    CMDBuild.Cache.setTables(decoded.classes);
                }
            },{
                url: 'services/json/schema/modmenu/getgroupmenu',
                success: function(response, options, decoded) {
                    if (decoded.length > 0) {
                        var itemsMap = CMDBuild.TreeUtility.arrayToMap(decoded);
                        CMDBuild.Cache.menuTree = CMDBuild.TreeUtility.buildTree(itemsMap, "menu", addAttributes = true);
                    }
                }
            },{
                url: 'services/json/management/modreport/getreporttypestree',
                success: function(response, options, decoded) {
                    CMDBuild.Cache.setTables(decoded);
                }
            }],
        fn: function() {
            displayViewport();
        }
	});
	
	function displayViewport() {
		var wfView = new CMDBuild.Management.ModWorkflow();
		var wfController = new CMDBuild.Management.WFController(wfView);
		
		var viewport = new CMDBuild.MainViewport({
			id: "management_main_viewport",
			trees: (function() {
				var trees = [];
				var structure = CMDBuild.Structure;				
				for (var tree in structure) {
					var t = structure[tree].createTree();
					if (t) {
						trees.push(t);
					}
				}
				return trees;
			})(),
			modules: [
			    new Ext.Panel({
			    	bodyCfg: {
				        cls: 'empty_panel x-panel-body'
				    }
			    }),
				new CMDBuild.Management.ModCard(),
				wfView,
				new CMDBuild.Management.ModReport(),
				new CMDBuild.Management.ModBulkCardUpdate(),
				new CMDBuild.Management.ModChangePassword(),
				new CMDBuild.Management.ModImportCSV(),
				new CMDBuild.Management.ModExportCSV()
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
