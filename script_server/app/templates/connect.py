# import sys
# from time import sleep
import splunklib.client as client
import splunklib.results as results

HOST = "localhost"
PORT = 8089
USERNAME = "CHANGEME" #CHANGE
PASSWORD = "CHANGEME" #CHANGE

service = client.connect(
    host=HOST,
    port=PORT,
    username=USERNAME,
    password=PASSWORD)

kwargs_oneshot = {"earliest_time": "",
                  "latest_time": "now",
                  "output_mode": "csv"}
searchquery_oneshot = 'search *' #replace search
oneshotsearch_results = service.jobs.oneshot(searchquery_oneshot, **kwargs_oneshot)

print oneshotsearch_results








