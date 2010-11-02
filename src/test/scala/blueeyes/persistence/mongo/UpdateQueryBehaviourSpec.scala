package blueeyes.persistence.mongo

import org.spex.Specification
import MongoQueryBuilder._
import MongoImplicits._
import blueeyes.json.JPathImplicits._
import org.mockito.Mockito.{times, when}
import org.mockito.Mockito
import blueeyes.persistence.mongo.json.MongoJson._
import blueeyes.json.JsonAST._

class UpdateQueryBehaviourSpec  extends Specification {
  private val collection  = mock[DatabaseCollection]
  private val jObject = JObject(JField("address", JObject( JField("city", JString("London")) :: JField("street", JString("Regents Park Road")) ::  Nil)) :: Nil)
  "Call collection method" in{
    import MongoFilterImplicits._

    val filter   = Some("name" === "Joe")

    when(collection.update(filter, jObject, false, false)).thenReturn(2)

    val query  = update("collection").set(jObject).where("name" === "Joe")
    val result = query(collection)

    Mockito.verify(collection, times(1)).update(filter, jObject, false, false)

    result mustEqual (JInt(2))
  }
}