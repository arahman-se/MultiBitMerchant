# Login as Alice using (preemptive) Basic authentication
curl -v --cookie cookies.txt --cookie-jar cookies.txt --user alice:alice1 -H "Accept: application/json" "http://localhost:8080/mbm/api/v1/item/1"