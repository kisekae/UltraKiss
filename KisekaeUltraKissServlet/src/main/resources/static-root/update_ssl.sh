#!/bin/bash

# --- Configuration ---
# Source paths from your certificate renewal (e.g., Certbot)
NEW_CERT_PEM="/var/www/ultrakiss/certs/cert.pem"
NEW_KEY_PEM="/var/www/ultrakiss/certs/privkey.pem"
# Often fullchain.pem contains both the server cert and the intermediate CA(s)
CA_BUNDLE_PEM="/var/www/ultrakiss/certs/fullchain.pem"

# Target PKCS12 file used by Jetty
TARGET_P12_FILE="/home/wmiles/KisekaeUltraKiss/keystore.p12"

# Password for the new PKCS12 file (Jetty config must use this password)
P12_PASS="kisekaeultrakiss"

# Optional: Backup the old keystore before replacing
BACKUP_DIR="/home/wmiles/KisekaeUltraKiss/ssl_backup"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
# ---------------------

# Ensure backup directory exists
mkdir -p "$BACKUP_DIR"

# 1. Create a backup of the current PKCS12 file
if [ -f "$TARGET_P12_FILE" ]; then
    cp "$TARGET_P12_FILE" "$BACKUP_DIR/keystore.p12.$TIMESTAMP.bak"
    echo "Backed up old keystore to $BACKUP_DIR/keystore.p12.$TIMESTAMP.bak"
else
    echo "Old keystore not found, proceeding with creation."
fi

# 2. Convert the new PEM files to PKCS12 format using openssl
# The -in argument should ideally use a file that includes the full chain (cert + intermediate CAs)
openssl pkcs12 -export \
    -name jetty_ssl \
    -out "$TARGET_P12_FILE.tmp" \
    -inkey "$NEW_KEY_PEM" \
    -in "$CA_BUNDLE_PEM" \
    -passout pass:"$P12_PASS"

if [ $? -eq 0 ]; then
    # If conversion is successful, move the temporary file to the target location
    mv "$TARGET_P12_FILE.tmp" "$TARGET_P12_FILE"
    echo "Successfully converted PEM to PKCS12: $TARGET_P12_FILE"

    # 3. Signal Jetty to reload the SSL context
    # Modern Jetty versions (9.4.x+) automatically detect file changes if configured correctly.
    # By simply replacing the file, Jetty should detect the change and reload the SslContextFactory.
    echo "Jetty should detect the keystore file change and reload SSL context automatically."
else
    echo "Error during OpenSSL conversion. PKCS12 file not updated."
    rm "$TARGET_P12_FILE.tmp" # Clean up temp file on failure
fi
