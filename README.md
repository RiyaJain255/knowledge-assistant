# 🧠 AI-Powered Knowledge Assistant

### Your documents. Your knowledge base. Answers grounded in your files.

**A Java-powered Retrieval-Augmented Generation (RAG) application** that
lets you upload PDF, DOCX, and TXT documents, ask questions in plain
English, and receive answers with source citations.



![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Spring
Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen?logo=springboot&logoColor=white)
![Spring
AI](https://img.shields.io/badge/Spring%20AI-1.1-6DB33F?logo=spring&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)
![pgvector](https://img.shields.io/badge/Vector%20DB-pgvector-336791)
![Ollama](https://img.shields.io/badge/Local%20AI-Ollama-black?logo=ollama&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?logo=docker&logoColor=white)
:::

------------------------------------------------------------------------

## ✨ What makes it special?

Stop searching through long documents manually. Upload your files and
let your own AI-powered knowledge assistant find the relevant
information.

-   📚 **Multi-format uploads** --- PDF, DOCX, and TXT documents.
-   💬 **Natural-language Q&A** --- ask questions as if you're talking
    to a person.
-   🎯 **Grounded answers with citations** --- responses include
    numbered sources, filenames, page details, and relevant excerpts.
-   👤 **Multi-user authentication** --- register and log in with JWT;
    each user searches only their own documents.
-   🗂️ **Document collections** --- organize files into collections such
    as *HR Policies*, *Java Notes*, or *Product Manuals*.
-   ⚙️ **Background ingestion** --- extraction, chunking, embedding, and
    indexing happen asynchronously.
-   🔎 **Semantic search** --- retrieve relevant passages using vector
    similarity.
-   🖥️ **Built-in web interface** --- no Node.js or npm setup required.
-   🔌 **REST API** --- interact with the application programmatically.
-   🏠 **Local AI option** --- run chat and embedding models locally
    through Ollama, without OpenAI API usage.

> **Current local setup:** Ollama with `llama3.2:1b` for answer
> generation and `nomic-embed-text` for embeddings. PostgreSQL is
> exposed on host port `5433`, and the application runs on port `8081`
> in the current development setup.

------------------------------------------------------------------------

## 🧰 Technology stack

  -----------------------------------------------------------------------
  Layer                   Technology              Responsibility
  ----------------------- ----------------------- -----------------------
  Language                Java 21                 Core application

  Backend                 Spring Boot 3.5         REST APIs, services,
                                                  application lifecycle

  AI integration          Spring AI 1.1           Chat and embedding
                                                  model integration

  Chat model              Ollama --- Llama 3.2 1B Generates answers from
                                                  retrieved context

  Embedding model         Ollama --- Nomic Embed  Converts text into
                          Text                    vectors

  Database                PostgreSQL 16           Users, collections,
                                                  documents, application
                                                  data

  Vector search           pgvector                Stores and searches
                                                  document embeddings

  Authentication          Spring Security + JWT   Secures user-specific
                                                  operations

  Document processing     PDFBox / Apache POI     Extracts text from
                                                  supported files

  Build tool              Maven                   Dependency management
                                                  and builds

  Containers              Docker Compose          Runs PostgreSQL with
                                                  pgvector
  -----------------------------------------------------------------------

------------------------------------------------------------------------

## 🏗️ How it works

The application follows a Retrieval-Augmented Generation (RAG) workflow.
Instead of asking the language model to answer from memory alone, it
first retrieves relevant passages from your uploaded documents.

``` mermaid
flowchart TD
    A[Browser UI / REST Client] -->|JWT authenticated request| B[Spring Boot Application]
    B --> C[Document Upload]
    C --> D[Text Extraction]
    D --> E[Text Chunking]
    E --> F[Embedding Model - Ollama]
    F --> G[(PostgreSQL + pgvector)]
    H[User Question] --> I[Question Embedding]
    I --> J[Similarity Search]
    G --> J
    J --> K[Relevant numbered excerpts]
    K --> L[Chat Model - Ollama]
    L --> M[Answer with source citations]
```

### 📥 Document ingestion flow

1.  The user uploads a PDF, DOCX, or TXT file to a collection.
2.  The application validates and saves the file.
3.  A document record is created with a processing status.
4.  A background worker extracts text from the file.
5.  The extracted text is split into smaller, manageable chunks.
6.  The embedding model converts each chunk into a vector.
7.  Text, vectors, and metadata are stored in PostgreSQL using pgvector.
8.  The document status changes to `READY` or `FAILED`.

Each stored chunk includes metadata such as:

-   `userId`
-   `collectionId`
-   `documentId`
-   `filename`
-   `page`
-   `chunkIndex`

### 💡 Question-answering flow

1.  The user asks a question in plain English.
2.  The question is converted into an embedding.
3.  pgvector finds the most relevant document chunks.
4.  Search is filtered by the authenticated user's ID and, optionally, a
    selected collection.
5.  Retrieved excerpts are supplied to the chat model as numbered
    sources.
6.  The model generates an answer grounded in those excerpts.
7.  The response includes citations and source information.

If no relevant context is found, the application can return a
no-relevant-information response instead of asking the model to invent
an answer.

------------------------------------------------------------------------

## 🚀 Run locally

### 1. Prerequisites

Install the following:

  Requirement      Version / notes
  ---------------- ---------------------
  JDK              Java 21
  Maven            3.9+
  Docker Desktop   Docker Compose v2
  Ollama           Local model runtime
  VS Code          Recommended editor

Verify your installations:

``` powershell
java -version
mvn -v
docker compose version
ollama --version
```

### 2. Get the project ready

Open the project folder in VS Code and open its integrated PowerShell
terminal.

Create your environment file if you have not already done so:

``` powershell
Copy-Item .env.example .env
```

Edit `.env` locally. Never commit this file or share its secrets.

### 3. Download the local AI models

Make sure Ollama is installed and running. Then run:

``` powershell
ollama pull llama3.2:1b
ollama pull nomic-embed-text
```

Confirm that the models are available:

``` powershell
ollama list
```

The application uses:

-   `llama3.2:1b` --- chat / answer generation.
-   `nomic-embed-text` --- document and question embeddings.

### 4. Configure environment variables

For the local Ollama setup, use these values in `.env`:

``` properties
OPENAI_API_KEY=ollama
OPENAI_BASE_URL=http://localhost:11434
CHAT_MODEL=llama3.2:1b
EMBEDDING_MODEL=nomic-embed-text
EMBEDDING_DIMENSIONS=768

JWT_SECRET=replace-with-a-new-random-secret-of-at-least-32-characters
DB_PASSWORD=knowledge
```

Generate a strong JWT secret locally. For example, in PowerShell:

``` powershell
$bytes = New-Object byte[] 48
$rng = [Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$secret = [Convert]::ToBase64String($bytes)
"JWT_SECRET=$secret"
$rng.Dispose()
Remove-Variable secret, bytes, rng
```

Copy the generated value into `.env`. Do not paste it into source code,
screenshots, issues, or chat messages.

**Important:** `EMBEDDING_DIMENSIONS` must match the embedding model's
output size. The current Nomic Embed Text configuration uses `768`. If
you change embedding models later, you must re-index your documents with
the new model.

### 5. Start PostgreSQL with pgvector

``` powershell
docker compose up -d --wait db
```

Check that the database container is healthy:

``` powershell
docker ps
```

In the current local configuration, PostgreSQL is available from the
host at `localhost:5433`.

### 6. Load `.env` and start Spring Boot

PowerShell does not automatically load `.env` when running Maven
directly. Load its variables into the current terminal session:

``` powershell
Get-Content .env | ForEach-Object {
    if ($_ -match '^\s*([A-Za-z_][A-Za-z0-9_]*)=(.*)$') {
        [Environment]::SetEnvironmentVariable($matches[1], $matches[2], 'Process')
    }
}
```

Then, in the **same terminal**, start the application:

``` powershell
mvn spring-boot:run
```

Wait for a startup message similar to:

``` text
Started KnowledgeAssistantApplication
Tomcat started on port 8081
```

### 7. Open the application

Visit:

**http://localhost:8081**

Keep the Spring Boot terminal running while using the application.

------------------------------------------------------------------------

## 🧪 Try it out

1.  Open the web interface.
2.  Register a user account and sign in.
3.  Create a collection, for example `Java Learning`.
4.  Upload a Java PDF, DOCX, or TXT file.
5.  Wait until the document status becomes `READY`.
6.  Ask questions about the uploaded content.
7.  Review the answer and its source citations.

### Sample questions for a Java PDF

-   What are the four pillars of object-oriented programming?
-   What is the difference between JDK, JRE, and JVM?
-   Explain inheritance with an example.
-   What is the difference between an interface and an abstract class?
-   What is method overloading?
-   Explain the difference between `==` and `.equals()` in Java.

**Grounding test:** Ask for information that is not present in the
uploaded file. The assistant should tell you that the document does not
provide that information rather than making up an answer.

------------------------------------------------------------------------

## 🔌 REST API

All endpoints except registration, login, and health require:

``` http
Authorization: Bearer <token>
```

  -----------------------------------------------------------------------------------
  Method                  Endpoint                            Purpose
  ----------------------- ----------------------------------- -----------------------
  `POST`                  `/api/auth/register`                Create an account

  `POST`                  `/api/auth/login`                   Authenticate and
                                                              receive a JWT

  `GET`                   `/api/auth/me`                      Get the current user

  `GET`                   `/api/collections`                  List the user's
                                                              collections

  `POST`                  `/api/collections`                  Create a collection

  `DELETE`                `/api/collections/{id}`             Delete a collection and
                                                              its associated data

  `POST`                  `/api/collections/{id}/documents`   Upload a document

  `GET`                   `/api/collections/{id}/documents`   List documents in a
                                                              collection

  `GET`                   `/api/documents/{id}`               Get document details
                                                              and ingestion status

  `DELETE`                `/api/documents/{id}`               Delete a document and
                                                              its vectors

  `POST`                  `/api/ask`                          Ask a question and
                                                              retrieve cited answers

  `GET`                   `/actuator/health`                  Application health
                                                              check
  -----------------------------------------------------------------------------------

### Example question request

``` http
POST /api/ask
Content-Type: application/json
Authorization: Bearer <token>
```

``` json
{
  "question": "What are the four pillars of OOP?",
  "collectionId": 2,
  "topK": 5
}
```

The `collectionId` is optional. When omitted, the application searches
across the authenticated user's collections, subject to the
application's retrieval rules.

### Example response shape

``` json
{
  "answer": "The four pillars of OOP are ... [1]",
  "sources": [
    {
      "index": 1,
      "documentId": 3,
      "filename": "java-oop.pdf",
      "page": 4,
      "chunkIndex": 2,
      "score": 0.82,
      "cited": true,
      "snippet": "The four pillars of object-oriented programming are ..."
    }
  ],
  "latencyMs": 1840
}
```

*Illustrative response only; actual values depend on the uploaded
documents and retrieval results.*

------------------------------------------------------------------------

## 🔍 Inspect the vector database

To check how many chunks have been indexed:

``` powershell
docker exec ka-db psql -U knowledge -d knowledge -c "SELECT COUNT(*) FROM vector_store;"
```

To inspect stored text and metadata:

``` powershell
docker exec ka-db psql -U knowledge -d knowledge -c "SELECT id, LEFT(content, 200) AS content_preview, metadata FROM vector_store LIMIT 5;"
```

You should see the extracted text alongside metadata such as the
filename, page, user ID, and chunk index.

------------------------------------------------------------------------

## ⚙️ Configuration reference

  ---------------------------------------------------------------------------------------------------
  Variable                     Current local value                            Description
  ---------------------------- ---------------------------------------------- -----------------------
  `OPENAI_API_KEY`             `ollama`                                       Placeholder key for the
                                                                              local OpenAI-compatible
                                                                              Ollama endpoint

  `OPENAI_BASE_URL`            `http://localhost:11434`                       Ollama API base URL

  `CHAT_MODEL`                 `llama3.2:1b`                                  Chat model

  `EMBEDDING_MODEL`            `nomic-embed-text`                             Embedding model

  `EMBEDDING_DIMENSIONS`       `768`                                          Embedding vector size

  `JWT_SECRET`                 Set privately                                  JWT signing secret; 32+
                                                                              characters

  `DB_URL`                     `jdbc:postgresql://localhost:5433/knowledge`   Database JDBC URL in
                                                                              this local setup

  `DB_USER`                    `knowledge`                                    Database username

  `DB_PASSWORD`                Set in `.env`                                  Database password

  `RAG_CHUNK_SIZE`             `800`                                          Target chunk size in
                                                                              tokens

  `RAG_TOP_K`                  `5`                                            Number of relevant
                                                                              excerpts to retrieve

  `RAG_SIMILARITY_THRESHOLD`   `0.25`                                         Similarity threshold;
                                                                              tune based on retrieval
                                                                              quality
  ---------------------------------------------------------------------------------------------------

Defaults may be supplied by `application.yml`; verify the project's
configuration before changing them.

------------------------------------------------------------------------

## 🧯 Troubleshooting

  -----------------------------------------------------------------------
  Problem                             What to check
  ----------------------------------- -----------------------------------
  `JWT_SECRET must be set`            Load `.env` into the same
                                      PowerShell session before starting
                                      Maven.

  Database connection refused         Run
                                      `docker compose up -d --wait db`;
                                      confirm the host port is `5433`.

  Ollama connection refused           Confirm Ollama is running and
                                      reachable at
                                      `http://localhost:11434`.

  Model not found                     Run `ollama list`; pull the
                                      configured chat and embedding
                                      models if needed.

  Embedding dimension mismatch        Confirm `EMBEDDING_DIMENSIONS`
                                      matches the selected embedding
                                      model. Recreate/re-index the vector
                                      store when changing models.

  Document status is `FAILED`         Inspect Spring Boot logs for
                                      extraction, embedding, or database
                                      errors.

  No relevant answer found            Confirm the document is `READY`,
                                      ask a question using terms present
                                      in the file, and review the
                                      retrieval threshold.

  Scanned PDF has no extracted text   OCR is not included in the basic
                                      extraction flow; add an OCR engine
                                      for image-only PDFs.

  Port 8080 is occupied               Use the configured application
                                      port. The current local setup uses
                                      `8081`.

  Port 5432 is occupied               The current local setup maps the
                                      PostgreSQL container to host port
                                      `5433`.
  -----------------------------------------------------------------------

### Changing embedding models

Do not mix vectors generated by different embedding models in the same
vector space. If you change the embedding model or its dimensions:

1.  Stop the Spring Boot application.
2.  Back up any data you need.
3.  Recreate the vector store schema with the correct dimensions.
4.  Re-ingest the documents so every stored vector is generated by the
    new model.

Deleting the vector table removes its indexed chunks. Do not use
`docker compose down -v` unless you intentionally want to delete the
entire database volume.

------------------------------------------------------------------------

## 🧪 Tests and quality

Run the project's test suite with:

``` powershell
mvn test
```

The repository includes tests for document extraction and
citation-related behavior. Check the test sources for the exact coverage
available in the current version.

Useful next integration tests include:

-   PostgreSQL + pgvector integration tests with Testcontainers.
-   End-to-end document ingestion tests.
-   User-isolation tests to ensure one user cannot retrieve another
    user's documents.
-   Retrieval and citation correctness tests.
-   Tests for unsupported files and failed ingestion.

------------------------------------------------------------------------

## 🗺️ Project structure

``` text
knowledge-assistant/
├── src/
│   ├── main/
│   │   ├── java/com/example/knowledge/
│   │   │   ├── auth/          # Registration, login, current-user APIs
│   │   │   ├── collection/    # Collection management
│   │   │   ├── common/        # Shared exceptions and error handling
│   │   │   ├── config/        # Application and async configuration
│   │   │   ├── document/     # Upload, extraction, chunking, ingestion
│   │   │   ├── rag/           # Retrieval, prompts, answers, citations
│   │   │   └── security/      # JWT and Spring Security
│   │   └── resources/
│   │       ├── db/migration/  # Flyway migrations
│   │       ├── static/        # Built-in browser UI
│   │       └── application.yml
│   └── test/
├── samples/                   # Sample documents
├── .vscode/                   # VS Code launch and task settings
├── Dockerfile
├── docker-compose.yml
├── docker-compose.prod.yml
├── pom.xml
└── .env.example
```

------------------------------------------------------------------------

## 🚧 Roadmap

Ideas for taking the project further:

-   [ ] Multi-page and large-document ingestion testing
-   [ ] More robust chunk overlap and configurable chunking
-   [ ] Hybrid retrieval: keyword search + vector search
-   [ ] Re-ranking for more relevant source passages
-   [ ] Query rewriting for complex questions
-   [ ] Conversation memory and follow-up questions
-   [ ] Streaming responses with Server-Sent Events (SSE)
-   [ ] OCR for scanned PDFs
-   [ ] Support for PPTX, HTML, and Markdown files
-   [ ] Collection sharing and role-based access
-   [ ] Testcontainers-based integration tests
-   [ ] Production deployment, monitoring, and automated backups

------------------------------------------------------------------------

## 🔐 Security notes

-   Never commit `.env`, API keys, JWT secrets, or database passwords.
-   Use a unique, strong `JWT_SECRET` in every environment.
-   Keep the database private in production; do not expose its port
    publicly.
-   Restrict registration or add approval controls before making the
    application public.
-   Back up both PostgreSQL data and uploaded original files.
-   Review authorization and user-isolation behavior before deploying
    with real user data.

------------------------------------------------------------------------

### 💡 Built to turn documents into knowledge.

**Java • Spring Boot • Spring AI • Ollama • PostgreSQL • pgvector**

If you find this project useful, consider giving it a ⭐ on GitHub.
:::
