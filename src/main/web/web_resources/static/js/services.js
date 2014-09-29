var services = angular.module('lancoder.services', []);
services.factory('apiService', function($http) {
  var service = {
    nodes: function() {
      var promise = $http({method: 'GET', url: '/api/nodes'})
          .success(function(data, status, headers, config) {
            var nodes = data;
            for (var i = 0; i < nodes.length; i++) {
              switch (nodes[i].status) {
                case 'WORKING':
                  nodes[i].panel = 'panel-success';
                  break;
                case 'CRASHED':
                  nodes[i].panel = 'panel-danger';
                  break;
                case 'FREE':
                  nodes[i].panel = 'panel-primary';
                  break;
                case 'PAUSED':
                  nodes[i].panel = 'panel-warning';
                  break;
                case 'NOT_CONNECTED':
                  nodes[i].panel = 'panel-info';
                  break;
                default:
                  nodes[i].panel = 'panel-default';
              }
            }
            //stepCount
            for (var i = 0; i < nodes.length; i++) {
              var node = nodes[i];
              for (var j = 0; j < node.currentTasks.length; j++) {
                // Get and set task step count
                var currentTask = node.currentTasks[j];
                nodes[i].currentTasks[j].task.taskProgress.stepCount = Object.keys(currentTask.task.taskProgress.steps).length;
                // Get current step
                var index = currentTask.task.taskProgress.currentPassIndex;
                var currentStep = nodes[i].currentTasks[j].task.taskProgress.steps[index];
                // Convert the unit type from enum constant to pretty string
                if (currentStep.unit === 'SECONDS') {
                  currentStep.prettyUnit = 'times playback speed';
                } else if (currentStep.unit === 'FRAMES') {
                  currentStep.prettyUnit = 'FPS';
                }
                // Add reference to the currentStep
                nodes[i].currentTasks[j].task.taskProgress.currentStep = currentStep;
              }
            }
            return nodes;
          })
          .error(function() {
            var nodes = {};
            nodes.error = 'Cannot not reach master server.';
            return nodes;
          });
      return promise;
    }
  };
  return service;
});