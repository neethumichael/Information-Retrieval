Read me for Homework 3 - Small Search Engine


1) Neethu_Prasad_CS6200_HW3.zip contains the followig
    a) Executable java file   : indexer.java
    b) Executable java file 2 : bm25.java
    b) input corpus file      : tccorpus.txt
    c) input query file       : queries.txt
    d) Output file 1          : index.out
    e) Output file 2          : results.eval
    d) readme.txt

2) Instructions for compiling and running:
     Compilation:
      javac indexer.java
      javac bm25.java
     Run:
      java indexer tccorpus.txt index.out
      java bm25 index.out queries.txt 100 > results.eval

3) Output of the program:

a) index.out - Inverted index after tokenization in the format word (docid1, tf1) (docid2, tf2)... 

b) results.eval - top 100 document IDs and their BM25 scores for each test query in the format 
                  1	|Q0	|3127	|1	|15.017122036293229	|DESKTOP-I9L2CPG

Implementation
--------------

1) indexer.java : 
    a) reads the corpus file 
    b) For tokenization, break the character sequence at any run of whitespace 
      and ignore any token with only digits.
    c) For each document in tccorpus.txt
          -> if the word is not in the Inverted index for the document
                add the word and termfrequency tf as 1
          -> else , update the term frequency
    d) Output the invereted index to index.out

2) bm25.java
    a) Retrieve queries.txt
    b) Retrieve all inverted lists corresponding to terms in a query.
    c) Calculate N,dl,avdl from the retrieved index.
          N -> total number of documents in the retrieved index.
          dl -> document length for each document.
          avdl -> average document length.
    d) For each query ,
          for each word i in the query,
             calculate n -> number of documents in which i occurs
                       f -> frequency of word in the document
                       qf-> frequency of word in the query
                       K = k1 *((1-b)+ (b* (dl/avdl)))
                         where  dl = length of the document
                         Given values : 
                         k1=1.2, b=0.75, k2=100
                         R =0 , r =0
            The BM25 score is calculated using the formula :
             summation : log ((r+0.5)/(R-r+0.5))/((n-r+0.5) (N-n-R+r)) . ((k1 + 1)f)/(K+f) . ((k2+1)qf)/(k2+qf)
    e) Store top 100 documents according to bm25 score for each query to results.eval
    
