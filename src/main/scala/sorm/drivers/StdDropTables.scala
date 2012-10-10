package sorm.drivers

import sext.Sext._
import sorm.jdbc.{JdbcConnection, Statement}

trait StdDropTables {
  protected def connection : JdbcConnection
  protected def quote ( x : String ) : String
  def dropTable ( table : String ) {
    table $ ("DROP TABLE " + quote(_)) $ (Statement(_)) $ connection.executeUpdate
  }
}
