package blog.server;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import com.proto.blog.Blog;
import com.proto.blog.BlogId;
import com.proto.blog.BlogServiceGrpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;


public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

    private final MongoCollection<Document> mongoCollection;

    BlogServiceImpl(MongoClient client){
        //if the database doesn't exists the client will create it
        MongoDatabase db = client.getDatabase("blogdb");
        //below code will get collection if exists else create it
        mongoCollection = db.getCollection("blog");
    }

    @Override
    public void createBlog(Blog request, StreamObserver<BlogId> responseObserver) {
        //create document
        Document doc = new Document("author",request.getAuthor())
                .append("title",request.getTitle())
                .append("content",request.getContent());
        // insert the document into mongodb
        InsertOneResult result = null;

        try{
            result = mongoCollection.insertOne(doc);
        }catch (MongoException e){
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getLocalizedMessage())
                    .asRuntimeException());
            return;
        }

        if(!result.wasAcknowledged() || result.getInsertedId() == null ){
            responseObserver.onError(Status.INTERNAL
                            .withDescription("Blog couldn't be created/inserted")
                            .asRuntimeException());
            return;
        }

        String id = result.getInsertedId().asObjectId().getValue().toString();

        responseObserver.onNext(BlogId.newBuilder().setId(id).build());
        responseObserver.onCompleted();
    }
}
