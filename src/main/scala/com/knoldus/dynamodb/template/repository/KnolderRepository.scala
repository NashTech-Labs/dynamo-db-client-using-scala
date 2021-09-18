package com.knoldus.dynamodb.template.repository

import akka.Done
import software.amazon.awssdk.services.dynamodb.model.{CreateTableResponse, UpdateContinuousBackupsResponse, UpdateTimeToLiveResponse}
import scala.concurrent.Future

trait KnolderRepository
{
    def createTable : Future[CreateTableResponse]
    
    def updateTableTTL() : Future[UpdateTimeToLiveResponse]
    
    def updateContinuousBackups(isEnablePointInTimeRecovery : Boolean) : Future[Done]
}
