var services = angular.module('lancoder.services', []);
services.factory('apiService', function ($http) {
  return({
    nodes: nodes,
    jobs: jobs
  });

  function nodes() {
    var request = $http({method: 'GET', url: '/api/nodes'});
    return(request.then(processNodes, handleServerError));
  }

  function jobs() {
    var request = $http({method: 'GET', url: '/api/jobs'});
    return(request.then(processJobs, handleServerError));
  }

  function processJobs(response) {
    var jobs = response.data;
    for (var i = 0; i < jobs.length; i++) {
      switch (jobs[i].jobStatus) {
        case 'JOB_COMPLETED':
        jobs[i].panel = 'panel-success';
        break;
        case 'JOB_FAILED':
        jobs[i].panel = 'panel-danger';
        break;
        case 'JOB_COMPUTING':
        case 'JOB_MUXING':
        jobs[i].panel = 'panel-primary';
        break;
        case 'JOB_PAUSED':
        jobs[i].panel = 'panel-warning';
        break;
        case 'JOB_TODO':
        jobs[i].panel = 'panel-info';
        break;
        default:
        jobs[i].panel = 'panel-default';
      }
      jobs[i].completedTasks = 0;
      jobs[i].taskCount = jobs[i].tasks.length;
      jobs[i].totalFps = 0;
      for (var j = 0; j < jobs[i].taskCount; j++) {
        switch (jobs[i].tasks[j].taskProgress.taskState) {
          case 'TASK_COMPLETED':
            jobs[i].completedTasks++;
            break;
          case 'TASK_COMPUTING':
            jobs[i].totalFps += jobs[i].tasks[j].taskProgress.fps;
            break;
        }
      }
    }
    return jobs;
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

  function handleServerError() {
    var data = {};
    data.error = 'Cannot not reach master server.';
    return data;
  }
});
