var controllers = angular.module('lancoder.controllers', ['lancoder.services']);
controllers.controller('nodes', function($scope, $http, $interval, apiService) {
  var refreshNodes = $scope.refreshNodes = function() {
    var old = $scope.nodes;
    apiService.nodes().then(function(nodes) {
      if (old) {
        var state = {};
        for (var i = 0; i < old.length;i++) {
          var oldNode = $scope.nodes[i];
          state[oldNode.unid] = oldNode.showCodecs;
        }
        for (var i = 0; i < nodes.length;i++) {
          if (state[nodes[i].unid]) {
            nodes[i].showCodecs = state[nodes[i].unid];
          }
        }
      }
      $scope.nodes = nodes;
    });
  };
  $scope.shutdown = function(node) {
    $http({method: 'POST', url: '/api/nodes/shutdown', data: node['unid']});
  };
  $scope.nodesAutoRefresh = function() {
    $interval(function() {
      $scope.refreshNodes();
    }, 5000);
  };
  refreshNodes();
  $scope.nodesAutoRefresh();
});
controllers.controller('jobs', function($scope, $http, $interval) {
  $http({method: 'GET', url: '/api/codecs/audio'})
      .success(function(data) {
        $scope.audioCodecs = data;
        for (var i = 0; i < data.length; i++) {
          var codec = data[i];
          if (codec.value === "VORBIS") {
            $scope.newJob.audioCodec = codec;
            break;
          }
        }
      });
  $http({method: 'GET', url: '/api/codecs/video'})
      .success(function(data) {
        $scope.videoCodecs = data;
        for (var i = 0; i < data.length; i++) {
          var codec = data[i];
          if (codec.value === "H264") {
            $scope.newJob.videoCodec = codec;
            break;
          }
        }
      });
  $scope.presets = ['ULTRAFAST', 'SUPERFAST', 'VERYFAST', 'FASTER', 'FAST', 'MEDIUM', 'SLOW', 'SLOWER', 'VERYSLOW', 'PLACEBO'];
  $scope.controlTypes = [
    {value: 'VBR', name: 'Variable Bitrate'},
    {value: 'CRF', name: 'Constant rate factor'}
  ];
  $scope.audioControlTypes = [
    {value: 'VBR', name: 'Variable Bitrate'},
    {value: 'CRF', name: 'Constant rate factor'}
  ];
  $scope.audioConfigs = [
    {value: 'AUTO', name: 'Automatic (Vorbis Q5 stereo)'},
    {value: 'COPY', name: 'Copy original stream'},
    {value: 'MANUAL', name: 'Manual configuration'}
  ];
  $scope.audioChannels = [
    {value: 'MONO', name: 'Mono'},
    {value: 'STEREO', name: 'Stereo'},
    {value: 'ORIGINAL', name: 'Keep original'}
  ];
  $scope.audioSampleRates = [8000, 11025, 22050, 44100, 48000, 88200, 96000];
  $scope.passes = [1, 2];

  var refreshJobs = $scope.refreshJobs = function() {
    // Get jobs
    $http({method: 'GET', url: '/api/jobs'})
        .success(function(data, status, headers, config) {
          for (var i = 0; i < data.length; i++) {
            switch (data[i].jobStatus) {
              case 'JOB_COMPLETED':
                data[i].panel = 'panel-success';
                break;
              case 'JOB_FAILED':
                data[i].panel = 'panel-danger';
                break;
              case 'JOB_COMPUTING':
                data[i].panel = 'panel-primary';
                break;
              case 'JOB_PAUSED':
                data[i].panel = 'panel-warning';
                break;
              case 'JOB_TODO':
                data[i].panel = 'panel-info';
                break;
              default:
                data[i].panel = 'panel-default';
            }
            data[i].completedTasks = 0;
            data[i].taskCount = data[i].tasks.length;
            data[i].totalFps = 0;
            for (var j = 0; j < data[i].taskCount; j++) {
              switch (data[i].tasks[j].taskProgress.taskState) {
                case 'TASK_COMPLETED':
                  data[i].completedTasks++;
                  break;
                case 'TASK_COMPUTING':
                  data[i].totalFps += data[i].tasks[j].taskProgress.fps;
                  break;
              }
            }
          }
          $scope.jobs = data;
        }).error(function() {
      $scope.jobs = [];
      $scope.jobs.error = 'Cannot not reach master server.';
    });
  };
  $scope.jobsAutoRefresh = function() {
    $interval(function() {
      $scope.refreshJobs();
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
  $scope.cleanJobs = function() {
    $http({method: 'POST', url: '/api/jobs/clean'})
    .success(function(data) {
        refreshJobs();
    });
  };
  refreshJobs();
  $scope.jobsAutoRefresh();
}).controller('HeaderController', function($scope, $location) {
  $scope.isActive = function(viewLocation) {
    return viewLocation === $location.path();
  };
});
