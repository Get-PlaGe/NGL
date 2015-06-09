angular.module('atomicTransfereServices', [])
.factory('experimentCommonFunctions', ['$rootScope','$http', '$parse', '$q','mainService',  function($rootScope, $http, $parse, $q, mainService){
/*  This module store all the common function between all the experiments such as:
 *  init a new experiment (loading containers from the basket...) with the function newExperiment that take a function in parameter (launched after the load of the containers)
 *  newExperimentDatatable is a wrapper of newExperiment that pass the function that set the data of the datatable you give it to
 *  containersToContainerUseds take a list of containers and convert it to a list of containerUseds
 *  newExperimentDragndrop  is a wrapper of newExperiment that pass the function that convert the containers in containerUseds
 *  removeNullProperties take a list of properties and set the null properties to undefined in order to not send them to the controllers
 *  loadContainer take a list of containerUseds and load the containers from the server, it return an object results containing,
 *   results.promise (the promise) and results.containers(the containers when the promise is ok
 */
	var constructor = function($scope){
		var common = {
				newExperiment: function(fn){
					var containers = [];
					var promises = [];
					$scope.basket = mainService.getBasket().get();
					angular.forEach($scope.basket, function(basket){
						var promise = $http.get(jsRoutes.controllers.containers.api.Containers.list().url,{params:{code:basket.code}})
						.success(function(data, status, headers, config) {
							$scope.clearMessages();
							if(data!=null){
								angular.forEach(data, function(container){
									containers.push(container);
								});
							}
						})
						.error(function(data, status, headers, config) {
							alert("error");
						});
						promises.push(promise);
					});

					$q.all(promises).then(function (res) {
						fn(containers);
						$scope.doPurifOrQc($scope.experiment.value.typeCode);
						if(!$scope.experiment.editMode) {
							$scope.init_experiment(containers, $scope.experimentType.atomicTransfertMethod);
						}else{
							$scope.addExperimentPropertiesInputsColumns();
						}

						$scope.addExperimentPropertiesOutputsColumns();
						$scope.addInstrumentPropertiesOutputsColumns();
					});
				},
				newExperimentDatatable : function(datatable){
					var that = this;
					this.newExperiment(function(containers){
						datatable.setData(containers,containers.length);
					});
				},
				containersToContainerUseds : function(containers){
					var containerUseds = [];
					angular.forEach(containers, function(container){
						var mesuredVolume = 0;
						if(container.mesuredVolume !== undefined){
							mesuredVolume = container.mesuredVolume;
						}
						containerUseds.push({"code":container.code, "state":container.state, "instrumentProperties":{},"experimentProperties":{}, "fromExperimentTypeCodes":container.fromExperimentTypeCodes,
							"percentage":100, "categoryCode":container.categoryCode, "volume":mesuredVolume,
							"concentration":container.mesuredConcentration, "contents":container.contents, "locationOnContainerSupport":container.support});
					});

					return containerUseds;
				},
				newExperimentDragndrop : function(){
					var that = this;
					this.newExperiment(function(containers){
						$scope.inputContainers = that.containersToContainerUseds(containers);
					});
				},
				removeNullProperties : function(properties){
					for (var p in properties) {
						if(properties[p] != undefined && (properties[p].value === undefined || properties[p].value === null || properties[p].value === "")){
							properties[p] = undefined;
						}
					}
				},
				loadContainer : function(containerUsed){
					var results = {container:{},promise:{}};
					var line = 1;
					var column = 1;

					if(containerUsed.locationOnContainerSupport != undefined){
						line = containerUsed.locationOnContainerSupport.line;
						column = containerUsed.locationOnContainerSupport.column;
					}

					results.promise = $http.get(jsRoutes.controllers.containers.api.Containers.list().url, {params:{"code":containerUsed.code, "line":line, "column":column}})
					.success(function(data, status, headers, config) {
						if(data!=null){
							results.container = data[0];
						}
					})
					.error(function(data, status, headers, config) {
						alert("error");
					});

					return results;
				}

		};
		return common;
	};
	return constructor;
}]).
factory('oneToX', ['$rootScope','experimentCommonFunctions', function($rootScope, experimentCommonFunctions){

	var constructor = function($scope, inputType){
		var inputType = inputType;
		var varExperimentCommonFunctions = undefined;

		var init = function(){
			varExperimentCommonFunctions = experimentCommonFunctions($scope);
		};

		var oneToX = {
				experimentToInput : function(input){
					if(inputType === "datatable"){
						var allData = input.getData();
						if(allData != undefined){
							for(var i=0;i<allData.length;i++){
								var position = this.searchInputContainerUsedPosition(allData[i].code);
								if($scope.experiment.value.atomicTransfertMethods[position] != null && angular.isDefined($scope.experiment.value.atomicTransfertMethods[position].inputContainerUseds[0])){
									allData[i].inputInstrumentProperties = $scope.experiment.value.atomicTransfertMethods[position].inputContainerUseds[0].instrumentProperties;
									allData[i].inputExperimentProperties = $scope.experiment.value.atomicTransfertMethods[position].inputContainerUseds[0].experimentProperties;
									allData[i].inputContainerUsed =  $scope.experiment.value.atomicTransfertMethods[position].inputContainerUseds[0];											
								}										
							}
							input.setData(allData, allData.length);
						}
					}
				},
				inputToExperiment : function(input){
					if(inputType === "datatable"){
						var allData = input.getData();
						if(allData != undefined){
							for(var i=0;i<allData.length;i++){
								if($scope.experiment.value.atomicTransfertMethods[i] === null || angular.isUndefined($scope.experiment.value.atomicTransfertMethods[i].inputContainerUseds[0])){
									$scope.experiment.value.atomicTransfertMethods[i] = {inputContainerUseds:[]};											
								}										
								//$scope.experiment.value.atomicTransfertMethods[i].inputContainerUseds[0].percentage = allData[i].inputContainerUseds[0].percentage;	
								$scope.experiment.value.atomicTransfertMethods[i].inputContainerUseds[0].instrumentProperties = allData[i].inputInstrumentProperties;
								$scope.experiment.value.atomicTransfertMethods[i].inputContainerUseds[0].experimentProperties = allData[i].inputExperimentProperties;
								$scope.experiment.value.atomicTransfertMethods[i].inputContainerUseds[0].concentration = allData[i].mesuredConcentration;
								$scope.experiment.value.atomicTransfertMethods[i].inputContainerUseds[0].volume = allData[i].mesuredVolume;

								varExperimentCommonFunctions.removeNullProperties($scope.experiment.value.atomicTransfertMethods[i].inputContainerUseds[0].instrumentProperties);
								varExperimentCommonFunctions.removeNullProperties($scope.experiment.value.atomicTransfertMethods[i].inputContainerUseds[0].experimentProperties);
							}

							input.setData(allData, allData.length);
						}
					}
				},
				searchInputContainerUsedPosition: function(inputContainerCode){
					var j=0;
					while($scope.experiment.value.atomicTransfertMethods[j]){
						if($scope.experiment.value.atomicTransfertMethods[j].inputContainerUseds[0].code===inputContainerCode){
							return j;
						}
						j++
					}
					return undefined;
				},

				loadInputContainers : function(ContainerUseds){
					var results = {containers:[],promises:[]};
					angular.forEach($scope.experiment.value.atomicTransfertMethods, function(atomicTransfertMethod){
						var result = varExperimentCommonFunctions.loadContainer(atomicTransfertMethod.inputContainerUseds[0]);
						results.promises.push(result.promise);
						result.promise.then(function(container){
							results.containers.push(container.data[0]);
						});
					});

					return results;
				}
		};
		init();
		return oneToX;
	};
	return constructor;
}]).factory('manyToX', ['$rootScope','experimentCommonFunctions', function($rootScope, experimentCommonFunctions){

	var constructor = function($scope, inputType){
		var inputType = inputType;
		var varExperimentCommonFunctions = undefined;

		var init = function(){
			varExperimentCommonFunctions = experimentCommonFunctions($scope);
		};

		var manyToX = {
				experimentToInput : function(input){
					if(inputType === "datatable"){
						var allData = input.getData();
						if(allData != undefined){
							for(var i=0;i<allData.length;i++){
								for(var j =0;j<$scope.experiment.value.atomicTransfertMethods[0].inputContainerUseds.length;j++){
									allData[i].inputInstrumentProperties = $scope.experiment.value.atomicTransfertMethods[0].inputContainerUseds[j].instrumentProperties;
									allData[i].inputExperimentProperties = $scope.experiment.value.atomicTransfertMethods[0].inputContainerUseds[j].experimentProperties;
									allData[i].inputContainerUsed =  $scope.experiment.value.atomicTransfertMethods[0].inputContainerUseds[j];
									i++;
								}
							}
							input.setData(allData, allData.length);
						}
					}else if(inputType === "dragndrop"){
						var allData = input.getData();
						if(allData != undefined){
							var currentIndex = {};
							for(var i=0;i<allData.length;i++){
								if(currentIndex[(allData[i].outputPositionX-1)] == undefined){
									currentIndex[(allData[i].outputPositionX-1)] = 0;
								}else{
									currentIndex[(allData[i].outputPositionX-1)]++;
								}
								var positions = this.searchContainer(allData[i].inputCode, allData[i].outputPositionX, allData[i].outputPositionY);
								if(angular.isUndefined(allData[i].inputContainerUsed)){
									allData[i].inputContainerUsed={}
								}

								if(angular.isDefined($scope.experiment.value.atomicTransfertMethods)){
									if(angular.isDefined(positions.x) && angular.isDefined(positions.y)){											
										if(angular.isDefined($scope.experiment.value.atomicTransfertMethods[positions.x].inputContainerUseds)){
											allData[i].inputContainerUsed.percentage = $scope.experiment.value.atomicTransfertMethods[positions.x].inputContainerUseds[positions.y].percentage;
											if($scope.experiment.value.atomicTransfertMethods[positions.x].inputContainerUseds[positions.y].instrumentProperties != undefined){
												allData[i].inputInstrumentProperties = $scope.experiment.value.atomicTransfertMethods[positions.x].inputContainerUseds[positions.y].instrumentProperties;
											}
											if($scope.experiment.value.atomicTransfertMethods[positions.x].inputContainerUseds[positions.y].experimentProperties != undefined){
												allData[i].inputExperimentProperties = $scope.experiment.value.atomicTransfertMethods[positions.x].inputContainerUseds[positions.y].experimentProperties;
											}
										}

									}else{
										positions.x = 0;											
										if(angular.isDefined($scope.experiment.value.atomicTransfertMethods[positions.x]) && angular.isDefined($scope.experiment.value.atomicTransfertMethods[positions.x].inputContainerUseds)){
											allData[i].inputContainerUsed.percentage = $scope.experiment.value.atomicTransfertMethods[positions.x].inputContainerUseds[i].percentage;
											if($scope.experiment.value.atomicTransfertMethods[positions.x].inputContainerUseds[i].instrumentProperties != undefined){
												allData[i].inputInstrumentProperties = $scope.experiment.value.atomicTransfertMethods[positions.x].inputContainerUseds[i].instrumentProperties;
											}
											if($scope.experiment.value.atomicTransfertMethods[positions.x].inputContainerUseds[i].experimentProperties != undefined){
												allData[i].inputExperimentProperties = $scope.experiment.value.atomicTransfertMethods[positions.x].inputContainerUseds[i].experimentProperties;
											}
										}

									}

								}



							}
							input.setData(allData, allData.length);
						}
					}
				},
				searchContainer : function(code, x, y){
					var i = 0;
					while($scope.experiment.value.atomicTransfertMethods[i] != undefined){
						for(var j=0;j<$scope.experiment.value.atomicTransfertMethods[i].inputContainerUseds.length;j++){
							if($scope.experiment.value.atomicTransfertMethods[i].inputContainerUseds[j].code == code && $scope.experiment.value.atomicTransfertMethods[i].line == x && $scope.experiment.value.atomicTransfertMethods[i].column == y){
								return {"x":i,"y":j};
							}
						}
						i++;
					}
					return {};
				},
				inputToExperiment : function(input){
					if(inputType === "datatable"){
						var allData = input.getData();
						if(allData != undefined){
							for(var i=0;i<allData.length;i++){
								for(var j =0;j<$scope.experiment.value.atomicTransfertMethods[0].inputContainerUseds.length;j++){											
									if(angular.isDefined(allData[i].inputContainerUsed) && angular.isDefined(allData[i].inputContainerUsed.percentage)){
										$scope.experiment.value.atomicTransfertMethods[0].inputContainerUseds[j].percentage = allData[i].inputContainerUsed.percentage;
									}											
									$scope.experiment.value.atomicTransfertMethods[0].inputContainerUseds[j].instrumentProperties = allData[i].inputInstrumentProperties;
									$scope.experiment.value.atomicTransfertMethods[0].inputContainerUseds[j].experimentProperties = allData[i].inputExperimentProperties;											

									varExperimentCommonFunctions.removeNullProperties($scope.experiment.value.atomicTransfertMethods[0].inputContainerUseds[j].instrumentProperties);
									varExperimentCommonFunctions.removeNullProperties($scope.experiment.value.atomicTransfertMethods[0].inputContainerUseds[j].experimentProperties);

									i++;
								}
							}
							input.setData(allData, allData.length);
						}
					}else if(inputType === "dragndrop"){
						var allData = input.getData();
						if(allData != undefined){
							for(var i=0;i<allData.length;i++){
								//$scope.experiment.value.atomicTransfertMethods[($scope.datatable.displayResult[i].data.outputPositionX-1)].inputContainerUseds[($scope.datatable.displayResult[i].data.outputPositionY-1)].percentage = $scope.datatable.displayResult[i].data.percentage;
								var positions = this.searchContainer(allData[i].inputCode, allData[i].outputPositionX, allData[i].outputPositionY);
								if(angular.isDefined(positions) && angular.isDefined(positions.x) && angular.isDefined(positions.y)){
									if(allData[i].inputInstrumentProperties != undefined){											
										$scope.experiment.value.atomicTransfertMethods[positions.x].inputContainerUseds[positions.y].instrumentProperties = allData[i].inputInstrumentProperties;
									}
									if(allData[i].inputExperimentProperties != undefined){											
										$scope.experiment.value.atomicTransfertMethods[positions.x].inputContainerUseds[positions.y].experimentProperties = allData[i].inputExperimentProperties;
									}

								}

							}
							input.setData(allData, allData.length);
						}
					}
				},
				loadInputContainers : function(ContainerUseds){
					var results = {containers:[],promises:[]};
					var that = this;
					angular.forEach($scope.experiment.value.atomicTransfertMethods, function(atomicTransfertMethod){
						angular.forEach(atomicTransfertMethod.inputContainerUseds, function(inputContainerUsed){
							var result = varExperimentCommonFunctions.loadContainer(inputContainerUsed);
							results.promises.push(result.promise);
							result.promise.then(function(container){
								results.containers.push(container.data[0]);
							});

						});
					});
					return results;
				}
		};
		init();
		return manyToX;
	};
	return constructor;
}]).factory('xToOne', ['$rootScope','experimentCommonFunctions', function($rootScope, experimentCommonFunctions){

	var constructor = function($scope, outputType){
		var outputType = outputType;

		var varExperimentCommonFunctions = undefined;

		var init = function(){
			varExperimentCommonFunctions = experimentCommonFunctions($scope);
		};

		var xToOne = {
				experimentToOutput : function(output){
					if(outputType === "none"){
						if(angular.isDefined(output.getData()) && output.getData().length>0){
							var allData = output.getData();									
							for(var i=0;i<allData.length;i++){										
								if(angular.isDefined(allData[i].outputContainerUsed) && allData[i].outputContainerUsed==null){
									var InputContainerPosition = this.searchInputContainerUsedPosition(allData[i].code);
									allData[i].outputContainerUsed  = $scope.experiment.value.atomicTransfertMethods[InputContainerPosition].outputContainerUseds[0];
									if($scope.experiment.value.atomicTransfertMethods[InputContainerPosition].outputContainerUseds[0]!=null){
										if(angular.isDefined($scope.experiment.value.atomicTransfertMethods[InputContainerPosition].outputContainerUseds[0].instrumentProperties)){
											allData[i].outputInstrumentProperties = $scope.experiment.value.atomicTransfertMethods[InputContainerPosition].outputContainerUseds[0].instrumentProperties;
										}
										if(angular.isDefined($scope.experiment.value.atomicTransfertMethods[InputContainerPosition].outputContainerUseds[0].experimentProperties)){
											allData[i].outputExperimentProperties = $scope.experiment.value.atomicTransfertMethods[InputContainerPosition].outputContainerUseds[0].experimentProperties;
										}
									}
								}else if(angular.isDefined(allData[i].outputContainerUsed) && allData[i].outputContainerUsed!=null && allData[i].outputContainerUsed.code!=null){
									var OutputContainerPosition = this.searchOutputContainerUsedPosition(allData[i].outputContainerUsed.code);
									var result = varExperimentCommonFunctions.loadContainer($scope.experiment.value.atomicTransfertMethods[OutputContainerPosition].outputContainerUseds[0]);
									var that = this;
									result.promise.then(function(container){
										for(var i=0;i<allData.length;i++){
											var index = that.searchOutputContainerUsedPosition(allData[i].outputContainerUsed.code);																									
											allData[i].outputContainerUsed = $scope.experiment.value.atomicTransfertMethods[index].outputContainerUseds[0];
											if(angular.isDefined($scope.experiment.value.atomicTransfertMethods[index].outputContainerUseds[0].instrumentProperties)){
												allData[i].outputInstrumentProperties = $scope.experiment.value.atomicTransfertMethods[index].outputContainerUseds[0].instrumentProperties;
											}										
											if(angular.isDefined($scope.experiment.value.atomicTransfertMethods[index].outputContainerUseds[0].experimentProperties)){
												allData[i].outputExperimentProperties = $scope.experiment.value.atomicTransfertMethods[index].outputContainerUseds[0].experimentProperties;
											}
											if(container.data.length>0 && container.data[0]!=null){												
												allData[i].outputContainerUsed.state = $scope.experiment.value.atomicTransfertMethods[index].outputContainerUseds[0].state = container.data[0].state;
											}
										}
										output.setData(allData,allData.length);
									});	
								}										
							}									
						}else{
							allData =[];
							angular.forEach($scope.experiment.value.atomicTransfertMethods, function(atomicTransfertMethod,index){
								if(atomicTransfertMethod.outputContainerUseds[0]!=undefined){									
									allData[index]= {outputContainerUsed:{}};								
									allData[index].outputContainerUsed = atomicTransfertMethod.outputContainerUseds[0];										
									allData[index].outputInstrumentProperties = atomicTransfertMethod.outputContainerUseds[0].instrumentProperties;
									allData[index].outputExperimentProperties = atomicTransfertMethod.outputContainerUseds[0].experimentProperties;
								}
							});
						}						
						if(angular.isDefined(allData) && allData.length>0){
							output.setData(allData,allData.length);
						}
					}else if(outputType === "datatable"){							
						var allData = output.getData();
						var j=0;
						while(allData[j]){
							var position = allData[j].outputPositionX-1;
							if(angular.isDefined($scope.experiment.value.atomicTransfertMethods[position].outputContainerUseds[0]) && $scope.experiment.value.atomicTransfertMethods[position].outputContainerUseds[0].code!=null){
								var result = varExperimentCommonFunctions.loadContainer($scope.experiment.value.atomicTransfertMethods[position].outputContainerUseds[0]);									
								result.promise.then(function(container){
									var i=0;
									while(allData[i]){
										var index = allData[i].outputPositionX-1;	
										allData[i].outputContainerUsed  = $scope.experiment.value.atomicTransfertMethods[index].outputContainerUseds[0];
										allData[i].outputInstrumentProperties = $scope.experiment.value.atomicTransfertMethods[index].outputContainerUseds[0].instrumentProperties;
										allData[i].outputExperimentProperties = $scope.experiment.value.atomicTransfertMethods[index].outputContainerUseds[0].experimentProperties;
										if(container.data.length>0 && container.data[0]!=null){												
											allData[i].outputContainerUsed.state = $scope.experiment.value.atomicTransfertMethods[index].outputContainerUseds[0].state = container.data[0].state;
										}
										i++;
									}
									output.setData(allData,allData.length)
								});		
							}else if(angular.isDefined($scope.experiment.value.atomicTransfertMethods[position].outputContainerUseds[0])){
								allData[j].outputContainerUsed  = $scope.experiment.value.atomicTransfertMethods[position].outputContainerUseds[0];
								allData[j].outputInstrumentProperties = $scope.experiment.value.atomicTransfertMethods[position].outputContainerUseds[0].instrumentProperties;
								allData[j].outputExperimentProperties = $scope.experiment.value.atomicTransfertMethods[position].outputContainerUseds[0].experimentProperties;
							}									

							j++;	
						}
						output.setData(allData,allData.length)

					}
				},
				searchOutputContainerUsedPosition : function(outputContainercode){
					var i = 0;
					while($scope.experiment.value.atomicTransfertMethods[i] != undefined){							
						if($scope.experiment.value.atomicTransfertMethods[i].outputContainerUseds[0].code === outputContainercode){
							return i;
						}							
						i++;
					}
					return undefined;
				},
				searchInputContainerUsedPosition: function(inputContainerCode){
					var j=0;
					while($scope.experiment.value.atomicTransfertMethods[j]){
						if($scope.experiment.value.atomicTransfertMethods[j].inputContainerUseds[0].code===inputContainerCode){
							return j;
						}
						j++
					}
					return undefined;
				},
				findOutputContainer : function(code, containers){						
					var j = -1;
					var i =0;
					while(containers[i]){
						if(code !== null && angular.isDefined(containers[i].outputContainerUseds[0]) && (code === containers[i].outputContainerUseds[0].code)){
							j++;
						}
						i++;
					}
					return j;

				},									
				outputToExperiment : function(output){
					if(outputType === "none"){
						var allData = output.getData();
						var j = 0;
						while($scope.experiment.value.atomicTransfertMethods[j]){
							if(allData != undefined){
								if(allData[j].outputContainerUsed != undefined){
									var volumeUnit = "µL";
									if($scope.experiment.value.atomicTransfertMethods[j].outputContainerUseds[0].volume && $scope.experiment.value.atomicTransfertMethods[j].outputContainerUseds[0].volume.unit!=null){
										volumeUnit = $scope.experiment.value.atomicTransfertMethods[j].outputContainerUseds[0].volume.unit;
									}
									var concentrationUnit = "nM";
									if($scope.experiment.value.atomicTransfertMethods[j].outputContainerUseds[0].concentration && $scope.experiment.value.atomicTransfertMethods[j].outputContainerUseds[0].concentration.unit!=null){
										concentrationUnit = $scope.experiment.value.atomicTransfertMethods[j].outputContainerUseds[0].concentration.unit;
									}
									var k = this.findOutputContainer(allData[j].outputContainerUsed.code, allData);
									if(k<1){
										$scope.experiment.value.atomicTransfertMethods[j].outputContainerUseds[0] = allData[j].outputContainerUsed;											
										if(allData[j].outputContainerUsed.volume){
											$scope.experiment.value.atomicTransfertMethods[j].outputContainerUseds[0].volume.unit = volumeUnit;
										}									

										if(allData[j].outputContainerUsed.concentration){
											$scope.experiment.value.atomicTransfertMethods[j].outputContainerUseds[0].concentration.unit = concentrationUnit; 
										}	

										$scope.experiment.value.atomicTransfertMethods[j].outputContainerUseds[0].instrumentProperties = allData[j].outputInstrumentProperties;					
										$scope.experiment.value.atomicTransfertMethods[j].outputContainerUseds[0].experimentProperties = allData[j].outputExperimentProperties;										
										varExperimentCommonFunctions.removeNullProperties($scope.experiment.value.atomicTransfertMethods[j].outputContainerUseds[0].experimentProperties);
										varExperimentCommonFunctions.removeNullProperties($scope.experiment.value.atomicTransfertMethods[j].outputContainerUseds[0].instrumentProperties);

									}else{
										for(var i=0;i<allData.length;i++){												
											$scope.experiment.value.atomicTransfertMethods[j].outputContainerUseds[0] = allData[j].outputContainerUsed;
											if(allData[j].outputContainerUsed.volume){
												$scope.experiment.value.atomicTransfertMethods[j].outputContainerUseds[0].volume.unit = volumeUnit;
											}
											if(allData[j].outputContainerUsed.concentration){
												$scope.experiment.value.atomicTransfertMethods[j].outputContainerUseds[0].concentration.unit = concentrationUnit; 
											} 
											$scope.experiment.value.atomicTransfertMethods[j].outputContainerUseds[0].instrumentProperties = allData[i].outputInstrumentProperties;					
											$scope.experiment.value.atomicTransfertMethods[j].outputContainerUseds[0].experimentProperties = allData[i].outputExperimentProperties;										
											varExperimentCommonFunctions.removeNullProperties($scope.experiment.value.atomicTransfertMethods[j].outputContainerUseds[0].experimentProperties);
											varExperimentCommonFunctions.removeNullProperties($scope.experiment.value.atomicTransfertMethods[j].outputContainerUseds[0].instrumentProperties);
										}

									}										
								}else{
									$scope.experiment.value.atomicTransfertMethods[j].outputContainerUseds[0] = {volume:{unit:"µL"},concentration:{unit:"nM"},experimentProperties:{}};	
									$scope.experiment.value.atomicTransfertMethods[j].outputContainerUseds[0].instrumentProperties = allData[j].outputInstrumentProperties;					
									$scope.experiment.value.atomicTransfertMethods[j].outputContainerUseds[0].experimentProperties = allData[j].outputExperimentProperties;										
									varExperimentCommonFunctions.removeNullProperties($scope.experiment.value.atomicTransfertMethods[j].outputContainerUseds[0].experimentProperties);
									varExperimentCommonFunctions.removeNullProperties($scope.experiment.value.atomicTransfertMethods[j].outputContainerUseds[0].instrumentProperties);
								}


							}
							output.setData(allData,allData.lenght);
							j++;
						}

					}else if(outputType === "datatable"){
						var allData = output.getData();
						if(allData != undefined){
							for(var i=0;i<allData.length;i++){
								if(angular.isDefined(allData[i].outputContainerUsed)){
									$scope.experiment.value.atomicTransfertMethods[(allData[i].outputPositionX-1)].outputContainerUseds[0] = allData[i].outputContainerUsed;
								}										
								if(allData[i].outputInstrumentProperties != undefined){
									$scope.experiment.value.atomicTransfertMethods[(allData[i].outputPositionX-1)].outputContainerUseds[0].instrumentProperties = allData[i].outputInstrumentProperties;
									varExperimentCommonFunctions.removeNullProperties($scope.experiment.value.atomicTransfertMethods[(allData[i].outputPositionX-1)].outputContainerUseds[0].instrumentProperties);
								}
								if(allData[i].outputExperimentProperties!= undefined){
									$scope.experiment.value.atomicTransfertMethods[(allData[i].outputPositionX-1)].outputContainerUseds[0].experimentProperties = allData[i].outputExperimentProperties;	
									varExperimentCommonFunctions.removeNullProperties($scope.experiment.value.atomicTransfertMethods[(allData[i].outputPositionX-1)].outputContainerUseds[0].experimentProperties);
								}
							}
							output.setData(allData,allData.lenght);
						}
					}
				},
				loadOutputContainers : function(ContainerUseds){
					var results = {containers:[],promises:[]};
					var that = this;
					angular.forEach($scope.experiment.value.atomicTransfertMethods, function(atomicTransfertMethod){
						if(atomicTransfertMethod.outputContainerUseds[0] != undefined && atomicTransfertMethod.outputContainerUseds[0].code != null){
							var result = varExperimentCommonFunctions.loadContainer(atomicTransfertMethod.outputContainerUseds[0]);
							results.promises.push(result.promise);
							result.promise.then(function(container){
								results.containers.push(container.data);
							});
						}
					});
					return results;
				}
		};
		init();
		return xToOne;
	};
	return constructor;
}]).factory('xToMany', ['$rootScope', function($rootScope){

	var constructor = function($scope, outputType){
		var outputType = outputType;

		var xToMany = {
				experimentToOutput : function(input){
					if(outputType === "none"){
						var allData = input.getData();
						if(allData != undefined){
							for(var i=0;i<$allData.length;i++){
								if($scope.experiment.value.atomicTransfertMethods[i].outputContainerUseds!=undefined){
									allData[i].outputContainerUseds = $scope.experiment.value.atomicTransfertMethods[i].outputContainerUseds;
									for(var j =0;j<$scope.experiment.value.atomicTransfertMethods[0].length;j++){
										allData[i].outputInstrumentProperties = $scope.experiment.value.atomicTransfertMethods[0].outputContainerUseds[j].instrumentProperties;
										allData[i].outputExperimentProperties = $scope.experiment.value.atomicTransfertMethods[0].outputContainerUseds[j].experimentProperties;
										i++;
									}
								}
							}
						}
					}
				},
				outputToExperiment : function(input){
					if(outputType === "none"){
						var allData = input.getData();
						if(allData != undefined){
							for(var i=0;i<$allData.length;i++){
								if($scope.experiment.value.atomicTransfertMethods[0].outputContainerUseds!=undefined){
									$scope.experiment.value.atomicTransfertMethods[0].outputContainerUseds = allData[i].outputContainerUseds;
									for(var j =0;j<$scope.experiment.value.atomicTransfertMethods[0].length;j++){
										$scope.experiment.value.atomicTransfertMethods[0].outputContainerUseds[j].instrumentProperties = allData[i].outputInstrumentProperties;
										$scope.experiment.value.atomicTransfertMethods[0].outputContainerUseds[j].experimentProperties = allData[i].outputExperimentProperties;
										varExperimentCommonFunctions.removeNullProperties($scope.experiment.value.atomicTransfertMethods[0].outputContainerUseds[j].experimentProperties);
										varExperimentCommonFunctions.removeNullProperties($scope.experiment.value.atomicTransfertMethods[0].outputContainerUseds[j].instrumentProperties);
										i++;
									}
								}
							}
						}
					}
				}
		};

		return xToMany;
	};
	return constructor;
}]).factory('oneToOne', ['$rootScope','oneToX','xToOne', '$http', '$parse', '$q', 'experimentCommonFunctions','mainService', function($rootScope, oneToX, xToOne, $http, $parse, $q, experimentCommonFunctions,mainService){

	var constructor = function($scope, inputType, outputType){
		var inputType = inputType;
		var outputType = outputType;
		var varOneToX = undefined;
		var varXToOne = undefined;

		var varexperimentCommonFunctions = undefined;

		var init = function(){
			varOneToX = oneToX($scope, inputType);
			varXToOne = xToOne($scope, outputType);
			varexperimentCommonFunctions = experimentCommonFunctions($scope);
		};

		var oneToOne = {
				experimentToInput : function(input){
					varOneToX.experimentToInput(input);
				},
				inputToExperiment : function(input){
					varOneToX.inputToExperiment(input);
				},
				experimentToOutput : function(output){
					varXToOne.experimentToOutput(output);
				},
				outputToExperiment : function(output){
					varXToOne.outputToExperiment(output);
				},
				reloadContainersDatatable : function(datatable){
					var promises = [];
					var resultInput = varOneToX.loadInputContainers($scope.experiment.value.atomicTransfertMethods);
					promises = promises.concat(resultInput.promises);
					if($scope.experiment.outputGenerated == true){
						var resultOutput = varXToOne.loadOutputContainers($scope.experiment.value.atomicTransfertMethods);
						promises = promises.concat(resultOutput.promises);
					}
					var that = this;
					$q.all(promises).then(function (res) {
						var oldDatatableValues = datatable.getData();
						datatable.setData(resultInput.containers,resultInput.containers.length);
						if($scope.experiment.outputGenerated == true){									
							var allData = datatable.getData();
							var j =0;
							while(allData[j]){
								if(angular.isUndefined(allData[j].outputContainerUsed)){
									allData[j].outputContainerUsed = oldDatatableValues[j].outputContainerUsed;
								}
								if(angular.isDefined(resultOutput.containers[j]) && resultOutput.containers[j].length>0){
									allData.outputContainerUsed = resultOutput.containers[j][0];
								}
								j++;
							}								

							datatable.setData(allData,allData.length);
						}
						that.experimentToInput(datatable);
						that.experimentToOutput(datatable);
					});
				},
				loadExperimentDatatable : function(datatable){
					var promises = [];
					var resultInput = varOneToX.loadInputContainers($scope.experiment.value.atomicTransfertMethods);
					promises = promises.concat(resultInput.promises);
					if($scope.experiment.outputGenerated == true){
						var resultOutput = varXToOne.loadOutputContainers($scope.experiment.value.atomicTransfertMethods);
						promises = promises.concat(resultOutput.promises);
					}

					$q.all(promises).then(function (res) {
						datatable.setData(resultInput.containers,resultInput.containers.length);
						$scope.atomicTransfere.experimentToInput(datatable);
						if($scope.experiment.outputGenerated == true){
							var allData = datatable.getData();
							var i = 0;
							if(resultOutput.containers.length > 0){
								angular.forEach(allData, function(data){
									data.outputContainerUsed = resultOutput.containers[i][0];
									i++;
								});
							}
							datatable.setData(allData,allData.length);
							$scope.atomicTransfere.experimentToOutput(datatable);
						}
						$scope.getInstrumentProperties($scope.experiment.value.instrument.typeCode,true);

						if(!angular.isUndefined(mainService.getBasket())){
							var containers = [];
							promises = [];
							$scope.basket = mainService.getBasket().get();
							angular.forEach($scope.basket, function(basket){
								var promise = $http.get(jsRoutes.controllers.containers.api.Containers.list().url,{params:{supportCode:basket.code}})
								.success(function(data, status, headers, config) {
									$scope.clearMessages();
									if(data!=null){
										angular.forEach(data, function(container){
											containers.push(container);
										});
									}
								})
								.error(function(data, status, headers, config) {
									alert("error");
								});
								promises.push(promise);
							});
							var that=this;
							$q.all(promises).then(function (res) {
								var containersDatatable = datatable.getData();
								containersDatatable = containersDatatable.concat(containers);
								datatable.setData(containersDatatable,containersDatatable.length);
								that.experimentToInput(datatable);
								that.experimentToOutput(datatable);
							});									
						}	

					});		
					if($scope.isOutputGenerated()){
						$scope.addOutputColumns();
						$scope.addExperimentPropertiesOutputsColumns();
						$scope.addInstrumentPropertiesOutputsColumns();
					}

					$scope.addExperimentPropertiesInputsColumns();
				},
				loadExperiment : function(input){
					if(inputType === "datatable"){
						this.loadExperimentDatatable(input);
					}
				},
				newExperiment : function(input){
					if(inputType === "datatable"){
						varexperimentCommonFunctions.newExperimentDatatable(input);
						$scope.addExperimentPropertiesInputsColumns();
					}							
				},
		};

		init();
		return oneToOne;
	};
	return constructor;
}]).factory('oneToMany', ['$rootScope', 'oneToX','xToMany','$http', '$parse', '$q', 'experimentCommonFunctions', function($rootScope, oneToX, xToMany, $http, $parse, $q, experimentCommonFunctions){

	var constructor = function($scope, inputType, outputType){
		var inputType = inputType;
		var outputType = outputType;
		var varOneToX = undefined;
		var varXToMany = undefined;

		var init = function(){
			varOneToX = oneToX($scope, inputType);
			varXToMany = xToMany($scope, outputType);
		};

		var oneToMany = {
				experimentToInput : function(input){
					varOneToX.experimentToInput(input);
				},
				inputToExperiment : function(input){
					varOneToX.inputToExperiment(input);
				},
				experimentToOutput : function(output){
					varXToMany.experimentToOutput(output);
				},
				outputToExperiment : function(output){
					varXToMany.outputToExperiment(output);
				}
		};

		init();
		return oneToMany;
	};
	return constructor;
}]).factory('manyToOne',['$rootScope','manyToX','xToOne','$http', '$parse', '$q', 'experimentCommonFunctions','mainService',  function($rootScope, manyToX, xToOne, $http, $parse, $q, experimentCommonFunctions,mainService){

	var constructor = function($scope, inputType, outputType){
		var inputType = inputType;
		var outputType = outputType;
		var varManyToX = undefined;
		var varXToOne = undefined;

		var varExperimentCommonFunctions = undefined;

		var init = function(){
			varManyToX = manyToX($scope, inputType);
			varXToOne = xToOne($scope, outputType);
			varExperimentCommonFunctions = experimentCommonFunctions($scope);

		};

		var manyToOne = {
				experimentToInput : function(input){
					varManyToX.experimentToInput(input);
				},
				inputToExperiment : function(input){
					varManyToX.inputToExperiment(input);
				},
				experimentToOutput : function(output){
					varXToOne.experimentToOutput(output);
				},
				outputToExperiment : function(output){
					varXToOne.outputToExperiment(output);
				},
				reloadContainersDatatable : function(datatable){
					var promises = [];
					var resultInput = varManyToX.loadInputContainers($scope.experiment.value.atomicTransfertMethods);
					promises = promises.concat(resultInput.promises);
					if($scope.experiment.outputGenerated == true){
						var resultOutput = varXToOne.loadOutputContainers($scope.experiment.value.atomicTransfertMethods);
						promises = promises.concat(resultOutput.promises);
					}
					var that = this;
					$q.all(promises).then(function (res) {
						datatable.setData(resultInput.containers,resultInput.containers.length);
						if($scope.experiment.outputGenerated == true){
							var allData = datatable.getData();
							if(resultOutput.containers.length > 0){
								angular.forEach(allData, function(data){
									data.outputContainerUsed = resultOutput.containers[0][0];
								});
							}
							datatable.setData(allData,allData.length);
						}
						that.experimentToInput(datatable);
						that.experimentToOutput(datatable);
					});					

				},
				reloadContainerDragNDrop : function(containersIn, containersOut, datatable){
					var i = 0;
					var containers = [];
					var promises = [];
					if(containersIn == undefined){
						containersIn = varManyToX.loadInputContainers($scope.experiment.value.atomicTransfertMethods);
					}
					promises = promises.concat(containersIn.promises);
					if(containersOut == undefined && $scope.outputGenerated === true){
						containersOut = varXToOne.loadOutputContainers($scope.experiment.value.atomicTransfertMethods);
						promises = promises.concat(containersOut.promises);
					}
					var that = this;
					$q.all(promises).then(function (res) {
						containersIn = containersIn;
						if(containersOut != undefined){
							containersOut = containersOut;									
						}
						while($scope.experiment.value.atomicTransfertMethods[i] != undefined){
							for(var j=0;j<$scope.experiment.value.atomicTransfertMethods[i].inputContainerUseds.length;j++){
								//create new container with in/out property
								var containerIn =  $scope.experiment.value.atomicTransfertMethods[i].inputContainerUseds[j];
								var percentage = containerIn.percentage;									
								var containerOut = {locationOnContainerSupport:{}};
								angular.forEach(containersIn.containers, function(container) {
									if(container.code == $scope.experiment.value.atomicTransfertMethods[i].inputContainerUseds[j].code){
										containerIn = container;
										containerIn.percentage = percentage;
									}
								});
								if(containersOut != undefined){
									angular.forEach(containersOut.containers, function(container) {
										if(container.code == $scope.experiment.value.atomicTransfertMethods[i].outputContainerUseds[0].code){
											containerOut = container;
										}
									});
								}
								var tags  = [];
								var sampleTypes = [];
								var libProcessTypeCodes = [];
								var sampleCodeAndTags = [];									
								angular.forEach(containerIn.contents, function(content){
									if(content.properties.tag != undefined && content.properties.tag != undefined && tags.indexOf(content.properties.tag.value) == -1){
										tags.push(content.properties.tag.value);
									}
									if(sampleTypes.indexOf(content.sampleTypeCode) == -1){
										sampleTypes.push(content.sampleTypeCode);
									}
									if(content.properties.libProcessTypeCode != undefined && libProcessTypeCodes.indexOf(content.properties.libProcessTypeCode.value) == -1){
										libProcessTypeCodes.push(content.properties.libProcessTypeCode.value);
									}
									if(content.properties.tag != undefined && content.sampleCode != undefined){
										sampleCodeAndTags.push(content.sampleCode+"/"+content.properties.tag.value);
									}
								});
								var concentration = undefined;
								if(angular.isDefined(containerIn.mesuredConcentration) && containerIn.mesuredConcentration!=null && containerIn.mesuredConcentration.value!=null){
									concentration = containerIn.mesuredConcentration.value;
								}



								var mesuredVolume = 0;
								if(containerIn.mesuredVolume !== undefined && containerIn.mesuredVolume !== null){
									mesuredVolume = containerIn.mesuredVolume;
								}
								var container = {"inputCode":containerIn.code,"inputSupportCode":containerIn.support.code, "projectCodes":containerIn.projectCodes,"sampleCodes":containerIn.sampleCodes,
										"inputX":containerIn.support.line, "inputTags":tags,"inputSampleTypes":sampleTypes, "inputLibProcessTypeCodes":libProcessTypeCodes, "inputState":containerIn.state,
										"inputY":containerIn.support.column, "experimentProperties":containerIn.experimentProperties,"fromExperimentTypeCodes":containerIn.fromExperimentTypeCodes,
										"instrumentProperties":containerIn.instrumentProperties, "outputPositionX":$scope.experiment.value.atomicTransfertMethods[i].line, "percentage": containerIn.percentage,
										"outputPositionY":$scope.experiment.value.atomicTransfertMethods[i].column,"inputConcentration":concentration,"sampleCodeAndTags":sampleCodeAndTags,"inputVolume":mesuredVolume};//Fake container
								containers.push(container);
							}
							i++;
						}

						datatable.setData(containers,containers.length);
						that.experimentToInput(datatable);
						$scope.addExperimentOutputDatatableToScope();
						that.experimentToOutput(datatable);
					});
				},
				loadExperimentCommon : function(fn){
					var promises = [];
					var resultInput = varManyToX.loadInputContainers($scope.experiment.value.atomicTransfertMethods);
					promises = promises.concat(resultInput.promises);
					if($scope.experiment.outputGenerated == true){
						var resultOutput = varXToOne.loadOutputContainers($scope.experiment.value.atomicTransfertMethods);
						promises = promises.concat(resultOutput.promises);
					}

					$q.all(promises).then(function (res) {
						if ('undefined' !== typeof fn) {
							fn(resultInput, resultOutput);
						}
						$scope.getInstrumentProperties($scope.experiment.value.instrument.typeCode,true);
						$scope.getInstruments(true);
						//$scope.atomicTransfere.experimentToInput();
						//$scope.atomicTransfere.experimentToOutput();

						if($scope.experiment.outputGenerated == true){
							$scope.addOutputColumns();
							$scope.addExperimentPropertiesOutputsColumns();
							$scope.addInstrumentPropertiesOutputsColumns();
						}
					});
				},					
				loadExperiment : function(input){
					var that = this;
					if(inputType === "datatable"){
						this.loadExperimentCommon(function(resultInput, resultOutput){
							input.setData(resultInput.containers,resultInput.containers.length);
							var allData = input.getData();
							if($scope.experiment.outputGenerated == true && resultOutput.containers[0].length>0){
								angular.forEach(allData, function(data){										
									data.outputContainerUsed = resultOutput.containers[0][0];
								});
							}
							if(!angular.isUndefined(mainService.getBasket())){
								$scope.basket = mainService.getBasket().get();
								angular.forEach($scope.basket, function(basket){
									$http.get(jsRoutes.controllers.containers.api.Containers.list().url,{params:{supportCode:basket.code}})
									.success(function(data, status, headers, config) {
										$scope.clearMessages();
										if(data!=null){
											angular.forEach(data, function(container){
												allData.push(container);
												$scope.experiment.value.atomicTransfertMethods[0].inputContainerUseds.push({code:container.code,instrumentProperties:{},experimentProperties:{},state:container.state});
											});
											input.setData(allData,allData.length);
											that.experimentToInput(input);
											that.experimentToOutput(input);
										}
									})
									.error(function(data, status, headers, config) {
										alert("error");
									});
								});
							}else{
								input.setData(allData,allData.length);
								that.experimentToInput(input);
								that.experimentToOutput(input);

							}
						});
					}else if(inputType === "dragndrop"){
						var that = this;
						this.loadExperimentCommon(function(resultInput, resultOutput){
							var containersOutput = undefined;
							if(resultOutput != undefined){
								containersOutput = resultOutput;
							}
							that.reloadContainerDragNDrop(resultInput, resultOutput, input);
							$scope.addExperimentPropertiesInputsColumns();
						});
					}
				},
				newExperiment : function(input){
					if(inputType === "datatable"){
						varExperimentCommonFunctions.newExperimentDatatable(input);
					}else if(inputType === "dragndrop"){
						varExperimentCommonFunctions.newExperimentDragndrop();
					}
					$scope.addExperimentPropertiesInputsColumns();					
				},
				containersToContainerUseds : function(containers){
					return varExperimentCommonFunctions.containersToContainerUseds(containers);
				}
		};

		init();
		return manyToOne;
	};
	return constructor;
}]).factory('oneToVoid',['$rootScope','oneToX','$http', '$parse', '$q', 'experimentCommonFunctions',  function($rootScope, oneToX, $http, $parse, $q, experimentCommonFunctions){

	var constructor = function($scope, inputType){
		var inputType = inputType;
		var varOneToX = undefined;

		var varexperimentCommonFunctions = undefined;

		var init = function(){
			varOneToX = oneToX($scope, inputType);
			varexperimentCommonFunctions = experimentCommonFunctions($scope);

		};

		var manyToOne = {
				experimentToInput : function(input){
					varOneToX.experimentToInput(input);
				},
				inputToExperiment : function(input){
					varOneToX.inputToExperiment(input);
				},

				loadExperimentDatatable : function(datatable){
					var containers = [];
					var promises = [];
					angular.forEach($scope.experiment.value.atomicTransfertMethods, function(atomicTransfertMethod){
						var promise = $http.get(jsRoutes.controllers.containers.api.Containers.get(atomicTransfertMethod.inputContainerUseds[0].code).url)
						.success(function(data, status, headers, config) {
							if(data!=null){
								containers.push(data);
							}
						})
						.error(function(data, status, headers, config) {
							alert("error");
						});

						promises.push(promise);
					});
					$q.all(promises).then(function (res) {
						datatable.setData(containers,containers.length);
						$scope.getInstrumentProperties($scope.experiment.value.instrument.typeCode,true);
						$scope.getInstruments(true);
						$scope.addExperimentPropertiesInputsColumns();
					});
				},
				loadExperiment : function(input){
					if(inputType === "datatable"){
						this.loadExperimentDatatable(input);
					}
				},
				newExperiment : function(input){
					if(inputType === "datatable"){
						varexperimentCommonFunctions.newExperimentDatatable(input);
					}
					$scope.addExperimentPropertiesInputsColumns();
				}
		};

		init();
		return manyToOne;
	};
	return constructor;
}]).factory('manyToVoid',['$rootScope','manyToX','$http', '$parse', '$q', 'experimentCommonFunctions',  function($rootScope, manyToX, $http, $parse, $q, experimentCommonFunctions){

	var constructor = function($scope, inputType){
		var inputType = inputType;
		var varManyToX = undefined;

		var varexperimentCommonFunctions = undefined;

		var init = function(){
			varManyToX = manyToX($scope, inputType);
			varexperimentCommonFunctions = experimentCommonFunctions($scope);

		};

		var manyToOne = {
				experimentToInput : function(input){
					varManyToX.experimentToInput(input);
				},
				inputToExperiment : function(input){
					varManyToX.inputToExperiment(input);
				},

				loadExperimentDatatable : function(datatable){
					var containers = [];
					var promises = [];
					angular.forEach($scope.experiment.value.atomicTransfertMethods, function(atomicTransfertMethod){
						angular.forEach(atomicTransfertMethod.inputContainerUseds,function(inputContainerUsed){
							var promise = $http.get(jsRoutes.controllers.containers.api.Containers.get(inputContainerUsed.code).url)
							.success(function(data, status, headers, config) {
								if(data!=null){
									containers.push(data);
								}
							})
							.error(function(data, status, headers, config) {
								alert("error");
							});
							promises.push(promise);
						});
					});
					$q.all(promises).then(function (res) {
						datatable.setData(containers,containers.length);
						$scope.getInstruments(true);
					});
				},
				loadExperiment : function(input){
					if(inputType === "datatable"){
						this.loadExperimentDatatable(input);
					}
				},
				newExperiment : function(input){
					if(inputType === "datatable"){
						varexperimentCommonFunctions.newExperimentDatatable(input);
					}
				}
		};

		init();
		return manyToOne;
	};
	return constructor;
}]);