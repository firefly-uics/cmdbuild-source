(function($) {
	var CONFIGURATION_FILE = "configurations/configuration.json";
	var OPTIONS_LABEL_ON_SELECTED = "Selected";
	var OPTIONS_LABEL_ON_ALL = "All";
	var MAX_DISTANCE_NODES = 10000;
	var MIN_STEP_ZOOMALL = 10;
	if (!$.Cmdbuild.g3d) {
		$.Cmdbuild.g3d = {};
	}
	var Viewer = function(idCanvas) {
		var canvasDiv;
		var camera, controls, scene, renderer;
		var plane;
		var thisViewer = undefined;
		var labelsInterval = undefined;
		var labels = [];
		var raycaster = new THREE.Raycaster();
		raycaster.linePrecision = 3;
		var mouse = new THREE.Vector2(), offset = new THREE.Vector3(), INTERSECTED, SELECTED, LASTSELECTED;
		var realMouse = new THREE.Vector2();
		this.model = undefined;
		var objects = [];
		var edges = [];
		this.init = function() {
			$.Cmdbuild.g3d.Options.loadConfiguration(CONFIGURATION_FILE, function(response) {
				$.Cmdbuild.custom.configuration = response;
				this.initCB();
				animate();
			}, this);
		};
		this.initCB = function() {
			THREE.ImageUtils.crossOrigin = '';
			thisViewer = this;
			this.model = new $.Cmdbuild.g3d.Model();
			this.model.observe(this);
			$.Cmdbuild.customvariables.options = new $.Cmdbuild.g3d.Options();
			$.Cmdbuild.customvariables.options.observe(this);
			$.Cmdbuild.g3d.Options.initFields();
			$.Cmdbuild.customvariables.viewer = this;
			$.Cmdbuild.customvariables.model = this.model;
			this.camera = new $.Cmdbuild.g3d.Camera(this.model);
			this.camera.observe(this);
			this.selected = new $.Cmdbuild.g3d.Selected(this.model);
			this.selected.observe(this);
			$.Cmdbuild.customvariables.camera = this.camera;
			$.Cmdbuild.customvariables.selected = this.selected;
			this.commandsManager = new $.Cmdbuild.g3d.CommandsManager(
					this.model);
			canvasDiv = $("#" + idCanvas)[0];// document.createElement('div');
			$.Cmdbuild.customvariables.commandsManager = this.commandsManager;

			camera = $.Cmdbuild.g3d.ViewerUtilities.camera();
			var cameraHelper = new THREE.CameraHelper(camera);
			scene = new THREE.Scene();

			scene.add(new THREE.AmbientLight(0x909090));
			// scene.add(cameraHelper);
			var light = $.Cmdbuild.g3d.ViewerUtilities.spotLight(camera, 2000);
			scene.add(light);
			var light = $.Cmdbuild.g3d.ViewerUtilities.spotLight(camera, -2000);
			scene.add(light);
			plane = $.Cmdbuild.g3d.ViewerUtilities.spacePlane();
			scene.add(plane);
			// var axes = buildAxes(1000);
			// scene.add(axes);
			renderer = $.Cmdbuild.g3d.ViewerUtilities.webGlRender(canvasDiv);
			canvasDiv.appendChild(renderer.domElement);

			controls = $.Cmdbuild.g3d.ViewerUtilities.trackballControls(camera,
					renderer.domElement);

			$.Cmdbuild.g3d.ViewerUtilities.declareEvents(this,
					renderer.domElement);
			var init = new $.Cmdbuild.g3d.commands.init_explode(
					thisViewer.model, $.Cmdbuild.start.httpCallParameters);
			this.commandsManager
					.execute(
							init,
							{},
							function(response) {
								if (true) {// $.Cmdbuild.customvariables.options["automaticZoom"])
									// {
									var me = this;
									setTimeout(
											function() {
												var box = me.boundingBox();
												me.zoomAll(box);
												$.Cmdbuild.customvariables.selected
														.erase();
												$.Cmdbuild.customvariables.selected
														.select($.Cmdbuild.start.httpCallParameters.cardId);
											}, 500);
								}

							}, this);
		};
		this.onWindowResize = function() {
			var canvas = $("#" + idCanvas);
			camera.aspect = canvas.innerWidth() / canvas.innerHeight();
			camera.updateProjectionMatrix();
			renderer.setSize(canvas.innerWidth(), canvas.innerHeight());
		};
		this.refreshCamera = function() {
			var position = this.camera.getData();
			camera.lookAt(position);
			controls.target.set(position.x, position.y, position.z);
		};
		this.getOpenCompoundCommands = function(node) {
			var elements = $.Cmdbuild.g3d.Model.getGraphData(node,
					"compoundData");
			var arCommands = [];
			var expandingThreshold = $.Cmdbuild.customvariables.options["expandingThreshold"];
			for (var i = 0; i < elements.length; i += expandingThreshold) {
				arCommands.push({
					command: "openChildren",
					id: node.id(),
					elements: elements.slice(i, i + expandingThreshold)
				});
			}
			return arCommands;
		};
		this.onDocumentMouseDblClick = function(event) {
			if (!LASTSELECTED) {
				return;
			}
			var node = thisViewer.model.getNode(LASTSELECTED.elementId);
			var className = $.Cmdbuild.g3d.Model
					.getGraphData(node, "className");
			if (className == $.Cmdbuild.g3d.constants.GUICOMPOUNDNODE) {
				var arCommands = thisViewer.getOpenCompoundCommands(node);
				var macroCommand = new $.Cmdbuild.g3d.commands.macroCommand(
						thisViewer.model, arCommands);
				$.Cmdbuild.customvariables.commandsManager.execute(
						macroCommand, {}, function() {
						}, thisViewer);

			} else {
				thisViewer
						.explodeNode({
							id: LASTSELECTED.elementId,
							domainList: null,
							levels: $.Cmdbuild.customvariables.options["explosionLevels"]
						// parseInt($("#levels").val())
						});
			}
		};
		this.explodeNode = function(params) {
			var explode = new $.Cmdbuild.g3d.commands.explode_levels(
					thisViewer.model, {
						id: params.id,
						domainList: params.domainList,
						levels: params.levels
					});
			thisViewer.commandsManager.execute(explode, {}, function() {
				var nodes = $.Cmdbuild.customvariables.model.getNodes();
				$.Cmdbuild.g3d.Model.removeGraphData(nodes, "exploded_children");
			}, this);
		};
		this.moveEdgeTooltip = function(intersected, node, mouseX, mouseY) {
			if (!$.Cmdbuild.customvariables.options["edgesTooltip"]) {
				return;
			}
			$('#viewerInformation').removeClass('viewerInformationNode')
					.addClass('viewerInformationEdge');
			var h = $("#viewerInformation").height();
			var w = $("#viewerInformation").width();
			$("#viewerInformation")[0].style.top = mouseY - (h + 50);
			$("#viewerInformation")[0].style.left = mouseX - w / 2;
			var label = intersected.object.label;
			var htmlStr = "<p>" + label + "</p>";
			var source = intersected.object.source;
			var target = intersected.object.target;
			var classSource = $.Cmdbuild.g3d.Model.getGraphData(source,
					"className");
			var classTarget = $.Cmdbuild.g3d.Model.getGraphData(target,
					"className");
			var labelSource = $.Cmdbuild.g3d.Model
					.getGraphData(source, "label");
			var labelTarget = $.Cmdbuild.g3d.Model
					.getGraphData(target, "label");
			var img = $.Cmdbuild.SpriteArchive.class2Sprite("relation");
			htmlStr += "<p>" + labelSource + " (" + classSource + ")</p>";
			htmlStr += "<img width=16px height=16px src='" + img + "'/>";
			htmlStr += "<p>" + labelTarget + " (" + classTarget + ")</p>";
			$("#viewerInformation").html(htmlStr);
			$("#viewerInformation")[0].style.display = "block";
		};
		this.moveNodeTooltip = function(intersected, node, mouseX, mouseY) {
			if (!$.Cmdbuild.customvariables.options["nodesTooltip"]) {
				return;
			}
			$('#viewerInformation').removeClass('viewerInformationEdge')
					.addClass('viewerInformationNode');
			var h = $("#viewerInformation").height();
			var w = $("#viewerInformation").width();
			$("#viewerInformation")[0].style.top = mouseY - (h + 50);
			$("#viewerInformation")[0].style.left = mouseX - w / 2;
			// the jquery version gives problems on the page's layout
			// $("#viewerInformation").position({
			// my: "center bottom-30",
			// of: event,
			// collision: "fit"
			// });
			var label = $.Cmdbuild.g3d.Model.getGraphData(node, "label");
			var className = $.Cmdbuild.g3d.Model
					.getGraphData(node, "className");
			var img = $.Cmdbuild.SpriteArchive.class2Sprite(className);
			var htmlStr = "<img width=32px height=32px src='" + img + "'/>";
			htmlStr += "<p>" + label + "</p>";
			htmlStr += "<p>" + className + "</p>";
			$("#viewerInformation").html(htmlStr);
			$("#viewerInformation")[0].style.display = "block";
		};
		this.closeTooltip = function() {
			$("#viewerInformation")[0].style.display = "none";
		};
		this.onDocumentMouseMove = function(event) {
			event.preventDefault();
			// event.stopPropagation();
			realMouse.x = event.clientX;
			realMouse.y = event.clientY;
			mouse.x = ((event.clientX - renderer.domElement.offsetLeft) / renderer.domElement.width) * 2 - 1;
			mouse.y = -((event.clientY - renderer.domElement.offsetTop) / renderer.domElement.height) * 2 + 1;
			raycaster.setFromCamera(mouse, camera);
			if (SELECTED) {
				var intersects = raycaster.intersectObject(plane, true);
				var node = thisViewer.model.getNode(SELECTED.elementId);
				if (intersects.length <= 0) {
					thisViewer.pushNewPosition(thisViewer.model,
							SELECTED.elementId, node.position(),
							SELECTED.position);
					SELECTED = LASTSELECTED = null;
					return;
				}
				var position = intersects[0].point.sub(offset);
				SELECTED.position.copy(position);
				thisViewer.refreshNodeEdges(SELECTED.elementId, position);
				if (node.selectionOnNode) {
					node.selectionOnNode.position.copy(position);
				}
				return;
			}
			var intersects = raycaster.intersectObjects(objects, true);
			if (intersects.length > 0) {
				if (INTERSECTED != intersects[0].object) {
					INTERSECTED = intersects[0].object;
					plane.position.copy(INTERSECTED.position);
					plane.lookAt(camera.position);
					var node = thisViewer.model.getNode(INTERSECTED.elementId);
				}
				canvasDiv.style.cursor = 'pointer';
			} else {
				INTERSECTED = null;
				canvasDiv.style.cursor = 'auto';
			}

			// tooltip && selection
			if (intersects.length > 0 && intersects[0].object.name) {
				try {
					var node = thisViewer.model.getNode(INTERSECTED.elementId);
					thisViewer.moveNodeTooltip(intersects[0], node,
							event.clientX, event.clientY);
					if (node.selectionOnNode) {
						node.selectionOnNode.position.copy(node.position());
					}
				} catch (e) {
					console
							.log("Viewer: onDocumentMouseMove error during tooltip show");
				}
			} else {
				thisViewer.closeTooltip();
				thisViewer.tooltipLine(event);
			}
		};
		this.tooltipLine = function(event) {
			var vector = new THREE.Vector3(mouse.x, mouse.y, 0.5)
					.unproject(camera);
			var raycaster = new THREE.Raycaster(camera.position, vector.sub(
					camera.position).normalize());
			raycaster.linePrecision = 30;
			var intersects = [];
			try {
				intersects = raycaster.intersectObjects(edges, false);

			} catch (e) {
				console.log(e, edges);
			}
			if (intersects.length > 0) {
				var node = thisViewer.model.getNode(intersects[0].object.id);
				thisViewer.moveEdgeTooltip(intersects[0], node, event.clientX,
						event.clientY);
			} else {
				thisViewer.closeTooltip();
			}
		};
		this.refreshNodeEdges = function(id, position) {
			var nodes = this.model.connectedEdges(id);
			for (var i = 0; i < nodes.length; i++) {
				var edge = nodes[i];
				var p2 = {};
				if (id == edge.source().id()) {
					p2 = $.Cmdbuild.g3d.ViewerUtilities.getCenterPosition(edge
							.target());
				} else if (id == edge.target().id()) {
					p2 = $.Cmdbuild.g3d.ViewerUtilities.getCenterPosition(edge
							.source());
				}
				$.Cmdbuild.g3d.ViewerUtilities.modifyLine(scene, edge,
						position, p2);
			}
		};
		this.onDocumentMouseDown = function(event) {
			thisViewer.closeTooltip();
			event.preventDefault();
			var vector = new THREE.Vector3(mouse.x, mouse.y, 0.5)
					.unproject(camera);
			var raycaster = new THREE.Raycaster(camera.position, vector.sub(
					camera.position).normalize());
			raycaster.linePrecision = 3;
			var intersects = raycaster.intersectObjects(objects);
			// LASTSELECTED = null;
			if (intersects.length > 0) {
				controls.enabled = false;
				SELECTED = intersects[0].object;
				if (SELECTED == LASTSELECTED) {
					return;
				}
				LASTSELECTED = SELECTED;
				var intersects = raycaster.intersectObject(plane, true);
				if (intersects.length <= 0) {
					return;
				}
				offset.copy(intersects[0].point).sub(plane.position);
				if (! event.ctrlKey) {
					thisViewer.clearSelection();
				}
				thisViewer.setSelection(SELECTED.elementId, ! event.ctrlKey);
				canvasDiv.style.cursor = 'move';
				// ---->>> controls.set( SELECTED.position);
			}
		};
		this.onDocumentMouseUp = function(event) {
			event.preventDefault();
			controls.enabled = true;
			var node = undefined;
			if (SELECTED) {
				node = thisViewer.model.getNode(SELECTED.elementId);
			}
			if (INTERSECTED && SELECTED) {
				plane.position.copy(INTERSECTED.position);
				if (!$.Cmdbuild.g3d.ViewerUtilities.equals(node.position(),
						INTERSECTED.position)) {
					thisViewer.pushNewPosition(thisViewer.model,
							SELECTED.elementId, node.position(),
							INTERSECTED.position);
				}
			} else if (SELECTED) {
				if (!$.Cmdbuild.g3d.ViewerUtilities.equals(node.position(),
						SELECTED.position)) {
					thisViewer.pushNewPosition(thisViewer.model,
							SELECTED.elementId, node.position(),
							SELECTED.position);
				}
			}
			canvasDiv.style.cursor = 'auto';
			SELECTED = null;
		};
		this.pushNewPosition = function(model, id, oldPosition, newPosition) {
			var node = thisViewer.model.getNode(id);
			if (node.selectionOnNode) {
				node.selectionOnNode.position.copy(newPosition);
			}
			if (!$.Cmdbuild.g3d.ViewerUtilities
					.equals(oldPosition, newPosition)) {
				var modifyPosition = new $.Cmdbuild.g3d.commands.modifyPosition(
						model, id, newPosition);
				thisViewer.commandsManager.execute(modifyPosition, {});
			}
		};
		this.refresh = function(rough) {
			if (this.duringRefresh) {
				return;
			}
			this.duringRefresh = true;
			this.model.changeLayout({
				name: "guisphere"// "guicircle"//"breadthfirst"//"concentric"//
			});
			if (rough) {
				$.Cmdbuild.g3d.ViewerUtilities.clearScene(scene, this.model,
						objects, edges);
			}
			objects = [];
			edges = [];
			if (rough) {
				this.clearSelection();
			}
			this.refreshNodes(scene, rough);
			this.refreshRelations(scene, rough);
			this.duringRefresh = false;
			if (this.toRefreshAgain === true) {
				this.toRefreshAgain = false;
				this.refresh(false);
			} else {
				this.refreshLabels();
			}

		};
		this.refreshNodes = function(scene, rough) {
			var nodes = this.model.getNodes();
			for (var i = 0; i < nodes.length; i++) {
				var node = nodes[i];
				if (node.removed()) {
					continue;
				}
				var glObject = scene.getObjectById($.Cmdbuild.g3d.Model
						.getGraphData(node, "glId"));// node.glObject;//
				if (glObject && !rough) {
					if (!$.Cmdbuild.g3d.ViewerUtilities.equals(
							glObject.position, node.position())) {
						new $.Cmdbuild.g3d.ViewerUtilities.moveObject(this,
								node);
					}
					objects.push(glObject);
				} else {
					var parentId = $.Cmdbuild.g3d.Model.getGraphData(node,
							"previousPathNode");
					var parentNode = this.model.getNode(parentId);
					var p = (parentNode && parentNode.glObject)
							? parentNode.glObject.position
							: {
								x: 0,
								y: 0,
								z: 0
							};
					var object = $.Cmdbuild.g3d.ViewerUtilities.objectFromNode(
							node, this.selected.isSelect(node.id()), p,
							renderer, scene);
					node.glObject = object;
					scene.add(object);
					objects.push(object);
					new $.Cmdbuild.g3d.ViewerUtilities.moveObject(this, node);
				}
			}
		};
		this.refreshRelations = function(scene, rough) {
			var modelEdges = this.model.getEdges();
			for (var i = 0; i < modelEdges.length; i++) {
				var edge = modelEdges[i];
				var source = edge.source();
				var target = edge.target();
				var p1 = $.Cmdbuild.g3d.ViewerUtilities
						.getCenterPosition(source);
				var p2 = $.Cmdbuild.g3d.ViewerUtilities
						.getCenterPosition(target);
				if (edge.glLine && !rough) {
					var glP1 = edge.glLine.geometry.vertices[0];
					var glP2 = edge.glLine.geometry.vertices[1];
					if (!($.Cmdbuild.g3d.ViewerUtilities.equals(glP1, p1) && $.Cmdbuild.g3d.ViewerUtilities
							.equals(glP2, p2))) {
						$.Cmdbuild.g3d.ViewerUtilities.modifyLine(scene, edge,
								p1, p2);
					}
					edges.push(edge.glLine);
				} else {
					var line = $.Cmdbuild.g3d.ViewerUtilities
							.lineFromEdge(edge);
					edge.glLine = line;
					edges.push(line);
					scene.add(line);
					$.Cmdbuild.g3d.ViewerUtilities.modifyLine(scene, edge, p1,
							p2);
				}
			}
		};
		this.refreshSelected = function() {
			this.removeSelectionGlObjects();
			var selected = this.selected.getData();
			for ( var key in selected) {
				this.showSelected(key);
			}
			if ($.Cmdbuild.customvariables.options["labels"] === OPTIONS_LABEL_ON_SELECTED) {
				this.refreshLabels();
			}
		};
		this.removeSelectionGlObjects = function() {
			// this is a viewer operation only for optimization issues
			var nodes = this.model.getNodes();
			for (var i = 0; i < nodes.length; i++) {
				var node = nodes[i];
				if (node && node.selectionOnNode) {
					scene.remove(node.selectionOnNode);
					node.selectionOnNode = undefined;
				}
			}
		};
		this.clearSelection = function() {
			this.removeSelectionGlObjects();
			this.selected.erase();
		};
		this.showSelected = function(id) {
			var node = thisViewer.model.getNode(id);
			var position = node.position();
			if (!node.selectionOnNode) {
				var object = $.Cmdbuild.g3d.ViewerUtilities
						.selectionOnNode(node);
				scene.add(object);
				node.selectionOnNode = object;
				object.position.set(position.x, position.y, position.z);
			}
		};
		this.setSelection = function(id, select) {
			if (select || ! this.selected.isSelect(id)) {
				this.selected.select(id);
				this.showSelected(id);
			} else {
				this.selected.unSelect(id);
			}
		};
		// ZOOM ALL
		this.boundingBox = function() {
			var bb = new THREE.Box3();
			var maxx = -Number.MAX_VALUE;
			var maxy = -Number.MAX_VALUE;
			var maxz = -Number.MAX_VALUE;
			var minx = Number.MAX_VALUE;
			var miny = Number.MAX_VALUE;
			var minz = Number.MAX_VALUE;
			for (var i = 0; i < objects.length; i++) {
				var p = objects[i].position;
				minx = Math.min(minx, p.x);
				miny = Math.min(miny, p.y);
				minz = Math.min(minz, p.z);
				maxx = Math.max(maxx, p.x);
				maxy = Math.max(maxy, p.y);
				maxz = Math.max(maxz, p.z);
			}
			return {
				vertices: [{
					x: minx,
					y: miny,
					z: minz
				}, {
					x: minx,
					y: miny,
					z: maxz
				}, {
					x: minx,
					y: maxy,
					z: minz
				}, {
					x: minx,
					y: maxy,
					z: maxz
				}, {
					x: maxx,
					y: miny,
					z: minz
				}, {
					x: maxx,
					y: miny,
					z: maxz
				}, {
					x: maxx,
					y: maxy,
					z: minz
				}, {
					x: maxx,
					y: maxy,
					z: maxz
				}],
				x: minx,
				y: miny,
				z: minz,
				w: maxx - minx,
				h: maxy - miny,
				d: maxz - minz
			};
		};
		this.zoomAll = function(vertices) {
			this.scaleInView(vertices);
			camera.updateProjectionMatrix();
		};
		this.projectVector = function(vector, projectionMatrix, matrixWorld) {
			var projScreenMatrix = new THREE.Matrix4();
			var matrixWorldInverse = new THREE.Matrix4();
			matrixWorldInverse.getInverse(matrixWorld);

			projScreenMatrix.multiplyMatrices(projectionMatrix,
					matrixWorldInverse);
			// projScreenMatrix.multiplyVector3( vector );
			vector = vector.applyProjection(projScreenMatrix);

			return vector;

		};
		this.vector2ScreenPosition = function(vector, camera, widthHalf,
				heightHalf) {
			var v = new THREE.Vector3();
			v.copy(vector);
			vector.project(camera);
			var projectionMatrix = new THREE.Matrix4();
			var matrixWorld = new THREE.Matrix4();
			projectionMatrix.copy(camera.projectionMatrix);
			matrixWorld.copy(camera.matrixWorld);
			for (var i = 0; i < 10; i++) {
				var vApp = new THREE.Vector3();
				vApp.copy(v);
				matrixWorld.makeTranslation(0, 0, i * 10);
				this.projectVector(vApp, projectionMatrix, matrixWorld);
			}
			camera.matrixWorld.copy(matrixWorld);
			vector.x = (vector.x * widthHalf) + widthHalf;
			vector.y = -(vector.y * heightHalf) + heightHalf;
			vector.z = -(vector.z * heightHalf) + heightHalf;

			return new THREE.Vector3(vector.x, vector.y, vector.z);
		};
		this.refreshLabels = function() {
			if (labelsInterval) {
				clearInterval(labelsInterval);
			}
			var canvas = $("#" + idCanvas);
			var wCanvas = canvas.innerWidth();
			var hCanvas = canvas.innerHeight();
			for (var i = 0; i < labels.length; i++) {
				scene.remove(labels[i].label);
				$("#label" + labels[i].id).remove();
			}
			labels = [];
			var showLabels = $.Cmdbuild.customvariables.options["labels"];
			if (showLabels) {
				var nodes = this.model.getNodes();
				for (var i = 0; i < nodes.length; i++) {
					var node = nodes[i];
					var label = $.Cmdbuild.g3d.Model
							.getGraphData(node, "label");
					if (showLabels == "Selected"
							&& !this.selected.isSelect(node.id())) {
						continue;
					}
					labels.push({
						object: node.glObject,
						id: node.id()
					});
					var strEvents = " onmousemove='$.Cmdbuild.customvariables.viewer.onDocumentMouseMove(event)' ";
					strEvents += " onmousedown='$.Cmdbuild.customvariables.viewer.onDocumentMouseDown(event)' ";
					strEvents += " onmouseup='$.Cmdbuild.customvariables.viewer.onDocumentMouseUp(event)' ";
					strEvents += " ondblclick='$.Cmdbuild.customvariables.viewer.onDocumentDblClick(event)' ";
					var strHtml = "<div id='label" + node.id()
							+ "' class='labelText'><span " + strEvents + ">"
							+ label + "</span></div>";
					$("#" + idCanvas).after(strHtml);
				}
			}
			labelsInterval = setInterval(
					function() {
						var showLabels = $.Cmdbuild.customvariables.options["labels"];
						if (!showLabels) {
							clearInterval(labelsInterval);
							return;
						}
						for (var i = 0; i < labels.length; i++) {
							var p = labels[i].object.position.clone();
							p.project(camera);
							var y = parseInt(hCanvas / 2 - p.y * hCanvas / 2);
							var x = parseInt(wCanvas / 2 + p.x * wCanvas / 2);
							if (Math.abs(realMouse.y - y) < 40
									&& (realMouse.x >= x - 40 && realMouse.x < x + 250)) {
								y = realMouse.y - 60;
							}
							if (y < 0 || x < 0 || x > wCanvas
									|| y > hCanvas - 40) {
								$("#label" + labels[i].id).css({
									display: "none"
								});

							} else if (!labels[i].x || !labels[i].y
									|| Math.abs(labels[i].y - y) > 4
									|| Math.abs(labels[i].x - x) > 4) {
								$("#label" + labels[i].id).css({
									top: y,
									left: x,
									display: "block"
								});
							}
							labels[i].x = x;
							labels[i].y = y;
						}
					}, 500);
		};
		this.pointOnScreen = function(vector, w, h, projectionMatrix,
				matrixWorld, bFirst) {
			var v = new THREE.Vector3();
			v.copy(vector);
			this.projectVector(v, projectionMatrix, matrixWorld);
			v.x = (v.x * w / 2) + w / 2;
			v.y = -(v.y * h / 2) + h / 2;
			if (v.x < 0 || v.x > w || v.y < 0 || v.y > h) {
				return false;
			}
			return true;
		};
		this.onVideo = function(box, w, h, projectionMatrix, matrixWorld) {
			for (var i = 0; i < box.vertices.length; i++) {
				var vertice = box.vertices[i];
				var vector = new THREE.Vector3(vertice.x, vertice.y, vertice.z);
				var bOnVideo = this.pointOnScreen(vector, w, h,
						projectionMatrix, matrixWorld);
				if (!bOnVideo) {
					return false;
				}
			}
			return true;
		};
		this.stepZoom = function(box, w, h) {
			var NORECURSE = 100;
			var me = this;
			function stepIn() {
				setTimeout(function() {
					if (me.onVideo(box, w, h, camera.projectionMatrix,
							camera.matrixWorld)
							&& NORECURSE-- > 0) {
						controls.setY(+1);
						stepIn();
					}
				}, 50);
			}
			function stepOut() {
				setTimeout(function() {
					if (!me.onVideo(box, w, h, camera.projectionMatrix,
							camera.matrixWorld)
							&& NORECURSE-- > 0) {
						controls.setY(-1);
						stepOut();
					}
				}, 100);
			}
			stepIn();
			stepOut();
		}
		this.scaleInView = function(box) {
			NORECURSE = 100;
			var x = box.x + box.w / 2;
			var y = box.y + box.h / 2;
			var z = box.z + box.d / 2;
			var canvas = $("#" + idCanvas);
			var w = canvas.innerWidth();
			var h = canvas.innerHeight();
			var position = {
				x: x,
				y: y,
				z: z
			};
			controls.enabled = true;
			var index = 10;
			$.Cmdbuild.customvariables.camera.zoomOnPosition(position,
					function() {
						if (box.w > 0 || box.h > 0) {
							this.stepZoom(box, w, h);
						}
					}, this);
		};
		this.refreshOptions = function() {
			this.refresh();
		};
		// initialization
		var animate = function() {
			requestAnimationFrame(animate);
			render();
		};

		var render = function() {
			controls.update();
			renderer.render(scene, camera);
		};
		this.init();
	};
	$.Cmdbuild.g3d.Viewer = Viewer;
	function buildAxes(length) {
		var axes = new THREE.Object3D();

		axes.add(buildAxis(new THREE.Vector3(0, 0, 0), new THREE.Vector3(
				length, 0, 0), 0xFF0000, false)); // +X
		axes.add(buildAxis(new THREE.Vector3(0, 0, 0), new THREE.Vector3(
				-length, 0, 0), 0xFF0000, true)); // -X
		axes.add(buildAxis(new THREE.Vector3(0, 0, 0), new THREE.Vector3(0,
				length, 0), 0x00FF00, false)); // +Y
		axes.add(buildAxis(new THREE.Vector3(0, 0, 0), new THREE.Vector3(0,
				-length, 0), 0x00FF00, true)); // -Y
		axes.add(buildAxis(new THREE.Vector3(0, 0, 0), new THREE.Vector3(0, 0,
				length), 0x0000FF, false)); // +Z
		axes.add(buildAxis(new THREE.Vector3(0, 0, 0), new THREE.Vector3(0, 0,
				-length), 0x0000FF, true)); // -Z

		return axes;

	}
	function buildAxis(src, dst, colorHex, dashed) {
		var geom = new THREE.Geometry(), mat;

		if (dashed) {
			mat = new THREE.LineDashedMaterial({
				linewidth: 3,
				color: colorHex,
				dashSize: 3,
				gapSize: 3
			});
		} else {
			mat = new THREE.LineBasicMaterial({
				linewidth: 3,
				color: colorHex
			});
		}

		geom.vertices.push(src.clone());
		geom.vertices.push(dst.clone());
		geom.computeLineDistances(); // This one is SUPER important,
		// otherwise dashed lines will appear as
		// simple plain lines

		var axis = new THREE.Line(geom, mat, THREE.LinePieces);

		return axis;

	}
})(jQuery);
