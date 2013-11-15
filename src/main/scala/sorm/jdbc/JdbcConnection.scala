package sorm.jdbc

import java.sql.DriverManager
import sext._, embrace._
import java.sql.{Connection, ResultSet, Statement => JdbcStatement}
import sorm.core.DbType

class JdbcConnection( protected val connection : Connection ) extends Transactions with JdbcConnectionLogging {

  def executeQuery
    [ T ]
    ( s : Statement )
    ( parse : ResultSetView => T = (_ : ResultSetView).indexedRowsTraversable.toList )
    : T
    = {
      logStatement(s)
      val js = preparedStatement(s)
      val rs = executeLoggingBenchmark(js.executeQuery())
      val r = parse(rs)
      rs.close()
      js.close()
      r
    }

  def executeUpdateAndGetGeneratedKeys
    ( s : Statement, keys: Iterable[String] )
    : Traversable[Seq[(String, Any)]]
    = {
      logStatement(s)

      val js = {
        if( s.data.isEmpty ) {
          val js = connection.createStatement()
          executeLoggingBenchmark(js.executeUpdate(s.sql, JdbcStatement.RETURN_GENERATED_KEYS))
          js
        } else {
          val js = preparedStatement(s, true)
          executeLoggingBenchmark(js.executeUpdate())
          js
        }
      }

      val keyList = keys.toList
      val rs = js.getGeneratedKeys
      val r = rs.partialByNameRowsTraversable(keys).toList
      rs.close()
      js.close()
      r
    }

  def executeUpdate
    ( s : Statement )
    : Int
    = {
      logStatement(s)
      if( s.data.isEmpty ){
        val js = connection.createStatement()
        val r = executeLoggingBenchmark(js.executeUpdate(s.sql))
        js.close()
        r
      } else {
        val js = preparedStatement(s)
        val r = executeLoggingBenchmark(js.executeUpdate())
        js.close()
        r
      }
    }

  private def preparedStatement
    ( stmt : Statement,
      generatedKeys : Boolean = false )
    = {
      val s =
        connection.prepareStatement(
          stmt.sql,
          if (generatedKeys) JdbcStatement.RETURN_GENERATED_KEYS
          else JdbcStatement.NO_GENERATED_KEYS
        )

      for( (v, i) <- stmt.data.view.zipWithIndex ) {
        s.set(i + 1, v)
      }

      s
    }

  def close() = connection.close()
}
object JdbcConnection {

  def apply
    ( url : String,
      user : String,
      password : String )
    = {
      //  preload driver
      url $ DbType.byUrl $ DbType.driverClass $ Class.forName

      //  get it
      DriverManager.getConnection(url, user, password) $ (new JdbcConnection(_))
    }


}
