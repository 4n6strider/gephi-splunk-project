from flask import Flask
import requests
import subprocess
import re
import os
import ast
import random
import time
from flask import render_template
from functools import wraps
from flask import request, Response

app = Flask(__name__)

@app.route("/")
def index():
    return render_template("index.html")

@app.route("/script")
def script():
    return render_template("script.html")

def check_auth(username, password):
    return username == 'admin' and password == 'secret'

def authenticate():
    return Response(
    'Could not verify your access level for that URL.\n'
    'You have to login with proper credentials', 401,
    {'WWW-Authenticate': 'Basic realm="Login Required"'})

def requires_auth(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        auth = request.authorization
        if not auth or not check_auth(auth.username, auth.password):
            return authenticate()
        return f(*args, **kwargs)
    return decorated

@app.route("/permissions")
@requires_auth
def see_permissions():
    onlyfiles = [f for f in os.listdir("templates") if os.path.isfile(os.path.join("templates", f))]
    permission_files = []
    file_contents = {}
    for f in onlyfiles:
        f = f.split("_")
        if f[0] == "permissions":
            group = f[1].split(".")[0]
            f = open("templates/" + f[0] + "_" + f[1], "r")
            contents = f.readlines()
            f.close()
            if contents:
                contents = contents[0].split(",")
            file_contents[group] = contents
            permission_files.append(group)
    return render_template("permissions.html", group=permission_files, contents=file_contents)

@app.route("/permissions/<group>")
def return_permissions(group):
    return render_template("permissions_" + group + ".txt")

@app.route("/graph/<name>")
def return_gml(name):
    return render_template(name + ".gml")

@app.route("/graph/<name>/nodes")
def return_graph_nodes(name):
    return requests.get("http://127.0.0.1:4999/graph/" + name + "/nodes").content

@app.route("/graph/<name>/edges")
def return_graph_edges(name):
    return render_template("edges.txt")

@app.route("/demo/<address>")
def return_demo_static(address):
    time.sleep(0.2)
    if address == "24.13.69.33":
        return render_template("ip1.txt")
    elif address == "76.109.69.126":
        return render_template("ip2.txt")
    elif address == "67.221.153.34":
        return render_template("ip3.txt")
    elif address == "64.41.165.181":
        return render_template("ip4.txt")
    elif address == "216.34.175.61":
        return render_template("ip5.txt")
    elif address == "68.193.181.125":
        return render_template("ip6.txt")

@app.route("/graph/<name>/<node>/<action>")
def specific_node_info(name, node, action):
    if action == "adduseragent":
        res = requests.get('http://127.0.0.1:4999/scripts/useragent/py/' + action + "/" + node).content
        return res

    if action == "iplocation":
        res = requests.get('http://127.0.0.1:4999/scripts/iplocation/py/' + action + "/" + node).content
        return res

    if action == "ipinf":
        res = requests.get('http://127.0.0.1:4999/scripts/ipinf/py/' + action + "/" + node).content
        return res

    # if action == "":
    #     res = requests.get('http://gesman-centos7x64-001/gephi/scripts/ipinf/py/' + action + "/" + node).content
    #     return res


@app.route("/admin")
@requires_auth
def administration():
    return render_template("admin.html")

@app.route("/admin/remove", methods=['POST'])
@requires_auth
def remove():
    group = dict(request.form)["group"]
    script = dict(request.form)["script"]

    f = open("templates/permissions_" + group[0] + ".txt", "r")
    contents = f.readlines()
    f.close()

    to_del = -1
    for i, line in enumerate(contents):
        line = line.split()
        if line[0] == script[0]:
            to_del = i 
    if to_del == -1:
        return render_template("admin.html")

    contents = contents[:to_del] + contents[to_del+1:]

    f = open("templates/permissions_" + group[0] + ".txt", "w")
    f.write(''.join(contents))
    f.close()

    return render_template("admin.html")

@app.route("/admin/add", methods=['POST'])
@requires_auth
def add():
    group = dict(request.form)["group"]
    script = dict(request.form)["script"]

    f = open("templates/permissions_" + group[0] + ".txt", "r")
    contents = f.readlines()
    f.close()


    f = open("templates/permissions_" + group[0] + ".txt", "a")
    if len(contents) > 0:
        f.write("," + script[0])
    else:
        f.write(script[0])
    f.close()

    return render_template("admin.html")

@app.route("/admin/addgroup", methods=['POST'])
@requires_auth
def add_group():
    group = dict(request.form)["text"]

    f = open("templates/permissions_" + group[0] + ".txt", "w")
    f.close()

    return render_template("admin.html")

@app.route("/admin/removegroup", methods=['POST'])
@requires_auth
def remove_group():
    group = dict(request.form)["text"]

    os.remove("templates/permissions_" + group[0] + ".txt")

    return render_template("admin.html")    

@app.route("/script/<scriptname>/<ext>")
def run_script(scriptname, ext):
    res = requests.get('http://127.0.0.1:4999/scripts/' + scriptname + "/" + ext).content
    return res

if __name__ == "__main__":
    app.run()
