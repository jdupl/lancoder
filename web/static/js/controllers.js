'use strict';

/* Controllers */

angular.module('lancoder.controllers', []).
        controller('nodes', function($scope, $http) {
        $scope.mySelections = [];

        var refresh = $scope.refresh = function() {

          $scope.nodes = [];

          // Get nodes
            $http({method: 'GET', url: 'http://localhost:8080/api/nodes'})
                .success(function(data, status, headers, config) {
                    $scope.nodes = data;
                });
        }
        refresh();
        })
        .controller('jobs', function($scope) {

    
        }).controller('HeaderController', function($scope, $location) {
            $scope.isActive = function(viewLocation) {
                return viewLocation === $location.path();
            };
        });
