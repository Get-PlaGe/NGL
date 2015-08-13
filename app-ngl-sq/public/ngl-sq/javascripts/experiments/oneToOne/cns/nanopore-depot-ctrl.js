angular.module('home').controller('NanoporeDepotCtrl',['$scope', '$parse', 'datatable','oneToOne','atmToSingleDatatable',
                                                               function($scope, $parse, datatable, oneToOne, atmToSingleDatatable) {
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
			        	 "header":Messages("containers.table.fromExperimentTypeCodes"),
			        	 "property":"inputContainer.fromExperimentTypeCodes",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			 			"render":"<div list-resize='cellValue | unique | codes:\"type\"' list-resize-min-size='3'>",
			        	 "position":4,
			        	 "extraHeaders":{0:"Inputs"}
			         },
			         					 
					 {
			        	 "header":function(){return Messages("containers.table.concentration") + " (ng/µL)"},
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
			        	 "header":Messages("containers.table.code"),
			        	 "property":"outputContainerUsed.code",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":400,
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
	        	mode:'local',
	        	changeClass:false,
				callback:function(datatable){
					copyOtherDTToMainDatatable(datatable);
				}
			},
			hide:{
				active:true
			},
			edit:{
				active: (!$scope.doneAndRecorded && !$scope.inProgressNow),
				columnMode:false
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
			},
			showTotalNumberRecords:false
	};
	
	
	var datatableConfigLoadingReport = {
			name:"NanoportLoadingReport",
			columns:[],
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
				by:'creationDate',
				reverse : true
			},
			remove:{
				active: true,
				showButton: true,
				mode:'local'
			},
			save:{
				active:true,
				showButton: false,
				mode:'local',
				changeClass:false
			},
			hide:{
				active:true
			},
			edit:{
				active: true,
				showButton: true,
				columnMode:false
			},
			add:{
				active:true
			},
			messages:{
				active:false,
				columnMode:true
			},
			extraHeaders:{
				number:1,
				dynamic:true,
			},
			showTotalNumberRecords:false
	};
	
	
	var datatableConfigQcFlowcell = {
			name:"NanoportQcFlowcell",
			columns:[],
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
				active:false,
			},
			save:{
				active:true,
				showButton: false,
				mode:'local',
				changeClass:false
			},
			hide:{
				active:true
			},
			edit:{
				active: !$scope.doneAndRecorded,
				showButton: true,
				columnMode:false
			},
			messages:{
				active:false,
				columnMode:true
			},
			extraHeaders:{
				number:1,
				dynamic:true,
			},
			showTotalNumberRecords:false
	};
	
	
	$scope.$on('save', function(e, promises, func, endPromises) {	
		console.log("call event save");
		$scope.datatableQcFlowcell.save();
		$scope.datatableLoadingReport.save();
		$scope.datatable.save();		
		$scope.atomicTransfere.viewToExperiment($scope.datatable);
		$scope.$emit('viewSaved', promises, func, endPromises);
	});
	
	//call by callback save datatable
	var copyOtherDTToMainDatatable = function(datatable){
		var dataMain = datatable.getData();
		var dataQCFlowcell = $scope.datatableQcFlowcell.getData();
		var dataLoadingReport = $scope.datatableLoadingReport.getData();
		
		$parse('outputContainerUsed.experimentProperties.qcFlowcell._type').assign(dataMain[0], "object_list");
		$parse('outputContainerUsed.experimentProperties.qcFlowcell.value').assign(dataMain[0], dataQCFlowcell);
		
		$parse('inputContainerUsed.experimentProperties.loadingReport._type').assign(dataMain[0], "object_list");		
		$parse('inputContainerUsed.experimentProperties.loadingReport.value').assign(dataMain[0], dataLoadingReport);
		
		//copy flowcell code to output code
		var codeFlowcell = $parse("instrumentProperties.containerSupportCode.value")($scope.experiment.value);
		$parse('outputContainerUsed.code').assign(dataMain[0],codeFlowcell);
		
		datatable.setData(dataMain);
	}
	
	$scope.$on('refresh', function(e) {
		console.log("call event refresh");		
		var dtConfig = $scope.datatable.getConfig();
		dtConfig.edit.active = (!$scope.doneAndRecorded && !$scope.inProgressNow);
		dtConfig.remove.active = (!$scope.doneAndRecorded && !$scope.inProgressNow);
		$scope.datatable.setConfig(dtConfig);
		
		$scope.atomicTransfere.refreshViewFromExperiment($scope.datatable);
		$scope.$emit('viewRefeshed');
	});
	
	//Init
	var qcFlowcellDefault =[{group: "total", preLoadingNbActivePores: undefined, postLoadingNbActivePores: undefined}, 
	    	                {group: "groupe1", preLoadingNbActivePores: undefined, postLoadingNbActivePores:undefined},
	    	                {group: "groupe2", preLoadingNbActivePores: undefined, postLoadingNbActivePores:undefined},
	    	                {group: "groupe3", preLoadingNbActivePores: undefined, postLoadingNbActivePores:undefined},
	    	                {group: "groupe4", preLoadingNbActivePores: undefined, postLoadingNbActivePores:undefined}];
	    	
	$scope.datatable = datatable(datatableConfig);
	$scope.datatableLoadingReport = datatable(datatableConfigLoadingReport);
	$scope.datatableLoadingReport.setData([]);
	$scope.datatableQcFlowcell = datatable(datatableConfigQcFlowcell);
	$scope.datatableQcFlowcell.setData(qcFlowcellDefault);
	
	var mainAtmView = atmToSingleDatatable($scope);
	
	//overide defaut method
	mainAtmView.convertOutputPropertiesToDatatableColumn = function(property){
		if(property.propertyValueType === "single"){
			return  this.$commonATM.convertSinglePropertyToDatatableColumn(property,"outputContainerUsed.experimentProperties.",{"0":"Outputs"});
		}else if(property.propertyValueType === "object_list"){
			var newColum = this.$commonATM.convertObjectListPropertyToDatatableColumn(property,"",{"0":"QC Flowcell"});
			var columns = $scope.datatableQcFlowcell.getColumnsConfig();
			columns.push(newColum);
			$scope.datatableQcFlowcell.setColumnsConfig(columns);
			return undefined;						
		}		
	};
	mainAtmView.convertInputPropertiesToDatatableColumn = function(property){
		if(property.propertyValueType === "single"){
			return this.$commonATM.convertSinglePropertyToDatatableColumn(property,"inputContainerUsed.experimentProperties.",{"0":"Inputs"});
		}else if(property.propertyValueType === "object_list"){
			var newColum = this.$commonATM.convertObjectListPropertyToDatatableColumn(property,"",{"0":"Bilan chargement"});
			var columns = $scope.datatableLoadingReport.getColumnsConfig();
			columns.push(newColum);
			$scope.datatableLoadingReport.setColumnsConfig(columns);
			return undefined;
		}
		
	};
	//custom view for the two other datatable
	mainAtmView.customExperimentToView = function(atm){
		var loadingReportData = $parse('inputContainerUseds[0].experimentProperties.loadingReport.value')(atm);
		if(null != loadingReportData && undefined !== loadingReportData)
			$scope.datatableLoadingReport.setData(loadingReportData);
		
		var qcFlowcellData = $parse('outputContainerUseds[0].experimentProperties.qcFlowcell.value')(atm);
		if(null != qcFlowcellData && undefined !== qcFlowcellData)
			$scope.datatableQcFlowcell.setData(qcFlowcellData);
	}
	
	$scope.atomicTransfere = oneToOne($scope, mainAtmView);
	$scope.atomicTransfere.defaultOutputUnit.volume = "µL";
	$scope.atomicTransfere.defaultOutputUnit.concentration = "ng/µL";
	$scope.atomicTransfere.defaultOutputUnit.quantity = "ng";
	
	$scope.atomicTransfere.experimentToView($scope.datatable);	
}]);