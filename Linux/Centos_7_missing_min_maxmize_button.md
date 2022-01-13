#### After installing latest version of Mozilla Firefox, there was no minimize anf maximize button.

For Firefox to work, had to install the gtk2-devel lib, using `sudo yum install gtk3-devel` 

Once above is done, in order to enable the maximize, minimize button used below command

[Link](https://trendoceans.com/how-to-get-minimize-and-maximize-button-in-gnome/)

Command:
```
$ gsettings set org.gnome.desktop.wm.preferences button-layout ":minimize,maximize,close"
```
