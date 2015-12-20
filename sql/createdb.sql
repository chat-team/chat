/* create database */
drop database if exists chat;
create database chat character set utf8 collate utf8_general_ci;

/* create all tables */
use chat;

create table user_info (
	userid integer primary key not null,
    nickname varchar(30) not null,
    passwd varchar(32) not null,
    email varchar(20) not null
) engine=innodb, charset=utf8;

create table group_info (
	groupid integer primary key not null auto_increment,
    groupname varchar(30) not null,
    admin varchar(10) not null,
    description varchar(50)
) engine=innodb, charset=utf8;

create table message (
	messageid integer primary key not null auto_increment,
    ctime timestamp not null default CURRENT_TIMESTAMP,
    content varchar(100) not null,
    userid integer not null,
    state bool not null
) engine=innodb, charset=utf8;

create table note (
	noteid integer primary key not null,
    ctime datetime not null,
    content varchar(100) not null
) engine=innodb, charset=utf8;

create table noteboard (
	boardid integer primary key not null
) engine=innodb, charset=utf8;

create table chatroom (
	roomid integer primary key not null auto_increment,
	roomname varchar(30) not null,
    description varchar(50)
) engine=innodb, charset=utf8;

create table friend (
	useraid integer not null,
    userbid integer not null,
    state bool not null default false,
    primary key (useraid, userbid)
) engine=innodb, charset=utf8;

create table note_record (
	userid integer not null,
    noteid integer not null,
    primary key (userid, noteid)
) engine=innodb, charset=utf8;

create table room_status (
	roomid integer not null,
    userid integer not null,
    primary key (roomid, userid)
) engine=innodb, charset=utf8;

create table chat_record (
	useraid integer not null,
    userbid integer not null,
    messageid integer not null,
    primary key (useraid, userbid, messageid)
) engine=innodb, charset=utf8;

create table group_belong (
	groupid integer not null,
    userid integer not null,
    primary key (groupid, userid)
) engine=innodb, charset=utf8;

create table group_record (
	groupid integer not null,
    messageid integer not null,
    primary key (groupid, messageid)
) engine=innodb, charset=utf8;

create table room_record (
	roomid integer not null,
    messageid integer not null,
    primary key (roomid, messageid)
) engine=innodb, charset=utf8;

create table note_belong (
	boardid integer not null,
    noteid integer not null,
    primary key (boardid, noteid)
) engine=innodb, charset=utf8;

create table board_belong (
	boardid integer not null,
    roomid integer not null,
    primary key (boardid, roomid)
) engine=innodb, charset=utf8;

CREATE TRIGGER add_group
AFTER INSERT ON group_info
FOR EACH ROW
insert into group_belong (groupid, userid) values (new.groupid, new.admin);
