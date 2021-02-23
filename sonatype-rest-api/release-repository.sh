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
      echo "Missing parameter(s) for sonatype 'username' | 'password' | 'stagedRepositoryId'."
      exit 1
fi

closingRepository=$(
  curl -s --request POST -u "$username:$password" \
    --url https://oss.sonatype.org/service/local/staging/bulk/close \
    --header 'Accept: application/json' \
    --header 'Content-Type: application/json' \
    --data '{ "data" : {"stagedRepositoryIds":["'"$stagedRepositoryId"'"], "description":"Close '"$stagedRepositoryId"'." } }'
)

if [ ! -z "$closingRepository" ]; then
    echo "Error while closing repository $stagedRepositoryId : $closingRepository."
    exit 1
fi

start=$(date +%s)
while true ; do
  # force timeout after 15 minutes
  now=$(date +%s)
  if [ $(( (now - start) / 60 )) -gt 15 ]; then
      echo "Closing process is to long, stopping the job (waiting for closing repository)."
      exit 1
  fi

  rules=$(curl -s --request GET -u "$username:$password" \
        --url https://oss.sonatype.org/service/local/staging/repository/"$stagedRepositoryId"/activity \
        --header 'Accept: application/json' \
        --header 'Content-Type: application/json')

  closingRules=$(echo "$rules" | jq '.[] | select(.name=="close")')
  if [ -z "$closingRules" ] ; then
    continue
  fi

  rulesPassed=$(echo "$closingRules" | jq '.events | any(.name=="rulesPassed")')
  rulesFailed=$(echo "$closingRules" | jq '.events | any(.name=="rulesFailed")')

  if [ "$rulesFailed" = "true" ]; then
    echo "Staged repository [$stagedRepositoryId] could not be closed."
    exit 1
  fi

  if [ "$rulesPassed" = "true" ]; then
      break
  else
      sleep 5
  fi
done

start=$(date +%s)
while true ; do
  # force timeout after 5 minutes
  now=$(date +%s)
  if [ $(( (now - start) / 60 )) -gt 5 ]; then
      echo "Closing process is to long, stopping the job (waiting for transitioning state)."
      exit 1
  fi

  repository=$(curl -s --request GET -u "$username:$password" \
    --url https://oss.sonatype.org/service/local/staging/repository/"$stagedRepositoryId" \
    --header 'Accept: application/json' \
    --header 'Content-Type: application/json')

  type=$(echo "$repository" | jq -r '.type' )
  transitioning=$(echo "$repository" | jq -r '.transitioning' )
  if [ "$type" = "closed" ] && [ "$transitioning" = "false" ]; then
      break
  else
      sleep 1
  fi
done

release=$(curl -s --request POST -u "$username:$password" \
  --url https://oss.sonatype.org/service/local/staging/bulk/promote \
  --header 'Accept: application/json' \
  --header 'Content-Type: application/json' \
  --data '{ "data" : {"stagedRepositoryIds":["'"$stagedRepositoryId"'"], "autoDropAfterRelease" : true, "description":"Release '"$stagedRepositoryId"'." } }')

if [ ! -z "$release" ]; then
    echo "Error while releasing $stagedRepositoryId : $release."
    exit 1
fi