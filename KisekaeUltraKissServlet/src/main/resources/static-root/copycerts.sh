#!/bin/bash

# This script should be installed in the KisekaeUltraKiss folder that holds all other
# scripts such as ultrakiss.sh and startUltraKiss.sh

#sudo setfacl -R -m u:wmiles:rx /etc/letsencrypt/live/ /etc/letsencrypt/archive/

#sudo mkdir -p /var/www/ultrakiss/certs/
#sudo chown -R wmiles:wmiles /var/www/ultrakiss/certs/

#sudo certbot reconfigure --cert-name mail.bronzeart.ca --deploy-hook /home/wmiles/KisekaeUltraKiss/copycerts.sh

cp /etc/letsencrypt/live/mail.bronzeart.ca/fullchain.pem /var/www/ultrakiss/certs/
cp /etc/letsencrypt/live/mail.bronzeart.ca/privkey.pem /var/www/ultrakiss/certs/
cp /etc/letsencrypt/live/mail.bronzeart.ca/cert.pem /var/www/ultrakiss/certs/
# Ensure correct permissions on copied files
chown wmiles:wmiles /var/www/ultrakiss/certs/*.pem
echo "Let's Encrypt SSL Certificates copied to /var/www/ultrakiss/certs/"

source /home/wmiles/KisekaeUltraKiss/update_ssl.sh
