package sorm.pooling

import sorm.jdbc.JdbcConnection
import com.typesafe.scalalogging.Logging

trait ConnectionPool extends Logging {
  protected def fetchConnection () : JdbcConnection
  protected def returnConnection ( c : JdbcConnection )

  def withConnection [ T ] ( f : JdbcConnection => T ) : T
    = {
      val cx = fetchConnection()
      try f(cx)
      finally returnConnection(cx)
    }
}
