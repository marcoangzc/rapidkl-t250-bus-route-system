#!/bin/bash
# Creates the GitHub repo and pushes the project. Token stays hidden.
set -e
cd "$(dirname "$0")"

REPO="rapidkl-t250-bus-route-system"

echo "== Step 1: read token from credential manager =="
TOKEN=$(printf "protocol=https\nhost=github.com\n" | GCM_INTERACTIVE=Never git credential fill 2>/dev/null | grep "^password=" | cut -d= -f2-)
if [ -z "$TOKEN" ]; then
  echo "NO_TOKEN: no github.com credential stored"
  exit 1
fi
echo "token found (hidden)"

echo "== Step 2: create repo via GitHub API =="
HTTP=$(curl -s -o /tmp/ghresp.json -w "%{http_code}" -X POST \
  -H "Authorization: token $TOKEN" \
  -H "Accept: application/vnd.github+json" \
  https://api.github.com/user/repos \
  -d "{\"name\":\"$REPO\",\"description\":\"Rapid KL Bus Route System (Route T250) - AMCS2034 Data Structures assignment: JavaFX graph with DFS/BFS traversal\",\"private\":false,\"has_wiki\":false}")
echo "HTTP $HTTP"
grep -o '"full_name": *"[^"]*"' /tmp/ghresp.json | head -1
grep -o '"message": *"[^"]*"' /tmp/ghresp.json | head -1

if [ "$HTTP" != "201" ]; then
  echo "Repo creation did not return 201 - aborting push."
  exit 1
fi

echo "== Step 3: add remote and push =="
git remote remove origin 2>/dev/null || true
git remote add origin "https://github.com/marcoangzc/$REPO.git"
git push -u origin main
echo "ALL_DONE"
