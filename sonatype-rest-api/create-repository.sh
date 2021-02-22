#!/usr/bin/env bash

username=
password=
profileId=
description="creating staging repository."

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
        -id | --profileId)
            shift
            profileId="$1"
            ;;
        -d | --description)
            shift
            description="$1"
            ;;
        *)
            echo "Unknown command $1"
            exit 1
            ;;
    esac
    shift
done

if test -z "$username" || test -z "$password" || test -z "$profileId"
then
      echo "missing parameter(s) for sonatype 'username' | 'password' | 'profileId'."
      exit 1
fi

stagedRepositoryId=$(
  curl -s --request POST -u $username:$password \
    --url https://oss.sonatype.org/service/local/staging/profiles/$profileId/start \
    --header 'Accept: application/json' \
    --header 'Content-Type: application/json' \
    --data '{ "data": {"description" : "'$description'"} }' | jq -r '.data.stagedRepositoryId'
)

echo "::set-output name=repository-id::$stagedRepositoryId"