# This is the cron script to ensure UltraKiss is running
cd /home/wmiles/KisekaeUltraKiss
date +"[%F %T] " | tr -d '\n'
vncserver -kill :1
vncserver :1
./ultrakiss.sh stop
sleep 1
./ultrakiss.sh start
ctail -n 1000 startultrakiss.log > tmpfile
mv tmpfile startultrakiss.log
