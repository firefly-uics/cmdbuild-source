(function() {

	var ICON_ACTION = "action-open-bim";

	Ext.define("CMDBuild.bim.management.CMBimController", {

		mixins: {
			cardGrid: "CMDBuild.view.management.common.CMCardGridDelegate"
		},

		constructor: function(view) {
			// this must be loaded with BIM configuration
			// before to initialize the application
			this.bimConfiguration = CMDBuild.Config.bim;
			this.rootClassName = this.bimConfiguration.rootClass;

			this.view = view;
			this.view.addDelegate(this);

			this.loginProxy = new BIMLoginProxy();

			this.bimWindow = null;
			this.bimSceneManager = null;
			this.viewportEventListener = null;
			this.roid = null;
		},

		// as CMDBuild.view.management.common.CMCardGridDelegate

		/**
		 * 
		 * @param {CMDBuild.view.management.common.CMCardGrid} grid
		 */
		onCMCardGridColumnsReconfigured: function(grid) {
			var entryType = _CMCardModuleState.entryType;
			if (entryType && entryType.getName() == this.rootClassName) {
				var column = Ext.create('Ext.grid.column.Column', {
					align: 'center',
					dataIndex: 'Id',
					fixed: true,
					header: '&nbsp',
					hideable: false,
					menuDisabled: true,
					renderer: renderBimIcon,
					sortable: false,
					width: 30
				});

				grid.headerCt.insert(grid.columns.length - 1, column);
				grid.getView().refresh();
			}
		},

		/**
		 * 
		 * @param {CMDBuild.view.management.common.CMCardGrid} grid
		 */
		onCMCardGridIconRowClick: function(grid, action, model) {
			if (action == ICON_ACTION) {
				var me = this;

				CMDBuild.bim.proxy.roidForCardId({
					params: {
						cardId: model.get("Id")
					},
					success: function(operation, options, response) {
						if (response.ROID) {
							startBIMPlayer(me, response.ROID);
						} else {
							CMDBuild.Msg.warn(
									CMDBuild.Translation.warnings.warning_message, //
									"@@ Questo edificio non ha un Progetto IFC associato"
							);
						}
					}
				});
			}
		}

	});

	function doLogin(me, callback) {
		var c = me.bimConfiguration;

		me.loginProxy.login({
			url: c.url,
			username: c.username,
			password: c.password,
			rememberMe: false,
			success: callback,
			failure: function() {
				CMDBuild.Msg.error("@@ ERRORE", "@@ Non sono riuscito a connettermi a BimServer", true);
			}
		});

	}

	function startBIMPlayer(me, roid) {
		// FIXME remove it
		window._BIM_LOGGER = console;

		/*
		 * Reuse the window if already
		 * open to this ROID
		 */
		if (me.roid == roid
				&& me.bimWindow != null) {

			me.bimWindow.show();
			return;
		}

		me.roid = roid;

		doLogin(me, function() {
			if (me.bimWindow == null) {
				me.bimWindow = new CMDBuild.bim.management.view.CMBimWindow();
			}
			me.bimWindow.show();

			if (me.bimSceneManager == null) {
				me.bimSceneManager = new BIMSceneManager({
					canvasId: me.bimWindow.CANVAS_ID,
					viewportId: me.bimWindow.getId(),
					progressBar: new BIMProgressBar()
				});

				me.viewportEventListener = new BIMViewportEventListener( //
						me.bimWindow.CANVAS_ID, //
						me.bimSceneManager //
				);
			}

			me.bimWindow.mon(me.bimWindow, "beforehide", function() {
				me.loginProxy.logout();
			});

			me.bimSceneManager.loadProjectWithRoid(me.roid);
		});
	}

	function renderBimIcon() {
		return '<img style="cursor:pointer" title="'
			+ "@@ Guarda il tuo edifiio in 3D"
			+'" class="' + ICON_ACTION + '" src="images/icons/application_home.png"/>';
	}
})();