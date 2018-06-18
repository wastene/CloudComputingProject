var app = angular.module('app', ['ngRoute']);

app.config(function($routeProvider){
	$routeProvider
		.when('/', {
			controller: 'Login',
			templateUrl: 'templates/login.html'
		})
		.when('/sensors', {
			controller: 'Sensors',
			templateUrl: 'templates/sensors.html'
		}).otherwise({
			redirectTo: '/'
		});
});
app.config(function($httpProvider){
	$httpProvider.defaults.withCredentials = true;
});

// Login
app.controller('Login', function($scope, $location, $http){
	
	$scope.title = "Login";
	
	$scope.init = function(){
		// Nothing to do here
	};
	
	$scope.login = function(username, password){
		if(typeof username !== 'string' || typeof password !== 'string'){
			return;
		}
		
		var protocol = $location.protocol();
		var host = $location.host();
		var port = $location.port();
		var url = protocol+"://"+host+":"+port+"/proxy/login";
		
		var passwordHash = SHA256(password);
		
		var data = {};
		data.user=username;
		data.pw=passwordHash;
		
		$http({
			method:'POST',
			url: url,
			data: data
		}).then(function(success){
			$scope.message = undefined;
			$scope.error = undefined;
			$location.path('/sensors');
		}, function(err){
			//$scope.message = err.data.error;
			$scope.message = undefined;
			$scope.error = err.data.error;
		});
	};
	
	$scope.register = function(username, password){
		if(typeof username !== 'string' || typeof password !== 'string'){
			return;
		}
		
		var protocol = $location.protocol();
		var host = $location.host();
		var port = $location.port();
		var url = protocol+"://"+host+":"+port+"/proxy/register";
		
		var passwordHash = SHA256(password);
		
		var data = {};
		data.user=username;
		data.pw=passwordHash;
		
		$http({
			method:'POST',
			url: url,
			data: data
		}).then(function(success){
			$scope.message = "Register successful";
			$scope.error = undefined;
		}, function(err){
			//$scope.message = err.data.error;
			$scope.message = undefined;
			$scope.error = err.data.error;
		});
	};
	
	$scope.init();
	
});

//
// Sensors
//
app.controller('Sensors', function($scope, $location, $http, $interval){
	$scope.title = "Sensors";
	
	$scope.eureka = {};
	$scope.eureka.host = $location.host();
	$scope.eureka.port = $location.port();
	
	$scope.sensorHosts = [];
	$scope.sensors = {};
	
	$scope.init = function(){
		$scope.getServers($scope.getSensorHosts);
		
		$scope.intervalID = $interval(function(){
			for(var i=0; i<$scope.sensorHosts.length; i++){
				var host = $scope.sensorHosts[i];
				
				$scope.getSensors(host.uri, host);
			}
		}, 1000);
		
		$scope.$on("$destroy", function(){
			if($scope.intervalID){
				$interval.cancel($scope.intervalID);
				$interval.cancel($scope.intervalServerID);
			}
		});
		
		$scope.$on('$locationChangeStart', function($event, next, current){
			if($scope.intervalID){
				$interval.cancel($scope.intervalID);
				$interval.cancel($scope.intervalServerID);
			}
		});
		
		$scope.intervalServerID = $interval(function(){
			$scope.getSensorHosts();
		}, 10000);
	};
	
	$scope.getServers = function(callback){
		var protocol = $location.protocol();
		var host = $location.host();
		var port = $location.port();
		var url = protocol+"://"+host+":"+port+"/servers";
		
		$http({
			method:'GET',
			url: url,
		}).then(function(success){
			
			eurekaInfo = success.data.eureka;
			if(eurekaInfo.host != ''){
				$scope.eureka.host = eurekaInfo.host;
			}
			if(eurekaInfo.port != ''){
				$scope.eureka.port = eurekaInfo.port;
			}
			
			if(callback !== undefined){
				callback();
			}
		}, function(err){
			console.log(err);
		});
		
	};
	
	$scope.getSensorHosts = function(){
		var protocol = $location.protocol();
		var host = $location.host();
		var port = $location.port();
		var url = protocol+"://"+host+":"+port+"/sensors";
		
		$http.get(url).then(function(success){	
			
			var data = success.data;
			
			for(var i=0; i<data.length; i++){
				for(var j=0; j<$scope.sensorHosts.length;j++){
					if(data[i].serviceId === $scope.sensorHosts[j].serviceId){
						data[i].collapse = $scope.sensorHosts[j].collapse;
					}
				}
			}
			$scope.sensorHosts = data;
			
			for(var i=0; i<$scope.sensorHosts.length;i++){
				var sensorUri = $scope.sensorHosts[i].uri;
				
				$scope.getSensors(sensorUri, $scope.sensorHosts[i]);
			}
		});
	};
	
	$scope.getSensors = function(uri, sensorHost){
		var protocol = $location.protocol();
		var url = uri+"/sensors";
		
		$http.get(url).then(function(success){
			var data = sensorHost;
			data.sensors = success.data;
			
			$scope.sensors[sensorHost.serviceId] = data;
		});
	};
	
	$scope.refresh = function(){
		$scope.sensorHosts = [];
		$scope.sensors = {};
		
		$scope.getServers($scope.getSensorHosts);
	};
	
	$scope.signout = function(){
		$location.path("/");
	};
	
	$scope.openAddSensor = function(host){
		if(host.collapse){
			host.collapse = false;
		}else {
			host.collapse = true;
		}
	};
	
	$scope.addSensor = function(host, name, unit){
		host.collapse = false;
		
		var url = host.uri+"/sensors";
		var data = {};
		data.name = name;
		data.unit = unit;
		
		$http({
			method: 'POST',
			url: url,
			data: data
		}).then(function(success){
			
		}).then(function(err){
			
		});
	};
	
	$scope.init();
});
