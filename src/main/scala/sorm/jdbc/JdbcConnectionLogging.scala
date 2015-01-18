package sorm.jdbc

import sext._
import org.joda.time.Period
import com.typesafe.scalalogging.slf4j.LazyLogging

trait JdbcConnectionLogging extends LazyLogging {
  private def logging = logger.underlying.isDebugEnabled

  protected def logStatement (s : Statement) {
    logger.debug(
      "Executing statement:\n" +
      (("sql" -> s.sql) +: s.data.map(_.value).notEmpty.map("data" -> _) ++: Stream())
        .toMap.valueTreeString
    )
  }

  protected def executeLoggingBenchmark [ Z ] ( f : => Z ) : Z = {
    if( logging ){
      Benchmarking.benchmarkDoing(ns => logger.debug("Executed statement in %,.3fms:\n".format(ns / 1000000d)))(f)
    } else {
      f
    }
  }

}
