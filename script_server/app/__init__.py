# coding: utf-8

from flask import Flask
import requests
import subprocess
import re
import os
from flask import render_template, url_for, request, redirect, Response
from functools import wraps
from werkzeug.utils import secure_filename

UPLOAD_FOLDER = 'templates'
ALLOWED_EXTENSIONS = set(['php', 'sh', 'py', 'rb'])


app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

def check_auth(username, password):
    return username == "admin" and password == "secret"

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

def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1] in ALLOWED_EXTENSIONS

@app.route("/graph/<name>/nodes")
def return_graph_nodes(name):
    return render_template("nodes.txt")

@app.route("/build_script", methods=['POST'])
@requires_auth
def build_script():
    args = {}
    res = dict(request.form)

    search = res["search"][0]
    name = res["name"][0]
    username = res["username"][0] 
    password = res["password"][0]

    for i, arg in enumerate(res):
        if arg != "search" and arg != "name" and arg != "username" and arg != "password":
            if res[arg] != "":
                args[str(i)] = res[arg]
    script_args = ["python", "templates/build_script.py", search, name, username, password]
 
    for elem in args:
        if elem != "Enter":
            script_args.append(args[elem][0])

    subprocess.call(script_args)

    return render_template("index.html")


@app.route("/")
def index():
    return render_template("index.html")

@app.route("/scripts/<scriptname>/<scripttype>/<action>/<node>")
def ret_action(scriptname, scripttype, action, node):
    if action == "ipinf":
        subprocess.call(["dos2unix", ("templates/" + scriptname + "." + scripttype)])
        output = subprocess.check_output(["python", "templates/" + scriptname + "." + scripttype, node])
	
        count = 0
        f = open("templates/nodes.txt", "w")
        g = open("templates/edges.txt", "w")	

        print output.split(os.linesep)

        for line in output.split(os.linesep):
            if count < 2:
                count+=1
            elif line != "":
                line = line.replace("\"", "")
                line  = line.split(",")
                src_ip = line[0]
                times = line[1]
                users = line[2]
                ua = line[3]
                session_ids= line[4]

                users = list(users.split(" "))
                times = list(times.split(" "))
                session_ids=list(session_ids.split(" "))

                ua = list(ua.split("$"))

                user_dict = {}
                session_dict = {}
                ua_dict = {}

                ua_fixed = []
                for elem in ua:
                    if elem != "":
                        if elem[0] == " ":
                            elem = elem[1:]
                        if elem not in ua_dict:
                            ua_fixed.append(elem)
                            ua_dict[elem] = "True"


                for i,user in enumerate(users):
                    if i < len(session_ids):
                        session_id = session_ids[i]

                    if user not in user_dict:
                        g.write(user + "|" + times[i] + "|" + "username█\n")
                        user_dict[user] = "True"

                    if session_id not in session_dict:
                        g.write(session_id + "|" + times[i] + "|" + "session_id█\n")
                        session_dict[session_id] = "True"

                    if i < len(ua_fixed):
                        g.write(ua_fixed[i] + "|" + times[i] + "|" + "useragent█\n")
    	
        g.close()
        f.close()
    	
        g = open("templates/edges.txt", "r")
        contents = g.readlines()
        g.close()
    	
        return "".join(contents)

    if action == "adduseragent":
        subprocess.call(["dos2unix", ("templates/" + scriptname + "." + scripttype)])
        output = subprocess.check_output(["python", "templates/" + scriptname + "." + scripttype, node])

        f = open("addresult.txt", "w")
        count = 0
        for line in output.split(os.linesep):
            line = line.replace("\"", "")
            if count <2:
                count+=1
            else:
                if line == '':
                    break
                line = line.split(",")
                f.write(line[0]+"\n")
        f.close()

        f = open("addresult.txt", "r")
        contents = f.readlines()
        f.close()

        return ''.join(contents)

    if action == "iplocation":
        subprocess.call(["dos2unix", "templates/" + scriptname + "." + scripttype])
        output = subprocess.check_output(["python", "templates/" + scriptname + "." + scripttype, node])
        res = ""
        count = 0
        for line in output.split(os.linesep):
            included = []
            if count < 2:
                count+=1
            else:
                if line == "":
                    break
                line = line.split(",")

                if line[0] != "":
                    included.append(line[0].replace('\"', ""))
                if line[1] != "":
                    included.append(line[1].replace('\"', ""))
                if line[2] != "":
                    included.append(line[2].replace('\"', ""))
                for elem in included:
                    res += elem + ":"

        return res[:len(res)-1]

@app.route("/scripts/<scriptname>/<scripttype>")
def ret_script(scriptname, scripttype):
    #return render_template(scriptname + "." + scripttype)
    ext = scripttype.lower()
    scriptname = scriptname.lower()

    if ext == "py":
        cl_arg = "python"
    elif ext == "php":
        cl_arg = "php"
    elif ext == "bash":
        cl_arg = "bash"
    elif ext == "rb":
        cl_arg = "ruby"
    elif ext == "sh":
        cl_arg = "sh"
    else:
        return "Script not supported"

    subprocess.call(["dos2unix", "templates/" + scriptname + "." + scripttype])
    output = subprocess.check_output([cl_arg, "templates/" + scriptname + "." + scripttype])

    f = open("templates/nodes.txt", "w")
    g = open("templates/edges.txt", "w")
    w = open("templates/test.txt", "w")

    count = 0
    for line in output.split(os.linesep):

        line = line.replace("\"", "") 
        if count <2:
            count+=1
            continue
        else:
            if line == '':
                break
            line = line.split(",")
        ips = line[0].split(" ")
        if len(ips) > 1:
            users = line[1].split(" ")

            times = line[2].split(" ")

            for i, elem in enumerate(ips):
                f.write(elem+ "," + times[i] + "," + "1" + "\n")
                f.write(users[i] + "," + times[i] + "," + "1" + "\n")
                g.write(users[i] + "," + elem + "," + "wow" + "\n") 
        else:
            times = line[2].split(" ")
            f.write(line[0].replace("\"", "") + "," + times[0]  + "," + line[3].replace("\"", "") + " \n")
            sources = line[1].split(" ")
            
            seen = {}
            for i, elem in enumerate(sources):
                if elem not in seen:
                    g.write(elem + "," + line[0].replace("\"", "")+ "," + line[2].replace("\"", "") + " \n")
                    f.write(elem + "," + times[i].replace("\"", "") +  ",1" + " \n")
                    seen[elem] = "True"

    f.close()
    g.close()

    output = ""
    f = open("templates/nodes.txt", "r")
    g = open("templates/edges.txt", "r")
    contents_f = f.readlines()
    contents_g = g.readlines()
    output = "NODES:\n" + ''.join(contents_f) + "EDGES:\n" + ''.join(contents_g)

    output = subprocess.check_output(["python", "to_xml.py", scriptname])

    return output

@app.route("/add_script", methods=['GET', 'POST'])
@requires_auth
def add_script():
    if request.method == 'POST':
        if 'file' not in request.files:
            flash('No file part')
            return redirect(request.url)
        file = request.files['file']
        if file.filename == '':
            flash('No selected file')
            return redirect(request.url)
        if file and allowed_file(file.filename):
            filename = secure_filename(file.filename)
            file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
            return redirect(url_for('index'))

    return render_template("add_script.html")

if __name__ == "__main__":
    app.run(port=4999)
