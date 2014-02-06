"use strict";


function getColumns(type){
	var columns = [];
	
	columns.push({	property:"code",
		    	  	header: Messages("readsets.code"),
		    	  	type :"String",
		    	  	order:true});
	
	columns.push({	property:"runCode",
					header: Messages("readsets.runCode"),
					type :"String",
					order:true});
	columns.push({	property:"laneNumber",
					header: Messages("readsets.laneNumber"),
					type :"String",
					order:true});
	columns.push({	property:"projectCode",
					header: Messages("readsets.projectCode"),
					type :"String",
					order:true});
	columns.push({	property:"sampleCode",
					header: Messages("readsets.sampleCode"),
					type :"String",
					order:true});
	columns.push({	property:"runSequencingStartDate",
					header: Messages("runs.sequencingStartDate"),
					type :"Date",
					order:true});
	
	if('state' === type){
		columns.push({	property:"state.code",
						render:function(value){
							return Codes("state."+value.state.code);
						},
						header: Messages("readsets.stateCode"),
						type :"String",
						edit:true,
						order:true,
				    	choiceInList:true,
				    	listStyle:'bt-select',
				    	//possibleValues:[{code:"IW-QC",name:Codes("state.IW-QC")},{code:"IW-V",name:Codes("state.IW-V")},{code:"F-V",name:Codes("state.F-V")}, {code:"F",name:Codes("state.F")}]
						possibleValues:'listsTable.getStates()'});
	}else{
		columns.push({	property:"state.code",
						render:function(value){
							return Codes("state."+value.state.code);
						},
						header: Messages("readsets.stateCode"),
						type :"String",
						order:true});
	}
	
	if('valuation' === type){
		columns.push({	property:"productionValuation.valid",
						render:function(value){
							return Codes("valuation."+value.productionValuation.valid);
						},
						header: Messages("readsets.productionValuation.valid"),
						type :"String",
				    	order:true,
				    	edit:true,
				    	choiceInList:true,
				    	listStyle:'bt-select',
				    	possibleValues:'listsTable.getValuations()'
				    	});
		columns.push({	property:"bioinformaticValuation.valid",
						render:function(value){
							return Codes("valuation."+value.bioinformaticValuation.valid);
						},
						header: Messages("readsets.bioinformaticValuation.valid"),
						type :"String",
						order:true,
				    	edit:true,
				    	choiceInList:true,
				    	listStyle:'bt-select',
				    	possibleValues:'listsTable.getValuations()'
				    	});
	}else{
		columns.push({	property:"productionValuation.valid",
						render:function(value){
							return Codes("valuation."+value.productionValuation.valid);
						},
						header: Messages("readsets.productionValuation.valid"),
						type :"String",
				    	order:true});
		columns.push({	property:"bioinformaticValuation.valid",
						render:function(value){
							return Codes("valuation."+value.bioinformaticValuation.valid);
						},
						header: Messages("readsets.bioinformaticValuation.valid"),
						type :"String",
				    	order:true});
	}
	
	if('batch' === type){
		columns.push({	property:"properties.isSentCCRT.value",
			
			header: Messages("readsets.properties.isSentCCRT"),
			type :"Boolean",
			order:true,
	    	edit:true
	    	});
		columns.push({	property:"properties.isSentCollaborator.value",
			
			header: Messages("readsets.properties.isSentCollaborator"),
			type :"Boolean",
			order:true,
	    	edit:true
	    	});
		
	}
	return columns;
}

function SearchFormCtrl($scope, $filter, lists){
	$scope.lists = lists;
	
	$scope.form = {
	}
	
	var search = function(values, query){
		var queryElts = query.split(',');
		
		var lastQueryElt = queryElts.pop();
		
		var output = [];
		angular.forEach($filter('filter')(values, lastQueryElt), function(value, key){
			if(queryElts.length > 0){
				this.push(queryElts.join(',')+','+value.code);
			}else{
				this.push(value.code);
			}
		}, output);
		
		return output;
	}
	
	$scope.searchProjects = function(query){
		return search(lists.getProjects(), query);
				
	}
	
	$scope.searchRuns = function(query){
		return search(lists.get('runs'), query);
				
	}
	
	$scope.refreshSamples = function(){
		if($scope.form.projectCodes){
			lists.refresh.samples({projectCodes:$scope.form.projectCodes.split(',')});
		}
	}
	
	$scope.searchSamples = function(query){
		return search(lists.getSamples(), query);				
	}
	
	$scope.search = function(){
		var form = angular.copy($scope.form);
		if(form.fromDate)form.fromDate = moment(form.fromDate, Messages("date.format").toUpperCase()).valueOf();
		if(form.toDate)form.toDate = moment(form.toDate, Messages("date.format").toUpperCase()).valueOf();	
		if(form.projectCodes) form.projectCodes = form.projectCodes.split(',');
		if(form.sampleCodes) form.sampleCodes = form.sampleCodes.split(',');
		if(form.runCodes) form.runCodes = form.runCodes.split(',');
		
		if ($scope.isHomePage('valuation')) {
			if(form.stateCodes == undefined || form.stateCodes.length == 0) {
				//No stateCodes selected, the filter by default (on the only two possible states for the valuation) is applied
				form.stateCodes = ["IW-V","IP-V"];
			}		
		}
		
		$scope.datatable.search(form);
	}
	
	$scope.reset = function(){
		$scope.form = {
				
		}
	}
	
	$scope.init = function(){
		
		$scope.states = lists.getStates();
		if ($scope.isHomePage('valuation')) {
			//If we want to show the 2 states used to filter the data...
			//$scope.form.stateCodes = ["IW-V","IP-V"];
			//Reduce data to the set of states specific to the valuation
			$scope.states = [{code:"IW-V",name:Codes("state.IW-V")},{code:"IP-V",name:Codes("state.IP-V")}];
		}
		
		$scope.lists.refresh.projects();
		$scope.lists.refresh.states({objectTypeCode:"ReadSet"});
		$scope.lists.refresh.types({objectTypeCode:"Run"});
		$scope.lists.refresh.runs();
		
	}
	
};
SearchFormCtrl.$inject = ['$scope', '$filter', 'lists'];

function SearchCtrl($scope, $routeParams, datatable) {

	$scope.form = {};
	
	$scope.datatableConfig = {
			order :{by:'traceInformation.creationDate'},
			search:{
				url:jsRoutes.controllers.readsets.api.ReadSets.list()
			},
			show:{
				active:true,
				add :function(line){
					$scope.addTabs({label:line.code,href:jsRoutes.controllers.readsets.tpl.ReadSets.get(line.code).url,remove:true});
				}
			},
			columns : getColumns('search')
	};
	
	
	
	$scope.init = function(){
		//to avoid to lost the previous search
		$scope.form = $routeParams;
		if(angular.isUndefined($scope.getDatatable())){
			$scope.datatable = datatable($scope, $scope.datatableConfig);
			$scope.setDatatable($scope.datatable);
		}else{
			$scope.datatable = $scope.getDatatable();
		}
		$scope.datatable.search($scope.form);
		if(angular.isUndefined($scope.getHomePage())){
			$scope.setHomePage('search');
			$scope.addTabs({label:Messages('readsets.menu.search'),href:jsRoutes.controllers.readsets.tpl.ReadSets.home("search").url,remove:false});
			$scope.activeTab(0); // desactive le lien !
		}
	}	
};

SearchCtrl.$inject = ['$scope', '$routeParams', 'datatable'];

function SearchStateCtrl($scope,  datatable, lists) {

	$scope.listsTable = lists;
	
	$scope.datatableConfig = {
			order :{by:'traceInformation.creationDate'},
			search:{
				url:jsRoutes.controllers.readsets.api.ReadSets.list()
			},
			edit : {
				active:true,
				columnMode:true		    	
			},
			save : {
				active:true,
				url:function(line){
					return jsRoutes.controllers.readsets.api.ReadSets.state(line.code).url;
				},
				method:'put',
				value:function(line){return line.state;}
			},
			show:{
				active:true,
				add :function(line){
					$scope.addTabs({label:line.code,href:jsRoutes.controllers.readsets.tpl.ReadSets.get(line.code).url,remove:true});
				}
			},
			columns : getColumns('state')
	};
	
	$scope.init = function(){
		//to avoid to lost the previous search
		if(angular.isUndefined($scope.getDatatable())){
			$scope.datatable = datatable($scope, $scope.datatableConfig);
			$scope.datatable.search();
			$scope.setDatatable($scope.datatable);
		}else{
			$scope.datatable = $scope.getDatatable();
		}
		
		if(angular.isUndefined($scope.getHomePage())){
			$scope.setHomePage('search');
			$scope.addTabs({label:Messages('readsets.menu.search'),href:jsRoutes.controllers.readsets.tpl.ReadSets.home("search").url,remove:false});
			$scope.activeTab(0); // desactive le lien !
		}
	}	
};

SearchStateCtrl.$inject = ['$scope', 'datatable', 'lists'];


function SearchValuationCtrl($scope, datatable, lists) {

	$scope.listsTable = lists;
	
	$scope.datatableConfig = {
			order :{by:'traceInformation.creationDate'},
			search:{
				url:jsRoutes.controllers.readsets.api.ReadSets.list()
			},
			edit : {
				active:true,
				columnMode:true		    	
			},			
			save : {
				active:true,
				url:function(line){
					return jsRoutes.controllers.readsets.api.ReadSets.valuation(line.code).url;
				},
				method:'put',
				value:function(line){return {productionValuation:line.productionValuation,bioinformaticValuation:line.bioinformaticValuation};}
			},
			show:{
				active:true,
				add :function(line){
					$scope.addTabs({label:line.code,href:jsRoutes.controllers.readsets.tpl.ReadSets.valuation(line.code).url,remove:true});
				}
			},
			columns : getColumns('valuation')
	};
	
	
	$scope.init = function(){
		//to avoid to lost the previous search
		if(angular.isUndefined($scope.getDatatable())){
			$scope.datatable = datatable($scope, $scope.datatableConfig);
			$scope.setDatatable($scope.datatable);
		}else{
			$scope.datatable = $scope.getDatatable();
		}
		$scope.datatable.search({stateCodes:["IW-V","IP-V"]});
		if(angular.isUndefined($scope.getHomePage())){
			$scope.setHomePage('valuation');
			$scope.addTabs({label:Messages('readsets.page.tab.validate'),href:jsRoutes.controllers.readsets.tpl.ReadSets.home("valuation").url,remove:false});
			$scope.activeTab(0); // desactive le lien !
		}
	}	
};

SearchValuationCtrl.$inject = ['$scope', 'datatable', 'lists'];

function SearchBatchCtrl($scope,  datatable) {

	$scope.datatableConfig = {
			order :{by:'traceInformation.creationDate'},
			search:{
				url:jsRoutes.controllers.readsets.api.ReadSets.list()
			},
			edit : {
				active:true,
				columnMode:true		    	
			},
			save : {
				active:true,
				url:function(line){
					return jsRoutes.controllers.readsets.api.ReadSets.properties(line.code).url;
				},
				method:'put',
				value:function(line){return {properties : line.properties};}
			},
			columns : getColumns('batch')
	};
	
	$scope.init = function(){
		//to avoid to lost the previous search
		if(angular.isUndefined($scope.getDatatable())){
			$scope.datatable = datatable($scope, $scope.datatableConfig);
			$scope.datatable.search();
			$scope.setDatatable($scope.datatable);
		}else{
			$scope.datatable = $scope.getDatatable();
		}
		
		if(angular.isUndefined($scope.getHomePage())){
			$scope.setHomePage('search');
			$scope.addTabs({label:Messages('readsets.menu.search'),href:jsRoutes.controllers.readsets.tpl.ReadSets.home("search").url,remove:false});
			$scope.activeTab(0); // desactive le lien !
		}
	}	
};

SearchBatchCtrl.$inject = ['$scope', 'datatable'];


