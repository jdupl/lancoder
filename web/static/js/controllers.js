'use strict';

/* Controllers */

angular.module('lancoder.controllers', []).
        controller('nodes', function($scope, $http, $timeout) {

          var refreshNodes = $scope.refreshNodes = function() {
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

          $scope.nodesAutoRefresh = function() {
            $timeout(function() {
              $scope.refreshNodes();
              $scope.nodesAutoRefresh();
            }, 5000);
          };

          refreshNodes();
          $scope.nodesAutoRefresh();
        })
        .controller('jobs', function($scope, $http, $timeout) {

          $scope.presets = ['ULTRAFAST', 'SUPERFAST', 'VERYFAST', 'FASTER', 'FAST', 'MEDIUM', 'SLOW', 'SLOWER', 'VERYSLOW', 'PLACEBO']
          $scope.controlTypes = [
            { value: 'VBR', name: 'Variable Bitrate' },
            { value: 'CRF', name:'Constant rate factor' }
          ];

          var refreshJobs = $scope.refreshJobs = function() {
            // Get jobs
            $http({method: 'GET', url: '/api/jobs'})
                    .success(function(data, status, headers, config) {
                      for (var i = 0; i < data.length; i++) {
                        switch (data[i].jobStatus) {
                          case "JOB_COMPLETED":
                            data[i].panel = "panel-success";
                            break;
                          case "JOB_CRASHED":
                            data[i].panel = "panel-danger";
                            break;
                          case "JOB_COMPUTING":
                            data[i].panel = "panel-primary";
                            break;
                          case "JOB_PAUSED":
                            data[i].panel = "panel-warning";
                            break;
                          case "JOB_TODO":
                            data[i].panel = "panel-info";
                            break;
                          default:
                            data[i].panel = "panel-default";
                        }
                        data[i].completedTasks = 0;
                        data[i].totalTasks = data[i].tasks.length;
                        data[i].totalFps = 0;
                        for (var j = 0; j < data[i].totalTasks; j++) {
                          switch (data[i].tasks[j].taskStatus.status) {
                            case "TASK_COMPLETED":
                              data[i].completedTasks++;
                              break;
                            case "TASK_COMPUTING":
                              data[i].totalFps += data[i].tasks[j].taskStatus.fps;
                              break;
                          }
                        }
                      }
                      $scope.jobs = data;
                    }).error(function() {
              $scope.jobs = [];
              $scope.jobs.error = "Could not reach master server.";
            });
          };

          $scope.jobsAutoRefresh = function() {
            $timeout(function() {
              $scope.refreshJobs();
              $scope.jobsAutoRefresh();
            }, 5000);
          };

          $scope.addjob = function(newjob) {
            $http({method: 'POST', url: '/api/jobs/add', data: newjob})
                    .success(function(data) {
                      if (data.success) {
                        refreshJobs();
                        $scope.showAddJobPanel = false;
                      } else {
                        alert(data.message);
                      }
                    }).error(function() {
              alert('Network failure');
            });
          };

          $scope.deletejob = function(oldjob) {
            $http({method: 'POST', url: '/api/jobs/delete', data: oldjob})
                    .success(function(data) {
                      if (data.success) {
                        refreshJobs();
                      } else {
                        alert(data.message);
                      }
                    }).error(function() {
              alert('Network failure');
            });
          };
          refreshJobs();
          $scope.jobsAutoRefresh();

        }).controller('HeaderController', function($scope, $location) {
  $scope.isActive = function(viewLocation) {
    return viewLocation === $location.path();
  };
});
