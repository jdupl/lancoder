'use strict';

/* Controllers */

angular.module('lancoder.controllers', []).
        controller('nodes', function($scope, $http, $timeout) {

          var refresh = $scope.refresh = function() {
            // Get nodes
            $http({method: 'GET', url: '/api/nodes'})
                    .success(function(data, status, headers, config) {
                      for (var i = 0; i < data.length; i++) {
                        switch (data[i].status) {
                          case "WORKING":
                            data[i].panel = "panel-success";
                            break;
                          case "CRASHED":
                            data[i].panel = "panel-danger";
                            break;
                          case "FREE":
                            data[i].panel = "panel-primary";
                            break;
                          case "PAUSED":
                            data[i].panel = "panel-warning";
                            break;
                          case "NOT_CONNECTED":
                            data[i].panel = "panel-info";
                            break;
                          default:
                            data[i].panel = "panel-default";
                        }
                      }
                      $scope.nodes = data;
                    }).error(function() {
                      $scope.nodes.error = "Could not reach master server.";
              });
          };

          $scope.intervalFunction = function() {
            $timeout(function() {
              $scope.refresh();
              $scope.intervalFunction();
            }, 5000);
          };
          refresh();
          $scope.intervalFunction();
        })
        .controller('jobs', function($scope) {


        }).controller('HeaderController', function($scope, $location) {
  $scope.isActive = function(viewLocation) {
    return viewLocation === $location.path();
  };
});
