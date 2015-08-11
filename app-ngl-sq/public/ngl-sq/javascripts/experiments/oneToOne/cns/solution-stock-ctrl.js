angular.module('home').controller('SolutionStockCtrl',['$scope', '$window','datatable','$http','lists','$parse','$q','$position','oneToOne','mainService','tabService', function($scope,$window, datatable, $http,lists,$parse,$q,$position,oneToOne,mainService,tabService) {
	var datatableConfig = {
			name:"FDR_Tube",
			columns:[			  
					 {
			        	 "header":Messages("containers.table.code"),
			        	 "property":"inputContainer.code",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":1,
			        	 "extraHeaders":{0:"Inputs"}
			         },
			         {
			        	"header":Messages("containers.table.projectCodes"),
			 			"property": "inputContainer.projectCodes",
			 			"order":false,
			 			"hide":true,
			 			"type":"text",
			 			"position":2,
			 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	 "extraHeaders":{0:"Inputs"}
				     },
				     {
			        	"header":Messages("containers.table.sampleCodes"),
			 			"property": "inputContainer.sampleCodes",
			 			"order":false,
			 			"hide":true,
			 			"type":"text",
			 			"position":3,
			 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	 "extraHeaders":{0:"Inputs"}
				     },
			         {
			        	"header":Messages("containers.table.tags"),
			 			"property": "inputContainer.contents",
			 			"order":true,
			 			"hide":true,
			 			"type":"text",
			 			"position":4,
			 			"render":"<div list-resize='value.data.contents | getArray:\"properties.tag.value\" | unique' ' list-resize-min-size='3'>",
			        	 "extraHeaders":{0:"Inputs"}
			         },
								 
					 {
			        	 "header":function(){return Messages("containers.table.concentration") + " (nM)"},
			        	 "property":"inputContainer.mesuredConcentration.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":5,
			        	 "extraHeaders":{0:"Inputs"}
			         },
			         {
			        	 "header":function(){return Messages("containers.table.volume") + " (µL)"},
			        	 "property":"inputContainer.mesuredVolume.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":6,
			        	 "extraHeaders":{0:"Inputs"}
			         },
			         {
			        	 "header":Messages("containers.table.state.code"),
			        	 "property":"inputContainer.state.code",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
						 "filter":"codes:'state'",
			        	 "position":7,
			        	 "extraHeaders":{0:"Inputs"}
			         },		
			         {
			        	 "header":function(){return Messages("containers.table.concentration") + " (nM)"},
			        	 "property":"outputContainerUsed.concentration.value",
			        	 "order":true,
						 "edit":true,
						 "hide":true,
			        	 "type":"number",
			        	 "defaultValues":10,
			        	 "position":50,
			        	 "extraHeaders":{0:"Outputs"}
			         },
			         {
			        	 "header":function(){return Messages("containers.table.volume")+ " (µL)"},
			        	 "property":"outputContainerUsed.volume.value",
			        	 "order":true,
						 "edit":true,
						 "hide":true,
			        	 "type":"number",
			        	 "position":51,
			        	 "extraHeaders":{0:"Outputs"}
			         },
			         {
			        	 "header":Messages("containers.table.code"),
			        	 "property":"outputContainerUsed.code",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":52,
			        	 "extraHeaders":{0:"Outputs"}
			         },
			         {
			        	 "header":Messages("containers.table.stateCode"),
			        	 "property":"outputContainer.state.code | codes:'state'",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":500,
			        	 "extraHeaders":{0:"Outputs"}
			         }
			         ],
			compact:true,
			pagination:{
				active:false
			},		
			search:{
				active:false
			},
			order:{
				mode:'local', //or 
				active:true,
				by:'code'
			},
			remove:{
				active: (!$scope.doneAndRecorded && !$scope.inProgressNow),
				showButton: (!$scope.doneAndRecorded && !$scope.inProgressNow),
				mode:'local'
			},
			save:{
				active:true,
	        	withoutEdit: true,
	        	showButton:false,
	        	mode:'local'
			},
			hide:{
				active:true
			},
			edit:{
				active: (!$scope.doneAndRecorded && !$scope.inProgressNow),
				columnMode:true
			},
			messages:{
				active:false,
				columnMode:true
			},
			exportCSV:{
				active:true,
				showButton:true,
				delimiter:";",
				start:false
			},
			extraHeaders:{
				number:2,
				dynamic:true,
			},
			otherButton:{
				active:true,
				template:'<button class="btn btn btn-info" ng-click="newPurif()" data-toggle="tooltip" ng-disabled="experiment.value.state.code != \'F\'" ng-hide="!experiment.doPurif" title="'+Messages("experiments.addpurif")+'">Messages("experiments.addpurif")</button><button class="btn btn btn-info" ng-click="newQc()" data-toggle="tooltip" ng-disabled="experiment.value.state.code != \'F\'" ng-hide="!experiment.doQc" title="Messages("experiments.addqc")">Messages("experiments.addqc")</button>'
			}
	};
	
	$scope.$on('deleteInstrumentProperties', function(e, header) {		
		$scope.atomicTransfere.deleteDatatableColumnFromHeader($scope.datatable, header);				
	});
	
	$scope.$on('addInstrumentPropertiesInput', function(e, data, possibleValues) {
		console.log("call event addInstrumentPropertiesInput");
		var column = $scope.datatable.newColumn(data.name,"inputContainerUsed.instrumentProperties."+data.code+".value",data.editable, true,true,$scope.getPropertyColumnType(data.valueType),data.choiceInList,possibleValues,{"0":"Inputs","1":"Instruments"});
		column.defaultValues = data.defaultValue;
		column.position = data.displayOrder;
		$scope.datatable.addColumn(2,column);
	});
	
	$scope.$on('addExperimentPropertiesInput', function(e, data, possibleValues) {
		console.log("call event addExperimentPropertiesInput");
		var unit = "";
		if(data.displayMeasureValue!=undefined) unit = "("+data.displayMeasureValue.value+")";
		var column = $scope.datatable.newColumn(function(){return data.name+" "+unit;},"inputContainerUsed.experimentProperties."+data.code+".value",data.editable, true,true,$scope.getPropertyColumnType(data.valueType),data.choiceInList,possibleValues,{"0":"Inputs"});
		column.defaultValues = data.defaultValue;
		column.position = data.displayOrder;
		$scope.datatable.addColumn(data.displayOrder,column);
	});
	
	$scope.$on('addExperimentPropertiesOutput', function(e, data, possibleValues) {
		console.log("call event addExperimentPropertiesOutput");
		var unit = "";
		if(data.displayMeasureValue!=undefined) unit = "("+data.displayMeasureValue.value+")";
		var column = $scope.datatable.newColumn(function(){return data.name+" "+unit;},"outputContainerUsed.experimentProperties."+data.code+".value",data.editable, true,true,$scope.getPropertyColumnType(data.valueType),data.choiceInList,possibleValues,{"0":"Outputs"});
		column.defaultValues = data.defaultValue;
		column.position = data.displayOrder;
		if(data.displayMeasureValue != undefined && data.displayMeasureValue != null){
			column.convertValue = {"active":true, "displayMeasureValue":data.displayMeasureValue.value, "saveMeasureValue":data.saveMeasureValue.value};
		}
		$scope.datatable.addColumn(-1,column);
	});
	
	$scope.$on('addInstrumentPropertiesOutput', function(e, data, possibleValues) {
		console.log("call event addInstrumentPropertiesOutput");
		var column = $scope.datatable.newColumn(data.name,"outputContainerUsed.instrumentProperties."+data.code+".value",data.editable, true,true,$scope.getPropertyColumnType(data.valueType),data.choiceInList,possibleValues,{"0":"Outputs","1":"Instruments"});
		column.defaultValues = data.defaultValue;
		column.position = data.displayOrder;
		$scope.datatable.addColumn(-1,column);
	});
	
	
	$scope.$on('save', function(e, promises, func, endPromises) {	
		console.log("call event save");
		$scope.datatable.save();
		$scope.atomicTransfere.viewToExperiment($scope.datatable);
		$scope.$emit('viewSaved', promises, func, endPromises);
	});
	
	$scope.$on('refresh', function(e) {
		console.log("call event refresh");
		
		var dtConfig = $scope.datatable.getConfig();
		dtConfig.edit.active = (!$scope.doneAndRecorded && !$scope.inProgressNow);
		dtConfig.remove.active = (!$scope.doneAndRecorded && !$scope.inProgressNow);
		dtConfig.remove.active = (!$scope.doneAndRecorded && !$scope.inProgressNow);
		$scope.datatable.setConfig(dtConfig);
		
		$scope.atomicTransfere.refreshViewFromExperiment($scope.datatable);
		$scope.$emit('viewRefeshed');
	});
	
	
	$scope.$on('outputToExperiment', function(e, atomicTransfertMethod) {
		console.log("call event outputToExperiment");
		//$scope.atomicTransfere.outputToExperiment($scope.datatable);		
	});
	$scope.$on('inputToExperiment', function(e, atomicTransfertMethod) {
		console.log("call event inputToExperiment");
		//$scope.atomicTransfere.inputToExperiment($scope.datatable);		
	});
	
	$scope.$on('disableEditMode', function(){
		console.log("call event disableEditMode");
		//$scope.datatable.config.edit.active = false;
	});
	
	$scope.$on('enableEditMode', function(){
		console.log("call event enableEditMode");
		//$scope.datatable.config.edit.active = true;
	});
	
	
	//Init
	$scope.datatable = datatable(datatableConfig);
	$scope.atomicTransfere = oneToOne($scope, "datatable", "datatable");
	$scope.atomicTransfere.experimentToView($scope.datatable);	
}]);