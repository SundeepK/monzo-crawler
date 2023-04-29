# Monzo crawler
This project aims to crawl the monzo.com domain. 

## Design
The idea behind the design is that it would request each page, then it would parse the document and extract all links. It would then return this list of urls so that subsequent executions could also parse each link and repeat the process.

This project uses CompletableFutures to run network requests in threads that are bound by a fixed thread pool which can be configured.

It also has a very basic sitemap so that we can view the results.

### Requirements
1. Java 17

### Build project and run tests
```bash
./gradlew build
```

to run just the test:
```bash
./gradlew test
```

### Crawl monzo 
```bash
./gradlew run    
```

Output can be seen in file `sitemap.txt`

### Docker

This project comes with a simple DockerFile which can be built using:
```bash
docker build -t crawler .
```

