import sys
import re

f = open("templates/edges.txt", "r")
contents_edges = f.readlines()
f.close

g = open("templates/nodes.txt", "r")
contents_nodes = g.readlines()
g.close()

xml = '<?xml version="1.0" encoding="UTF-8"?>\n' + '<gexf xmlns="http://www.gexf.net/1.3" version="1.3" xmlns:viz="http://www.gexf.net/1.3/viz" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.gexf.net/1.3 http://www.gexf.net/1.3/gexf.xsd">\n' + '\t<meta lastmodifieddate="2016-07-21">\n' + '\t\t<creator>Gephi 0.9</creator>\n' +'\t\t<description></description>\n' +'\t</meta>\n' +'\t<graph defaultedgetype="directed" timeformat="double" timerepresentation="timestamp" mode="dynamic">\n' +'\t\t<attributes class="node">\n' +'\t\t\t<attribute id="nodetype" title="nodetype" type="string"></attribute>\n' + '\t\t\t<default>true</default>\n' + '\t\t\t<attribute id="Notes" title="Notes" type="string"></attribute>\n' + '\t\t\t<default>true</default>\n' + '\t\t</attributes>\n' +'\t\t<nodes>\n'
 #3 tabs start
icondict = {"User": "username", "IP": "ipaddress", "UserAgent": "useragent", "SessionID": "session_id"}
pattern = re.compile("^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$")

for node in contents_nodes:
    node = node.split(",")
    if pattern.match(node[0]):
        xml += '\t\t\t<node id=' + "\"" + node[0] + "\"" + ' label=' + "\"" + node[0] + "\"" + '>\n'
    else:
        xml += '\t\t\t<node id=' + "\"" + node[0] + "\"" + ' label=' + "\"" + node[0] + "\"" + '>\n'
    xml += '\t\t\t\t<spells>\n'
    xml += '\t\t\t\t\t<spell timestamp=' + "\"" + node[1] + "\"></spell>\n"
    xml += '\t\t\t\t</spells>\n'
    xml += '\t\t\t\t<attvalues>\n'
    if pattern.match(node[0]):
        xml += '\t\t\t\t\t<attvalue for=\"nodetype\" value=' + "\"" + icondict["IP"] + "\"" + "></attvalue>\n"
    else:
        ### BAD BAD BAD BAD ###
        if len(node[0]) == 28:
            xml += '\t\t\t\t\t<attvalue for=\"nodetype\" value=' + "\"" + icondict["SessionID"] + "\"" + "></attvalue>\n"
        else:
            xml += '\t\t\t\t\t<attvalue for=\"nodetype\" value=' + "\"" + icondict["User"] + "\"" + "></attvalue>\n"
    xml += '\t\t\t\t</attvalues>\n'
    xml += '\t\t\t</node>\n'

xml += '\t\t</nodes>\n' + '\t\t<edges>\n'

for i, edge in enumerate(contents_edges):
    edge = edge.split(",")
    xml += '\t\t\t<edge id=' + "\"" + str(i) + "\"" + " source=" + "\"" + edge[0] + "\"" + " target=" + "\"" + edge[1] + "\"></edge>\n"

xml += '\t\t</edges>\n'
xml += '\t</graph>\n'
xml += '</gexf>\n'

print xml
