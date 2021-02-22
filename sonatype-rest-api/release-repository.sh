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

if test -z "$username" || test -z "$password" || test -z "stagedRepositoryId"
then
      echo "missing parameter(s) for sonatype 'username' | 'password' | 'stagedRepositoryId'."
      exit 1
fi

closing=$(
  curl -s --request POST -u $username:$password \
    --url https://oss.sonatype.org/service/local/staging/bulk/close \
    --header 'Accept: application/json' \
    --header 'Content-Type: application/json' \
    --data '{ "data" : {"stagedRepositoryIds":["'$stagedRepositoryId'"], "description":"Close '$stagedRepositoryId'." } }'
)
  # todo discuss autoReleaseAfterClose

if [ ! -z "$closing" ]; then
    echo "error while closing $stagedRepositoryId : $closing"
    exit 1
fi

type=
transitioning=
for i in {1..60} ; do
  response=$(curl -s --request GET -u $username:$password \
        --url https://oss.sonatype.org/service/local/staging/repository/$stagedRepositoryId \
        --header 'Accept: application/json' \
        --header 'Content-Type: application/json')

  type=$(echo "$response" | jq -r '.type' )
  transitioning=$(echo "$response" | jq -r '.transitioning' )
  echo "$i type=$type / transitioning=$transitioning"
  if [ $type = "closed" ] && [ "$transitioning" = "false" ]; then
      break
  else
      sleep 10
  fi
done

if [ ! -z "$type" ] && [ $type = "closed" ] && [ ! -z "$transitioning" ] && [ transitioning = "false" ];
then
  release=$(curl -s --request POST -u $username:$password \
    --url https://oss.sonatype.org/service/local/staging/bulk/promote \
    --header 'Accept: application/json' \
    --header 'Content-Type: application/json' \
    --data '{ "data" : {"stagedRepositoryIds":["'$stagedRepositoryId'"], "autoDropAfterRelease" : true, "description":"Release '$stagedRepositoryId'." } }')
  if [ ! -z "$release" ]; then
      echo "error while releasing $stagedRepositoryId : $release"
      exit 1
  fi
else
  echo "Staged repository [$stagedRepositoryId] could not be closed."
  exit 1
fi
