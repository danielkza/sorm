package sorm.test

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import sorm._
import core._
import persisted._
import query._
import reflection._
import mappings._
import jdbc._
import sext._, embrace._

@RunWith(classOf[JUnitRunner])
class OptionEntitySeqItemSupportSuite extends FunSuite with ShouldMatchers {

  import OptionEntitySeqItemSupportSuite._

  TestingInstances.instances(Set() + Entity[A]() + Entity[B]()) foreach { case (db, dbId) =>
    val b1 = db.save(B("abc"))
    val b2 = db.save(B("cba"))

    test(dbId + ": saving goes ok"){
      db.save(A( Seq() ))
      db.save(A( Seq(Some(b1), None, Some(b2)) ))
      db.save(A( Seq(None, Some(b2)) ))
      db.save(A( Seq(None) ))
    }
    test(dbId + ": empty seq"){
      db.fetchById[A](1).seq should be === Seq()
    }
    test(dbId + ": seq of none"){
      db.fetchById[A](4).seq should be === Seq(None)
    }
    test(dbId + ": not empty seqs are correct"){
      db.fetchById[A](2).seq should be === Seq(Some(b1), None, Some(b2))
      db.fetchById[A](3).seq should be === Seq(None, Some(b2))
    }
  }
}
object OptionEntitySeqItemSupportSuite {
  case class A ( seq : Seq[Option[B]] )
  case class B ( z : String )
}