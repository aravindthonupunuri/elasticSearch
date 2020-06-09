#!/usr/bin/env bash

set -e

# script to read/write app private key to secrets-key-manager as well as post as vela secret
scriptname="vela_secretkey"
. `dirname $0`/appinfo.sh
. $scriptDir/logging.sh
. $scriptDir/utils.sh

usage="
./${scriptname}.sh <command | list> <environment | $SUPPORTED_ENVIRONMENTS>
./${scriptname}.sh <command | read> <environment | $SUPPORTED_ENVIRONMENTS>
./${scriptname}.sh <command | write> <environment | $SUPPORTED_ENVIRONMENTS> <priv-key-file-base64>
"
# priv-key-file in pem format is already a base64 encoded file but with new lines in it.
# priv-key-file-base64 represents original pem file further base64 encoded to become a single line base64 encoded string
# priv-key-file-base64 filename needs to end with _base64.txt

echo " "
echo "Running: $0 $@"
echo " "

if [ -z "$1" ]; then
    log_usage_err "Missing command argument"
elif [[ "$1" != "list" && "$1" != "read" && "$1" != "write" ]]; then
    log_usage_err "Invalid command $1"
fi

check_environment "$2"
envname=$2
privkey_value_file=""

if [ "$1" == "write" ]; then
    if [ -z "$3" ]; then
        log_usage_err "Missing private Key File"
    elif [ ! -f "$3" ]; then
        log_usage_err "Nonexisting private Key File: $3"
    elif [[ ! "$3" =~ .*"_base64.txt"$ ]]; then
        log_usage_err "Invalid private Key File [without _base64.txt extension]: $3"
    fi
    privkey_value_file="$3"
fi

cd $scriptDir

echo " "
privkey_name=${appname}-privkey-${envname}
privkey_name_full="${gitorg}_${gitrepo}_$privkey_name"
if [ "$1" == "list" ]; then
    log_msg "Listing private keys from secrets-key-manager"
    echo " "
    $SECRET_MANAGER_PATH/secrets.sh --action list
elif [ "$1" == "read" ]; then
    log_msg "Reading private key from secrets-key-manager"
    echo " "
    $SECRET_MANAGER_PATH/secrets.sh --action show --secret-name $privkey_name_full
elif [ "$1" == "write" ]; then
    # warn if we are overwriting an existing private key
    set +e
    existing_key=`${SECRET_MANAGER_PATH}/secrets.sh --action show --secret-name ${privkey_name_full} 2>&1`
    set -e
    if [[ "$existing_key" != *"ERROR: path does not exist"* ]]; then
        proceed=""
        while [[ "$proceed" != "Y" && "$proceed" != "y" && "$proceed" != "N" && "$proceed" != "n" ]]; do
            echo " "
            read -p "WARNING: $privkey_name_full already exists, do you want to overwrite? [Y/N]: " proceed
            if [[ "$proceed" == "N" || "$proceed" == "n" ]]; then
                echo "Exiting without overwriting..."
                exit 0
            fi
        done
    fi
    log_msg "Writing private key to secrets-key-manager"
    echo " "
    echo "$SECRET_MANAGER_PATH/secrets.sh --action create --secret-name $privkey_name_full --secret-value @${privkey_value_file} --remove-eof-newlines-from-file
"
    $SECRET_MANAGER_PATH/secrets.sh --action create --secret-name $privkey_name_full --secret-value @${privkey_value_file} --remove-eof-newlines-from-file

    echo " "
    log_msg "Now writing private key to vela"
    vela add secret --engine native --type repo --org $gitorg --repo $gitrepo --name $privkey_name --value @${privkey_value_file}

    echo " "
    echo "=========================================="
    log_msg "Removing local copy of secret key file:"
    echo "=========================================="
    rm -f ${privkey_value_file}
fi
echo " "

cd $currDir
