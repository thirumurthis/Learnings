package org.example.calculator.server;

import com.proto.calculator.AverageRequest;
import com.proto.calculator.AverageResponse;
import com.proto.calculator.CalculatorServiceGrpc;
import com.proto.calculator.MaxRequest;
import com.proto.calculator.MaxResponse;
import com.proto.calculator.PrimeRequest;
import com.proto.calculator.PrimeResponse;
import com.proto.calculator.SqrtRequest;
import com.proto.calculator.SqrtResponse;
import com.proto.calculator.SumRequest;
import com.proto.calculator.SumResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class CalculatorServiceImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {

    @Override
    public void sum(SumRequest request, StreamObserver<SumResponse> streamObserver){

        int result = request.getFirstNumber()+request.getSecondNumber();
        streamObserver.onNext(SumResponse.newBuilder().setResult(result).build());
        streamObserver.onCompleted();

    }

    @Override
    public void prime(PrimeRequest request, StreamObserver<PrimeResponse> responseStreamObserver){

        int primeNumber = request.getInputNumber();
        int k=2 ;
        while ( primeNumber > 1){
            if (primeNumber % k == 0){
                responseStreamObserver.onNext(PrimeResponse.newBuilder().setResult(k).build());
                primeNumber = primeNumber/k;
            }else{
                k = k+1;
            }
        }
        responseStreamObserver.onCompleted();

    }

    @Override
    public StreamObserver<AverageRequest> average(StreamObserver<AverageResponse> responseObserver) {

        return  new StreamObserver<>() {
            int sum=0;
            int numOfInput=0;
            @Override
            public void onNext(AverageRequest averageRequest) {
                 sum = sum + averageRequest.getInputNumber();
                 ++numOfInput;
            }

            @Override
            public void onError(Throwable throwable) {
                responseObserver.onError(throwable);

            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(AverageResponse.newBuilder().setResult( (double) sum /numOfInput).build());
                responseObserver.onCompleted();

            }
        };
    }

    @Override
    public StreamObserver<MaxRequest> max(StreamObserver<MaxResponse> responseObserver) {
        //return super.max(responseObserver);

        return new StreamObserver<MaxRequest>() {
            int previousMax =Integer.MIN_VALUE;
            @Override
            public void onNext(MaxRequest maxRequest) {
              if (maxRequest.getInputNumber() > previousMax) {
                  previousMax = maxRequest.getInputNumber();
                  responseObserver.onNext(MaxResponse.newBuilder().setResult(previousMax).build());
              }
            }

            @Override
            public void onError(Throwable throwable) {
                responseObserver.onError(throwable);

            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void sqrt(SqrtRequest request, StreamObserver<SqrtResponse> responseObserver) {
        int inputNumber = request.getInputNumber();

        if (inputNumber < 0){
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Number can't be negative")
                    .augmentDescription("input number: "+inputNumber)
                    .asRuntimeException());
        }
        responseObserver.onNext(SqrtResponse.newBuilder().setResult(Math.sqrt(inputNumber)).build());
        responseObserver.onCompleted();
    }
}
