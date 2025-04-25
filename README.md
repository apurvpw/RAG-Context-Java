# RAG-Context-Java

This project implements a Retrieval Augmented Generation (RAG) system with context management, document uploading, and embedding generation functionality. It's built using Spring Boot and LangChain4j, a Java library for creating LLM applications.

## Features

- Context management for organizing documents
- Document upload and processing via context creation
- Integration with LangChain4j for LLM interactions and embedding generation
- Embedding generation using the Ollama API
- Embedding storage in ChromaDB
- Chat functionality with context-aware responses
- Chat history storage in MongoDB
- Session management for resuming previous conversations

## Running the Application

1. Make sure you have Java 17+ installed
2. Make sure MongoDB and ChromaDB are running
3. Configure the Ollama API in `application.yml`
4. Start the application:
   ```bash
   ./mvnw spring-boot:run
   ```
5. Access the UI at `http://localhost:8080`

## Using the Web Interface

### Creating a Context and Uploading Documents

1. Open `http://localhost:8080` in your browser
2. On the "Create Context" tab:
   - Enter a name and description for your context
   - Upload a document file or provide a URL to a document
   - Click "Create Embeddings"
3. Once the context is created, you can start a chat session by clicking "Start New Chat Session"

### Managing Chat Sessions

1. Go to the "Chat Sessions" tab
2. Select a context from the dropdown menu
3. Select an existing session or create a new one
4. Click "Load Selected Session" to open the chat interface

### Using the Chat Interface

1. Type your question in the input field and press Enter or click "Send"
2. The AI response will be displayed in the chat window
3. Your conversation history is automatically saved to MongoDB
4. You can rename the session by clicking on the session name
5. You can clear the chat window using the "Clear Chat" button
6. Use "Back to Context Manager" to return to the main interface

## Document Upload and Create Document Embeddings

### Using the API

You can upload documents and create emebedddings using the HTTP API directly:

```bash
# Create a context with embeddings
curl -X POST "http://localhost:8080/api/create-context-embeddings" \
    -F "contextName=My Context" \
    -F "contextDescription=My description" \
    -F "document=@/path/to/your/file.pdf"
```

## Supported Document Types

The system supports the following document types:
- PDF documents
- Word documents (.doc, .docx)
- Plain text files
- Web pages (via URL)

## Configuration

Configure the application in `src/main/resources/application.yml`:

```yaml
app:
  chroma:
    url: http://localhost:8000  # ChromaDB URL
  ollama:
    url: http://localhost:11434  # Ollama API URL
    chatModel: llama3.1   # Model for chat
    embeddingModel: llama3.1  # Model for embeddings
  embedding:
    dimensions: 384  # Embedding dimensions
  document:
    chunkSize: 500   # Document chunk size
    chunkOverlap: 50 # Chunk overlap
```

## API Endpoints

### Contexts

```
GET /api/contexts
```
Returns a list of all available contexts.

### Create Context with Embeddings

```
POST /api/create-context-embeddings
Content-Type: multipart/form-data

contextName: <context-name>
contextDescription: <context-description>
document: <file-upload> (Optional)
documentUrl: <url> (Optional)
```

### Chat Sessions

```
GET /api/sessions
```
Returns a list of all chat sessions.

```
GET /api/contexts/{contextId}/sessions
```
Returns all chat sessions for a specific context.

```
GET /api/sessions/{sessionId}/history
```
Returns the chat history for a specific session.

```
PUT /api/sessions/{sessionId}/name
Content-Type: application/json

{
  "sessionName": "New Session Name"
}
```
Updates the name of a chat session.

### Create Chat Session

```
POST /api/create-chat-session
Content-Type: application/json

{
  "contextName": "<context-name>",
  "contextId": "<context-id>", (Optional, will be created if not provided)
  "contextDescription": "<context-description>", (Optional)
  "sessionName": "<session-name>" (Optional)
}
```

## WebSocket Communication

Connect to WebSocket endpoint: `/ws`

Send messages to: `/app/chat`

```json
{
  "sessionId": "<session-id>",
  "contextName": "<context-name>",
  "contextId": "<context-id>", (Optional, will be created if not provided)
  "question": "Your question here"
}
```

Subscribe to receive responses: `/topic/chat/<session-id>`

Response format:
```json
{
  "sessionId": "<session-id>",
  "message": "Response token or error message",
  "done": false
}
```

When the response is complete, a final message with `"done": true` will be sent.

## Data Model

### Context
Represents a collection of related documents
- `id`: Unique identifier
- `name`: Human-readable name
- `description`: Optional description

### ChatSession
Represents a conversation session
- `id`: Unique identifier
- `contextId`: Reference to the associated context
- `contextName`: Name of the associated context
- `sessionName`: Optional name for the session
- `createdAt`: Timestamp when session was created
- `lastActivity`: Timestamp of the last activity

### ChatMessage
Represents a single message in a chat session
- `id`: Unique identifier
- `sessionId`: Reference to the chat session
- `type`: Message type (USER or AI)
- `content`: Message content
- `timestamp`: When the message was created

## LangChain4j Integration

This application leverages LangChain4j, a Java framework for building LLM applications, in several key areas:

- **Document Processing**: Using LangChain4j's document loaders and text splitters to process various document types
- **Embedding Generation**: Generating embeddings through LangChain4j's integration with Ollama
- **Vector Store**: Storing and retrieving embeddings in ChromaDB via LangChain4j's ChromaEmbeddingStore
- **Retrieval**: Using LangChain4j's similarity search to find relevant document chunks based on user queries
- **LLM Integration**: Communicating with the language model using LangChain4j's unified API

The application demonstrates how to build a production-grade RAG application using LangChain4j's components in a Spring Boot environment.
