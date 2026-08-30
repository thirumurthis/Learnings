#!/usr/bin/env bash

#set -euo pipefail

DNS_NAME=${1:-"s3.swfs.com"}
HTTPS_PORT=${2:-443}

CERT_NAME="cert.pem"

usage() {
    echo "Usage: $0 [OPTIONS]"
    echo "Options:"
    echo "  --dns <dns-name>  Specify the DNS server (Default: ${DNS_NAME})"
    echo "  --port <port>     Specify the connection port (Default: ${HTTPS_PORT})"
    echo "  -h, --help        Display this help message"
    exit 1
}

# 3. Parse arguments using a while loop and case statement
while [[ $# -gt 0 ]]; do
    case "$1" in
        --dns)
            if [[ -z "${2:-}" ]]; then
                echo "Error: --dns requires a value." >&2
                usage
            fi
            DNS_NAME="$2"
            shift 2
            ;;
        --port)
            if [[ -z "${2:-}" ]]; then
                echo "Error: --port requires a value." >&2
                usage
            fi
            HTTPS_PORT="$2"
            shift 2
            ;;
        -h|--help)
            usage
            ;;
        *)
            echo "Error: Unknown argument: $1" >&2
            usage
            ;;
    esac
done

if ( ! [[ "$HTTPS_PORT" =~ ^[0-9]+$ ]] || [ "$HTTPS_PORT" -le 0 ] || [ "$HTTPS_PORT" -gt 65535 ] ) ; then
    echo "Error: Port must be a valid number between 1 and 65535. Given: $HTTPS_PORT" >&2
    exit 1
fi

# 5. Core execution script logic goes here
echo "--- Configuration ---"
echo "DNS Server : ${DNS_NAME}"
echo "Port       : ${HTTPS_PORT}"
echo "---------------------"

openssl s_client -connect ${DNS_NAME}:${HTTPS_PORT} -showcerts </dev/null 2>/dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > "${CERT_NAME}"
openssl x509 -in ${CERT_NAME} -noout -text | egrep "Not|Issuer|Subject|DNS"
