var app = angular.module("chatApp", ['ngRoute', 'ngAnimate']);

app.config(function ($routeProvider) {
    $routeProvider
        .when("/home", {
            templateUrl: '/w/home.html',
            controller: 'HomeCtrl'
        })
        .when('/login', {
            templateUrl: '/w/login.html',
            controller: 'LoginCtrl',
        })
        .when("/register", {
            templateUrl: '/w/register.html',
            controller: 'RegisterCtrl',
        })
        .otherwise( {
            redirectTo: '/login',
        });
});

app.controller("HomeCtrl", function($scope) {

});

app.controller("LoginCtrl", function($scope, $http, $location) {
    $scope.login = function () {
        var req = {
            method: "POST",
            url: "/login",
            data: {
                "userid": $scope.userid,
                "password": $scope.password,
            },
        };
        $http(req).then(
            function (res) {
                $location.path("/home");
            },
            function (res) {
                console.log("Network Failed (Login), Retry");
            });
    }
});

app.controller("RegisterCtrl", function($scope, $http, $location) {
    $scope.register = function () {
        if ($scope.password !== $scope.confirmpassword) {
            console.log("Password unmatch.");
            return false;
        }
        var req = {
            method: "POST",
            url: "/register",
            data: {
                "nickname": $scope.nickname,
                "password": $scope.password,
                "email": $scope.email,
            },
        };
        $http(req).then(
            function (res) {
                $location.path("/login");
            },
            function (res) {
                console.log("Network Failed (Register), Retry");
            });
    }
});



