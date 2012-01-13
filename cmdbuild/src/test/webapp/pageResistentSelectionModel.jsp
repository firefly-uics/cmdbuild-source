<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>Selection model che tiene la selezione al cambio pagina</title>

		<link rel="stylesheet" type="text/css" href="javascript/ext-4.0.0/resources/css/ext-all.css" />

		<script type="text/javascript" src="javascript/ext-4.0.0/ext-all-debug.js"></script>
<!--		<script type="text/javascript" src="javascript/test/lib/OverrideForTesting.js"></script>-->

		<script type="text/javascript" src="javascript/app/view/TGrid.js"></script>
		<script type="text/javascript" src="javascript/app/controller/appController.js"></script>
		<script type="text/javascript" src="javascript/app/controller/TGridController.js"></script>
		<script type="text/javascript" src="javascript/app/controller/TGridControllerDelegate1.js"></script>

		<script type="text/javascript" src="javascript/sm/CMMultiPageSelectionModel.js"></script>
		<script type="text/javascript" src="javascript/sm/testSelectionModel.js"></script>
		<script type="text/javascript" src="javascript/sm/testSelectionModelController.js"></script>


		<script type="text/javascript">
			Ext.onReady(function() {
				new TestSelectionModel();
			});
		</script>
	</head>

	<body>

	</body>
</html>