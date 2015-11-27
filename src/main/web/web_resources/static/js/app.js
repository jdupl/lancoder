angular.module('lancoder', [
  'ngRoute',
  'ui.bootstrap',
  'angularMoment',
  'lancoder.controllers',
  'lancoder.services',
]).config(['$routeProvider', function($routeProvider) {
    $routeProvider.when('/nodes', {templateUrl: 'partials/nodes.html', controller: 'nodes'});
    $routeProvider.when('/jobs', {templateUrl: 'partials/jobs.html', controller: 'jobs'});
    $routeProvider.when('/logs', {templateUrl: 'partials/logs.html', controller: 'logs'});
    $routeProvider.otherwise({redirectTo: '/nodes'});
  }]);
