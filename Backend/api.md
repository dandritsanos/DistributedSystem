API Overview

Request:
{
  "user-stats": "<username>"
}

Response:
{
  "dist_user": <distance>, 
  "ele_user": <elevation>, 
  "sec_user": <seconds>,
  "dist_avg": <distance>, 
  "ele_avg": <elevation>, 
  "sec_avg": <seconds>,
}

Request:
{
  "user": "<username>",
  "<filename>": "<waypoints>",
}

Response: {
  "dist": <distance>, 
  "ele": <elevation>, 
  "speed": <speed>, 
  "sec": <seconds>,
},


