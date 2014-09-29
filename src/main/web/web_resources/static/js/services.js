var services = angular.module('lancoder.services', []);
services.factory('apiService', function($http) {
  var getNodes = {
    nodes: function() {
      var promise = $http({method: 'GET', url: '/api/nodes'})
          .success(function(data, status, headers, config) {
            for (var i = 0; i < data.length; i++) {
              switch (data[i].status) {
                case 'WORKING':
                  data[i].panel = 'panel-success';
                  break;
                case 'CRASHED':
                  data[i].panel = 'panel-danger';
                  break;
                case 'FREE':
                  data[i].panel = 'panel-primary';
                  break;
                case 'PAUSED':
                  data[i].panel = 'panel-warning';
                  break;
                case 'NOT_CONNECTED':
                  data[i].panel = 'panel-info';
                  break;
                default:
                  data[i].panel = 'panel-default';
              }
            }
            var nodes = data;
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
            var nodes;
            nodes.error = 'Cannot not reach master server.';
            return nodes;
          });
      return promise;
    }
  };
  return getNodes;
});