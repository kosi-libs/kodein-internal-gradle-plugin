#!/usr/bin/env bash

username=
password=
stagedRepositoryId=

while [ "$1" != "" ]; do
    case $1 in
        -u | --username)
            shift
            username="$1"
            ;;
        -p | --password)
            shift
            password="$1"
            ;;
        -id | --stagedRepositoryId)
            shift
            stagedRepositoryId="$1"
            ;;
        *)
            echo "Unknown command $1"
            exit 1
            ;;
    esac
    shift
done

if test -z "$username" || test -z "$password" || test -z "$stagedRepositoryId"
then
      echo "missing parameter(s) for sonatype 'username' | 'password' | 'stagedRepositoryId'."
      exit 1
fi

response=$(curl -s --request POST -u $username:$password \
  --url https://oss.sonatype.org/service/local/staging/bulk/drop \
  --header 'Accept: application/json' \
  --header 'Content-Type: application/json' \
  --data '{ "data" : {"stagedRepositoryIds":["'$stagedRepositoryId'"], "description":"Drop '$stagedRepositoryId'." } }')

if [ ! -z "$response" ]; then
    echo "error while dropping repository $stagedRepositoryId : $response"
    exit 1
fi