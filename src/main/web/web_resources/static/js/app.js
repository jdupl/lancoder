'use strict';
angular.module('lancoder', [
  'ngRoute',
  'lancoder.controllers'
]).config(['$routeProvider', function($routeProvider) {
    $routeProvider.when('/nodes', {templateUrl: 'partials/nodes.html', controller: 'nodes'});
    $routeProvider.when('/jobs', {templateUrl: 'partials/jobs.html', controller: 'jobs'});
    $routeProvider.otherwise({redirectTo: '/nodes'});
  }]);
