package blueeyes.persistence.mongo

import org.specs.Specification
import MongoQueryBuilder._
import MongoFilterBuilder._
import MongoFilterOperators._
import blueeyes.json.JPath
import blueeyes.json.JPathImplicits._
import blueeyes.json.JsonAST.JObject
import scala.collection.immutable.ListSet

class MongoGroupQuerySpec extends Specification{
  private val query = group(JObject(Nil), "dummy", "foo", "bar").from(MongoCollectionReference("collection"))

  "'where' method sets new filter" in {
    query.where("name" === "Joe") mustEqual (MongoGroupQuery(MongoSelection(ListSet.empty + JPath("foo") + JPath("bar")), "collection", JObject(Nil), "dummy", Some(MongoFieldFilter("name", $eq, "Joe"))))
  }
}