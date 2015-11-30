(function($) {
	if (! $.Cmdbuild.g3d) {
		$.Cmdbuild.g3d = {};
	}
	if (! $.Cmdbuild.g3d.backend) {
		$.Cmdbuild.g3d.backend = {};
	}
	$.Cmdbuild.g3d.colors = {
			node: '#6FB1FC',
			selected: '#FFFF00',
			edge: "#FF9900"
	};
	var elements = {
			nodes: [{
				data: {
					id: 'j',
					name: 'Jerry',
					weight: 65,
					faveColor: $.Cmdbuild.g3d.colors.node,
					faveShape: 'triangle',
					position: {
						x : Math.random() * 1000 - 500,
						y : Math.random() * 600 - 300,
						z : Math.random() * 800 - 400
					},
					rotation: {
						x : 0, //Math.random() * 2 * Math.PI,
						y : 0, // Math.random() * 2 * Math.PI,
						z : 0 //Math.random() * 2 * Math.PI
					},
					scale: {
						x : 1, //Math.random() * 2 + 1,
						y : 1, //Math.random() * 2 + 1,
						z : 1  //Math.random() * 2 + 1
					}
				}
			}, {
				data: {
					id: 'e',
					name: 'Elaine',
					weight: 45,
					faveColor: $.Cmdbuild.g3d.colors.node,
					faveShape: 'ellipse',
					position: {
						x : Math.random() * 1000 - 500,
						y : Math.random() * 600 - 300,
						z : Math.random() * 800 - 400
					},
					rotation: {
						x : 0, //Math.random() * 2 * Math.PI,
						y : 0, // Math.random() * 2 * Math.PI,
						z : 0 //Math.random() * 2 * Math.PI
					},
					scale: {
						x : 1, //Math.random() * 2 + 1,
						y : 1, //Math.random() * 2 + 1,
						z : 1  //Math.random() * 2 + 1
					}


				}
			}, {
				data: {
					id: 'k',
					name: 'Kramer',
					weight: 75,
					faveColor: $.Cmdbuild.g3d.colors.node,
					faveShape: 'octagon',
					position: {
						x : Math.random() * 1000 - 500,
						y : Math.random() * 600 - 300,
						z : Math.random() * 800 - 400
					},
					rotation: {
						x : 0, //Math.random() * 2 * Math.PI,
						y : 0, // Math.random() * 2 * Math.PI,
						z : 0 //Math.random() * 2 * Math.PI
					},
					scale: {
						x : 1, //Math.random() * 2 + 1,
						y : 1, //Math.random() * 2 + 1,
						z : 1  //Math.random() * 2 + 1
					}


				}
			}, {
				data: {
					id: 'g',
					name: 'George',
					weight: 70,
					faveColor: $.Cmdbuild.g3d.colors.node,
					faveShape: 'rectangle',
					position: {
						x : Math.random() * 1000 - 500,
						y : Math.random() * 600 - 300,
						z : Math.random() * 800 - 400
					},
					rotation: {
						x : 0, //Math.random() * 2 * Math.PI,
						y : 0, // Math.random() * 2 * Math.PI,
						z : 0 //Math.random() * 2 * Math.PI
					},
					scale: {
						x : 1, //Math.random() * 2 + 1,
						y : 1, //Math.random() * 2 + 1,
						z : 1  //Math.random() * 2 + 1
					}


				}
			}],
			edges: [{
				data: {
					source: 'j',
					target: 'e',
					faveColor: $.Cmdbuild.g3d.colors.edge,
					strength: 90
				}
			}, {
				data: {
					source: 'j',
					target: 'k',
					faveColor: $.Cmdbuild.g3d.colors.edge,
					strength: 70
				}
			}, {
				data: {
					source: 'j',
					target: 'g',
					faveColor: $.Cmdbuild.g3d.colors.edge,
					strength: 80
				}
			},

			{
				data: {
					source: 'e',
					target: 'j',
					faveColor: $.Cmdbuild.g3d.colors.edge,
					strength: 95
				}
			}, {
				data: {
					source: 'e',
					target: 'k',
					faveColor: $.Cmdbuild.g3d.colors.edge,
					strength: 60
				},
				classes: 'questionable'
			},

			{
				data: {
					source: 'k',
					target: 'j',
					faveColor: $.Cmdbuild.g3d.colors.edge,
					strength: 100
				}
			}, {
				data: {
					source: 'k',
					target: 'e',
					faveColor: $.Cmdbuild.g3d.colors.edge,
					strength: 100
				}
			}, {
				data: {
					source: 'k',
					target: 'g',
					faveColor: $.Cmdbuild.g3d.colors.edge,
					strength: 100
				}
			},

			{
				data: {
					source: 'g',
					target: 'j',
					faveColor: $.Cmdbuild.g3d.colors.edge,
					strength: 90
				}
			}]
		};
	var BackendTest = function() {
		this.setModel = function(model) {
			this.model = model;
		};
		this.getInitModel = function() {
			return elements;
		};
		this.getANodesBunch = function() {
			var elements = {
				nodes: [],
				edges: []
			};
			for (var i = 0; i < 50; i++) {
				var data = {
					id: 'j' + i + parseInt(Math.random() * 10000),
					name: 'j' + i + parseInt(Math.random() * 10000),
					weight: 65,
					faveColor: $.Cmdbuild.g3d.colors.node,
					faveShape: 'triangle',
					position: {
						x : Math.random() * 1000 - 500,
						y : Math.random() * 600 - 300,
						z : Math.random() * 800 - 400
					}
				};
				var node = {
						data: data
				};
				var modelNodes = this.model.getNodes();
				for (var j = 0; j < 2; j++) {
					var index = parseInt(Math.random() * modelNodes.length);
					var edge = {
							source: data.id,
							target: modelNodes[index].id(),
							faveColor: $.Cmdbuild.g3d.colors.edge,
							strength: 90
						};
						elements.edges.push({
							data: edge
						});					
				}
				elements.nodes.push(node);
			}
			return elements;
		};
	};
	$.Cmdbuild.g3d.backend.BackendTest = BackendTest;
})(jQuery);
