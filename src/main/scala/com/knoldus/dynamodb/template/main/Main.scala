package com.knoldus.dynamodb.template.main

import com.knoldus.dynamodb.template.model.nosql.dynamodb.DynamoDBActionsImpl
import com.knoldus.dynamodb.template.repository.dynamo.KnolderRepositoryImpl
import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App
{
    val scanamoAlpakkaDynamoDBActions = new DynamoDBActionsImpl
    val knolderRepository = new KnolderRepositoryImpl(scanamoAlpakkaDynamoDBActions)
    
    // To create a table
    (for (_ <- knolderRepository.createTable) yield ()).map(_ => println("table created"))
    
    // To enable TTL
    (for(_ <- knolderRepository.updateTableTTL()) yield ()).map(_ => println("TTL added to column ttl"))
    
    // To enable continuous backups
    (for (_ <- knolderRepository.updateContinuousBackups(true)) yield ()).map(_ => println("continuous backup added"))
}
