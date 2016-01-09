(function($) {
	$('#cy').cytoscape({
		  elements: {
		    nodes: [
		    ],
		    edges: [
		    ]
		  },
		  
		  ready: function(){
		    window.cy = this;
		  }
	});
})(jQuery);

