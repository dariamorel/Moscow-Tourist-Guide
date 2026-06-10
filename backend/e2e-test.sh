#!/usr/bin/env bash

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8000}"

require() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "Required command not found: $1"
    exit 1
  }
}

require curl
require jq

echo "======================================"
echo "Tour Guide API Full Flow Test"
echo "BASE_URL=$BASE_URL"
echo "======================================"

LOGIN="testuser_$(date +%s)"
PASSWORD="StrongPass123"

echo
echo "[1] Registration"

REGISTER_RESPONSE=$(
curl -s \
  -X POST \
  "$BASE_URL/users/registration" \
  -H "Content-Type: application/json" \
  -d "{
    \"login\":\"$LOGIN\",
    \"password\":\"$PASSWORD\"
  }"
)

echo "$REGISTER_RESPONSE" | jq .

USER_ID=$(echo "$REGISTER_RESPONSE" | jq -r '.id')

if [[ -z "$USER_ID" || "$USER_ID" == "null" ]]; then
  echo "Registration failed"
  exit 1
fi

echo
echo "[2] Login"

LOGIN_RESPONSE=$(
curl -s \
  -X POST \
  "$BASE_URL/users/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"login\":\"$LOGIN\",
    \"password\":\"$PASSWORD\"
  }"
)

echo "$LOGIN_RESPONSE" | jq .

TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.token')

if [[ -z "$TOKEN" || "$TOKEN" == "null" ]]; then
  echo "Login failed"
  exit 1
fi

AUTH_HEADER="Authorization: Bearer $TOKEN"

echo
echo "[3] Get sights"

SIGHTS_RESPONSE=$(
curl -s \
  "$BASE_URL/sights?limit=5&offset=0"
)

echo "$SIGHTS_RESPONSE" | jq .

SIGHT_COUNT=$(echo "$SIGHTS_RESPONSE" | jq length)

if [[ "$SIGHT_COUNT" -lt 5 ]]; then
  echo "Need at least 5 sights in DB"
  exit 1
fi

S1=$(echo "$SIGHTS_RESPONSE" | jq '.[0].id')
S2=$(echo "$SIGHTS_RESPONSE" | jq '.[1].id')
S3=$(echo "$SIGHTS_RESPONSE" | jq '.[2].id')
S4=$(echo "$SIGHTS_RESPONSE" | jq '.[3].id')
S5=$(echo "$SIGHTS_RESPONSE" | jq '.[4].id')

echo
echo "[4] Get sight by id"

curl -s \
  "$BASE_URL/sights/$S1" | jq .

echo
echo "[5] Optimize route (5 sights)"

OPTIMIZE_RESPONSE=$(
curl -s \
  -X POST \
  "$BASE_URL/routes/optimize" \
  -H "Content-Type: application/json" \
  -d "{
    \"sight_ids\": [$S1,$S2,$S3,$S4,$S5]
  }"
)

echo "$OPTIMIZE_RESPONSE" | jq .

echo
echo "[6] Create route"

CREATE_ROUTE_RESPONSE=$(
curl -s \
  -X POST \
  "$BASE_URL/routes" \
  -H "$AUTH_HEADER" \
  -H "Content-Type: application/json" \
  -d "{
    \"user_id\": $USER_ID,
    \"planned_order\": [$S1,$S2,$S3,$S4,$S5]
  }"
)

echo "$CREATE_ROUTE_RESPONSE" | jq .

ROUTE_ID=$(echo "$CREATE_ROUTE_RESPONSE" | jq -r '.id')

if [[ -z "$ROUTE_ID" || "$ROUTE_ID" == "null" ]]; then
  echo "Route creation failed"
  exit 1
fi

echo
echo "[7] Verify created route"

ROUTE_RESPONSE=$(
curl -s \
  -H "$AUTH_HEADER" \
  "$BASE_URL/routes/$ROUTE_ID"
)

echo "$ROUTE_RESPONSE" | jq .

ROUTE_STATUS=$(echo "$ROUTE_RESPONSE" | jq -r '.status')

if [[ "$ROUTE_STATUS" != "planned" ]]; then
  echo "Expected route status=planned, got=$ROUTE_STATUS"
  exit 1
fi

echo
echo "[8] Move route to in_progress"

curl -s \
  -X PUT \
  "$BASE_URL/routes/$ROUTE_ID" \
  -H "$AUTH_HEADER" \
  -H "Content-Type: application/json" \
  -d "{
    \"order\": [$S1],
    \"status\": \"in_progress\"
  }" | jq .

echo
echo "[9] Verify route is in_progress"

ROUTE_RESPONSE=$(
curl -s \
  -H "$AUTH_HEADER" \
  "$BASE_URL/routes/$ROUTE_ID"
)

echo "$ROUTE_RESPONSE" | jq .

ROUTE_STATUS=$(echo "$ROUTE_RESPONSE" | jq -r '.status')

if [[ "$ROUTE_STATUS" != "in_progress" ]]; then
  echo "Expected route status=in_progress, got=$ROUTE_STATUS"
  exit 1
fi

echo
echo "[10] Get user routes"

curl -s \
  -H "$AUTH_HEADER" \
  "$BASE_URL/routes?user_id=$USER_ID" | jq .

echo
echo "[11] Get in_progress routes"

curl -s \
  -H "$AUTH_HEADER" \
  "$BASE_URL/routes/in_progress?user_id=$USER_ID" | jq .

echo
echo "[12] Complete route after visiting all 5 sights"

curl -s \
  -X PUT \
  "$BASE_URL/routes/$ROUTE_ID" \
  -H "$AUTH_HEADER" \
  -H "Content-Type: application/json" \
  -d "{
    \"order\": [$S1,$S2,$S3,$S4,$S5],
    \"status\": \"completed\",
    \"total_time\": \"2h 15m\"
  }" | jq .

echo
echo "[13] Verify route is completed"

ROUTE_RESPONSE=$(
curl -s \
  -H "$AUTH_HEADER" \
  "$BASE_URL/routes/$ROUTE_ID"
)

echo "$ROUTE_RESPONSE" | jq .

ROUTE_STATUS=$(echo "$ROUTE_RESPONSE" | jq -r '.status')

if [[ "$ROUTE_STATUS" != "completed" ]]; then
  echo "Expected route status=completed, got=$ROUTE_STATUS"
  exit 1
fi

echo
echo "[14] Get completed routes"

curl -s \
  -H "$AUTH_HEADER" \
  "$BASE_URL/routes/passed?user_id=$USER_ID" | jq .

echo
echo "[15] Get achievements"

curl -s \
  "$BASE_URL/achievements?user_id=$USER_ID" | jq .

echo
echo "[16] Verify achievement unlocked after visiting 5 sights"

ACHIEVEMENTS_RESPONSE=$(
curl -s \
  -H "$AUTH_HEADER" \
  "$BASE_URL/achievements/completed?user_id=$USER_ID"
)

echo "$ACHIEVEMENTS_RESPONSE" | jq .

VISITED_ACHIEVEMENT_COUNT=$(
echo "$ACHIEVEMENTS_RESPONSE" | jq '
  map(
    select(
      .type == "sights_visited"
      and (
        (.required_count // 0) <= 5
      )
    )
  ) | length
'
)

if [[ "$VISITED_ACHIEVEMENT_COUNT" -eq 0 ]]; then
  echo "Achievement for visiting 5 sights was not unlocked"
  exit 1
fi

echo "Achievement unlocked successfully"

echo
echo "[17] Delete route"

HTTP_CODE=$(
curl -s \
  -o /dev/null \
  -w "%{http_code}" \
  -X DELETE \
  "$BASE_URL/routes/$ROUTE_ID" \
  -H "$AUTH_HEADER"
)

if [[ "$HTTP_CODE" != "204" ]]; then
  echo "Delete failed. HTTP=$HTTP_CODE"
  exit 1
fi

echo "Route deleted"

echo
echo "[18] Create second route for negative scenario"

CREATE_DECLINED_ROUTE_RESPONSE=$(
curl -s \
  -X POST \
  "$BASE_URL/routes" \
  -H "$AUTH_HEADER" \
  -H "Content-Type: application/json" \
  -d "{
    \"user_id\": $USER_ID,
    \"planned_order\": [$S1,$S2,$S3,$S4,$S5]
  }"
)

echo "$CREATE_DECLINED_ROUTE_RESPONSE" | jq .

DECLINED_ROUTE_ID=$(echo "$CREATE_DECLINED_ROUTE_RESPONSE" | jq -r '.id')

if [[ -z "$DECLINED_ROUTE_ID" || "$DECLINED_ROUTE_ID" == "null" ]]; then
  echo "Failed to create route for negative scenario"
  exit 1
fi

echo
echo "[19] Decline route"

curl -s \
  -X PUT \
  "$BASE_URL/routes/$DECLINED_ROUTE_ID" \
  -H "$AUTH_HEADER" \
  -H "Content-Type: application/json" \
  -d '{
    "status":"declined"
  }' | jq .

echo
echo "[20] Verify route is declined"

DECLINED_STATUS=$(
curl -s \
  -H "$AUTH_HEADER" \
  "$BASE_URL/routes/$DECLINED_ROUTE_ID" \
  | jq -r '.status'
)

if [[ "$DECLINED_STATUS" != "declined" ]]; then
  echo "Expected declined status, got $DECLINED_STATUS"
  exit 1
fi

echo
echo "[21] Verify route appears in declined list"

DECLINED_ROUTES=$(
curl -s \
  -H "$AUTH_HEADER" \
  "$BASE_URL/routes/declined?user_id=$USER_ID"
)

echo "$DECLINED_ROUTES" | jq .

DECLINED_FOUND=$(
echo "$DECLINED_ROUTES" | jq \
  --argjson routeId "$DECLINED_ROUTE_ID" \
  'map(select(.id == $routeId)) | length'
)

if [[ "$DECLINED_FOUND" -eq 0 ]]; then
  echo "Declined route not found in declined list"
  exit 1
fi

echo
echo "[22] Verify no additional sights_visited achievement was granted"

ACHIEVEMENTS_AFTER_DECLINE=$(
curl -s \
  -H "$AUTH_HEADER" \
  "$BASE_URL/achievements/completed?user_id=$USER_ID"
)

echo "$ACHIEVEMENTS_AFTER_DECLINE" | jq .

ACH_COUNT=$(echo "$ACHIEVEMENTS_AFTER_DECLINE" | jq 'length')

if [[ "$ACH_COUNT" -ne 0 ]]; then
  echo "Expected 0 achievements after deleting completed route and declining new route"
  exit 1
fi

echo "No achievements present as expected"

echo
echo "[23] Delete declined route"

HTTP_CODE=$(
curl -s \
  -o /dev/null \
  -w "%{http_code}" \
  -X DELETE \
  "$BASE_URL/routes/$DECLINED_ROUTE_ID" \
  -H "$AUTH_HEADER"
)

if [[ "$HTTP_CODE" != "204" ]]; then
  echo "Failed to delete declined route"
  exit 1
fi

echo "Declined route deleted"

echo
echo "======================================"
echo "FULL FLOW PASSED"
echo "======================================"