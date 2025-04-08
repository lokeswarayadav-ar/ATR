### Setup ATR with Docker

1. **Start the docker compose file.**
    ```
    docker compose up -d
    ```
    
    This will start Ollama and a PostgreSQL instance with the pgai extension installed.

1. **Download the Ollama models.** We'll use the `all-minilm` model for embeddings and the `tinyllama` model for reasoning.

    ```
    docker compose exec ollama ollama pull all-minilm
    docker compose exec ollama ollama pull tinyllama
    docker compose exec ollama ollama pull llama3
    ```   

### Create a table, run a vectorizer, and perform semantic search

1. **Connect to the database in your local developer environment**
   The easiest way connect to the database is with the following command:
   `docker compose exec -it db psql`. 
   
   Alternatively, you can connect to the database with the following connection string: `postgres://postgres:postgres@localhost:5432/postgres`.

1. **Enable pgai on your database**

    ```sql
    CREATE EXTENSION IF NOT EXISTS ai CASCADE;
    ```
    
1. **Create a table with the data you want to embed from a huggingface dataset**

    We'll create a table named `wiki` from a few rows of the english-language `wikimedia/wikipedia` dataset.
    
    First, we'll create the table:

    ```sql
    CREATE TABLE kb (
        id      TEXT PRIMARY KEY,
        url     TEXT,
        title   TEXT,
        text    TEXT
    );
    ```
1. **Run ATR and import knowledge base to database **

    ```sql
    Run src/main/java/com/example/atr/AutomaticTicketResponseApplication.java

    execute http://localhost:8080/import
    ```
1. **Create a vectorizer for `kb`**

    To enable semantic search on the `kb` table, we need to create vector embeddings for the `text` column.
    We use a vectorizer to automatically create these embeddings and keep them in sync with the data in the  `kb` table.
    
    ```sql
    SELECT ai.create_vectorizer(
     'public.kb'::regclass,
     destination => 'kb_embedding',
     embedding => ai.embedding_ollama('all-minilm', 384),
     chunking => ai.chunking_recursive_character_text_splitter('text'),
     formatting => ai.formatting_python_template('Title: $title\nURL: $url\nContent: $chunk')
    );
    ```
     Related documentation: [vectorizer usage guide](/docs/vectorizer/overview.md) and [vectorizer API reference](/docs/vectorizer/api-reference.md).

1. **Check the progress of the vectorizer embedding creation**

    ```sql
    select * from ai.vectorizer_status;
    ```
    All the embeddings have been created when the `pending_items` column is 0. This may take a few minutes as the model is running locally and not on a GPU.



### Perform Retrieval Augmented Generation (RAG)

In this section, we'll have the LLM answer questions about pgai based on the wiki entry we added by using RAG. The LLM was never trained on the pgai wiki entry, and so it needs data in the database to answer questions about pgai.

You can perform RAG purely from within the database using SQL or use a python script to interact with the database and perform RAG. We often find that using SQL is easier to create a quick prototype and get started but as the project matures people easily switch to using Python to have more control and make use of Python tooling. 


<details>
<summary>Click to perform RAG within SQL</summary>

 1. **Define a function to perform RAG**
 
    We'll create a function that uses RAG to allow an LLM to answer questions about pgai based on the wiki entry we added.

    RAG involves two steps:
    1. Perform a similarity search to find the most relevant chunks of data.
    2. Use the LLM to generate a response using the relevant chunks as context.
    
    ```sql
    CREATE OR REPLACE FUNCTION generate_rag_response(query_text TEXT)
    RETURNS TEXT AS $$
    DECLARE
       context_chunks TEXT;
       response TEXT;
  BEGIN
     -- Perform similarity search to find relevant blog posts
     SELECT string_agg(embedding_uuid || ': ' || chunk, E'\n') INTO context_chunks
     FROM
     (
       SELECT embedding_uuid, chunk
       FROM kb_embedding_store
       ORDER BY embedding <=> ai.ollama_embed('all-minilm', query_text)
       LIMIT 3
     ) AS relevant_posts;

     -- Generate a summary using llama3
     SELECT ai.ollama_chat_complete
     ( 'llama3'
     , jsonb_build_array
      ( jsonb_build_object('role', 'system', 'content', 'you are a helpful assistant')
     , jsonb_build_object
       ('role', 'user'
       , 'content', query_text || E'\nUse the following context to respond.\n' || context_chunks
       )
     )
   )->'message'->>'content' INTO response;

   RETURN response;
  END;
$$ LANGUAGE plpgsql;
    ```

1. **Use the RAG function to answer questions about the wiki data**

    ```sql
    SELECT generate_rag_response('Issues are not listing in ARM after running SCA to codescan');
    ```

    <details>
    <summary>Click here to see the output</summary>

    | response |
    |-----------------------|
    |   I see that you're experiencing issues with ARM not listing the results from a CodeScan scan. This can be frustrating!

To troubleshoot this issue, I'd like to walk you through some potential solutions:

1. **Check the CodeScan report**: Make sure that the CodeScan report was generated successfully and contains the expected results. You can check this by logging into your CodeScan account and reviewing the report.
2. **Verify ARM settings**: Double-check your ARM settings, especially the SCA tool configuration. Ensure that you've selected CodeScan as the SCA tool and that the baseline branch is correctly set.
3. **Check for missing or incorrect dependencies**: Sometimes, a scan might fail due to missing or incorrect dependencies in your project. Review your project's dependencies and ensure they are correct and up-to-date.
4. **Re-run the scan**: Try re-running the CodeScan scan from within ARM to see if that resolves the issue.

If none of these solutions work, you may want to consider reaching out to the CodeScan support team or searching for more specific troubleshooting guides related to your issue.

Remember, it's always a good idea to have multiple avenues of approach when troubleshooting issues like this!|
    </details>

</details>

