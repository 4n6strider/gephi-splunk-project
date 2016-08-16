import sys
# from time import sleep
import splunklib.client as client
import splunklib.results as results

HOST = "localhost"
PORT = 8089
<<<<<<< HEAD
USERNAME = "admin"
PASSWORD = "password"
=======
USERNAME = "CHANGEME" #CHANGEME
PASSWORD = "CHANGEME" #CHANGEME
>>>>>>> 6b2e6ecd9b823650de192b4574147689c4ce61ce

service = client.connect(
    host=HOST,
    port=PORT,
    username=USERNAME,
    password=PASSWORD)

kwargs_oneshot = {"earliest_time": "",
                  "latest_time": "now",
                  "output_mode": "csv"}

searchquery_oneshot = 'search sourcetype="sample" src_ip=' + sys.argv[1] + ' | table src_ip,timestamp,user,ua,session_id,count,notes'

oneshotsearch_results = service.jobs.oneshot(searchquery_oneshot, **kwargs_oneshot)

print oneshotsearch_results
