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

app.factory('RedirectInterceptor', ['$q','$location', function ($q, $location) {
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

app.controller("HomeCtrl", function ($scope, $http, $location, $timeout) {
    $scope.userid = $.cookie('userid') || '';
    $scope.nickname = $.cookie('nickname') || '';

    $scope.friend = {
        BindContext: function () {
            $('.context-friend').contextmenu({
                target:'#context-friend-menu', 
                before: function(e, context) {
                    // execute code before context menu if shown
                },
                onItem: function(context, e) {
                    var action = $(e.currentTarget).attr("data-action");
                    var userid = $(context.context).attr("data-id");
                    if (action === "chat") {
                        $(context.context).click();
                    }
                    else {
                        $scope.friend.Delete(userid);
                    }
                },
            });
        },

        Query: function () {
            $http({
                method: "POST",
                url: "/queryfriend",
                data: { },
            }).then(
                function (res) {
                    $scope.friend.all = res.data.friend;
                    $timeout($scope.friend.BindContext);
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

        Delete: function (userid) {
            console.log("delete friend, id: " + userid);
            // $http({
            //     method: 'POST',
            //     url: '/',
            //     data: {
            //         target: userid,
            //     },
            // }).then(
            //     function (res) {
            //         $scope.friend.Query();
            //     },
            //     function (res) {
            //         console.log(res);
            //     }
            // );
        },
    };

    $scope.group = {
        BindContext: function () {
            $('.context-group').contextmenu({
                target:'#context-group-menu', 
                before: function(e, context) {
                    // execute code before context menu if shown
                },
                onItem: function(context, e) {
                    // execute on menu item selection
                    var action = $(e.currentTarget).attr("data-action");
                    var groupid = $(context.context).attr("data-id");
                    if (action === "chat") {
                        $(context.context).click();
                    }
                    else {
                        $scope.group.Delete(groupid);
                    }
                }
            });
        },

        Query: function () {
            $http({
                method: "POST",
                url: "/querygroup",
                data: { },
            }).then(
                function (res) {
                    $scope.group.admined = [];
                    $scope.group.joined = [];
                    for (var i = 0; i < res.data.group.length; ++i) {
                        var g = res.data.group[i];
                        if (g.admin === $.cookie('userid')) {
                            $scope.group.admined.push(g);
                        }
                        else {
                            $scope.group.joined.push(g);
                        }
                    }
                    $timeout($scope.group.BindContext);
                },
                function (res) {
                    console.log("Query all groups ERROR");
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
                    console.log("Create a new group ERROR");
                }
            );
        },

        Join: function () {
            if (!$scope.group.search || $scope.group.search.length <= 0) {
                return;
            }
            $http({
                method: "POST",
                url: "/joingroup",
                data: {
                    "targetid": $scope.group.search,
                },
            }).then(
                function (res) {
                    $scope.group.Query();
                },
                function (res) {
                    console.log("Join into group ERROR");
                }
            );
        },

        Delete: function (groupid) {
            console.log("delete group, id: " + groupid);
            // $http({
            //     method: 'POST',
            //     url: '/',
            //     data: {
            //         target: groupid,
            //     },
            // }).then(
            //     function (res) {
            //         $scope.group.Query();
            //     },
            //     function (res) {
            //         console.log(res);
            //     }
            // );
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
                    $scope.room.all = res.data['room'];
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

    $scope.title = {
        text: '',
        kind: '',
        id: '',
        member: ["aaa", "bbb", "ccc", "ddd", "eee", "aaa1", "bbb2", "ccc3", "ddd4", 
            "eee16", "aaa15", "bbb12", "ccc11", "ddd10", "eee9", "aaa8", "bbb7", "ccc6", "ddd5", 
            "eee17", "aaa14", "bbb13", "ccc19", "ddd18", "eee20"],

        Query: function ($event) {
            var dom = $($event.currentTarget);
            // if ($scope.title.kind === "friend") {
            //     return;
            // }
            // $http({
            //     method: "POST",
            //     url: "/"
            // })
            // dom.attr("data-content", $("div#member")[0].outerHTML);
        },
    };

    $scope.chat = {
        socket: {
            friend: undefined,
            group: undefined,
            room: undefined,
        },
        target: {
            kind: '',
            id: '-1',
        },
        log: {
            friend: {

            },
            group: {

            },
            room: {

            },
        },
        unreads: [],

        Connect: function () {
            $scope.chat.socket.friend = new WebSocket('ws://' + location.host + '/chat/friend');
            $scope.chat.socket.friend.onmessage = function (evt) {
                var message = JSON.parse(evt.data);
                console.log("friend chat get message: " + JSON.stringify(message));
                var sender = message.sender;
                if ($scope.chat.target.kind === 'friend' && $scope.chat.target.id === sender) {
                    $scope.chat.unreads.push(message); // show it.
                }
                else {
                    var badge = $("span#badge-friend-" + sender);
                    badge.html(eval((badge.html() && badge.html().length > 0) ? badge.html() : 0) + 1);
                    $scope.chat.log['friend'][sender] = $scope.chat.log['friend'][sender] || [];
                    $scope.chat.log['friend'][sender].push(message);
                }
                $scope.$apply();
            };
            $scope.chat.socket.friend.onclose = function (evt) {
                console.log(evt);
            };
            $scope.chat.socket.group = new WebSocket('ws://' + location.host + '/chat/group');
            $scope.chat.socket.group.onmessage = function (evt) {
                var message = JSON.parse(evt.data);
                console.log("group chat get message: " + JSON.stringify(message));
                var sender = message.sender, group = message.group;
                if (sender === $scope.userid) { return; } // ignore response message to user self.
                if ($scope.chat.target.kind === "group" && $scope.chat.target.id === group) {
                    $scope.chat.unreads.push(message);
                }
                else {
                    var badge = $("span#badge-group-" + group);
                    badge.html(eval((badge.html() && badge.html().length > 0) ? badge.html() : 0) + 1);
                    $scope.chat.log['group'][group] = $scope.chat.log['group'][group] || [];
                    $scope.chat.log['group'][group].push(message); // store it.
                }
                $scope.$apply();
            };
            $scope.chat.socket.group.onclose = function (evt) {
                console.log(evt);
            };
        },

        EnterRoom: function (roomid) {
            $scope.chat.socket.room = new WebSocket('ws://' + location.host + '/chat/room');
            $scope.chat.socket.room.onopen = function (evt) {
                $scope.chat.socket.room.send(JSON.stringify({
                    content: $scope.nickname + ' enter.',
                    target: roomid,
                    status: 'enter',
                }));
                console.log("enter message sent");
            };
            $scope.chat.socket.room.onmessage = function (evt) {
                var message = JSON.parse(evt.data);
                console.log("room chat get message: " + JSON.stringify(message));
                var sender = message.sender, room = message.room;
                if (sender === $scope.userid) { return; } // ignore response message to user self.
                if (!$scope.chat.unreads) {
                    $scope.chat.unreads = [];
                }
                $scope.chat.unreads.push(message);
                $scope.$apply();
            };
            $scope.chat.socket.room.onclose = function (evt) {
                console.log(evt);
            };
        },

        LeaveRoom: function (roomid) {
            if (!$scope.chat.socket.room || $scope.chat.socket.room.readyState !== WebSocket.OPEN) {
                return;
            }
            $scope.chat.socket.room.send(JSON.stringify({
                content: $scope.nickname + ' leave.',
                target: roomid,
                status: 'exit',
            }));
            $scope.chat.socket.room.close();
        },

        Send: function () {
            var kind = $scope.chat.target.kind;
            var id = $scope.chat.target.id;
            var content = $scope.chat.message;
            if (!content || content.length == 0) {
                content = " ";
            }
            if (content.length > 512) {
                content = "Too long message!";
            }
            var message = {
                target: id,
                content: content,
            };
            var socket = $scope.chat.socket[kind];
            if (socket) {
                console.log(message);
                socket.send(JSON.stringify(message));
            }
            $scope.chat.unreads.push({
                content: content,
                self: true,
            });
            $scope.chat.message = ""; // reset.
        },

        SetContext: function ($event) {
            var dom = $($event.currentTarget);
            var kind = dom.attr("data-kind");
            var id = dom.attr("data-id");
            console.log("switch context: " + kind + "  " + id);
            if (kind === 'room') {
                if (id !== $scope.chat.target.id) {
                    $scope.chat.LeaveRoom($scope.chat.target.id);
                    $scope.chat.EnterRoom(id);
                }
            }
            else {
                $scope.chat.LeaveRoom(id);
                $scope.chat.unreads = $scope.chat.log[kind][id] || [];
                dom.children("span.badge").html('');
            }
            // toggle
            $("li.select").each(function (idx) {
                $(this).removeClass("active");
            });
            dom.addClass("active");
            $scope.title.text = kind + "  " + dom.attr("data-name");
            $scope.title.kind = kind;
            $scope.title.id = id;

            // store state
            $scope.chat.target.kind = kind;
            $scope.chat.target.id = id;
        },
    };

    $scope.init = {
        // environment initialize.
        Initial: function () {
            $scope.friend.Query();
            $scope.group.Query();
            $scope.room.Query();

            $scope.chat.Connect();

            // js binding.
            $('div.chatting').bind('DOMSubtreeModified', function (evt) {
                var dom = $(evt.currentTarget);
                dom.scrollTop(dom.prop("scrollHeight"));
            });

            $("[data-toggle='popover']").popover({
                html: true,
                content: function () {
                    return $("div#member").html();
                },
            });
        },

        // toggle tab.
        Toggle: function ($event) {
            $("#spage").children().each(function (idx) {
                $(this).delay(100).fadeOut(100);
            })
            var nav = $event.currentTarget;
            $("#" + $(nav).attr("data-page")).fadeIn(100);
            $("li.nav-item").each(function (idx) {
                $(this).removeClass("active");
            });
            $(nav).addClass("active");
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
                    // store user ID in cookie.
                    $.cookie("userid", $scope.userid);
                    $.cookie("nickname", res.data['nickname']);
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


