var app = angular.module("chatApp", ['ngRoute', 'ui.bootstrap', 'ngAnimate']);

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
        .when("/hint/:username/", {
            templateUrl: '/w/hint.html',
            controller: 'HintCtrl',
        })
        .otherwise( {
            redirectTo: '/login',
        });
});

app.factory('RedirectInterceptor',['$q','$location', function ($q, $location) {
    return {
        responseError: function (res) {
            if (res.status === 401) {
                $location.path('/');
            }
            return $q.reject(res);
        },
    }
}]);
//Http Intercpetor to check auth failures for xhr requests
app.config(function ($httpProvider) {
    $httpProvider.interceptors.push('RedirectInterceptor');
});

app.controller("HomeCtrl", function ($scope, $http, $location) {
    $scope.friend = {
        Query: function () {
            $http({
                method: "POST",
                url: "/queryfriend",
                data: { },
            }).then(
                function (res) {
                    $scope.friend.all = res.data.friend;
                },
                function (res) {
                    console.log("ERROR");
                }
            );
        },

        Search: function () {
            if (!$scope.friend.find || $scope.friend.find.length <= 0) {
                $scope.friend.search = [];
            }
            else {
                $scope.friend.search = [
                    { userid: "1", username: "eee"},
                    { userid: "2", username: "dddd"},
                    { userid: "3", username: "fff"},
                ];
                console.log($scope.friend.search);
            }
        },

        MakeF: function () {
            if (!$scope.friend.targetid || $scope.friend.targetid.length <= 0) {
                console.log("Target ID Error");
                return;
            }
            $http({
                method: "POST",
                url: "/makefriends",
                data: {
                    targetid: $scope.friend.targetid,
                },
            }).then(
                function (res) {
                    $('#make_friends_modal').modal("hide");
                    $scope.friend.Query();
                },
                function (res) {
                    console.log(res);
                }
            );
        },
    };

    $scope.group = {
        Query: function () {
            $http({
                method: "POST",
                url: "/querygroup",
                data: { },
            }).then(
                function (res) {
                    $scope.group.admined = res.data.group;
                },
                function (res) {
                    console.log("ERROR");
                }
            );
        },

        Create: function () {
            if (!$scope.group.create || $scope.group.create.length <= 0) {
                return;
            }
            $http({
                method: "POST",
                url: "/constructgroup",
                data: {
                    "groupname": $scope.group.create,
                    "description": "description of " + $scope.group.create,
                },
            }).then(
                function (res) {
                    $scope.group.Query();
                },
                function (res) {
                    console.log("ERROR");
                }
            );
        },

        Join: function () {
            console.log("join into a group");
        },
    };

    $scope.room = {
        Query: function () {
            $http({
                method: "POST",
                url: "/querychatroom",
                data: { },
            }).then(
                function (res) {
                    
                },
                function (res) {
                    console.log("ERROR");
                }
            );
        },
        Create: function () {
            if (!$scope.room.create || $scope.room.create.length <= 0) {
                return;
            }
            $http({
                method: "POST",
                url: "/constructchatroom",
                data: {
                    "roomname": $scope.room.create,
                    "description": "description of " + $scope.room.create,
                },
            }).then(
                function (res) {
                    $scope.room.Query();
                },
                function (res) {
                    console.log("ERROR");
                }
            );
        },
    };

    $scope.chat = {
        f2fchat: new WebSocket('ws://localhost:8080' + '/f2fchat'),
        groupchat: undefined,
        roomchat: undefined,
        target: '-1',

        Connect: function () {
            $scope.chat.f2fchat.onmessage = function (evt) {
                console.log(evt);
            };
        },

        Send: function () {
            var message = {
                target: $scope.chat.target,
                content: $scope.chat.message,
            };
            console.log(message);
            $scope.chat.f2fchat.send(JSON.stringify(message));
        },

        SetContext: function ($event) {
            var uid = $($event.currentTarget).attr("data-id");
            console.log("switch context: " + uid);
            $scope.chat.target = uid;
        },
    };

    $scope.init = {
        // environment initialize.
        Initial: function () {
            $scope.friend.Query();
            $scope.group.Query();
            $scope.room.Query();

            $scope.chat.Connect();
        },

        // toggle tab.
        Toggle: function ($event) {
            $("#spage").children().each(function (idx) {
                $(this).delay(100).fadeOut(100);
            })
            var nav = $event.currentTarget;
            $("#" + $(nav).attr("data-page")).fadeIn(100);
            $("#snav").children().each(function (idx) {
                $(this).removeClass("active");
            });
            $(nav).parent().addClass("active");
            $event.preventDefault();
        },
    };
});

app.controller("LoginCtrl", function ($scope, $http, $location) {
    $scope.login = function () {
        var req = {
            method: "POST",
            url: "/login",
            data: {
                "userid": $scope.userid,
                "passwd": $scope.password,
            },
        };
        $http(req).then(
            function (res) {
                if (res.data['status'] === 'success') {
                    $location.path("/home");
                }
                else {
                    $scope.message = 'Login failed, please retry!';
                }
            },
            function (res) {
                $scope.message = 'Network outage!';
            }
        );
    }
});

app.controller("RegisterCtrl", function ($scope, $http, $location) {
    $scope.register = function () {
        if ($scope.password !== $scope.confirmpassword) {
            $scope.message = 'The passwords you entered must be the same!';
        }
        else {
            $http({
                method: "POST",
                url: "/register",
                data: {
                    "nickname": $scope.nickname,
                    "passwd": $scope.password,
                    "email": $scope.email,
                },
            }).then(
                function (res) {
                    if (res.data['status'] === 'success') {
                        $location.path("/hint/" + res.data['username']);
                    }
                    else {
                        $scope.message = 'Register failed, please retry!';
                    }
                },
                function (res) {
                    $scope.message = 'Network outage!';
                }
            );
        }
    }
});

app.controller("HintCtrl", function ($scope, $routeParams, $location) {
    $scope.username = $routeParams.username;
    // Other actions.
});


