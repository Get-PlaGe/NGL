// FDS 27/09/2016 copié depuis prep-pcr-free-ctrl.js car qq differences...

angular.module('home').controller('PrepWgNanoCtrl',['$scope', '$parse',  '$filter', 'atmToSingleDatatable','$http',
                                                     function($scope, $parse, $filter, atmToSingleDatatable, $http){

	
	var inputExtraHeaders=Messages("experiments.inputs");
	var outputExtraHeaders=Messages("experiments.outputs");	
	
	// NGL-1055: name explicite pour fichier CSV exporté: typeCode experience
	var datatableConfig = {
			name: $scope.experiment.typeCode.toUpperCase(),
			//Guillaume le 04/03 => utiliser containerUsed seulement pour proprietes dynamiques...
			"columns":[
			         //--------------------- INPUT containers section -----------------------
			         
			         /* plus parlant pour l'utilisateur d'avoir Plate barcode | line | column
					  {
			        	 "header":Messages("containers.table.code"),
			        	 "property":"inputContainer.code",
			        	 "order":true,
						 "hide":true,
			        	 "type":"text",
			        	 "position":0,
			        	 "extraHeaders":{0: inputExtraHeaders}
			          },	
			          */		        
			          { // barcode plaque entree == input support Container code
			        	 "header":Messages("containers.table.support.name"),
			        	 "property":"inputContainer.support.code",
						 "hide":true,
			        	 "type":"text",
			        	 "position":1,
			        	 "extraHeaders":{0: inputExtraHeaders}
			         },    
			         { // Ligne
			        	 "header":Messages("containers.table.support.line"),
			        	 "property":"inputContainer.support.line",
			        	 "order":true,
						 "hide":true,
			        	 "type":"text",
			        	 "position":2,
			        	 "extraHeaders":{0: inputExtraHeaders}
			         },
			         { // colonne
			        	 "header":Messages("containers.table.support.column"),
			        	 // astuce GA: pour pouvoir trier les colonnes dans l'ordre naturel forcer a numerique.=> type:number,   property:  *1
			        	 "property":"inputContainer.support.column*1",
			        	 "order":true,
						 "hide":true,
			        	 "type":"number",
			        	 "position":3,
			        	 "extraHeaders":{0: inputExtraHeaders}
			         },	  
			         { // Projet(s)
			        	"header":Messages("containers.table.projectCodes"),
			 			"property": "inputContainer.projectCodes",
			 			"order":true,
			 			"hide":true,
			 			"type":"text",
			 			"position":11,
			 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	 "extraHeaders":{0: inputExtraHeaders}
				     },
				     { // Echantillon(s) 
			        	"header":Messages("containers.table.sampleCodes"),
			 			"property": "inputContainer.sampleCodes",
			 			"order":true,
			 			"hide":true,
			 			"type":"text",
			 			"position":12,
			 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	"extraHeaders":{0: inputExtraHeaders}
				     },
				   /*  { // sampleAliquoteCode 
				        "header":Messages("containers.table.codeAliquot"),
				 		"property": "inputContainer.contents", 
				 		"filter": "getArray:'properties.sampleAliquoteCode.value'",
				 		"order":true,
				 		"hide":true,
				 		"type":"text",
				 		"position":13,
				 		"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
				        "extraHeaders":{0: inputExtraHeaders}
					 },
					*/
					 { // Concentration
			        	 "header":Messages("containers.table.concentration") + " (ng/µL)",
			        	 "property":"inputContainerUsed.concentration.value",
			        	 "order":true,
						 "hide":true,
			        	 "type":"number",
			        	 "position":14,
			        	 "extraHeaders":{0: inputExtraHeaders}
			         },  
			         { // Volume
			        	 "header":Messages("containers.table.volume") + " (µL)",
			        	 "property":"inputContainerUsed.volume.value",
			        	 "order":true,
						 "hide":true,
			        	 "type":"number",
			        	 "position":15,
			        	 "extraHeaders":{0: inputExtraHeaders}
			         },
			         { // Etat input Container 
			        	 "header":Messages("containers.table.state.code"),
			        	 "property":"inputContainer.state.code | codes:'state'",
			        	 "order":true,
						 "hide":true,
			        	 "type":"text",
			        	 "position":16,
			        	 "extraHeaders":{0: inputExtraHeaders}
			         },
			         //--->  colonnes specifiques experience s'inserent ici  (inputUsed ??)     
			         
			         //------------------------- OUTPUT containers section --------------------------
			         
			         //--->  colonnes specifiques experience s'inserent ici  (outputUsed ??)
			         
		            /* ne pas aficher les containercodes  sauf pour DEBUG
			         {
			        	 "header":"DEBUG code",
			        	 "property":"outputContainer.code",
			        	 "order":true,
						 "hide":true,
						 "edit":false,
			        	 "type":"text",
			        	 "position":99,
			        	 "extraHeaders":{0: outputExtraHeaders}
			         },*/
			        
			          { // Volume avec valeur par defaut
			        	 "header":Messages("containers.table.volume") + " (µL)",
			        	 "property":"outputContainerUsed.volume.value",
			        	 "hide":true,
			        	 "edit":true,
			        	 "type":"number",
			        	 "position":34,
			        	 "extraHeaders":{0: outputExtraHeaders}
			         },
			        
			         { //  barcode plaque sortie == support Container used code... faut Used 
			        	 "header":Messages("containers.table.support.name"),
			        	 "property":"outputContainerUsed.locationOnContainerSupport.code", 
						 "hide":true,
			        	 "type":"text",
			        	 "position":35,
			        	 "extraHeaders":{0: outputExtraHeaders}
			         },  
			         { //  Ligne 
			        	 "header":Messages("containers.table.support.line"),
			        	 "property":"outputContainerUsed.locationOnContainerSupport.line", 
			        	 "order":true,
						 "hide":true,
			        	 "type":"text",
			        	 "position":36,
			        	 "extraHeaders":{0: outputExtraHeaders}
			         },     
			         { // colonne
			        	 "header":Messages("containers.table.support.column"),
			        	 // astuce GA: pour pouvoir trier les colonnes dans l'ordre naturel forcer a numerique.=> type:number,   property:  *1
			        	 "property":"outputContainerUsed.locationOnContainerSupport.column*1", 
			        	 "order":true,
						 "hide":true,
			        	 "type":"number",
			        	 "position":37,
			        	 "extraHeaders":{0: outputExtraHeaders}
			         },	
			         { // Etat outpout container      
			        	 "header":Messages("containers.table.state.code"),
			        	 "property":"outputContainer.state.code | codes:'state'",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":40,
			        	 "extraHeaders":{0: outputExtraHeaders}
			         }
			         ],
			"compact":true,
			"pagination":{
				"active":false
			},		
			"search":{
				"active":false
			},
			"order":{
				"mode":"local",
				"active":true//,
				// FDS : ce tri donne 1,10,11,12,2.... comment avoir un tri 1,2....10,11,12,13 ??
				//"by":"inputContainer.support.column*1"
			},
			"remove":{
				"active": ($scope.isEditModeAvailable() && $scope.isNewState()),
				"showButton": ($scope.isEditModeAvailable() && $scope.isNewState()),
				"mode":"local"
			},
			"save":{
				"active":true,
	        	"withoutEdit": true,
	        	"changeClass":false,
	        	"showButton":false,
	        	"mode":"local",
	        	"callback":function(datatable){
	        		copyContainerSupportCodeAndStorageCodeToDT(datatable);
	        	}
			},
			"hide":{
				"active":true
			},
			"edit":{ 
				active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
				showButton: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
				byDefault:($scope.isCreationMode()),
				columnMode:true
			},
			"messages":{
				"active":false,
				"columnMode":true
			},
			"exportCSV":{
				"active":true,
				"showButton":true,
				"delimiter":";",
				"start":false
			},
			"extraHeaders":{
				"number":2,
				"dynamic":true,
			},
			"otherButtons": {
                active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
                complex:true,
                template:''
                	+'<div class="btn-group" style="margin-left:5px">'
                	+'<button class="btn btn-default" ng-click="copyVolumeInToExp()" data-toggle="tooltip" title="'+Messages("experiments.button.plate.copyVolumeTo")+' vol. eng. librairie ET vol. eng. fragmentation'
                	+'" ng-disabled="!isEditMode()" ng-if="experiment.instrument.outContainerSupportCategoryCode!==\'tube\'"><i class="fa fa-files-o" aria-hidden="true"></i> Volume </button>'                	                	
                	+'</div>'
			}
	}; // fin struct datatableConfig
	
	
	$scope.$on('save', function(e, callbackFunction) {	
		console.log("call event save");
		$scope.atmService.data.save();
		$scope.atmService.viewToExperimentOneToOne($scope.experiment);
		$scope.$emit('childSaved', callbackFunction);
	});
	
	var copyContainerSupportCodeAndStorageCodeToDT = function(datatable){

		var dataMain = datatable.getData();
		
		var outputContainerSupportCode = $scope.outputContainerSupport.code;
		var outputContainerSupportStorageCode = $scope.outputContainerSupport.storageCode;

		if ( null != outputContainerSupportCode && undefined != outputContainerSupportCode){
			for(var i = 0; i < dataMain.length; i++){
				
				var atm = dataMain[i].atomicTransfertMethod;
				var newContainerCode = outputContainerSupportCode+"_"+atm.line + atm.column;

				$parse('outputContainerUsed.code').assign(dataMain[i],newContainerCode);
				$parse('outputContainerUsed.locationOnContainerSupport.code').assign(dataMain[i],outputContainerSupportCode);
				
				if( null != outputContainerSupportStorageCode && undefined != outputContainerSupportStorageCode){
				    $parse('outputContainerUsed.locationOnContainerSupport.storageCode').assign(dataMain[i],outputContainerSupportStorageCode);
				}
			}
		}
		
		//ne plus faire...datatable.setData(dataMain);
	}
	
	// ajout showButton + suppression start = false;
	$scope.$on('refresh', function(e) {
		console.log("call event refresh");		
		var dtConfig = $scope.atmService.data.getConfig();
		dtConfig.edit.active = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));
		dtConfig.edit.showButton = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));
		dtConfig.edit.byDefault = false;
		dtConfig.remove.active = ($scope.isEditModeAvailable() && $scope.isNewState());
		$scope.atmService.data.setConfig(dtConfig);
		$scope.atmService.refreshViewFromExperiment($scope.experiment);
		$scope.$emit('viewRefeshed');
	});
	
	$scope.$on('cancel', function(e) {
		console.log("call event cancel");
		$scope.atmService.data.cancel();
		
		if($scope.isCreationMode()){
			var dtConfig = $scope.atmService.data.getConfig();
			dtConfig.edit.byDefault = false;
			$scope.atmService.data.setConfig(dtConfig);
		}
		
	});
	
	$scope.$on('activeEditMode', function(e) {
		console.log("call event activeEditMode");
		$scope.atmService.data.selectAll(true);
		$scope.atmService.data.setEdit();
	});
	
    // 24/11/2016 FDS copier le volume containerIn dans le volume engagé Librairie ET volume Engagé Frag...
	//     code adapté depuis copyVolumeInToOut de x-to-plates-ctrl.js
	$scope.copyVolumeInToExp = function(){
		console.log("copyVolumeInToExp");
		
		var data = $scope.atmService.data.displayResult;		
		data.forEach(function(value){
			
			if ( !value.data.inputContainerUsed.experimentProperties ){
				value.data.inputContainerUsed.experimentProperties = {};
			}
			value.data.inputContainerUsed.experimentProperties.inputVolumeLib=value.data.inputContainerUsed.volume;
			value.data.inputContainerUsed.experimentProperties.inputVolumeFrag=value.data.inputContainerUsed.volume;	
		})		
	};
		
	//Init
	
	var atmService = atmToSingleDatatable($scope, datatableConfig);
	//defined new atomictransfertMethod
	atmService.newAtomicTransfertMethod = function(l, c){
		return {
			class:"OneToOne",
			line: l, 
			column: c, 				
			inputContainerUseds:new Array(0), 
			outputContainerUseds:new Array(0)
		};
	};
	
	//defined default output unit
	atmService.defaultOutputUnit = {
			volume : "µL"
	}
	
	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	$scope.atmService = atmService;
	
	var importData = function(){
		$scope.messages.clear();

		$http.post(jsRoutes.controllers.instruments.io.IO.importFile($scope.experiment.code).url, $scope.file)
		.success(function(data, status, headers, config) {
			
			$scope.messages.clazz="alert alert-success";
			$scope.messages.text=Messages('experiments.msg.import.success');
			$scope.messages.showDetails = false;
			$scope.messages.open();	
			//only atm because we cannot override directly experiment on scope.parent
			$scope.experiment.atomicTransfertMethods = data.atomicTransfertMethods;
			$scope.file = undefined;
			// reinit select File...
			angular.element('#importFile')[0].value = null;
			$scope.$emit('refresh');
			
		})
		.error(function(data, status, headers, config) {
			
			$scope.messages.clazz = "alert alert-danger";
			$scope.messages.text = Messages('experiments.msg.import.error');
			$scope.messages.setDetails(data);
			$scope.messages.open();	
			$scope.file = undefined;
			// reinit select File...
			angular.element('#importFile')[0].value = null;
		});		
	};
	
	$scope.outputContainerSupport = { code : null , storageCode : null};	
		
	if ( undefined !== $scope.experiment.atomicTransfertMethods[0]) { 
		 $scope.outputContainerSupport.code=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.code;
		//console.log("previous code: "+ $scope.outputContainerSupport.code);
	}
	if ( undefined !== $scope.experiment.atomicTransfertMethods[0]) {
		$scope.outputContainerSupport.storageCode=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.storageCode;
		//console.log("previous storageCode: "+ $scope.outputContainerSupport.storageCode);
	}
	
	// importer un fichier definissant quels index sont déposés dans quels containers
	$scope.button = {
		isShow:function(){
			return ( $scope.isInProgressState() && !$scope.mainService.isEditMode())
			},
		isFileSet:function(){
			return ($scope.file === undefined)?"disabled":"";
		},
		click:importData,		
	};
	
	// Autre mode possible : utiliser une plaque d'index prédéfinis, l'utilisateur a juste a indiquer a partir de quelle colonne
	// de cette plaque le robot doit prelever les index
	$scope.columns = [ {name:'---', position:-1 },
	                   {name:'1', position:0}, {name:'2', position:8}, {name:'3', position:16}, {name:'4',  position:24}, {name:'5',  position:32}, {name:'6',  position:40},
	                   {name:'7', position:48},{name:'8', position:56},{name:'9', position:64}, {name:'10', position:72}, {name:'11', position:80}, {name:'12', position:88},
	                 ];
	$scope.tagPlateColumn = $scope.columns[0]; // defaut du select
	
	 //actuellement 1 seule definie
	$scope.plates = [ {name:"DAP TruSeq DNA HT", tagCategory:"DUAL-INDEX"} ];
	$scope.tagPlate = $scope.plates[0]; // defaut du select
	
	// pour l'instant une seule plaque => faire un simple tableau
	// l'indice dans le tableau correspond a l'ordre "colonne d'abord" dans la plaque
	var tagPlateCode=[];
	tagPlateCode.push("D701-D501", "D701-D502", "D701-D503", "D701-D504", "D701-D505", "D701-D506", "D701-D507", "D701-D508");
	tagPlateCode.push("D702-D501", "D702-D502", "D702-D503", "D702-D504", "D702-D505", "D702-D506", "D702-D507", "D702-D508");
	tagPlateCode.push("D703-D501", "D703-D502", "D703-D503", "D703-D504", "D703-D505", "D703-D506", "D703-D507", "D703-D508");
	tagPlateCode.push("D704-D501", "D704-D502", "D704-D503", "D704-D504", "D704-D505", "D704-D506", "D704-D507", "D704-D508"); 
	tagPlateCode.push("D705-D501", "D705-D502", "D705-D503", "D705-D504", "D705-D505", "D705-D506", "D705-D507", "D705-D508"); 
	tagPlateCode.push("D706-D501", "D706-D502", "D706-D503", "D706-D504", "D706-D505", "D706-D506", "D706-D507", "D706-D508"); 
	tagPlateCode.push("D707-D501", "D707-D502", "D707-D503", "D707-D504", "D707-D505", "D707-D506", "D707-D507", "D707-D508"); 
	tagPlateCode.push("D708-D501", "D708-D502", "D708-D503", "D708-D504", "D708-D505", "D708-D506", "D708-D507", "D708-D508"); 
	tagPlateCode.push("D709-D501", "D709-D502", "D709-D503", "D709-D504", "D709-D505", "D709-D506", "D709-D507", "D709-D508"); 
	tagPlateCode.push("D710-D501", "D710-D502", "D710-D503", "D710-D504", "D710-D505", "D710-D506", "D710-D507", "D710-D508"); 
	tagPlateCode.push("D711-D501", "D711-D502", "D711-D503", "D711-D504", "D711-D505", "D711-D506", "D711-D507", "D711-D508"); 
	tagPlateCode.push("D712-D501", "D712-D502", "D712-D503", "D712-D504", "D712-D505", "D712-D506", "D712-D507", "D712-D508"); 
	
	var setTags = function(){
		$scope.messages.clear();
		//console.log("selected column=" +$scope.tagPlateColumn.name);
		
        var dataMain = atmService.data.getData();
        // trier dans l'ordre "colonne d'abord"
        var dataMain = $filter('orderBy')(dataMain, ['atomicTransfertMethod.column*1','atomicTransfertMethod.line']);
        
       //attention certains choix de colonne sont incorrrects !!!
		if  ($scope.tagPlateColumn.position + dataMain.length > 96 ) {			
			$scope.messages.clazz="alert alert-danger";
			//$scope.messages.text=Messages('select.WrongStartColumnTagPlate'+ " "+$scope.tagPlateColumn.position +"+"+dataMain.length+"="+ ($scope.tagPlateColumn.position + dataMain.length));
			$scope.messages.text=Messages('select.wrongStartColumnTagPlate');
			$scope.messages.showDetails = false;
			$scope.messages.open();	
			return;
		}
       
	    for(var i = 0; i < dataMain.length; i++){
			var udtData = dataMain[i];
			var ocu=udtData.outputContainerUsed;
			//console.log("outputContainerUsed.code"+udtData.outputContainerUsed.code);
			
			if ($scope.tagPlateColumn.position != -1 ){
				//calculer la position sur la plaque:   pos= (col -1)*8 + line      (line est le code ascii - 65)
				var libPos= (udtData.atomicTransfertMethod.column  -1 )*8 + ( udtData.atomicTransfertMethod.line.charCodeAt(0) -65);
				var indexPos= libPos + $scope.tagPlateColumn.position;
				//console.log("=> setting index "+indexPos+ ": "+ tagPlateCode[indexPos] );
				
				//ajouter dans experimentProperties les PSV tagCategory et tag
				var ocu=udtData.outputContainerUsed;
				if(ocu.experimentProperties===undefined || ocu.experimentProperties===null){
					ocu.experimentProperties={};
				}				
				ocu.experimentProperties["tag"]={"_type":"single","value":tagPlateCode[indexPos]};
				ocu.experimentProperties["tagCategory"]={"_type":"single","value":$scope.tagPlate.tagCategory};

			} else {
				//l'utilisateur n'a rien selectionné => suprimer les PSV tagCategory et tagCode 
				
				ocu.experimentProperties["tag"]= undefined;
				ocu.experimentProperties["tagCategory"]=undefined;
			}
		}	
	    atmService.data.setData(dataMain);
	};
	
	$scope.selectCol = {
		isShow:function(){
			return ( $scope.isInProgressState() && !$scope.mainService.isEditMode())
			},	
		select:setTags,
	};
	
	// 27/09/2016 calculer les qtés inputQuantityFrag et inputQuantityLib a partir de inputVolumeFrag et inputVolumeLib
	// updatePropertyFromUDT  est automatiqut defini pour les colonnes injectees dans le datatable....
	$scope.updatePropertyFromUDT = function(value, col){
		//console.log("update from property : "+col.property);

		if(col.property === 'inputContainerUsed.experimentProperties.inputVolumeFrag.value'){
			// verifier si le volume saisi est > au volume IN:  si oui ecraser le volume saisi par volume IN
			// TODO...?? plus tard
			computeQuantityFrag(value.data);
		}
		
		if(col.property === 'inputContainerUsed.experimentProperties.inputVolumeLib.value'){
			// verifier si le volume saisi est > au volume IN:  si oui ecraser le volume saisi par volume IN
			// TODO...?? plus tard
			computeQuantityLib(value.data);
		}
	}

	//inputQuantity=inputContainerUsed.concentration.value * inputContainerUsed.experimentProperties.inputVolume.value
	var computeQuantityFrag = function(udtData){
		var getter = $parse("inputContainerUsed.experimentProperties.inputQuantityFrag.value");
		var getter2 = $parse("inputContainerUsed.experimentProperties.inputVolumeLib.value");

		var compute = {
				inputConc : $parse("inputContainerUsed.concentration.value")(udtData),
				inputVolume : $parse("inputContainerUsed.experimentProperties.inputVolumeFrag.value")(udtData),		
				isReady:function(){
					// traiter le cas ou il y a 1 des 2 valeurs (en general c'est la conc) est a 0
					return (this.inputConc >= 0  && this.inputVolume >= 0);
				}
			};
		
		if(compute.isReady()){
			var result = $parse("inputConc * inputVolume")(compute);
			//console.log("result = "+result);
			
			if(angular.isNumber(result) && !isNaN(result)){
				inputQuantity = Math.round(result*10)/10;				
			}else{
				//inputQuantity = undefined;
				inputQuantity = 0;
			}	
			getter.assign(udtData, inputQuantity);
			
			//copie inputVolumeLib--> inputVolumeFrag
			getter2.assign(udtData, $parse("inputVolume")(compute));
			
		}else{
			console.log("Missing values to exec computeQuantityFrag");
		}
	}
	
	/// TODO: faire une seule fonction mais avec un parametre Lib ou Frag
	var computeQuantityLib = function(udtData){
		var getter = $parse("inputContainerUsed.experimentProperties.inputQuantityLib.value");

		var compute = {
				inputConc : $parse("inputContainerUsed.concentration.value")(udtData),
				inputVolume : $parse("inputContainerUsed.experimentProperties.inputVolumeLib.value")(udtData),		
				isReady:function(){
					// traiter le cas ou il y a 1 des 2 valeurs (en general c'est la conc) est a 0
					return (this.inputConc >= 0 && this.inputVolume >= 0 );
				}
			};
		
		if(compute.isReady()){
			var result = $parse("inputConc * inputVolume")(compute);
			console.log("result = "+result);
			
			if(angular.isNumber(result) && !isNaN(result)){
				inputQuantity = Math.round(result*10)/10;				
			}else{
				///inputQuantity = undefined;\\
				inputQuantity = 0;
			}	
			getter.assign(udtData, inputQuantity);
			
		}else{
			console.log("Missing values to exec computeQuantityLib");
		}
	}	
    
}]);