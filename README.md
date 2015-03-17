About ACL Manipulator

I'm working as a network administrator at a bank. Here we got a lot of routers and switches. We have to manage thousands of access-lists on these devices. Sometimes we need to add a rule but it's not the same with every branch, the ip addresses in the access-lists must be altered to match the branch's dedicated ip portion. I did a research and found out that Ciscoworks bundle can support this feature, but it has not been updated since "Y2K" and is not for sale any more. So I created this piece of software and share it with you who may have the same problem. Currently ACLMan only support telnet to Cisco IOS devices but you can expand it to work with Juniper, over SSH etc...


User guide

For you guys network professionals it's pretty straightforward, but use it with precautions because the program deals with access-lists as normal text. It does not check your syntax before applying into routers and switches. So please check carefully.

In order to manage thousands of access lists, the program use a MySQL database. It stores device credentials unencrypted so you should install a new MySQL instance or have a separated database with restricted privileges.


ACL Template example

Variables {
  $host = "host 10.[ip(2)].10.10";
  $anotherhost = "host 10.[ip(2)].10.[ip(4) + 1]";
  $subnet = "123.45.0.0 0.0.255.255";
  $name = hostname();
  $ip = ip();
}

Content {
  Name = "[hostname()]_in";
  Type = "Extended";
  Rules = "
    10 permit tcp any host [ip()] eq telnet
    20 permit tcp $host $subnet eq www
    30 deny icmp $host any
    40 deny tcp $host $subnet eq 445
    50 deny tcp $host $subnet eq 139
    60 permit ip any any
  ";
}

Syntax notes
1. Functions:

ip(): Returns ip address of the interface you want to apply access-list on (in ACLMan it's called Access Interface). Eg: 123.45.67.89
ip(1), ip(2), ip(3), ip(4): Returns an octet of the ip address. Eg: ip(1) returns 123.
hostname(): Returns the hostname as it indicated.
Escape functions in "quotes" by enclosing in [brackets].

2. Variables contains text and functions, cannot refer to other variables.

3. You can use text, functions and variables in Content area.
