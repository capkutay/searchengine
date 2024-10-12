# Web Crawler and Search Engine

This project implements a multi-threaded web crawler and search engine. It crawls web pages, indexes their content, and provides a search functionality.

## Features

- **Web Crawling**: Fetches HTML content from web pages starting from a seed URL.
- **HTML Parsing**: Extracts links and removes HTML tags, scripts, and styles.
- **Inverted Index**: Builds an inverted index of words found in the crawled pages.
- **Multi-threaded**: Uses a thread pool for concurrent crawling and indexing.
- **Search Functionality**: Allows searching the indexed content using query terms.

## Main Components

### Driver

The main entry point of the application. It sets up logging, initializes the crawler, and processes search queries.

java
public static void main(String[] args) {
// ... (initialization code)
InvertedIndexBuilder builder = new InvertedIndexBuilder();
index = builder.createInvertedIndex(seedURL);
index.writeInvertedIndex();
Search search = new Search(index);
search.processSearch(queryFile, writer);
// ... (error handling and cleanup)
}

### InvertedIndexBuilder

Responsible for crawling web pages and building the inverted index.

```java
ArrayList<String> newLinks = HTMLParser.parseLinks(fetch.getHTML());
// ... (link processing code)
for (String newLink : newLinks) {
    // ... (URL handling code)
    if (newLink.startsWith("http")) {
        newURL = new URL(newLink);
    } else {
        newURL = new URL(new URL(url), newLink);
    }
    // ... (more processing)
}

```



### HTMLParser

Parses HTML content, removes tags, and extracts links.

### Search

Processes search queries and returns results based on the inverted index.

```java
public void processSearch(String path, PrintWriter writer) {
    ArrayList<String> queryList = QueryListFactory.createQueryList(path);
    index.processQueries(queryList);
    HashMap<Integer, TreeSet<Map.Entry<String, Integer>>> results = index.getSearchResults();
// ... (result processing and writing)
}
```

## Usage

Run the Driver class with the following command-line arguments:

```
java Driver -u <seed_url> -q <query_file>
```

## Output

1. `invertedindex.txt`: Contains the built inverted index.
2. `searchresults.txt`: Contains the search results for the given queries.
3. `debug.log`: Contains debug information and logs.

## Dependencies

- Java 6 or higher
- Log4j for logging
- JUnit for testing (optional)

## Note

This project is designed to crawl up to 30 pages starting from the seed URL. It uses multi-threading to improve performance and handles both absolute and relative URLs.

## License

[Add your license information here]

## Contributing

[Add contribution guidelines if applicable]

## Authors

[Add author information]

## Acknowledgments

[Add any acknowledgments or credits]
