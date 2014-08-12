Evaluation
==========

The projects in this repository help you evaluating GitHub search results for e4xmi-files.
All results of your search request will be stored in a nice human readable xls sheet. Including some e4xmi-file content information (size of the file, is it a fragment, how many commands, menus,...).

How To
==========
In order to perform a complete search of all existing e4xmi-files on GitHub run the at.ecrit.github project.
Copy&Paste this link:
https://github.com/search?o=desc&p=1&q=in%3Apath+*.e4xmi+extension%3Ae4xmi&type=Code&ref=searchresults&s=indexed

If you adapt the search request, please make sure you keep the order ascending or descending turned on otherwise you could run into the problem of receiving some results twice and others not at all.

Next enter the number of search result pages you get from this request.
And finally enter your GitHub credentials.

I'd recommend to take your coffee break now as it will take some time till all the results are collected.

In the end you should end up with a toc.xls that contains all usefull information. You can see samples of this file in one of the at.ecrit.github/rsc/result.... folders.

File description
==========
When you run the application in the at.ecrit.github/rsc/ folder a directory with the name result_ddMMyyyyHHmmss will be created.  
All the files that are created during the result analysation will be put into this directory.
* links.txt -> a collection of the links to the e4xmi file and repository of each search result
* evaluation.xmi -> stores each results url and content info in a so called ApplicationModelReference object
* toc.xls -> the final excel file that gives you a number of information concerning repository name, url, readme, whether it's a fragement, number of parts, perspectives, menus, toolbars and commands.
Based on this file a further evaluation can be eased.
