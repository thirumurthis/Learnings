package blog.client;

import com.google.protobuf.Empty;
import com.proto.blog.Blog;
import com.proto.blog.BlogId;
import com.proto.blog.BlogServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

public class BlogClient {

    public static void main(String[] args) throws InterruptedException {

        //create a channel
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .usePlaintext() //not using SSL/TLS now
                .build();

       run(channel);

        //close the channel
        System.out.println("client shutdown channel");
        channel.shutdown();
    }

    private static void run(ManagedChannel channel){
        BlogServiceGrpc.BlogServiceBlockingStub stub = BlogServiceGrpc.newBlockingStub(channel);

        BlogId blogId = createblog(stub);

        if(blogId == null ){
            return;
        }
        readBlog(stub, blogId);

        updateBlog(stub, blogId);
        listBlogs(stub);
        deleteBlog(stub,blogId);
    }

    private static BlogId createblog(BlogServiceGrpc.BlogServiceBlockingStub stub){
        try{

            BlogId createResponse = stub.createBlog(
                    Blog.newBuilder()
                            .setAuthor("Author1")
                            .setContent("This is a sample content for the application")
                            .setTitle("New Blog 1").build());
            System.out.println("Response ID - "+createResponse.getId());
            return createResponse;
        }catch (StatusRuntimeException e){
            System.out.println("couldn't create blog");
            e.printStackTrace();
            return null;
        }
    }

    private static void readBlog(BlogServiceGrpc.BlogServiceBlockingStub stub, BlogId id){

        try{
            Blog readResponse =  stub.readBlog(id);
            System.out.println("blog read "+readResponse);
        }catch (StatusRuntimeException e){
            System.out.println("Not able to read the blog");
            e.printStackTrace();
        }
    }

    private static void updateBlog(BlogServiceGrpc.BlogServiceBlockingStub stub, BlogId id){

        try{

            Blog updateBlog = Blog.newBuilder()
                    .setId(id.getId())
                    .setAuthor("New Author 2")
                    .setContent("Updated the fields here")
                    .setTitle("New title")
                    .build();
            stub.updateBlog(updateBlog);
            System.out.println("Blog updated "+updateBlog);

        }catch (StatusRuntimeException e){
            System.out.println("exception occurred updating blog");
            e.printStackTrace();
        }
    }

    private static void listBlogs(BlogServiceGrpc.BlogServiceBlockingStub stub){

        stub.listBlogs(Empty.getDefaultInstance()).forEachRemaining(System.out::println);
    }

    private static void deleteBlog(BlogServiceGrpc.BlogServiceBlockingStub stub, BlogId id){

        try {
            System.out.println("Deleting the blogId "+id.getId());
            stub.deleteBlog(id);
        }catch (StatusRuntimeException e){
            System.out.println("exception occurred during delete");
            e.printStackTrace();
        }
    }
}
