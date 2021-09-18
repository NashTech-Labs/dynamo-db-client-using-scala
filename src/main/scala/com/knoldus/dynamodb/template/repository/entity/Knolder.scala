package com.knoldus.dynamodb.template.repository.entity

object Knolder
{
    final val EMP_ID = "empID"
    final val NAME = "name"
    final val ROLE = "role"
    final val TTL = "ttl"
    final val TABLE_NAME = "Knolder"
}

case class Knolder(empID : String, name : String, role : String, ttl : Long)
