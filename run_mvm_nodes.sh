#!/bin/bash

usage() {
  echo "Usage: $0 [--node <node>] <docker_compose_action>"
  echo "Example: $0 --node provider --profile marketplace up -d"
  echo "Example: $0 --profile marketplace up -d"
  exit 1
}

node=""
action=()

while [[ $# -gt 0 ]]; do
  case "$1" in
    --node)
      node="$2"
      shift 2
      ;;
    *)
      action+=("$1")
      shift
      ;;
  esac
done

if [ ${#action[@]} -eq 0 ]; then
  usage
fi

base_interpolation_env_file="./deployment/assets/env/base_interpolation.env"
compose_file="./deployment/docker-compose.mvm.yml"

if [ -n "$node" ]; then
  override_env_file="./deployment/overrides/${node}.env"
  override_file="./deployment/overrides/${node}.override.yml"

  missing_files=()
  [ ! -f "$base_interpolation_env_file" ] && missing_files+=("$base_interpolation_env_file")
  [ ! -f "$override_env_file" ] && missing_files+=("$override_env_file")
  [ ! -f "$override_file" ] && missing_files+=("$override_file")
  [ ! -f "$compose_file" ] && missing_files+=("$compose_file")

  if [ ${#missing_files[@]} -ne 0 ]; then
    echo "Error: Missing required files:"
    for file in "${missing_files[@]}"; do
      echo "  - $file"
    done
    exit 1
  fi

  docker compose --env-file "$base_interpolation_env_file" --env-file "$override_env_file" -f "$compose_file" -f "$override_file" "${action[@]}"
else
  if [ ! -f "$base_interpolation_env_file" ] || [ ! -f "$compose_file" ]; then
    echo "Error: Missing required files:"
    [ ! -f "$base_interpolation_env_file" ] && echo "  - $base_interpolation_env_file"
    [ ! -f "$compose_file" ] && echo "  - $compose_file"
    exit 1
  fi

  docker compose --env-file "$base_interpolation_env_file" -f "$compose_file" "${action[@]}"
fi

#(
#  set -a
#  source "$base_interpolation_env_file"
#  source "$override_env_file"
#  set +a
#  docker compose -f "$compose_file" -f "$override_file" "${action[@]}"
#)

