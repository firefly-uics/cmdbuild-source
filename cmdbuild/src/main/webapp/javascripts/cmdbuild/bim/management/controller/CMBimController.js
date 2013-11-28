(function() {

	var ICON_ACTION = "action-open-bim";
	var MAX_ZOOM = 15;

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
			this.currentObjectId = null;
			this.roid = null;
		},

		/* ******************************************************
		 *  as CMDBuild.view.management.common.CMCardGridDelegate
		 * ******************************************************* */

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
									CMDBuild.Translation.no_bim_project_for_card
							);
						}
					}
				});
			}
		},

		/* **************************************************************
		 * As scene manager delegate
		 *  **************************************************************/

		sceneLoaded: function(sceneManager, scene) {
			if (this.bimWindow != null) {
				var sceneData = scene.data();
				var ifcTypes = sceneData.ifcTypes;
				var data = [];
	
				for (var i=0, l=ifcTypes.length; i<l; ++i) {
					var ifcType = ifcTypes[i];

					data.push({
						description: ifcType.substring(3), // remove the ifc prefix from the label
						id: ifcType,
						checked: false
					});
				}

				this.bimWindow.loadLayers(data);
				this.bimWindow.resetControls();
			}
		},

		layerDisplayed: function(sceneManager, layerName) {
			if (this.bimWindow) {
				this.bimWindow.selectLayer(layerName);
			}
		},

		objectSelected: function(sceneManager, objectId) {
			this.currentObjectId = objectId;
			_debug("Object selected", objectId);
			if (this.bimWindow) {
				this.bimWindow.enableObjectSliders();
			}
		},

		objectSelectedForLongPressure: function(sceneManager, objectId) {
			var me = this;

			CMDBuild.bim.proxy.fetchCardFromViewewId({
				params: {
					revisionId: me.roid,
					objectId: objectId
				},

				success: function(fp, request, response) {
					if (response.card) {
						openCardDataWindow(me, response.card);
					}
				}
			});
		},

		selectionCleaned: function() {
			this.currentObjectId = null;
			if (this.bimWindow) {
				this.bimWindow.disableObjectSliders();
			}
		},

		/* **************************************************************
		 * As CMBimWindow delegate
		 *  **************************************************************/

		/*
		 * @param {CMDBuild.bim.management.view.CMBimPlayerLayers} bimLayerPanel
		 * the layers panel that call the method
		 * @param {String} ifcLayerName
		 * the name of the layer for which the check is changed
		 * @param {Boolean} checked
		 * the current value of the check
		 */
		onLayerCheckDidChange: function(bimLayerPanel, ifcLayerName, checked) {
			if (checked) {
				this.bimSceneManager.showLayer(ifcLayerName);
			} else {
				this.bimSceneManager.hideLayer(ifcLayerName);
			}
		},

		/* **************************************************************
		 * As CMBimControlPanel delegate
		 * **************************************************************/

		onBimControlPanelResetButtonClick: function() {
			this.bimSceneManager.defaultView();
		},

		onBimControlPanelFrontButtonClick: function() {
			this.bimSceneManager.frontView();
		},

		onBimControlPanelSideButtonClick: function() {
			this.bimSceneManager.sideView();
		},

		onBimControlPanelTopButtonClick: function() {
			this.bimSceneManager.topView();
		},

		onBimControlPanelPanButtonClick: function() {
			this.bimSceneManager.togglePanRotate();
		},

		onBimControlPanelRotateButtonClick: function() {
			this.bimSceneManager.togglePanRotate();
		},

		/**
		 * @param {Number} value
		 * the current value of the slider
		 */
		onBimControlPanelZoomSliderChange: function(value) {
			var zoom = MAX_ZOOM - (value/5);
			this.bimSceneManager.setZoomLevel(zoom);
		},

		/**
		 * @param {Number} value
		 * the current value of the slider
		 */
		onBimControlPanelExposeSliderChange: function(value) {
			this.bimSceneManager.exposeNodeWithItsStorey(this.currentObjectId, (value/2.5));
		},

		/**
		 * @param {Number} value
		 * the current value of the slider
		 */
		onBimControlPanelTransparentSliderChange: function(value) {
			var factor = 100 - value;
			this.bimSceneManager.setNodeTransparentLevel(this.currentObjectId, factor);
		},

		// CMCardDataWinodwDelegate

		/**
		 * @param {CMDBuild.bim.view.CMCardDataWindow} cardDataWindow
		 */
		cardDataWindowOpenCardButtonWasClicked: function(cardDataWindow) {
			var cardData = cardDataWindow.cmCardData;
			cardDataWindow.destroy();
			this.bimWindow.hide();

			_CMMainViewportController.openCard({
				Id: cardData.Id,
				IdClass: cardData.IdClass
			});
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
				CMDBuild.Msg.error( //
						CMDBuild.Translation.error, //
						CMDBUild.Translation.error_bimserver_connection, //
						true //
					);
			},
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
				me.bimWindow = new CMDBuild.bim.management.view.CMBimWindow({
					delegate: me
				});
			}
			me.bimWindow.show();

			if (me.bimSceneManager == null) {
				me.bimSceneManager = new BIMSceneManager({
					canvasId: me.bimWindow.CANVAS_ID,
					viewportId: me.bimWindow.getId(),
					progressBar: new CMDBuild.common.CMLoadingBar()
				});

				me.bimSceneManager.addDelegate(me);

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

	function openCardDataWindow(me, card) {
		var classId = card.IdClass;

		_CMCache.getAttributeList(classId, function(attributes) {
			var cardWindow = new CMDBuild.bim.view.CMCardDataWindow({
				cmCardData: card,
				attributeConfigurations: attributes,
				delegate: me
			});
			cardWindow.show();

		});

	}

	function renderBimIcon() {
		return '<img style="cursor:pointer"' +
			'" class="' + ICON_ACTION + '" src="images/icons/application_home.png"/>';
	}
})();