var controllers = angular.module('lancoder.controllers', ['lancoder.services']);

controllers.controller('nodes', function($scope, $http, $interval, apiService) {

  $scope.getJobNameFromID = function(jobId) {
    return $scope.jobs.filter(function(job) { return job.jobId == jobId })[0].jobName;
  }

  $scope.timeFromNow = function(diff, noPrefix) {
    var now = (new Date).getTime();
    var momentNow = moment(now);
    var momentDiff = moment(now + diff);

    return momentDiff.from(momentNow, noPrefix);
  }

  $scope.refresh = function() {
    apiService.jobs().then(function(jobs) {
      $scope.jobs = jobs;
      $scope.complJobCount = jobs.filter(function(job) { return job.jobStatus == "JOB_COMPLETED" }).length;
    });
    apiService.nodes().then(function(nodes) {
      $scope.nodes = nodes;
      $scope.connectedCount = nodes.filter(function(node) { return !node.offline }).length;
    });
  };

  $scope.shutdown = function(node) {
    $http({method: 'POST', url: '/api/nodes/shutdown', data: node['unid']})
        .success(function(){
          $scope.refresh();
        });
  };

  $scope.nodesShowCodec = {};
  $scope.refresh();
  var intervalPromise = $interval(function() {
      $scope.refresh();
    }, 5000);
  $scope.$on('$destroy', function () { $interval.cancel(intervalPromise); });
});

controllers.controller('jobs', function($scope, $http, $interval, apiService) {

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

  $scope.refresh = function() {
    apiService.jobs().then(function(jobs) {
      $scope.jobs = jobs;
      $scope.complJobCount = jobs.filter(function(job) { return job.jobStatus == "JOB_COMPLETED" }).length;
    });
    apiService.nodes().then(function(nodes) {
      $scope.nodes = nodes;
      $scope.connectedCount = nodes.filter(function(node) { return !node.offline }).length;
    });
  };

  $scope.addjob = function(newjob) {
    $http({method: 'POST', url: '/api/jobs/', data: newjob})
        .success(function(data) {
            $scope.refresh();
            $scope.showAddJobPanel = false;
        }).error(function(data, status) {
          alert(data.message);
        });
  };

  $scope.deletejob = function(oldjob) {
    $http({method: 'DELETE', url: '/api/jobs/' + oldjob})
        .success(function(data) {
            $scope.refresh();
        }).error(function(data, status, err) {
          alert(data.message);
        });
  };

  $scope.cleanJobs = function() {
    $http({method: 'GET', url: '/api/jobs/clean'})
    .success(function(data) {
        $scope.refresh();
    });
  }

  $scope.timeFrom = function(start, end, noPrefix) {
    var momentStart = moment(start);
    var momentEnd = moment(end);

    return momentStart.from(momentEnd, noPrefix);
  }

  $scope.newJob = {};
  $scope.refresh();

  var intervalPromise = $interval(function() {
    $scope.refresh();
  }, 5000);
  $scope.$on('$destroy', function () { $interval.cancel(intervalPromise); });
}).controller('HeaderController', function($scope, $location) {
  $scope.isActive = function(viewLocation) {
    return viewLocation === $location.path();
  };
});
