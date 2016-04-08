(function () {

	Ext.define('CMDBuild.core.proxy.index.Json', {

		singleton: true,

		attachment: {
			create: '',
			read: '',
			update: '',
			remove: 'services/json/attachments/deleteattachment',

			readAll: 'services/json/attachments/getattachmentlist',

			download: 'services/json/attachments/downloadattachment',
			getContext: 'services/json/attachments/getattachmentscontext'
		},

		attribute: {
			create: '', // TODO: waiting for refactor (crud)
			read: 'services/json/schema/modclass/getattributelist', // TODO: waiting for refactor (crud + rename)
			update: 'services/json/schema/modclass/saveattribute', // TODO: waiting for refactor (crud + rename)
			remove: 'services/json/schema/modclass/deleteattribute',

			readAll: 'services/json/schema/modclass/getattributelist', // TODO: waiting for refactor (crud + rename)
			readTypes: 'services/json/schema/modclass/getattributetypes', // TODO: waiting for refactor (rename name, description)
			readRenceableDomains: 'services/json/schema/modclass/getreferenceabledomainlist',

			sorting: {
				reorder: 'services/json/schema/modclass/reorderattribute',
				update: 'services/json/schema/modclass/saveordercriteria',
			}
		},

		bim: {
			create: 'services/json/bim/create',
			read: 'services/json/bim/read',
			update: 'services/json/bim/update',
			remove: '',

			enable: 'services/json/bim/enableproject',
			disable: 'services/json/bim/disableproject',
			readLayer: 'services/json/bim/readbimlayer',
			saveLayer: 'services/json/bim/savebimlayer',
			rootLayer: 'services/json/bim/rootclassname',
			roidForCardId: 'services/json/bim/getroidforcardid',
			importIfc: 'services/json/bim/importifc',
			downloadIfc: 'services/json/bim/download',
			activeForClassName: 'services/json/bim/getactiveforclassname',
			fetchCardFromViewewId: 'services/json/bim/fetchcardfromviewewid',
			fetchJsonForBimViewer: 'services/json/bim/fetchjsonforbimviewer'
		},

		card: {
			create: '', // TODO: waiting for refactor (crud)
			read: 'services/json/management/modcard/getcard',
			update: 'services/json/management/modcard/updatecard', // TODO: waiting for refactor (crud)
			remove: 'services/json/management/modcard/deletecard',

			bulkUpdate: 'services/json/management/modcard/bulkupdate',
			bulkUpdateFromFilter: 'services/json/management/modcard/bulkupdatefromfilter',
			getList: 'services/json/management/modcard/getcardlist',
			getListShort: 'services/json/management/modcard/getcardlistshort',
			getPosition: 'services/json/management/modcard/getcardposition',
			getSqlCardList: 'services/json/management/modcard/getsqlcardlist'
		},

		classes: {
			create: 'services/json/schema/modclass/savetable', // TODO: waiting for refactor (crud)
			read: 'services/json/schema/modclass/getallclasses', // TODO: waiting for refactor (crud)
			update: 'services/json/schema/modclass/savetable', // TODO: waiting for refactor (crud)
			remove: 'services/json/schema/modclass/deletetable',

			readAll: 'services/json/schema/modclass/getallclasses',
			foreignKeyTargetClass: 'services/json/schema/modclass/getfktargetingclass',

			cards: {
				lock: 'services/json/lock/lockcard',
				unlock: 'services/json/lock/unlockcard',
				unlockAll: 'services/json/lock/unlockall'
			}
		},

		configuration: {
			create: '',
			read: 'services/json/schema/setup/getconfiguration',
			update: 'services/json/schema/setup/saveconfiguration',
			remove: '',

			apply: 'services/json/configure/apply',
			connectionTest: 'services/json/configure/testconnection',
			readAll: 'services/json/schema/setup/getconfigurations',

			dms: {
				getPresets: 'services/json/attachments/getpresets'
			}
		},

		csv: {
			clearSession: 'services/json/management/importcsv/clearsession',
			exports: 'services/json/management/exportcsv/writecsv',
			getRecords: 'services/json/management/importcsv/getcsvrecords',
			read: 'services/json/management/importcsv/readcsv',
			storeRecords: 'services/json/management/importcsv/storecsvrecords',
			updateRecords: 'services/json/management/importcsv/updatecsvrecords',
			upload: 'services/json/management/importcsv/uploadcsv'
		},

		customPage: {
			create: '',
			read: '',
			update: '',
			remove: '',

			readAll: '',
			readForCurrentUser: 'services/json/custompages/readforcurrentuser'
		},

		dashboard: {
			create: 'services/json/dashboard/add',
			read: '',
			update: 'services/json/dashboard/modifybaseproperties',
			remove: 'services/json/dashboard/remove',

			readAll: 'services/json/dashboard/fulllist', // fullList

			chart: {
				create: 'services/json/dashboard/addchart',
				read: 'services/json/dashboard/getchartdata',
				update: 'services/json/dashboard/modifychart',
				remove: 'services/json/dashboard/removechart',

				readForPreview: 'services/json/dashboard/getchartdataforpreview'
			},

			columns: {
				create: '',
				read: '',
				update: 'services/json/dashboard/modifycolumns',
				remove: '',
			}
		},

		dataView: {
			filter: {
				create: 'services/json/viewmanagement/createfilterview',
				read: 'services/json/viewmanagement/readfilterview', // TODO: waiting for refactor (crud)
				update: 'services/json/viewmanagement/updatefilterview',
				remove: 'services/json/viewmanagement/deletefilterview',

				readAll: 'services/json/viewmanagement/readfilterview' // TODO: waiting for refactor (crud)
			},

			sql: {
				create: 'services/json/viewmanagement/createsqlview',
				read: 'services/json/viewmanagement/readsqlview', // TODO: waiting for refactor (crud)
				update: 'services/json/viewmanagement/updatesqlview',
				remove: 'services/json/viewmanagement/deletesqlview',

				readAll: 'services/json/viewmanagement/readsqlview' // TODO: waiting for refactor (crud)
			},

			readAll: 'services/json/viewmanagement/read' // TODO: waiting for refactor (rename on server)
		},

		domain: {
			create: 'services/json/schema/modclass/savedomain', // TODO: waiting for refactor (crud)
			read: 'services/json/schema/modclass/getalldomains', // TODO: waiting for refactor (crud)
			update: 'services/json/schema/modclass/savedomain', // TODO: waiting for refactor (crud)
			remove: 'services/json/schema/modclass/deletedomain',

			readAll: 'services/json/schema/modclass/getalldomains',
			getList: 'services/json/schema/modclass/getdomainlist'
		},

		email: {
			accounts:{
				create: 'services/json/schema/emailaccount/post',
				read: 'services/json/schema/emailaccount/get',
				update: 'services/json/schema/emailaccount/put',
				remove: 'services/json/schema/emailaccount/delete',

				readAll: 'services/json/schema/emailaccount/getall',
				setDefault: 'services/json/schema/emailaccount/setdefault'
			},

			attachment: {
				copy: 'services/json/email/attachment/copy',
				download: 'services/json/email/attachment/download',
				readAll: 'services/json/email/attachment/readall',
				remove: 'services/json/email/attachment/delete',
				upload: 'services/json/email/attachment/upload'
			},

			queue: {
				configuration: 'services/json/email/queue/configuration',
				configure: 'services/json/email/queue/configure',
				running: 'services/json/email/queue/running',
				start: 'services/json/email/queue/start',
				stop: 'services/json/email/queue/stop'
			},

			templates:{
				create: 'services/json/email/template/create',
				read: 'services/json/email/template/read',
				update: 'services/json/email/template/update',
				remove: 'services/json/email/template/delete',

				readAll: 'services/json/email/template/readall'
			},

			remove: 'services/json/email/email/delete',
			get: 'services/json/email/email/read',
			post: 'services/json/email/email/create',
			put: 'services/json/email/email/update',

			enabled: 'services/json/email/email/enabled',
			getStore: 'services/json/email/email/readall'
		},

		filter: {
			group: {
				create: 'services/json/filter/create',
				read: 'services/json/filter/readallgroupfilters', // TODO: waiting for refactor (CRUD)
				remove: 'services/json/filter/delete',
				update: 'services/json/filter/update',

				readAll: 'services/json/filter/readallgroupfilters',

				defaults: {
					read: 'services/json/filter/getgroups',
					update: 'services/json/filter/setdefault'
				}
			},

			user: {
				create: '',
				read: 'services/json/filter/read',
				remove: '',
				update: '',

				readAll: 'services/json/filter/readforuser'
			}
		},

		functions: {
			create: '',
			read: '',
			remove: '',
			update: '',

			readAll: 'services/json/schema/modclass/getfunctions',
			readCards: 'services/json/management/modcard/getsqlcardlist'
		},

		gis: {
			geoAttribute: {
				create: 'services/json/gis/addgeoattribute',
				read: '',
				remove: 'services/json/gis/deletegeoattribute',
				update: 'services/json/gis/modifygeoattribute'
			},

			geoServer: {
				layer: {
					create: 'services/json/gis/addgeoserverlayer',
					read: '',
					remove: 'services/json/gis/deletegeoserverlayer',
					update: 'services/json/gis/modifygeoserverlayer',

					readAll: 'services/json/gis/getgeoserverlayers'
				}
			},

			icons: {
				create: 'services/json/icon/upload',
				read: '',
				remove: 'services/json/icon/remove',
				update: 'services/json/icon/update',

				readAll: 'services/json/icon/list'
			},

			treeNavigation: {
				create: '', // TODO: waiting for refactor (CRUD)
				read: 'services/json/gis/getgistreenavigation',
				remove: 'services/json/gis/removegistreenavigation',
				update: 'services/json/gis/savegistreenavigation' // TODO: waiting for refactor (CRUD)
			},

			expandDomainTree: 'services/json/gis/expanddomaintree',
			getFeatures: 'services/json/gis/getfeature',
			getGeoCardList: 'services/json/gis/getgeocardlist',
			readAllLayers: 'services/json/gis/getalllayers',
			setLayerOrder: 'services/json/gis/setlayersorder',
			setLayerVisibility: 'services/json/gis/setlayervisibility'
		},

		group: {
			create: 'services/json/schema/modsecurity/savegroup', // TODO: waiting for refactor (crud)
			read: 'services/json/schema/modsecurity/getgrouplist', // TODO: waiting for refactor (crud)
			remove: '',
			update: 'services/json/schema/modsecurity/savegroup', // TODO: waiting for refactor (crud)

			enableDisableGroup: 'services/json/schema/modsecurity/enabledisablegroup',
			getUiConfiguration: 'services/json/schema/modsecurity/getuiconfiguration',
			readAll: 'services/json/schema/modsecurity/getgrouplist',

			defaultFilters: {
				read: 'services/json/filter/getdefault',
				update: 'services/json/filter/setdefault',

				readAllGroupFilters: 'services/json/filter/readallgroupfilters'
			},

			users: {
				getGroupUserList: 'services/json/schema/modsecurity/getgroupuserlist',
				saveGroupUserList: 'services/json/schema/modsecurity/savegroupuserlist'
			},

			userInterface: {
				getGroupUiConfiguration: 'services/json/schema/modsecurity/getgroupuiconfiguration',
				saveGroupUiConfiguration: 'services/json/schema/modsecurity/savegroupuiconfiguration'
			}
		},

		history: { // TODO: waiting for refactor (different endpoints)
			classes: {
				getCardHistory: 'services/json/management/modcard/getcardhistory',
				getHistoricCard: 'services/json/management/modcard/gethistoriccard',
				getRelationsHistory: 'services/json/management/modcard/getrelationshistory',
				getHistoricRelation: 'services/json/management/modcard/gethistoricrelation'
			},
			workflow: {
				getWorkflowHistory: 'services/json/management/modcard/getprocesshistory',
				getHistoricWorkflow: 'services/json/management/modcard/gethistoriccard',
				getRelationsHistory: 'services/json/management/modcard/getrelationshistory',
				getHistoricRelation: 'services/json/management/modcard/gethistoricrelation'
			}
		},

		localizations: {
			translation: {
				create: '',
				read: 'services/json/schema/translation/read',
				remove: '',
				update: 'services/json/schema/translation/update',

				readAll: 'services/json/schema/translation/readall'
			},
			importExport: {
				exportCsv: 'services/json/schema/translation/exportcsv',
				importCsv: 'services/json/schema/translation/importcsv'
			}
		},

		lookup: {
			create: 'services/json/schema/modlookup/savelookup', // TODO: waiting for refactor (crud)
			read: 'services/json/schema/modlookup/getlookuplist', // TODO: waiting for refactor (crud)
			remove: '',
			update: 'services/json/schema/modlookup/savelookup', // TODO: waiting for refactor (crud)

			disable: 'services/json/schema/modlookup/disablelookup',
			enable: 'services/json/schema/modlookup/enablelookup',
			getParentList: 'services/json/schema/modlookup/getparentlist',
			readAll: 'services/json/schema/modlookup/getlookuplist',
			setOrder: 'services/json/schema/modlookup/reorderlookup',

			type: {
				create: 'services/json/schema/modlookup/savelookuptype', // TODO: waiting for refactor (crud)
				read: 'services/json/schema/modlookup/tree', // TODO: waiting for refactor (crud)
				remove: '',
				update: 'services/json/schema/modlookup/savelookuptype', // TODO: waiting for refactor (crud)

				readAll: 'services/json/schema/modlookup/tree'
			}
		},

		menu: {
			create: '', // TODO: waiting for refactor (crud)
			read: 'services/json/schema/modmenu/getassignedmenu',
			update: 'services/json/schema/modmenu/savemenu', // TODO: waiting for refactor (crud)
			remove: 'services/json/schema/modmenu/deletemenu',

			readAvailableItems: 'services/json/schema/modmenu/getavailablemenuitems',
			readConfiguration: 'services/json/schema/modmenu/getmenuconfiguration'
		},

		navigationTree: {
			create: 'services/json/navigationtree/create',
			read: 'services/json/navigationtree/read',
			update: 'services/json/navigationtree/save', // TODO: waiting for refactor (rename update)
			remove: 'services/json/navigationtree/remove',

			readAll: 'services/json/navigationtree/get' // TODO: waiting for refactor (rename readAll)
		},

		patchManager: {
			update: 'services/json/configure/applypatches',

			readAll: 'services/json/configure/getpatches'
		},

		privileges: {
			classes: {
				read: 'services/json/schema/modsecurity/getclassprivilegelist',
				update: 'services/json/schema/modsecurity/saveclassprivilege',

				setRowAndColumnPrivileges: 'services/json/schema/modsecurity/setrowandcolumnprivileges',
				loadClassUiConfiguration: 'services/json/schema/modsecurity/loadclassuiconfiguration',
				saveClassUiConfiguration: 'services/json/schema/modsecurity/saveclassuiconfiguration'
			},
			customPages: {
				read: 'services/json/schema/modsecurity/getcustompageprivilegelist',
				update: 'services/json/schema/modsecurity/savecustompageprivilege'
			},
			dataView: {
				read: 'services/json/schema/modsecurity/getviewprivilegelist',
				update: 'services/json/schema/modsecurity/saveviewprivilege'
			},
			filter: {
				read: 'services/json/schema/modsecurity/getfilterprivilegelist',
				update: 'services/json/schema/modsecurity/savefilterprivilege'
			},
			workflow: {
				read: 'services/json/schema/modsecurity/getprocessprivilegelist',
				update: 'services/json/schema/modsecurity/saveprocessprivilege',

				setRowAndColumnPrivileges: 'services/json/schema/modsecurity/setrowandcolumnprivileges'
			},
		},

		relations: {
			create: 'services/json/management/modcard/createrelations',
			read: '',
			update: 'services/json/management/modcard/modifyrelation',
			remove: 'services/json/management/modcard/deleterelation',

			getAlreadyRelatedCards: 'services/json/management/modcard/getalreadyrelatedcards',
			readAll: 'services/json/management/modcard/getrelationlist',
			removeDetail: 'services/json/management/modcard/deletedetail'
		},

		report: {
			createReportFactory: 'services/json/management/modreport/createreportfactory',
			createReportFactoryByTypeCode: 'services/json/management/modreport/createreportfactorybytypecode',
			getReportsByType: 'services/json/management/modreport/getreportsbytype',
			getReportTypesTree: 'services/json/management/modreport/getreporttypestree',
			menuTree: 'services/json/schema/modreport/menutree',
			printReportFactory: 'services/json/management/modreport/printreportfactory',
			updateReportFactoryParams: 'services/json/management/modreport/updatereportfactoryparams',

			jasper: {
				create: 'services/json/management/modreport/createreportfactory',
				read: '',
				update: '',
				remove: 'services/json/schema/modreport/deletereport',
				save: 'services/json/schema/modreport/savejasperreport',

				analyze: 'services/json/schema/modreport/analyzejasperreport',
				getReportsByType: 'services/json/management/modreport/getreportsbytype',
				import: 'services/json/schema/modreport/importjasperreport',
				resetSession: 'services/json/schema/modreport/resetsession'
			},

			print: {
				cardDetails: 'services/json/management/modreport/printcarddetails',
				classSchema: 'services/json/schema/modreport/printclassschema',
				currentView: 'services/json/management/modreport/printcurrentview',
				schema: 'services/json/schema/modreport/printschema',
				sqlView: 'services/json/management/modreport/printsqlview'
			}
		},

		session: {
			jsonRpc: {
				login: 'services/json/login/login',
				logout: 'services/json/login/logout'
			},
			rest: 'services/rest/v2/sessions',
		},

		tasks: {
			getStore: 'services/json/schema/taskmanager/readall',
			start: 'services/json/schema/taskmanager/start',
			stop: 'services/json/schema/taskmanager/stop',

			connector: {
				create: 'services/json/schema/taskmanager/connector/create',
				read: 'services/json/schema/taskmanager/connector/read',
				update: 'services/json/schema/taskmanager/connector/update',
				remove: 'services/json/schema/taskmanager/connector/delete',

				getStore: 'services/json/schema/taskmanager/connector/readall',
				getSqlSources: 'services/json/schema/taskmanager/connector/availablesqlsources'
			},

			email: {
				create: 'services/json/schema/taskmanager/reademail/create',
				read: 'services/json/schema/taskmanager/reademail/read',
				update: 'services/json/schema/taskmanager/reademail/update',
				remove: 'services/json/schema/taskmanager/reademail/delete',

				getStore: 'services/json/schema/taskmanager/reademail/readall'
			},

			event: {
				getStore: 'services/json/schema/taskmanager/event/readall',

				asynchronous: {
					create: 'services/json/schema/taskmanager/event/asynchronous/create',
					read: 'services/json/schema/taskmanager/event/asynchronous/read',
					update: 'services/json/schema/taskmanager/event/asynchronous/update',
					remove: 'services/json/schema/taskmanager/event/asynchronous/delete',

					getStore: 'services/json/schema/taskmanager/event/asynchronous/readall'
				},

				synchronous: {
					create: 'services/json/schema/taskmanager/event/synchronous/create',
					read: 'services/json/schema/taskmanager/event/synchronous/read',
					update: 'services/json/schema/taskmanager/event/synchronous/update',
					remove: 'services/json/schema/taskmanager/event/synchronous/delete',

					getStore: 'services/json/schema/taskmanager/event/synchronous/readall'
				}
			},

			workflow: {
				create: 'services/json/schema/taskmanager/startworkflow/create',
				read: 'services/json/schema/taskmanager/startworkflow/read',
				update: 'services/json/schema/taskmanager/startworkflow/update',
				remove: 'services/json/schema/taskmanager/startworkflow/delete',

				getStore: 'services/json/schema/taskmanager/startworkflow/readall',
				getStoreByWorkflow: 'services/json/schema/taskmanager/startworkflow/readallbyworkflow'
			}
		},

		user: {
			create: 'services/json/schema/modsecurity/saveuser', // TODO: waiting for refactor (crud)
			read: 'services/json/schema/modsecurity/getgrouplist', // TODO: waiting for refactor (crud)
			remove: '',
			update: 'services/json/schema/modsecurity/saveuser', // TODO: waiting for refactor (crud)

			disable: 'services/json/schema/modsecurity/disableuser',
			getGroupList: 'services/json/schema/modsecurity/getusergrouplist',
			readAll: 'services/json/schema/modsecurity/getuserlist'
		},

		utils: {
			clearCache: 'services/json/utils/clearcache',
			generateId: 'services/json/utils/generateid',
			getLanguage: 'services/json/utils/getlanguage',
			listAvailableTranslations: 'services/json/utils/listavailabletranslations'
		},

		widget: {
			create: 'services/json/widget/create',
			read: 'services/json/widget/read',
			update: 'services/json/widget/update',
			remove: 'services/json/widget/delete',

			readAll: 'services/json/widget/readall',
			readAllForClass: 'services/json/widget/readallforclass',

			// Widgets end-points
			ping: 'services/json/widget/callwidget',
			webService: 'services/json/widget/callwidget'
		},

		workflow: {
			create: 'services/json/schema/modclass/savetable', // TODO: waiting for refactor (crud)
			read: 'services/json/schema/modclass/getallclasses', // TODO: waiting for refactor (crud)
			update: 'services/json/schema/modclass/savetable', // TODO: waiting for refactor (crud)
			remove: 'services/json/schema/modclass/deletetable',

			readAll: 'services/json/schema/modclass/getallclasses', // TODO: waiting for refactor (crud)

			isProcessUpdated: 'services/json/workflow/isprocessupdated',
			synchronize: 'services/json/workflow/sync',

			activity: {
				create: '', // TODO: waiting for refactor (crud)
				read: 'services/json/workflow/getactivityinstance', // TODO: waiting for refactor (crud)
				update: 'services/json/workflow/saveactivity', // TODO: waiting for refactor (crud)
				remove: '',

				readStart: 'services/json/workflow/getstartactivity',

				abort: 'services/json/workflow/abortprocess',
				lock: 'services/json/lock/lockactivity',
				unlock: 'services/json/lock/unlockactivity'
			},

			xpdl: {
				download: 'services/json/workflow/downloadxpdl',
				downloadTemplate: 'services/json/workflow/downloadxpdltemplate',
				upload: 'services/json/workflow/uploadxpdl',
				versions: 'services/json/workflow/xpdlversions'
			}
		}
	});

})();
