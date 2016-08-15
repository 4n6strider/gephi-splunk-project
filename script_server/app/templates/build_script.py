import sys
import re

search = sys.argv[1]
name = sys.argv[2]
username = sys.argv[3]
password = sys.argv[4]
split_search = search.split("$$")

for i, elem in enumerate(split_search):
    if elem == "ARG1":
        split_search[i] = sys.argv[5]
    if elem == "ARG2":
        split_search[i] = sys.argv[6]
    if elem == "ARG3":
        split_search[i] = sys.argv[7]

search = ''.join(split_search)

f = open("templates/connect.py", "r")
contents = f.readlines()
f.close()


for i, line in enumerate(contents):
    line = line.split()
    if line:
        if line[0] == "USERNAME":
            contents[i] = "USERNAME = \"" + username +"\"\n"
        if line[0] == "PASSWORD":
            contents[i] = "PASSWORD = \"" + password +"\"\n"
        if line[0] == "searchquery_oneshot":
            contents[i] = "searchquery_oneshot = \"" + search +"\"\n"

f = open("templates/" + name + ".py", "w")
f.write(''.join(contents))
f.close()
