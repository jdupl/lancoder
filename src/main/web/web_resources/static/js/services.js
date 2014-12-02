var services = angular.module('lancoder.services', []);
services.factory('apiService', function ($http) {
  return({
    nodes: nodes
  });

  function nodes() {
    var request = $http({method: 'GET', url: '/api/nodes'});
    return(request.then(processNodes, handleError));
  }
  function processNodes(response) {
    var nodes = response.data;
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
          nodes[i].offline = true;
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
      node.cleanCodecs = [];
      for (var k = 0; k < node.codecs.length; k++) {
        node.cleanCodecs.push(node.codecs[k].name);
      }
    }
    return nodes;
  }
  function handleError() {
    var nodes = {};
    nodes.error = 'Cannot not reach master server.';
    return nodes;
  }
});
