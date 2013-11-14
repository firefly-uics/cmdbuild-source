(function() {

	// TODO: delegate the jquery async calls

	BIMLoadingBarInterface = function() {};

	BIMLoadingBarInterface.prototype = {
		setProgress: function(progress) {},

		beginProcessing: function() {},

		show: function() {},

		hide: function() {},

		reset: function() {},

		setText: function(text) {}
	};

	BIMProjectLoader = function(delegate, classNames, progressBar) {
		this.currentAction = {};

		/**
		 * this object must
		 * implements that methods
		 *		projectDidLoad(scene)
		 * 		putGeometriesInScene(data, currentLayerName, currentLayerId);
		 */
		this.delegate = delegate;

		/**
		 *	an array with the name of
		 * 	the IFC class to load for a project
		 */
		this.classNames = classNames || [];

		/**
		 * this array contains the name
		 * of the loaded layers
		 * 
		 * ex: xfcRoof, xfcDoor
		 * 
		 * it's filled incrementally
		 * after each call to load a layer
		 */
		this.loadedTypes = [];

		this.progressBar = progressBar || new BIMLoadingBarInterface();
	};

	BIMProjectLoader.prototype.load = function(roid) {
		this.loadedTypes = [];
		this.currentAction = {
			roid: roid
		};

		var me = this;

		window._BIM_SERVER_API.call( //
			"PluginInterface", //
			"getSerializerByPluginClassName", //
			{ //
				pluginClassName: "org.bimserver.geometry.jsonshell.SceneJsShellSerializerPlugin" //
			}, //
			function (serializer) {
				getSerializerByPluginClassNameSuccess(serializer, roid, me);
			},
			function () {
				window._BIM_LOGGER.log("BIM_SERVER_API_ERROR: getSerializerByPluginClassName");
			}
		);
	};

	function getSerializerByPluginClassNameSuccess(serializer, roid, me) {
		me.serializer = serializer;
		window._BIM_SERVER_API.call(//
			"Bimsie1ServiceInterface", //
			"download", //
			{ //
				roid: roid,
				serializerOid: serializer.oid,
				showOwn: true,
				sync: false
			}, //
			function (laid) {
				onDownloadSuccess(laid, me);
			},
			function () {
				window._BIM_LOGGER.log("BIM_SERVER_API_ERROR: download");
			}
		);
	};

	function onDownloadSuccess(laid, me) {

		me.progressHandler = function (topicId, state) {
			progressHandler(topicId, state, me);
		};

		window._BIM_SERVER_API.registerProgressHandler( //
			laid, //
			me.progressHandler, //
			function onRegisterProgressHandlerSuccess() { //
				window._BIM_SERVER_API.call( //
					"Bimsie1NotificationRegistryInterface", //
					"getProgress", //
					{
						topicId: laid
					}, //
					function onGetProgressSuccess(state) {
						progressHandler(null, state, me);
					}
				);
			}
		);

		me.progressBar.reset();
		me.progressBar.setText("Loading BIM model");
		me.progressBar.show();

		me.currentAction.serializerOid = me.serializer.oid;
		me.currentAction.laid = laid;
	};

	function progressHandler(topicId, state, me) {
		if (state.state == "FINISHED") {
			window._BIM_SERVER_API.unregisterProgressHandler(//
				me.currentAction.laid, //
				me.progressHandler //
			);

			var url = window._BIM_SERVER_API.generateRevisionDownloadUrl({
				serializerOid: me.currentAction.serializerOid,
				laid : me.currentAction.laid
			});

			me.progressBar.reset();
			me.progressBar.setText("Downloading BIM model");
			me.progressBar.setProgress(100);
			me.progressBar.beginProcessing();

			$.ajax(url).done(function(data) {
				window._BIM_LOGGER.log("PROGES HANDLER: state = FINISHED", data);
				if (me.delegate 
					&& typeof me.delegate.projectDidLoad == "function") {

					me.delegate.projectDidLoad(data);
				}
			});

		} else {
			if (state.progress != -1) {
				me.progressBar.setProgress(state.progress);
			}
		}
	};

	BIMProjectLoader.prototype.loadGeometries = function(ifcTypes) {
		var me = this;
		window._BIM_SERVER_API.call( //
			"PluginInterface", //
			"getSerializerByPluginClassName", //
			{
				pluginClassName : "org.bimserver.geometry.json.JsonGeometrySerializerPlugin"
			}, //
			function(serializer) {
				me.typeDownloadQueue = me.classNames.slice(0);

				// Remove the types that are not there anyways
				me.typeDownloadQueue.sort();
				ifcTypes.sort();
				me.typeDownloadQueue = intersect_safe(me.typeDownloadQueue, ifcTypes);

				me.loadGeometry(me.currentAction.roid, serializer.oid);
			}, //
			function() {
				window._BIM_LOGGER.log("GET_SERIALIZER_BY_PLUGIN_CLASSNAME: fail");
			}
		);
	};

	BIMProjectLoader.prototype.loadGeometry = function(roid, serializerOid, typeDownloadQueue) {
		if (this.typeDownloadQueue.length == 0) {
			this.progressBar.hide();
			return;
		}

		var className = this.typeDownloadQueue[0];
		this.progressBar.reset();
		this.progressBar.setText("Loading " + className);
		this.progressBar.show();

		this.typeDownloadQueue = this.typeDownloadQueue.slice(1);

		var me = this;

		window._BIM_SERVER_API.call( //
			"Bimsie1ServiceInterface", //
			"downloadByTypes", //
			{
				roids: [roid],
				classNames: [className],
				serializerOid: serializerOid,
				includeAllSubtypes: false,
				useObjectIDM: false,
				sync: false,
				deep: true
			}, //
			function(laid) {
				me.mode = "loading";
	
				window._BIM_SERVER_API.registerProgressHandler( //
					laid, //
					function(laid, state) {
						me.progressHandlerType(laid, state);
					},
					function() {
						window._BIM_SERVER_API.call( //
							"Bimsie1NotificationRegistryInterface", //
							"getProgress", //
							{
								topicId: laid
							}, //
							function(state) {
								window._BIM_LOGGER.log("STATE:", state);
								me.progressHandlerType(laid, state);
							}, //
							function() {
								window._BIM_LOGGER.log("GET_PROGRESS_FAIL");
							}
						);
					} //
				);
	
				me.currentAction.serializerOid = serializerOid;
				me.currentAction.laid = laid;
				me.currentAction.roid = roid;
				me.currentAction.className = className;
			} //
		);
	};

	BIMProjectLoader.prototype.progressHandlerType = function(topicId, state) {
		var me = this;

		me.progressBar.setProgress(state.progress);

		if (state.state == "FINISHED"
			&& me.mode == "loading") {

			me.mode = "processing";

			window._BIM_SERVER_API.unregisterProgressHandler( //
				me.currentAction.laid, //
				me.progressHandlerType //
			);

			var url = window._BIM_SERVER_API.generateRevisionDownloadUrl({
				serializerOid : me.currentAction.serializerOid,
				laid : me.currentAction.laid
			});

			me.progressBar.beginProcessing();

			$.getJSON(url, function(data) {
				var currentLayerName = me.currentAction.className;
				var currentLayerId = currentLayerName.toLowerCase();

				me.loadedTypes.push(currentLayerName);
				me.loadGeometry(me.currentAction.roid, me.currentAction.serializerOid);

				if (me.delegate
					&& typeof me.delegate.putGeometriesInScene == "function") {

					me.delegate.putGeometriesInScene(data, currentLayerName, currentLayerId);
				}

			});
		}
	};

	BIMProjectLoader.prototype.loadGeometryForType = function(typeName) {
		this.typeDownloadQueue = [typeName];
		var me = this;
		window._BIM_SERVER_API.call("PluginInterface", "getSerializerByPluginClassName", {
			pluginClassName : "org.bimserver.geometry.json.JsonGeometrySerializerPlugin"
		}, function(serializer) {
			me.loadGeometry(me.currentAction.roid, serializer.oid);
		});
	};

	BIMProjectLoader.prototype.isTypeLoaded = function(typeName) {
		return this.loadedTypes.indexOf(typeName) > -1;
	};

	// http://stackoverflow.com/questions/1885557/simplest-code-for-array-intersection-in-javascript	
	function intersect_safe(a, b) {
		var ai=0, bi=0;
		var result = new Array();

		while(ai < a.length && bi < b.length) {
			if (a[ai] < b[bi]){
				ai++;
			} else if (a[ai] > b[bi]) {
				bi++;
			} else { // are equals
				result.push(a[ai]);
				ai++;
				bi++;
			}
		}

		return result;
	}

})();
