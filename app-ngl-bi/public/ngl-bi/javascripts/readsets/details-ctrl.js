 "use strict";

 angular.module('home').controller('DetailsCtrl', ['$scope', '$http', '$q', '$routeParams', '$sce', 'mainService', 'tabService', 'datatable', 'messages', 'lists', 'treatments', '$window', 'valuationService', 
                                                   function($scope, $http, $q, $routeParams, $sce, mainService, tabService, datatable, messages, lists, treatments, $window, valuationService) {

	 
	 $scope.goToRun=function(){
		$window.open(jsRoutes.controllers.runs.tpl.Runs.get($scope.readset.runCode).url, 'runs');
	}
	
	$scope.save = function(){
		var queries = [];
		queries.push($http.put(jsRoutes.controllers.readsets.api.ReadSets.properties($scope.readset.code).url,
				{properties : $scope.readset.properties}));
		queries.push($http.put(jsRoutes.controllers.readsets.api.ReadSets.valuation($scope.readset.code).url, 
				{productionValuation:$scope.readset.productionValuation,bioinformaticValuation:$scope.readset.bioinformaticValuation}));
		
		$q.all(queries).then(function(results){
			var error = false;
			for(var i = 0; i  < results.length; i++){
				var result = results[i];
				if(result.status !== 200){
					error = true;
				}
			}
			if(error){
				$scope.messages.setError("save");	
			}else{
				$scope.messages.setSuccess("save");
				mainService.stopEditMode();
				updateData();
			}
		});						
	};
	
	$scope.cancel = function(){
		$scope.messages.clear();
		mainService.stopEditMode();
		updateData();				
	};
	
	$scope.activeEditMode = function(){
		$scope.messages.clear();
		mainService.startEditMode();			
	};
	
	$scope.activePrintMode = function(code){
		$window.open(jsRoutes.controllers.readsets.tpl.ReadSets.getPrint(code).url, 'print');		
	};
	
	$scope.setImage = function(imageData, imageName, treatmentContext, treatmentCode, imageFullSizeWidth, imageFullSizeHeight) {
		$scope.modalImage = imageData;
	
		$scope.modalTitle = '';
		if (treatmentContext != '') {
			$scope.modalTitle = treatmentContext + ' : ';
		}
		$scope.modalTitle = $scope.modalTitle + Messages('readsets.treatments.' + treatmentCode + '.' + imageName);
	
		var margin = Messages("readsets.treatments.images.margin");		
		var zoom = Math.min((document.body.clientWidth - margin) / imageFullSizeWidth, 1);

		$scope.modalWidth = imageFullSizeWidth * zoom;
		$scope.modalHeight = imageFullSizeHeight * zoom; //in order to conserve image ratio
		$scope.modalLeft = (document.body.clientWidth - $scope.modalWidth)/2;
	
		$scope.modalTop = (window.innerHeight - $scope.modalHeight)/2;
	
		$scope.modalTop = $scope.modalTop - 50; //height of header and footer
	}
	
	var updateData = function(){
		$http.get(jsRoutes.controllers.readsets.api.ReadSets.get($routeParams.code).url).success(function(data) {
			$scope.readset = data;	
			
		});
	}
	
	var isValuationMode = function(){
		return (mainService.isHomePage('valuation') || ($routeParams.page && $routeParams.page.indexOf('valuation') == 0));
	}
	
	
    $scope.deliberatelyTrustHTMLComment = function() {
		if ($scope.readset && $scope.readset.productionValuation.comment && $scope.readset.productionValuation.comment != null) {
			return $sce.trustAsHtml($scope.readset.productionValuation.comment.trim().replace(/\n/g, "<br>"));
		}
		else {
			return "";
		}
    };
	
    $scope.highLight = function(prop){
		if (lists.getValuationCriterias() && $scope.readset && $scope.readset.productionValuation) {
			return "bg-" + $scope.valuationService.valuationCriteriaClass($scope.readset, $scope.readset.productionValuation.criteriaCode, prop);
		}
		else {
			return undefined;
		}
    };
    
	$scope.showSuspectedKmers = function(read, treatmentCode) {
		if (read === 'read1') {
			if (angular.isDefined($scope.readset.treatments) && angular.isDefined($scope.readset.treatments[treatmentCode])) {
				return (
						angular.isDefined($scope.readset.treatments[treatmentCode].read1.suspectedKmers) 
						&& $scope.readset.treatments[treatmentCode].read1.suspectedKmers != null 
						&& $scope.readset.treatments[treatmentCode].read1.suspectedKmers.value.length > 0);
			}
		}
		if (read === 'read2') {
			if (angular.isDefined($scope.readset.treatments) && angular.isDefined($scope.readset.treatments[treatmentCode])) {
				return ( 
						$scope.readset.treatments[treatmentCode].read2.suspectedKmers != undefined 
						&& $scope.readset.treatments[treatmentCode].read2.suspectedKmers != null 
						&& $scope.readset.treatments[treatmentCode].read2.suspectedKmers.value.length > 0);
			}
		}
		return false;
	}
	
	$scope.showSuspectedPrimers = function(read, treatmentCode) {
		if (read === 'read1') {
			if (angular.isDefined($scope.readset.treatments) && angular.isDefined($scope.readset.treatments[treatmentCode]))  {
				return ( angular.isDefined($scope.readset.treatments[treatmentCode].read1.suspectedPrimers)  
						&& $scope.readset.treatments[treatmentCode].read1.suspectedPrimers != null 
						&& $scope.readset.treatments[treatmentCode].read1.suspectedPrimers.value.length > 0);
			}
		}
		if (read === 'read2') {
			if (angular.isDefined($scope.readset.treatments) && angular.isDefined($scope.readset.treatments[treatmentCode]))  {
				return ( angular.isDefined($scope.readset.treatments[treatmentCode].read2.suspectedPrimers)  
						&& $scope.readset.treatments[treatmentCode].read2.suspectedPrimers != null 
						&& $scope.readset.treatments[treatmentCode].read2.suspectedPrimers.value.length > 0);
			}
		}
		return false;
	}
	
	$scope.showMaxSizeReads = function(read, treatmentCode) {
		if (read === 'read1') {
			if (angular.isDefined($scope.readset.treatments) && angular.isDefined($scope.readset.treatments[treatmentCode]))  {
				return ( angular.isDefined($scope.readset.treatments[treatmentCode].read1.maxSizeReads)  
						&& $scope.readset.treatments[treatmentCode].read1.maxSizeReads != null );
			}
		}
		if (read === 'read2') {
			if (angular.isDefined($scope.readset.treatments) && angular.isDefined($scope.readset.treatments[treatmentCode]))  {
				return ( angular.isDefined($scope.readset.treatments[treatmentCode].read2.maxSizeReads)  
						&& $scope.readset.treatments[treatmentCode].read2.maxSizeReads != null );
			}
		}
		return false;
	}
	
	$scope.showAdapters = function(read, treatmentCode) {
		if (read === 'read1') {
			if (angular.isDefined($scope.readset.treatments) && angular.isDefined($scope.readset.treatments[treatmentCode]))  {
				return ( angular.isDefined($scope.readset.treatments[treatmentCode].read1.adapters)   
						&& $scope.readset.treatments[treatmentCode].read1.adapters != null 
						&& $scope.readset.treatments[treatmentCode].read1.adapters.value.length > 0);
			}
		}
		if (read === 'read2') {
			if (angular.isDefined($scope.readset.treatments) && angular.isDefined($scope.readset.treatments[treatmentCode]))  {
				return ( angular.isDefined($scope.readset.treatments[treatmentCode].read2.adapters)   
						&& $scope.readset.treatments[treatmentCode].read2.adapters != null 
						&& $scope.readset.treatments[treatmentCode].read2.adapters.value.length > 0);
			}
		}
		return false;
	}
	

	
	
	$scope.isDataExistsForPhylogeneticTree = function(trtCode) {
		var b = true;
		if (angular.isDefined($scope.readset.treatments) && (trtCode != undefined)) {
			var treatments = $scope.readset.treatments;
			if (!angular.isDefined(treatments[trtCode].read1.phylogeneticTree) ||
					( angular.isDefined(treatments[trtCode].read1.phylogeneticTree.value) && (treatments[trtCode].read1.phylogeneticTree.value == null) ) ) {
				b = false;
			}
		}
		return b;
	}
	
	
	$scope.getKrona = function(trtCode) {
		if (angular.isDefined($scope.readset.treatments)  && (trtCode != undefined)) {
			return "data:text/html;base64,"+$scope.readset.treatments["taxonomy"].read1.krona.value;
		}
	}
	

	var init = function(){
		$scope.messages = messages();
		$scope.lists = lists;
		$scope.treatments = treatments;
		$scope.valuationService = valuationService();
		mainService.stopEditMode();
		if(isValuationMode()){
			mainService.startEditMode();			
		}
		
		$http.get(jsRoutes.controllers.readsets.api.ReadSets.get($routeParams.code).url).success(function(data) {
			$scope.readset = data;	
				
			if(tabService.getTabs().length == 0){
				if(isValuationMode()){ //valuation mode
					tabService.addTabs({label:Messages('readsets.page.tab.validate'),href:jsRoutes.controllers.readsets.tpl.ReadSets.home("valuation").url,remove:true});
					tabService.addTabs({label:$scope.readset.code,href:jsRoutes.controllers.readsets.tpl.ReadSets.valuation( $scope.readset.code).url,remove:true})
				}else{ //detail mode
					tabService.addTabs({label:Messages('readsets.menu.search'),href:jsRoutes.controllers.readsets.tpl.ReadSets.home("search").url,remove:true});
					tabService.addTabs({label:$scope.readset.code,href:jsRoutes.controllers.readsets.tpl.ReadSets.get($scope.readset.code).url,remove:true})									
				}
				tabService.activeTab($scope.getTabs(1));
			}
			
			$scope.lists.refresh.resolutions({typeCode:$scope.readset.typeCode});
			$scope.lists.refresh.valuationCriterias({typeCode:$scope.readset.typeCode, objectTypeCode:"ReadSet"});
			$scope.lists.refresh.states({objectTypeCode:"ReadSet"});
			
			if(angular.isDefined($scope.readset.treatments)){				
				$scope.treatments.init($scope.readset.treatments, jsRoutes.controllers.readsets.tpl.ReadSets.treatments, 'readsets', {global:true});				
			}
			
			$http.get(jsRoutes.controllers.runs.api.Lanes.get($scope.readset.runCode, $scope.readset.laneNumber).url).success(function(data) {
				$scope.lane = data;	
			});	
			
			$http.get(jsRoutes.controllers.runs.api.RunTreatments.get($scope.readset.runCode, "ngsrg").url).success(function(data) {
				$scope.runNGSRG = data;	
			});	
			
			$http.get(jsRoutes.controllers.commons.api.StatesHierarchy.list().url,  {params: {objectTypeCode:"ReadSet"}}).success(function(data) {
				$scope.statesHierarchy = data;	
			});	
						
		});
		
		$scope.ncbiUrl = Messages("readsets.treatments.taxonomy.beginNcbiUrl");

	};
	
	init();
	
}]);
 
 
 
 

 angular.module('home').controller('DetailsPrintCtrl', ['$scope', '$http', '$sce', '$routeParams',  'mainService', 'tabService', 'treatments', 
    function($scope,  $http, $sce, $routeParams,  mainService, tabService, treatments ) {
	 
		//function to call just one time the sub-function getCascadedArray
		$scope.getArray = function(property, treatmentCode, read, type) {
			if (angular.isDefined($scope.readset.treatments[treatmentCode])) {
				var numberOfColumnsPerPage = 4;
				var numberOfElementsByColumn = 50;	 
				if (treatmentCode === "readQualityRaw")
					if (read === "read1")
						if (type === "suspectedKmers") {
							if (angular.isDefined($scope.readQualityRawRead1Kmers) && $scope.readQualityRawRead1Kmers.length == 0) {
								$scope.readQualityRawRead1Kmers = getCascadedArray(property, numberOfColumnsPerPage, numberOfElementsByColumn);;
							}
							return $scope.readQualityRawRead1Kmers;
						}
						else {
							if (angular.isDefined($scope.readQualityRawRead1Primers) && $scope.readQualityRawRead1Primers.length == 0) {
								$scope.readQualityRawRead1Primers = getCascadedArray(property, numberOfColumnsPerPage, numberOfElementsByColumn);;
							}
							return $scope.readQualityRawRead1Primers;
						}
					else
						if (type === "suspectedKmers") {
							if (angular.isDefined($scope.readQualityRawRead2Kmers) && $scope.readQualityRawRead2Kmers.length == 0) {
								$scope.readQualityRawRead2Kmers = getCascadedArray(property, numberOfColumnsPerPage, numberOfElementsByColumn);;
							}
							return $scope.readQualityRawRead2Kmers;
						}
						else {
							if (angular.isDefined($scope.readQualityRawRead2Primers) && $scope.readQualityRawRead2Primers.length == 0) {
								$scope.readQualityRawRead2Primers = getCascadedArray(property, numberOfColumnsPerPage, numberOfElementsByColumn);;
							}
							return $scope.readQualityRawRead2Primers;
						}
				else
					if (read === "read1")
						if (type === "suspectedKmers") {
							if (angular.isDefined($scope.readQualityCleanRead1Kmers) && $scope.readQualityCleanRead1Kmers.length == 0) {
								$scope.readQualityCleanRead1Kmers = getCascadedArray(property, numberOfColumnsPerPage, numberOfElementsByColumn);;
							}
							return $scope.readQualityCleanRead1Kmers;
						}
						else {
							if (angular.isDefined($scope.readQualityCleanRead1Primers) && $scope.readQualityCleanRead1Primers.length == 0) {
								$scope.readQualityCleanRead1Primers = getCascadedArray(property, numberOfColumnsPerPage, numberOfElementsByColumn);;
							}
							return $scope.readQualityCleanRead1Primers;
						}
					else
						if (type === "suspectedKmers") {
							if (angular.isDefined($scope.readQualityCleanRead2Kmers) && $scope.readQualityCleanRead2Kmers.length == 0) {
								$scope.readQualityCleanRead2Kmers = getCascadedArray(property, numberOfColumnsPerPage, numberOfElementsByColumn);;
							}
							return $scope.readQualityCleanRead2Kmers;
						}
							
						else {
							if (angular.isDefined($scope.readQualityCleanRead2Primers) && $scope.readQualityCleanRead2Primers.length == 0) {
								$scope.readQualityCleanRead2Primers = getCascadedArray(property, numberOfColumnsPerPage, numberOfElementsByColumn);;
							}
							return $scope.readQualityCleanRead2Primers;
						}
			}
			return;
		}
	    
		//function to make the "pagination" of the suspectedKmers & suspectedPrimers
		//output: an array of pages. each of them contains arrays of columns. each of them contains the data ! 
		var getCascadedArray = function(propertyArray, numberOfColumnsPerPage, numberOfElementsByColumn) {
			if (angular.isDefined(propertyArray)) {
				var tmpArray = propertyArray.slice(0);
				tmpArray.sort(function(a, b){return b.nbOccurences-a.nbOccurences});
				
				for (var i=0, len=tmpArray.length; i<len; i++) {
					tmpArray[i] = {"id":i, "data":tmpArray[i]};
				}
				
				var totalNumberOfColumns = Math.ceil(tmpArray.length / numberOfElementsByColumn); 	
				var pageArray = new Array(Math.ceil(totalNumberOfColumns/numberOfColumnsPerPage));
				var exit = false;
					
				for (var p=0, len=pageArray.length; p<len; p++) {			
					for (var c=0; c<numberOfColumnsPerPage; c++) {
						for (var d=0; d<numberOfElementsByColumn; d++) {
							if (d===0) {var dataArray = new Array();}
							
							var idx = p*numberOfColumnsPerPage*numberOfElementsByColumn + c*numberOfElementsByColumn + d;
							if (idx < tmpArray.length) {
								dataArray.push({"id":d, "line":tmpArray[idx]});
							}
							else {
								exit = true;
								break;
							}
						}
						if (c===0) {var colArray = new Array();}
						colArray.push({"id":c, "columns":dataArray});
						
						if (exit) {break;}
					}
					pageArray[p] = {"id":p, "pages":colArray};
				}	
				return pageArray;
			}
		}

		
	 
		var initArraysOfSuspectedContaminations = function(){
			$scope.readQualityRawRead1Kmers = [];
			$scope.readQualityRawRead1Primers = [];
			$scope.readQualityRawRead2Kmers = [];
			$scope.readQualityRawRead2Primers = [];
			$scope.readQualityCleanRead1Kmers = [];
			$scope.readQualityCleanRead1Primers = [];
			$scope.readQualityCleanRead2Kmers = [];
			$scope.readQualityCleanRead2Primers = [];
		}
	 
		var init = function(){
			initArraysOfSuspectedContaminations(); 
		};
		
		init();
 }]);

 
 
 
/*
 angular.module('home').controller('TaxonomyCtrl', ['$scope', function($scope) {
	 
		$scope.isDataExistsForPhylogeneticTree = function() {
			var b = true;
			if (angular.isDefined($scope.readset.treatments)) {
				var treatments = $scope.readset.treatments;
				if (!angular.isDefined(treatments["taxonomy"].read1.phylogeneticTree) ||
						( angular.isDefined(treatments["taxonomy"].read1.phylogeneticTree.value) && (treatments["taxonomy"].read1.phylogeneticTree.value == null) ) ) {
					b = false;
				}
			}
			return b;
		}
		
		$scope.getKrona = function(treatmentCode) {
			if (angular.isDefined($scope.readset.treatments)) {
				return "data:text/html;base64,"+$scope.readset.treatments["taxonomy"].read1.krona.value;
			}
		}
			
		var init = function() {		
			$scope.$watch('readset', function() { 
				if (angular.isDefined($scope.readset.treatments) && angular.isDefined($scope.treatments.getTreatment().code)) {				
					$scope.krona = "data:text/html;base64,"+$scope.readset.treatments[$scope.treatments.getTreatment().code].read1.krona.value;
					
					$scope.ncbiUrl = Messages("readsets.treatments.taxonomy.beginNcbiUrl");
				}
			});
		}
		
		init();
}]);
*/ 
 
 
 angular.module('home').controller('MappingCtrl', ['$scope', function($scope) {
	 
		$scope.isDataExistsForRead1 = function() {
			var b = true;
			if (angular.isDefined($scope.readset.treatments)) {
				var treatments = $scope.readset.treatments;
				if (((!angular.isDefined(treatments["mapping"].read1)) || (!angular.isDefined(treatments["mapping"].read1.errorPosition.value))) ||
						( angular.isDefined(treatments["mapping"].read1.errorPosition.value) && (treatments["mapping"].read1.errorPosition.value == null) ))  {
					b = false;
				}
			}
			return b;
		}
		
		$scope.isDataExistsForRead2 = function() {
			var b = true;
			if (angular.isDefined($scope.readset.treatments)) {
				var treatments = $scope.readset.treatments;
				if (((!angular.isDefined(treatments["mapping"].read2)) || (!angular.isDefined(treatments["mapping"].read2.errorPosition.value))) ||
						( angular.isDefined(treatments["mapping"].read2.errorPosition.value) && (treatments["mapping"].read2.errorPosition.value == null) ))  {
					b = false;
				}
			}
			return b;
		}

}]);
