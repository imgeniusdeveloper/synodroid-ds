### Download task are stuck in waiting state? ###

Log into your NAS with user **root** using SSH then type the following commands (**Note:** the password for the root user is the password of your admin user.):
```
/usr/syno/etc/rc.d/S20pgsql.sh restart 
su -l admin
/usr/syno/pgsql/bin/dropdb download
exit
/usr/syno/etc/rc.d/S25download.sh restart
```