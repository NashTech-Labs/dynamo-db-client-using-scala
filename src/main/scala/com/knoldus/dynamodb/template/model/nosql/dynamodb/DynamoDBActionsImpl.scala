package com.knoldus.dynamodb.template.model.nosql.dynamodb

import java.net.URI
import akka.Done
import akka.actor.{ActorSystem, Scheduler}
import akka.pattern.after
import com.knoldus.dynamodb.template.model.nosql.DynamoDBActions
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model._
import scala.compat.java8.FutureConverters.CompletionStageOps
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.{FiniteDuration, _}


class DynamoDBActionsImpl extends DynamoDBActions
{
    val awsHttpClient = NettyNioAsyncHttpClient
        .builder()
        .maxConcurrency(2)
        .build()
    
    val system = ActorSystem("dynamo-db")
    
    // Change the URI according to the region in use
    val overrideEndpointURI = URI.create(s"https://dynamodb.eu-west-1.amazonaws.com:443")
    val dynamoClient = DynamoDbAsyncClient.builder().endpointOverride(overrideEndpointURI).httpClient(awsHttpClient).build()
    
    
    override def createTable(tableName : String, createTableRequest : CreateTableRequest) : Future[CreateTableResponse] =
    {
        val result = dynamoClient.createTable(createTableRequest).toScala
        result
    }
    
    override def updateTableTTL(tableName : String, columnName : String) : Future[UpdateTimeToLiveResponse] =
    {
        val ttlSpec = TimeToLiveSpecification.builder().attributeName(columnName).enabled(true).build()
        
        val request = UpdateTimeToLiveRequest.builder().tableName(tableName).timeToLiveSpecification(ttlSpec).build()
        
        dynamoClient.updateTimeToLive(request).toScala
    }
    
    override def updateContinuousBackups(tableName : String, isEnablePointInTimeRecovery : Boolean) : Future[Done] =
    {
        val pointInTimeRecoverySpecification = PointInTimeRecoverySpecification.builder().pointInTimeRecoveryEnabled(isEnablePointInTimeRecovery).build()
        
        val request = UpdateContinuousBackupsRequest.builder().tableName(tableName).pointInTimeRecoverySpecification(pointInTimeRecoverySpecification).build()
        
        dynamoClient.updateContinuousBackups(request).toScala.flatMap(_ => retry(waitForDynamoTableToBeActive(tableName)))
    }
    
    def waitForDynamoTableToBeActive(tableName : String) : Future[Done] =
    {
        println("Checking status of NoSQL table: {}", tableName)
        getDescribeTableRequest(tableName).flatMap
        {
            describeTableStatus =>
            {
                describeTableStatus.table.tableStatus match
                {
                    case TableStatus.ACTIVE =>
                    {
                        println("Status of NoSQL table: {} is Active", tableName)
                        Future.successful(Done)
                    }
                    case _ =>
                    {
                        val databaseException = new Exception(s"NoSQL table: $tableName is not ready to perform DB operations")
                        println(databaseException.toString)
                        Future.failed(databaseException)
                    }
                }
            }
        }
    }
    
    private def getDescribeTableRequest(tableName : String) : Future[DescribeTableResponse] =
    {
        dynamoClient.describeTable(DescribeTableRequest.builder.tableName(tableName).build()).toScala
    }
    
    private def retry[T](op : => Future[T]) : Future[T] =
    {
        retry(op, 5000 millis, 3)(system.scheduler)
    }
    
    private def retry[T](op : => Future[T],
                         delay : FiniteDuration,
                         retries : Int)(implicit s : Scheduler) : Future[T] =
    {
        op.recoverWith
        {
            case _ if retries > 0 => after(delay, s)(retry(op, delay, retries - 1))
        }
    }
}
