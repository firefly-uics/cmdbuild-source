(function() {

	var FULL_SCREAN_CANVAS_CLASS = "canvas-wrapper-full";

	BIMSurfer = {
		go: function() {

			var webGL = checkWebGL();

			if (!webGL) {

				$("body").empty().append('<div class="grave-error">'+
					'<p>OOOOOO snap,</p>' +
					'<p>looks like you have not WebGL support on your PC...</p>' +
					'<p>:(</p>' +
				'</div>')
				.css("background-color", "#EEEEEE");
				return;
			}

			// TODO inherited from old version
			// I've not really understood it
			document.multitouchData = true;
			var pageElement = document.getElementById('viewport');
			pageElement.style.MozUserSelect = 'none';
			pageElement.onmousedown = function() {
				return false;
			};

			/* 
			 * Build a global logger
			 */
			window._BIM_LOGGER = new Logger();

			/*
			 * Build a global scene manager
			 */
			// TODO: maybe could be better that
			// only who use it knows it
			window._BIM_SCENE_MANAGER = new BIMSceneManager({
				canvasId: "scenejsCanvas",
				viewportId: "viewport",
				progressBar: new BIMProgressBar()
			});
	
			/*
			 * Build the loading mask that
			 * show a Loading... message
			 * during an Ajax call
			 */
			window._LOADING_MASK = new LoadingMask();
	
			/**
			 * Init the interface
			 */
			new BIMControlPanel(window._BIM_SCENE_MANAGER, "controls-navigation");
			new BIMLayersPanel(window._BIM_SCENE_MANAGER, "controls-layers");
			new BIMObjectTree(window._BIM_SCENE_MANAGER, "controls-object-tree");
			new BIMMenuBar({
				containerElementId: "menu-wrapper"
			});
	
	
			/**
			 * Init a global event listener
			 */
			new BIMViewportEventListener("scenejsCanvas", window._BIM_SCENE_MANAGER);
	
	
			/*
			 * Display the login window by default
			 */
			new BIMLoginAndImportWindow();

			doLayout();
		},

		toggleControls: function() {
			var controls = $("#controls-wrapper");
			var canvas = $("#canvas-wrapper");

			controls.toggle();
			if (controls.css("display") == "none") {
				canvas.addClass(FULL_SCREAN_CANVAS_CLASS);
			} else {
				canvas.removeClass(FULL_SCREAN_CANVAS_CLASS);
			}

			updateCameraRatio();
		},

		doLayout: doLayout
	};

	function doLayout() {

		var viewPort = $('#viewport');
		var controls = $("#controls-wrapper");
		var splitter = $("#controls-wrapper-collapse");
		var canvasWrapper = $("#canvas-wrapper");

		var windowH = $(window).height();
		var menuHeight = $("#menu-wrapper").height();
		var currentHeight = windowH - ($('body').innerHeight() - viewPort.outerHeight(true));

		viewPort.height(currentHeight);

		var componentsHeight = currentHeight - menuHeight;
		controls.height(componentsHeight);
		splitter.height(componentsHeight);
		canvasWrapper.height(componentsHeight);

		updateCameraRatio();
	}

	function updateCameraRatio() {
		var canvas = document.getElementById("scenejsCanvas");
		var canvasWrapper = $("#canvas-wrapper");
		canvas.width = canvasWrapper.width();
		canvas.height = canvasWrapper.height();

		_BIM_SCENE_MANAGER.updateCameraRatio(canvas);
	}

	function checkWebGL() {
		try {
			return !!window.WebGLRenderingContext && !! document.createElement('canvas').getContext('experimental-webgl');
		} catch( e ) {
			return false;
		}
	}

})();
