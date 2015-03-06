package org.broadinstitute.dsde.vault.datamanagement.services

import org.broadinstitute.dsde.vault.datamanagement.domain._
import spray.json.DefaultJsonProtocol

object JsonImplicits extends DefaultJsonProtocol {
  // via https://github.com/jacobus/s4/blob/8dc0fbb04c788c892cb93975cf12f277006b0095/src/main/scala/s4/rest/S4Service.scala
  implicit val impAttribute = jsonFormat4(Attribute)
  implicit val impMetadata = jsonFormat12(Metadata)
  implicit val impUnmappedBAM = jsonFormat3(UnmappedBAM)
}
