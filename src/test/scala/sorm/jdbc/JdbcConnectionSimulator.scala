package sorm.jdbc

import com.weiglewilczek.slf4s.Logging
import java.sql.ResultSet

class JdbcConnectionSimulator
  extends JdbcConnection(null)
  with Logging
  {
    override def executeQuery
      [ T ]
      ( s : Statement )
      ( parse : ResultSetView => T = (_ : ResultSetView).indexedRowsTraversable.toList )
      : T
      = {
        println(s.toString)
        ???
      }

    override def executeUpdateAndGetGeneratedKeys
      ( stmt : Statement, keys: Iterable[String] )
      : Traversable[Seq[(String, Any)]]
      = {
        println(stmt.toString)
        Seq(keys.map{ _ -> 7771 }.toSeq)
      }

    override def executeUpdate
      ( stmt : Statement )
      : Int = {
        println(stmt.toString)
        1
      }

  }