package com.knoldus.dynamodb.template.model.nosql

import akka.Done
import software.amazon.awssdk.services.dynamodb.model.{CreateTableRequest, CreateTableResponse, UpdateTimeToLiveResponse}
import scala.concurrent.Future

trait DynamoDBActions
{
    def createTable(tableName : String, createTableRequest : CreateTableRequest) : Future[CreateTableResponse]
    
    def updateTableTTL(tableName : String, columnName : String) : Future[UpdateTimeToLiveResponse]
    
    def updateContinuousBackups(tableName : String, isEnablePointInTimeRecovery : Boolean) : Future[Done]
}
