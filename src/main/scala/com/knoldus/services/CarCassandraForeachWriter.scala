package com.knoldus.services

import com.datastax.spark.connector.cql.CassandraConnector
import com.knoldus.model.Car
import org.apache.spark.sql.{ForeachWriter, SparkSession}

/**
  * CarCassandraForeachWriter Class extending ForeachWriter abstract class
  * and implementing its three abstract method open, process and close for
  * writing custom logic to process data generated by a query.
  * It also extends Logging Trait to add loggers.
  */
class CarCassandraForeachWriter(spark: SparkSession) extends ForeachWriter[Car] {

  /*
    - on every batch, on every partition `partitionId`
      - on every "epoch" = chunk of data
        - call the open method; if false, skip this chunk
        - for each entry in this chunk, call the process method
        - call the close method either at the end of the chunk or with an error if it was thrown
   */

  val keyspace = "public"
  val table = "car"
  val connector: CassandraConnector = CassandraConnector(spark.sparkContext.getConf)

  override def open(partitionId: Long, epochId: Long): Boolean = {
    println("Open connection.")
    true
  }

  override def process(car: Car): Unit = {

    // Executing insert query to cassandraDB via CassandraConnector.
    connector.withSessionDo { session =>
      session.execute(
        s"""
           |insert into $keyspace.$table("Name", "Cylinders", "Horsepower")
           |values ('${car.Name}', ${car.Cylinders.orNull}, ${car.Horsepower.orNull})
           """.stripMargin)
    }
  }

  override def close(errorOrNull: Throwable): Unit = println("Closing connection.")

}
