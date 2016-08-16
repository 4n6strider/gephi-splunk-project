import sys
# from time import sleep
import splunklib.client as client
import splunklib.results as results

HOST = "localhost"
PORT = 8089
USERNAME = "CHANGEME" #CHANGEME
PASSWORD = "CHANGEME" #CHANGEME

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
