/* create database */
drop database if exists chat;
create database chat character set utf8 collate utf8_general_ci;

/* create all tables */
use chat;

create table user_info (
	userid integer primary key not null auto_increment,
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
    content varchar(512) not null,
    userid integer not null,
    state bool not null
) engine=innodb, charset=utf8;

create table note (
	noteid integer primary key not null auto_increment,
    ctime timestamp not null default CURRENT_TIMESTAMP,
    content varchar(100) not null,
    userid integer not null
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

create table note_belong (
	boardid integer not null,
    noteid integer not null,
    primary key (boardid, noteid)
) engine=innodb, charset=utf8;

create table board_belong (
	boardid integer not null auto_increment,
    roomid integer not null,
    primary key (boardid, roomid)
) engine=innodb, charset=utf8;

CREATE TRIGGER add_group
AFTER INSERT ON group_info
FOR EACH ROW
insert into group_belong (groupid, userid) values (new.groupid, new.admin);

CREATE TRIGGER del_friend
AFTER DELETE ON friend
FOR EACH ROW
DELETE FROM chat_record WHERE chat_record.useraid = old.useraid AND chat_record.userbid = old.userbid;

DELIMITER $$
CREATE TRIGGER del_group
AFTER DELETE ON group_info
FOR EACH ROW
BEGIN
DELETE FROM group_belong WHERE group_belong.groupid = old.groupid;
DELETE FROM group_record WHERE group_record.groupid = old.groupid;
END;
$$
DELIMITER ;

CREATE TRIGGER add_noteboard
AFTER INSERT ON chatroom
FOR EACH ROW
insert into board_belong (roomid) values (new.roomid);

DELIMITER $$
CREATE PROCEDURE add_note(IN content_note varchar(100), IN userid_note int)
BEGIN
INSERT INTO note(content, userid) VALUES (content_note, userid_note);
SELECT noteid FROM note ORDER BY noteid DESC LIMIT 1;
END;
$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE add_user(IN nickname_user varchar(30),IN passwd_user varchar(32), IN email_user varchar(20))
BEGIN
INSERT INTO user_info(nickname, passwd, email) VALUES (nickname_user, passwd_user, email_user);
SELECT userid FROM user_info ORDER BY userid DESC LIMIT 1;
END;
$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE add_message(IN content_message varchar(512), IN userid_message int)
BEGIN
INSERT INTO message(content, userid, state) VALUES (content_message, userid_message, false);
SELECT messageid, ctime FROM message ORDER BY messageid DESC LIMIT 1;
END;
$$
DELIMITER ;