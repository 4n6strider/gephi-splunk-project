import sys
import re

f = open("templates/edges.txt", "r")
contents_edges = f.readlines()
f.close

g = open("templates/nodes.txt", "r")
contents_nodes = g.readlines()
g.close()

label_dict = {}
gml = "graph\n[\n"
pattern = re.compile("^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$")
icons = {"IP": "P", "User": "B"}

for i, line in enumerate(contents_nodes):
    line = line.split(",")
    gml += "  node\n  [\n"
    gml += "    id " + str(i) + "\n"
    label_dict[line[0]] = str(i)
    if pattern.match(line[0]):
    	gml += "    label \"" + icons["IP"] + line[0] + "\"\n"
    else:
        gml += "    label \"" + icons["User"]  + line[0] + "\"\n"
    gml += "  ]\n"

for line in contents_edges:
    line = line.split(",")
    gml += "  edge\n  [\n"
    gml += "    source " + label_dict[line[0]] + "\n"
    gml += "    target " + label_dict[line[1]] + "\n"
    gml += "    value " + line[2] + "\n"
    gml += "  ]\n"

gml+="]"

f = open("templates/" + sys.argv[1] + ".gml", "w")
f.write(gml)
f.close()

print gml

