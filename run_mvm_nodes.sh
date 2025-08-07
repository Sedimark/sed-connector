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

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

global_env_file="$SCRIPT_DIR/deployment/assets/env/global.env"
compose_file="$SCRIPT_DIR/deployment/docker-compose.mvm.yml"

if [ -n "$node" ]; then
  override_env_file="$SCRIPT_DIR/deployment/overrides/${node}.env"
  override_file="$SCRIPT_DIR/deployment/overrides/${node}.override.yml"

  missing_files=()
  [ ! -f "$global_env_file" ] && missing_files+=("$global_env_file")
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

  docker network inspect sed_public_shared_network >/dev/null 2>&1 || docker network create --subnet=192.168.23.0/24 sed_public_shared_network
  docker compose --env-file "$global_env_file" --env-file "$override_env_file" -f "$compose_file" -f "$override_file" "${action[@]}"
else
  if [ ! -f "$global_env_file" ] || [ ! -f "$compose_file" ]; then
    echo "Error: Missing required files:"
    [ ! -f "$global_env_file" ] && echo "  - $global_env_file"
    [ ! -f "$compose_file" ] && echo "  - $compose_file"
    exit 1
  fi

  docker compose --env-file "$global_env_file" -f "$compose_file" "${action[@]}"
fi

#(
#  set -a
#  source "$global_env_file"
#  source "$override_env_file"
#  set +a
#  docker compose -f "$compose_file" -f "$override_file" "${action[@]}"
#)

