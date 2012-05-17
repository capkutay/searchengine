John A Kutay
John.Kutay@gmail.com
SID: 20238251

Project 4 is a continuation of multi-threaded search, adding web crawling functionality. Instead of indexing the pages in a local directory,
html is being crawled, parsed (to find links and remove tags), and indexed. The functionality and primary data structures of multi-threaded search have been preserved.
However, The inverted index builder will now execute a thread that will fetch HTML, compile up to 30 links, and index the words in the pages.

A socket is initially opened on a web page. The program then reads the output stream from the socket into a string. The links on the page are parsed using RegEx and compiled into
an ArrayList of strings. An ArrayList was used because the links do not need to be sorted and appending to the end of an arraylist is efficient for this task. Copying the webpage into the file system
was avoided to optimize efficiency. Instead, the HTML was read in directly to a String, also making it easier to parse with regex. 

When compiling links, the URL class was used to avoid parsing duplicate page locations and to easily and intelligently append resources to the link. Once all the web pages were indexed, the functionality
for building the inverted index remained the same. 