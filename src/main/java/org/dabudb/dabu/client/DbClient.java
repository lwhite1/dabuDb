package org.dabudb.dabu.client;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.dabudb.dabu.shared.exceptions.DatastoreException;
import org.dabudb.dabu.client.exceptions.JsonSerializationException;
import org.dabudb.dabu.client.exceptions.ProtobufSerializationException;
import org.dabudb.dabu.client.exceptions.SerializationException;
import org.dabudb.dabu.shared.Document;
import org.dabudb.dabu.shared.DocumentFactory;
import org.dabudb.dabu.shared.exceptions.OptimisticLockException;
import org.dabudb.dabu.shared.exceptions.PersistenceException;
import org.dabudb.dabu.shared.protobufs.Request;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static org.dabudb.dabu.shared.DocumentUtils.*;
import static org.dabudb.dabu.shared.msg.MessageUtils.*;
import static org.dabudb.dabu.shared.protobufs.Request.*;

/**
 * The database client interface
 */
public class DbClient implements KeyValueStoreApi {

  private final ClientSettings settings = ClientSettings.getInstance();

  /**
   * Returns a DbClient instance
   */
  public static DbClient get() {
    return new DbClient();
  }

  private DbClient() {
  }

  /**
   * Writes the given document to the database as an "upsert"
   */
  @Override
  public void write(@NotNull Document document) throws DatastoreException {

    Request.Document doc = getDocument(document);
    Header header = getHeader();
    WriteRequestBody body = getWriteRequestBody(doc);
    WriteRequest request = getWriteRequest(header, body);
    Request.WriteReply reply = settings.getCommClient().sendRequest(request);
    checkErrorCondition(reply.getErrorCondition());
  }

  /**
   * Writes all the documents in documentCollection to the database
   * <p>
   * All writes are "upserts"
   */
  @Override
  public void write(@NotNull List<Document> documentCollection) throws DatastoreException {
    List<Request.Document> documentList = new ArrayList<>();
    for (Document document : documentCollection) {
      Request.Document doc = getDocument(document);
      documentList.add(doc);
    }
    Header header = getHeader();
    WriteRequestBody body = getWriteRequestBody(documentList);
    WriteRequest request = getWriteRequest(header, body);
    Request.WriteReply reply = settings.getCommClient().sendRequest(request);
    checkErrorCondition(reply.getErrorCondition());
  }

  /**
   * Returns the document with the given key, or null if it doesn't exist
   */
  @Nullable
  @Override
  public Document get(@NotNull byte[] key) throws DatastoreException {
    ByteString keyBytes = ByteString.copyFrom(key);
    Header header = getHeader();
    GetRequestBody body = getGetRequestBody(keyBytes);
    GetRequest request = getGetRequest(header, body);

    Request.GetReply reply = settings.getCommClient().sendRequest(request);
    checkErrorCondition(reply.getErrorCondition());
    ByteString resultBytes = reply.getDocumentBytesList().get(0);
    return getDocumentFromRequestDoc(resultBytes);
  }

  /**
   * Returns a collection (possibly empty) of documents associated with the given list of keys
   */
  @Override
  public List<Document> get(@NotNull List<byte[]> keys) throws DatastoreException {
    Header header = getHeader();
    List<ByteString> byteStrings = new ArrayList<>();
    for (byte[] bytes : keys) {
      byteStrings.add(ByteString.copyFrom(bytes));
    }
    GetRequestBody body = getGetRequestBody(byteStrings);
    GetRequest request = getGetRequest(header, body);
    Request.GetReply reply = settings.getCommClient().sendRequest(request);
    checkErrorCondition(reply.getErrorCondition());
    List<ByteString> byteStringList = reply.getDocumentBytesList();
    List<Document> results = new ArrayList<>();
    for (ByteString bytes : byteStringList) {
      results.add(getDocumentFromRequestDoc(bytes));
    }
    return results;
  }

  /**
   * Deletes the given document if it exists in the database
   * <p>
   * Does nothing if the document does not exist
   */
  @Override
  public void delete(@NotNull Document document) throws DatastoreException {
    Request.Document doc = getDocument(document);
    Header header = getHeader();
    DeleteRequestBody body = getDeleteRequestBody(doc);
    WriteRequest request = getDeleteRequest(header, body);
    Request.WriteReply reply = settings.getCommClient().sendRequest(request);
    checkErrorCondition(reply.getErrorCondition());
  }

  /**
   * Deletes all the given documents that exist in the database
   * <p>
   * Any documents not in the database are ignored
   */
  @Override
  public void delete(@NotNull List<Document> documents) throws DatastoreException {
    List<Request.Document> docs = new ArrayList<>();

    for (Document document : documents) {
      Request.Document doc = getDocument(document);
      docs.add(doc);
    }

    Header header = getHeader();
    DeleteRequestBody body = getDeleteRequestBody(docs);
    WriteRequest request = getDeleteRequest(header, body);
    Request.WriteReply reply = settings.getCommClient().sendRequest(request);
    checkErrorCondition(reply.getErrorCondition());
  }

  private void checkErrorCondition(ErrorCondition condition) throws DatastoreException {
    if (condition != null && condition.getErrorType() != ErrorType.NONE) {
      System.out.println(condition);
      ErrorType type = condition.getErrorType();

      switch (type) {
        case JSON_SERIALIZATION_EXCEPTION:
          throw new JsonSerializationException(condition);
        case PROTOCOL_BUFFER_SERIALIZATION_EXCEPTION:
          throw new ProtobufSerializationException(condition);
        case SERIALIZATION_EXCEPTION:
          throw new SerializationException(condition);
        case OPTIMISTIC_LOCK_EXCEPTION:
          throw new OptimisticLockException(condition);
        case PERSISTENCE_EXCEPTION:
          throw new PersistenceException(condition);
      /*  case SEVERE_SERVER_EXCEPTION:
            throw new SevereServerException();
        default:
          throw new DatastoreException("An unhandled error-type condition occurred");
          */
      }
    }
  }

  private Document getDocumentFromRequestDoc(ByteString resultBytes) {

    Document document = DocumentFactory.documentForClass(settings.getDocumentClass());

    if (document == null) {
      throw new RuntimeException("Failed to get document from DocumentFactory.");
    }

    Request.Document result;
    try {
      result = Request.Document.parseFrom(resultBytes);
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
      throw new RuntimeException("Unable to parse from protobuf");
    }
    document.setContentClass(result.getContentClass());
    document.setContentType(result.getContentType());
    document.setKey(result.getKey().toByteArray());
    document.setSchemaVersion((short) result.getSchemaVersion());
    document.setInstanceVersion(result.getInstanceVersion());
    document.setContents(result.getContentBytes().toByteArray());
    return document;
  }
}
