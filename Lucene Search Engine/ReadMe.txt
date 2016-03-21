Read me for Homework 4 - Lucene Search Engine
-----------------------------------------------
Neethu_Prasad_CS6200_HW4.zip contains the followig
     i) ReadMe.txt (Handin-1)
    ii) Source code folder (Handin-2)
       a) Executable java file 1 : LuceneSearchEngine.java
       b) Executable java file 2 : LuceneIndex.java
       c) Executable java file 3 : LuceneZipf.java
       d) Executable java file 4 : LuceneQuerySearch.java
       e) External jar file 1    : jcommon-1.0.23.jar
       f) External jar file 2    : jfreechart-1.0.19.jar
       g) External jar file 3    : lucene-analyzers-common-4.7.2.jar
       h) External jar file 4    : lucene-core-4.7.2.jar
       i) External jar file 5    : lucene-highlighter-4.7.2.jar
       j) External jar file 6    : lucene-queryparser-4.7.2.jar
       k) External jar file 7    : jsoup-1.8.3.jar 
       l) input query file       : queries.txt
       m) the corpus             : cacm
    iii) Output          
       a) Output file 1          : wordFrequency.txt
       e) Output file 2          : searchResult.txt
       f) Output file 3          : zipf.jpg
       g) Output file 4          : log-zipf.jpg
    iv) wordFrequency.txt (Handin-3)
     v) HandIn4_ZIPFIAN CURVE.pdf 
     vi)HandIn5_Ranked Query Results-1.pdf
    vii)HandIn6_Comparison of doc retrieved using Lucene and BM25.pdf

2) Instructions for compiling and running:
    NOTE : queries.txt, cacm, all the jar files and .java files should be in the same folder(path).
           Ie, all the files provided in the folder 'source code' should be in same directory

     Compilation:
      javac -cp ".:*" LuceneSearchEngine.java LuceneIndex.java LuceneZipf.java LuceneQuerySearch.java
     Run:
       java -cp ".:*" LuceneSearchEngine LuceneIndex LuceneZipf LuceneQuerySearch

GENERAL NOTES :
--------------
(Simple analyzer is used for both indexing and Retrieval)
1) The program execution starts from LuceneSearchEngine.java
2) INDEX CREATION -From LuceneSearchEngine.java the program directs to LuceneIndex.java 
          which provides user with a prompt
             Enter the FULL path where the index will be created: (e.g. /Usr/index or c:\temp\index)
          followed by
             Enter the FULL path to add into the index (q=quit): (e.g. /home/mydir/docs or c:\Users\mydir\docs)
             [Acceptable file types: .xml, .html, .html, .txt]
  After this, all the documents in the corpus we provide (2nd prompt) are added to the directory at path specified
  in prompt 1.
3) SORTED TERM-FREQUENCIES:
  LuceneZipf.java reads the index , created a list of sorted term and their frequency 
  Also draw zipfian curves.
4) QUERY SEARCH:
   LuceneQuerySearch.java reads the index , perform search for the queries in queries.txt and displays
   the top 100 results sorted with lucene in-built function.This program also display a highlight text snippet
   (which displays the query term in bold with tag <b>)


SAMPLE EXECUTION (IN WINDOWS)

Enter the FULL path where the index will be created: (e.g. /Usr/index or c:\temp\index)
C:\Users\neeth\workspace\LuceneSearchEngine\test400
Enter the FULL path to add into the index (q=quit): (e.g. /home/mydir/docs or c:\Users\mydir\docs)
[Acceptable file types: .xml, .html, .html, .txt]
C:\Users\neeth\workspace\LuceneSearchEngine\cacm

************************
3204 documents added.
************************
Enter the FULL path to add into the index (q=quit): (e.g. /home/mydir/docs or c:\Users\mydir\docs)
[Acceptable file types: .xml, .html, .html, .txt]
q
Index created is created successfully at C:\Users\neeth\workspace\LuceneSearchEngine\test400
Sorted Word-Frequency is written to file
Sorted Term-Frequency list written to file wordFrequency.txt
zip graph is plotted successfully zipf.jpg and log-zipf.jpg
portable operating systems Found 440 hits.
code optimization for space efficiency Found 1579 hits.
parallel algorithms Found 272 hits.
parallel processor in information retrieval Found 1529 hits.
Query results written to file searchResult.txt