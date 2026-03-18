#!/bin/bash
# Requires: jq (sudo apt install jq / brew install jq)

BASE_URL="http://localhost:8090"
JSON_FILE="$(dirname "$0")/test-api.json"
PASS=0; FAIL=0; TOTAL=0

echo "============================================================"
echo "  Running API Tests from: $JSON_FILE"
echo "============================================================"

COUNT=$(jq length "$JSON_FILE")

for i in $(seq 0 $((COUNT - 1))); do
  NAME=$(jq -r ".[$i].name" "$JSON_FILE")
  METHOD=$(jq -r ".[$i].method" "$JSON_FILE")
  PATH_URL=$(jq -r ".[$i].path" "$JSON_FILE")
  BODY=$(jq -c ".[$i].body" "$JSON_FILE")
  EXPECT=$(jq -r ".[$i].expect" "$JSON_FILE")
  TOTAL=$((TOTAL + 1))

  if [ "$BODY" = "null" ]; then
    HTTP_CODE=$(curl -s -o /tmp/api_response.json -w "%{http_code}" \
      -X "$METHOD" "${BASE_URL}${PATH_URL}")
  else
    HTTP_CODE=$(curl -s -o /tmp/api_response.json -w "%{http_code}" \
      -X "$METHOD" "${BASE_URL}${PATH_URL}" \
      -H "Content-Type: application/json" \
      -d "$BODY")
  fi

  RESPONSE=$(cat /tmp/api_response.json)

  if [ "$HTTP_CODE" = "$EXPECT" ]; then
    echo "[PASS] #$TOTAL $METHOD $PATH_URL - $NAME (HTTP $HTTP_CODE)"
    PASS=$((PASS + 1))
  else
    echo "[FAIL] #$TOTAL $METHOD $PATH_URL - $NAME (HTTP $HTTP_CODE, expected $EXPECT)"
    FAIL=$((FAIL + 1))
  fi
  echo "       Response: $RESPONSE"
  echo ""
done

echo "============================================================"
echo "  Results: $PASS passed, $FAIL failed, $TOTAL total"
echo "============================================================"