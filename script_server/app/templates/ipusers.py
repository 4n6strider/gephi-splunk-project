
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

searchquery_oneshot = 'search sourcetype=bank source="Archive.zip:./stream_weblogs_20141127_031752_4968_1.log"| dedup txtUsername | chart list(_time), list(txtUsername) by src_ip | rename list(_time) as time | rename list(txtUsername) as user | eval count=mvcount(user)| search count=1 user!="ret user nma"| table src_ip | head 1000 | mvcombine src_ip | appendcols [search sourcetype=bank source="Archive.zip:./stream_weblogs_20141127_031752_4968_1.log" | dedup txtUsername | chart list(_time), list(txtUsername) by src_ip | rename list(_time) as time | rename list(txtUsername) as user | eval count=mvcount(user)| search count=1 user!="ret user nma"| table user | head 1000 | mvcombine user]|appendcols [search sourcetype=bank source="Archive.zip:./stream_weblogs_20141127_031752_4968_1.log" | dedup txtUsername | chart list(_time), list(txtUsername) by src_ip | rename list(_time) as time | rename list(txtUsername) as user | eval count=mvcount(user)| search count=1 OR count=2 OR count=3 user!="ret user nma"| table time | head 1000 | mvcombine time]'

oneshotsearch_results = service.jobs.oneshot(searchquery_oneshot, **kwargs_oneshot)

print oneshotsearch_results
