import splunklib.client as client
import splunklib.results as results

HOST = "localhost"
PORT = 8089
USERNAME = "admin"
PASSWORD = "changeme"

service = client.connect(
    host=HOST,
    port=PORT,
    username=USERNAME,
    password=PASSWORD)

kwargs_oneshot = {"earliest_time": "",
                  "latest_time": "now",
                  "output_mode": "csv"}
searchquery_oneshot = 'search sourcetype=bank | dedup txtUsername | chart list(txtUsername) by src_ip | rename list(txtUsername) as user | eval count=mvcount(user) | search count>4'

oneshotsearch_results = service.jobs.oneshot(searchquery_oneshot, **kwargs_oneshot)

print oneshotsearch_results
