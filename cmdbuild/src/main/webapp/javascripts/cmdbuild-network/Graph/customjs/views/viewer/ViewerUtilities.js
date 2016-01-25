(function($) {
	if (!$.Cmdbuild.g3d) {
		$.Cmdbuild.g3d = {};
	}
	var movingNodes = {};
	var ViewerUtilities = {
		spotLight: function(camera, z) {
			var light = new THREE.SpotLight(0xffffff, 1.5);
			light.position.set(0, 500, z);
			light.castShadow = true;

			light.shadowCameraNear = 200;
			light.shadowCameraFar = camera.far;
			light.shadowCameraFov = 50;

			light.shadowBias = -0.00022;
			light.shadowDarkness = 0.5;

			light.shadowMapWidth = 2048;
			light.shadowMapHeight = 2048;
			return light;
		},
		declareEvents: function(viewer, domElement) {
			$(domElement).on("mousemove", viewer.onDocumentMouseMove);
			$(domElement).on("mousedown", viewer.onDocumentMouseDown);
			$(domElement).on("mouseup", viewer.onDocumentMouseUp);
			$(domElement).on("mouseleave", viewer.closeTooltip);
			$(domElement).on("dblclick", viewer.onDocumentMouseDblClick);
			$(document).on("keypress", function(event) {
				if (event.keyCode == 26 && event.ctrlKey) {
					viewer.commandsManager.undo();
				}
			});
			window.addEventListener('resize', viewer.onWindowResize, false);
		},
		trackballControls: function(camera, domElement) {
			var controls = new THREE.TrackballControls(camera, domElement);
			controls.rotateSpeed = 1.0;
			controls.zoomSpeed = 1.2;
			controls.panSpeed = 0.8;
			controls.noZoom = false;
			controls.noPan = false;
			controls.staticMoving = true;
			controls.dynamicDampingFactor = 0.3;
			return controls;
		},
		webGlRender: function(container) {
			var renderer = new THREE.WebGLRenderer({
				antialias: true
			});
			renderer.setClearColor(0xf0f0f0);
			renderer.setPixelRatio(1.);
			renderer.setSize(container.clientWidth, container.clientHeight,
					true);
			renderer.sortObjects = false;

			renderer.shadowMapEnabled = true;
			renderer.shadowMapType = THREE.PCFShadowMap;
			return renderer;
		},
		header: function() {
			var header = document.createElement('div');
			header.style.position = 'absolute';
			header.style.top = '10px';
			header.style.width = '100%';
			header.style.textAlign = 'left';
			var strCommand = "$.Cmdbuild.standard.commands.navigate({form: 'commandPanel', dialog: 'commandPanelDialog' });";
			header.innerHTML = '<p onclick="' + strCommand + '">MENU</p>';
			return header;
		},
		spacePhisycalPlane: function(normal, dist) {
			var plane = new THREE.Plane(normal, dist);
			plane.visible = true;
			return plane;
		},
		spacePlane: function(visible, w, h) {
			w = (w) ? w : 12000;
			h = (h) ? h : 12000;
			var plane = new THREE.Mesh(
					new THREE.PlaneBufferGeometry(w, h, 8, 8),
					new THREE.MeshBasicMaterial({
						color: 0xaa0000,
						opacity: 0.25,
						transparent: true
					}));
			plane.visible = (visible) ? visible : false;
			return plane;
		},
		text: function(str, position, camera) {
			var size = 108;
			var curveSegments = 3;
			var text = new THREE.TextGeometry(str, {
				font: "helvetiker",
				size: 10,
				height: 3,
			});
			var material = new THREE.MeshBasicMaterial({
				color: '#000044'
			});
			var textMesh = new THREE.Mesh(text, material);
			textMesh.position.x = position.x;
			textMesh.position.y = position.y;
			textMesh.position.z = position.z;
			return textMesh;
		},
		objOnPlane: function(position) {
			var selectionShape = $.Cmdbuild.g3d.constants.SELECTION_SHAPE;
			var sprite = $.Cmdbuild.SpriteArchive.class2Sprite(selectionShape);
			var map = THREE.ImageUtils.loadTexture(sprite);
			var material = new THREE.SpriteMaterial({
				map: map,
				color: 0xffffff,
				fog: false
			});
			var object = new THREE.Sprite(material);
			object.scale.set(20, 20, 2);
			object.material.ambient = object.material.color;
			object.position.set(position.x, position.y, position.z);
			object.material.ambient = object.material.color;
			return object;
		},
		camera: function(param) {
			var fov = (param && param.fov) ? param.fov : 70;
			var camera = new THREE.PerspectiveCamera(fov, window.innerWidth
					/ window.innerHeight, 1, 100000);
			camera.position.z = $.Cmdbuild.custom.configuration.camera.position.z;
			camera.position.y = $.Cmdbuild.custom.configuration.camera.position.y;
			return camera;
		},
		// OPENGL
		selectionOnNode: function(node) {
			var selectionShape = $.Cmdbuild.g3d.constants.SELECTION_SHAPE;
			var sprite = $.Cmdbuild.SpriteArchive.class2Sprite(selectionShape);
			var map = THREE.ImageUtils.loadTexture(sprite);
			var material = new THREE.SpriteMaterial({
				map: map,
				color: 0xffffff,
				fog: false
			});
			var object = new THREE.Sprite(material);
			var sd = $.Cmdbuild.custom.configuration.spriteDimension;
			object.scale.set(sd * 2, sd * 2, 2);
			object.material.ambient = object.material.color;
			return object;
		},
		objectFromNode: function(node, selected, position) {
			var sprite = $.Cmdbuild.SpriteArchive
					.class2Sprite($.Cmdbuild.g3d.Model.getGraphData(node,
							"className"));
			try {
				var map = THREE.ImageUtils.loadTexture(sprite, {}, function() {

				});
				if (!map) {
					console.log("Error on Sprite: "
							+ $.Cmdbuild.g3d.Model.getGraphData(node,
									"className") + " the sprite is " + sprite);
				}
				var material = new THREE.SpriteMaterial({
					map: map,
					color: 0xffffff,
					fog: false
				});
				var object = new THREE.Sprite(material);
				var sd = $.Cmdbuild.custom.configuration.spriteDimension;
				object.scale.set(sd, sd, 1);
				object.material.ambient = object.material.color;
				object.position.x = position.x;
				object.position.y = position.y;
				object.position.z = position.z;
				object.elementId = node.id();
				$.Cmdbuild.g3d.Model.setGraphData(node, "glId", object.id);
				object.name = node.id();
				return object;
			} catch (e) {
				console.log("Error on Sprite: "
						+ $.Cmdbuild.g3d.Model.getGraphData(node, "className")
						+ " the sprite is " + sprite);
				return null;
			}
		},
		lineFromEdge: function(edge) {
			var source = edge.source();
			var target = edge.target();
			var p1 = $.Cmdbuild.g3d.ViewerUtilities.getCenterPosition(source);
			var p2 = $.Cmdbuild.g3d.ViewerUtilities.getCenterPosition(target);
			var material = new THREE.LineBasicMaterial({
				color: $.Cmdbuild.custom.configuration.edgeColor,
				linewidth: 1
			});

			var geometry = new THREE.Geometry();
			geometry.vertices.push(p1, p2);
			var line = new THREE.Line(geometry, material);
			line.source = source;
			line.target = target;
			line.label = edge.data("label");
			return line;
		},
		moveObject: function(viewer, node) {
			var STEPS = 10;
			var INTERVAL = 50;
			var me = this;
			var p = node.position();
			var glObject = node.glObject;
			var id = node.id();
			if ($.Cmdbuild.g3d.ViewerUtilities.equals(glObject.position, p)) {
				return;
			}

			if (movingNodes[id] != undefined) {
				movingNodes[id] = {
					nextPosition: node
				};
				return;
			}
			movingNodes[id] = {};
			var i = 1;
			this.TM = setInterval(function() {
				var glp = {
					x: glObject.position.x,
					y: glObject.position.y,
					z: glObject.position.z
				};
				glObject.position.copy({
					x: glp.x + ((p.x - glp.x) / STEPS) * i,
					y: glp.y + ((p.y - glp.y) / STEPS) * i,
					z: glp.z + ((p.z - glp.z) / STEPS) * i
				});
				if (node.selectionOnNode) {
					node.selectionOnNode.position.copy(glObject.position);
				}
				viewer.refreshNodeEdges(id, glObject.position);
				if (++i > STEPS) {
					clearInterval(me.TM);
					var nextPosition = movingNodes[id].nextPosition;
					delete movingNodes[id];
					if (nextPosition) {
						new $.Cmdbuild.g3d.ViewerUtilities.moveObject(viewer,
								nextPosition);
					}
				}
			}, INTERVAL);
		},
		modifyLine: function(scene, edge, p1, p2) {
			if (!edge.glLine) {
				console.log("Line not found!");
				return;
			}
			edge.glLine.geometry.vertices = [];
			edge.glLine.geometry.vertices.push(new THREE.Vector3(p1.x, p1.y,
					p1.z));
			edge.glLine.geometry.vertices.push(new THREE.Vector3(p2.x, p2.y,
					p2.z));
			edge.glLine.geometry.dynamic = true;
			edge.glLine.geometry.verticesNeedUpdate = true;
			edge.glLine.geometry.computeBoundingSphere();
		},
		clearScene: function(scene, model, nodes, edges) {
			for (var i = 0; i < edges.length; i++) {
				var edge = edges[i];
				scene.remove(edge);
			}
			for (var i = 0; i < nodes.length; i++) {
				var node = nodes[i];
				scene.remove(node);
			}
		},
		getCenterPosition: function(node) {
			return node.position();
		},
		equals: function(p1, p2) {
			var epsilon = 10;
			return !(Math.abs(p1.x - p2.x) > epsilon
					|| Math.abs(p1.y - p2.y) > epsilon || Math.abs(p1.z - p2.z) > epsilon);
		}
	};
	$.Cmdbuild.g3d.ViewerUtilities = ViewerUtilities;
})(jQuery);
