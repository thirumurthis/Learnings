package org.example.calculator.client;

import com.proto.calculator.AverageRequest;
import com.proto.calculator.AverageResponse;
import com.proto.calculator.CalculatorServiceGrpc;
import com.proto.calculator.MaxRequest;
import com.proto.calculator.MaxResponse;
import com.proto.calculator.PrimeRequest;
import com.proto.calculator.SqrtRequest;
import com.proto.calculator.SqrtResponse;
import com.proto.calculator.SumRequest;
import com.proto.calculator.SumResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.checkerframework.checker.units.qual.C;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CalculatorClient {

    public static void main(String[] args) throws InterruptedException {
        int port = 50052;
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost",port)
                .usePlaintext()
                .build();

        if(args.length == 0){
            System.out.println("requires two arguments to perform operation");
            return;
        }

        switch (args[0]) {
            case "sum" -> doAdd(channel, Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            case "prime" -> doPrimeFactor(channel, Integer.parseInt(args[1]));
            case "max" -> doMax(channel, args);
            case "sqrt" -> dpSqrt(channel, args);
            case "average" -> doAverage(channel, args);
            default -> {
                System.out.println("no calculation service provided");
            }
        }

    }

    private static void doAdd(ManagedChannel channel, int num1, int num2){
        SumRequest addRequest = SumRequest.newBuilder()
                .setFirstNumber(num1)
                .setSecondNumber(num2)
                .build();

        CalculatorServiceGrpc.CalculatorServiceBlockingStub stub =
                CalculatorServiceGrpc.newBlockingStub(channel);

        SumResponse response = stub.sum(addRequest);
        System.out.println(response.getResult());
    }

    private static void doPrimeFactor(ManagedChannel channel, int inputPrime) throws InterruptedException {

        CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);


        stub.prime(PrimeRequest.newBuilder().setInputNumber(inputPrime).build())
                .forEachRemaining(primeResponse ->  {
                    System.out.println(primeResponse.getResult());
                });

    }

    private static void doAverage(ManagedChannel channel, String[] numbers) throws InterruptedException {

        CalculatorServiceGrpc.CalculatorServiceStub stub = CalculatorServiceGrpc.newStub(channel);
        //args[0] = command
        //args[1] = numbers
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<AverageRequest> stream = stub.average(new StreamObserver<AverageResponse>() {
            @Override
            public void onNext(AverageResponse averageResponse) {
                System.out.println(averageResponse.getResult());
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        for(int i =1; i< numbers.length; i++){
            stream.onNext(AverageRequest.newBuilder().setInputNumber(Integer.parseInt(numbers[i])).build());
        }

        stream.onCompleted();
        latch.await(3, TimeUnit.SECONDS);

    }

    private static void doMax(ManagedChannel channel,String [] numbers) throws InterruptedException {

        CalculatorServiceGrpc.CalculatorServiceStub stub = CalculatorServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<MaxRequest> stream = stub.max(new StreamObserver<MaxResponse>() {
            @Override
            public void onNext(MaxResponse maxResponse) {
                System.out.println(maxResponse.getResult());
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                latch.countDown();

            }
        });

        Arrays.stream(numbers).skip(1).forEach(itm ->{
            stream.onNext(MaxRequest.newBuilder().setInputNumber(Integer.parseInt(itm)).build());
        });

        stream.onCompleted(); //if this value is not added it will wait for latch to be released 3 sec
        latch.await(3,TimeUnit.SECONDS);
    }

    private static void dpSqrt(ManagedChannel channel,String[] args){

        CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);

       SqrtResponse response = stub.sqrt(SqrtRequest.newBuilder().setInputNumber(Integer.parseInt(args[1])).build());

        System.out.println(response.getResult());
        try{
            response = stub.sqrt(SqrtRequest.newBuilder().setInputNumber(-1).build());
            System.out.println(response.getResult());
        }catch (RuntimeException e){
            System.out.println("error occured");
            e.printStackTrace();
        }
    }
}
