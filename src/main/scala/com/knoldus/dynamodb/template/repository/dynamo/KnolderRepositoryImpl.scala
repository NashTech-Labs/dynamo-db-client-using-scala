package com.knoldus.dynamodb.template.repository.dynamo

import akka.Done
import com.knoldus.dynamodb.template.model.nosql.DynamoDBActions
import com.knoldus.dynamodb.template.repository.KnolderRepository
import com.knoldus.dynamodb.template.repository.entity.Knolder.{EMP_ID, TABLE_NAME, TTL}
import software.amazon.awssdk.services.dynamodb.model._
import scala.concurrent.Future

class KnolderRepositoryImpl(dynamoDBAction : DynamoDBActions) extends KnolderRepository
{
    override def createTable : Future[CreateTableResponse] =
    {
        val createTableRequest = CreateTableRequest.builder()
            .tableName(TABLE_NAME)
            .keySchema(
                KeySchemaElement.builder().attributeName(EMP_ID).keyType(KeyType.HASH).build()
            )
            .attributeDefinitions(
                AttributeDefinition.builder().attributeName(EMP_ID).attributeType(ScalarAttributeType.S).build()
            )
            .provisionedThroughput(
                ProvisionedThroughput.builder()
                    .readCapacityUnits(2)
                    .writeCapacityUnits(2).build()
            )
            .sseSpecification(SSESpecification.builder().enabled(true).build()).build()
        
        dynamoDBAction.createTable(TABLE_NAME, createTableRequest)
    }
    
    override def updateTableTTL() : Future[UpdateTimeToLiveResponse] =
    {
        dynamoDBAction.updateTableTTL(TABLE_NAME, TTL)
    }
    
    override def updateContinuousBackups(isEnablePointInTimeRecovery : Boolean) : Future[Done] =
    {
        dynamoDBAction.updateContinuousBackups(TABLE_NAME, isEnablePointInTimeRecovery)
    }
}
