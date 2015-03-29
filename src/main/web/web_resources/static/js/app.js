angular.module('lancoder', [
  'ngRoute',
  'ui.bootstrap',
  'angularMoment',
  'lancoder.controllers',
  'lancoder.services',
]).config(['$routeProvider', function($routeProvider) {
    $routeProvider.when('/nodes', {templateUrl: 'partials/nodes.html', controller: 'nodes'});
    $routeProvider.when('/jobs', {templateUrl: 'partials/jobs.html', controller: 'jobs'});
    $routeProvider.otherwise({redirectTo: '/nodes'});
  }]);
