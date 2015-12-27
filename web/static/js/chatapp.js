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
                var len = $scope.friend.all.length;
                var res = [];
                var input = $scope.friend.find;
                for (var i = 0; i < len; ++i) {
                    if ($scope.friend.all[i].userid.startsWith(input)
                            || $scope.friend.all[i].nickname.startsWith(input)) {
                        res.push($scope.friend.all[i]);
                    }
                }
                $scope.friend.search = res;
            }
        },

        SearchClick: function ($event) {
            var dom = $($event.currentTarget);
            $scope.friend.find = "";
            $scope.friend.search = [];
            var userid = dom.attr("data-id");
            $timeout(function () { //  DO NOT break out of the current $apply() cycle. 
                $("div#friends > ul > li[data-id='" + userid + "']").trigger("click");
            });
        },

        Find: function ($event) {
            $("div.modal#make_friends_modal").modal("show");
            $http({
                method: "POST",
                url: "/findfriend",
                data: {
                    keyword: "user",
                },
            }).then(
                function (res) {
                    $scope.friend.found = res.data['friend'];
                },
                function (res) {
                    console.log(res);
                }
            );
        },

        SetTarget: function ($event) {
            var userid = $($event.currentTarget).attr("data-id");
            $scope.friend.targetid = userid;
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
                    if (res.data['status'] === 'success') {
                        $scope.friend.addmessage = res.data['status'];
                        $scope.friend.Query();
                        $timeout(function () {
                            $('#make_friends_modal').modal("hide");
                        }, 1000);
                    }
                    else {
                        $scope.friend.addresult = res.data['message'];
                    }
                },
                function (res) {
                    console.log(res);
                }
            );
        },

        Delete: function (userid) {
            $http({
                method: 'POST',
                url: '/deletefriend',
                data: {
                    targetid: userid,
                },
            }).then(
                function (res) {
                    $scope.friend.Query();
                },
                function (res) {
                    console.log(res);
                }
            );
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

        Search: function () {
            if (!$scope.group.find || $scope.group.find.length <= 0) {
                $scope.group.search = [];
            }
            else {
                var res = [];
                var input = $scope.group.find;
                var len = $scope.group.admined.length;
                for (var i = 0; i < len; ++i) {
                    if ($scope.group.admined[i].groupid.startsWith(input)
                            || $scope.group.admined[i].groupname.startsWith(input)) {
                        res.push($scope.group.admined[i]);
                    }
                }
                len = $scope.group.joined.length;
                for (var i = 0; i < len; ++i) {
                    if ($scope.group.joined[i].groupid.startsWith(input)
                            || $scope.group.joined[i].groupname.startsWith(input)) {
                        res.push($scope.group.joined[i]);
                    }
                }
                $scope.group.search = res;
            }
        },

        SearchClick: function ($event) {
            var dom = $($event.currentTarget);
            $scope.group.find = "";
            $scope.group.search = [];
            var groupid = dom.attr("data-id");
            $timeout(function () { //  DO NOT break out of the current $apply() cycle.
                if ($("div#admined-groups > div > li[data-id='" + groupid + "']")) {
                    $("div#admined-groups > div > li[data-id='" + groupid + "']").trigger("click");
                }
                else {
                    $("div#joined-groups > div > li[data-id='" + groupid + "']").trigger("click");
                }
            });
        },

        Find: function ($event) {
            $("div.modal#join_group_modal").modal("show");
            $http({
                method: "POST",
                url: "/findgroup",
                data: {
                    keyword: "g",
                },
            }).then(
                function (res) {
                    $scope.group.found = res.data['group'];
                },
                function (res) {
                    console.log(res);
                }
            );
        },

        SetTarget: function ($event) {
            var groupid = $($event.currentTarget).attr("data-id");
            $scope.groupid.targetid = groupid;
        },

        Create: function () {
            if (!$scope.group.newname || $scope.group.newname.length <= 0) {
                return;
            }
            $http({
                method: "POST",
                url: "/constructgroup",
                data: {
                    "groupname": $scope.group.newname,
                    "description": $scope.group.newdesc,
                },
            }).then(
                function (res) {
                    $scope.group.createmessage = res.data['status'];
                    $scope.group.Query();
                    $timeout(function () {
                        $('#create_group_modal').modal("hide");
                    }, 1000);
                },
                function (res) {
                    console.log("Create a new group ERROR");
                }
            );
        },

        Join: function () {
            if (!$scope.group.findid || $scope.group.findid.length <= 0) {
                return;
            }
            $http({
                method: "POST",
                url: "/joingroup",
                data: {
                    "targetid": $scope.group.findid,
                },
            }).then(
                function (res) {
                    $scope.group.joinmessage = res.data['status'];
                    $scope.group.Query();
                    $timeout(function () {
                        $('#join_group_modal').modal("hide");
                    }, 2000);
                },
                function (res) {
                    console.log("Join into group ERROR");
                }
            );
        },

        Delete: function (groupid) {
            $http({
                method: 'POST',
                url: '/deletegroup',
                data: {
                    targetid: groupid,
                },
            }).then(
                function (res) {
                    $scope.group.Query();
                },
                function (res) {
                    console.log(res);
                }
            );
        },
    };

    $scope.room = {
        boardid: '0',
        note: [],
        
        BindContext: function () {
            $('.context-room').contextmenu({
                target:'#context-room-menu', 
                before: function(e, context) {
                    // execute code before context menu if shown
                },
                onItem: function(context, e) {
                    var action = $(e.currentTarget).attr("data-action");
                    var roomid = $(context.context).attr("data-id");
                    if (action === "enter") {
                        // enter.
                        $(context.context).click();
                    }
                    else {
                        // view notes.
                        $scope.room.ViewNotes(roomid);
                    }
                },
            });
        },

        ViewNotes: function (roomid) {
            $scope.room.boardid = roomid;
            $http({
                method: "POST",
                url: "/shownote",
                data: {
                    boardid: $scope.room.boardid,
                },
            }).then(
                function (res) {
                    $scope.room.note = res.data['note'];
                    $timeout(function () {
                        $("div.modal#view_notes_modal").modal('show');
                    });
                },
                function (res) {
                    console.log(res);
                }
            );
        },

        MakeNote: function () {
            $http({
                method: "POST",
                url: "/addnote",
                data: {
                    targetid: $scope.room.boardid,
                    content: $scope.room.newnote || "",
                }
            }).then(
                // refresh.
                function (res) {
                    $http({
                        method: "POST",
                        url: "/shownote",
                        data: {
                            boardid: $scope.room.boardid,
                        },
                    }).then(
                        function (res) {
                            $scope.room.note = res.data['note'];
                        },
                        function (res) {
                            console.log(res);
                        }
                    );
                },
                function (res) {
                    console.log(res);
                }
            );
        },

        Query: function () {
            $http({
                method: "POST",
                url: "/querychatroom",
                data: { },
            }).then(
                function (res) {
                    $scope.room.all = res.data['room'];
                    $timeout($scope.room.BindContext);
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

        Search: function () {
            if (!$scope.room.find || $scope.room.find.length <= 0) {
                $scope.room.search = [];
            }
            else {
                var len = $scope.room.all.length;
                var res = [];
                var input = $scope.room.find;
                for (var i = 0; i < len; ++i) {
                    if ($scope.room.all[i].roomid.startsWith(input)
                            || $scope.room.all[i].roomname.startsWith(input)) {
                        res.push($scope.room.all[i]);
                    }
                }
                $scope.room.search = res;
            }
        },

        SearchClick: function ($event) {
            var dom = $($event.currentTarget);
            $scope.room.find = "";
            $scope.room.search = [];
            var roomid = dom.attr("data-id");
            $timeout(function () { //  DO NOT break out of the current $apply() cycle. 
                $("div#rooms > ul > li[data-id='" + roomid + "']").trigger("click");
            });
        },
    };

    $scope.title = {
        text: '',
        kind: '',
        id: '',
        member: [],

        Query: function ($event) {
            var dom = $($event.currentTarget);
            if ($scope.title.kind === "friend") {
                return;
            }
            $http({
                method: "POST",
                url: $scope.title.kind === "group" ? "/memberofgroup" : "/memberofroom",
                data: {
                    targetid: $scope.chat.target.id,
                },
            }).then(
                function (res) {
                    $scope.title.member = res.data['member'];
                    $timeout(function () { dom.attr("data-content", $("div#member").html()); });
                },
                function (res) {
                    console.log("ERROR");
                }
            );
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

            $timeout(function () {
                $("[data-toggle='popover']").popover({
                    html: true,
                    content: function () {
                        return $("div#member").html();
                    },
                });
            });
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
                // trigger: 'manual',
                content: function () { return $("div#member").html(); },
                delay: { "show": 1000, "hide": 100 },
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


