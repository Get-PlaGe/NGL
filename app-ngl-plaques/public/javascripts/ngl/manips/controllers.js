"use strict";

function SearchCtrl($scope, $http, datatable,basket) {

	$scope.datatableConfig = {
			order :{by:'matmanom'},
			search:{
				url:jsRoutes.controllers.manips.api.Manips.list()
			},
			show:{
				active:true
			}
	};
	
	$scope.addToBasket = function(manips){
		for(var i = 0; i < manips.length; i++){
				var puit = {
						
						matmaco:manips[i].matmaco,
						matmanom:manips[i].matmanom,
						x:$scope.positionPlaque96()
				};			
				this.basket.add(puit);
		}					
	}
	
	$scope.positionPlaque96= function(){
		
		if($scope.y==undefined){
			$scope.y=1;
			$scope.x="A";
		}
		else {
			//changement de colonne
			if($scope.y==12){
				
				if(String.fromCharCode($scope.x.charCodeAt(0))=="H")
				{ return "Erreur"};
				
				$scope.x= String.fromCharCode($scope.x.charCodeAt(0)+1);
				$scope.y=1;
			}
			
			$scope.y=$scope.y+1; 
		
		};
				
		return $scope.x+$scope.y;
	}	
	
	
	$scope.init = function(){
		
		if(angular.isUndefined($scope.getDatatable())){
			$scope.tabs.push({label:"Puits",href:jsRoutes.controllers.manips.tpl.Manips.home("list").url,remove:false});//$scope.tabs[1]
			$scope.datatable = datatable($scope, $scope.datatableConfig);
			$scope.setDatatable($scope.datatable);	
			
		}else{
			$scope.datatable = $scope.getDatatable();
		}
		
		
		if(angular.isUndefined($scope.getBasket())){
			$scope.basket = basket($scope,$scope.basketsLocalConfig);
			$scope.setBasket($scope.basket);
		}else{
			$scope.basket = $scope.getBasket();
		}
		
		if(angular.isUndefined($scope.getForm())){
			$scope.form = {
					projects:{},
					etmateriels:{},
					etmanips:{}
			};
			
			$scope.setForm($scope.form);
			
			//jsRoutes.controllers.lists.api.Lists.processTypes().url
			$http.get(jsRoutes.controllers.Lists.projects().url).
			success(function(data, status, headers, config){
				$scope.form.projects.options = data;
			});
			
			$http.get(jsRoutes.controllers.Lists.etmateriels().url).
			success(function(data, status, headers, config){
				$scope.form.etmateriels.options = data;
			});
			
			$http.get(jsRoutes.controllers.Lists.etmanips().url).
			success(function(data, status, headers, config){
				$scope.form.etmanips.options = data;
			});
						
		}else{
			$scope.form = $scope.getForm();			
		}
		
}
	
	
	$scope.search = function(){
		
		var jsonSearch = {};
		
		if($scope.form.etmanips.selected){
			jsonSearch.emateriel = '2';
		
			if($scope.form.projects.selected){
				jsonSearch.project = $scope.form.projects.selected.code;
			}
		
			if($scope.form.etmanips.selected){
				jsonSearch.etmanip = $scope.form.etmanips.selected.code;
			}

			$scope.datatable.search(jsonSearch);
			
		}
	}
		
};

SearchCtrl.$inject = ['$scope', '$http','datatable','basket'];


function ListCtrl($scope, datatable) {
	
	$scope.datatableConfig = {
			pagination:{
				active:false
			},		
			search:{
				active:false
			},
			save:{
				active:true,
				withoutEdit:true,
			},
			remove:{
				active:true,
				mode:'local',
				callback : function(datatable){
					$scope.basket.reset();
					$scope.basket.add(datatable.allResult);
				}
			}
		};
	
	$scope.init = function(){
		$scope.datatable = datatable($scope, $scope.datatableConfig);
		$scope.basket = $scope.getBasket();
		$scope.datatable.setData($scope.basket.get(),$scope.basket.get().length);
	}
	
};
ListCtrl.$inject = ['$scope', 'datatable'];